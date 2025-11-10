/*
 * UPYOG  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) <2019>  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *      Further, all user interfaces, including but not limited to citizen facing interfaces,
 *         Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *         derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *      For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *      For any further queries on attribution, including queries on brand guidelines,
 *         please contact contact@egovernments.org
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.constants.MdmsFeatureConstants;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.constants.EdcrRulesMdmsConstants;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.service.FetchEdcrRulesMdms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.egov.edcr.constants.CommonFeatureConstants.*;
import static org.egov.edcr.constants.EdcrReportConstants.*;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;


@Service
public class ToiletDetails_Assam extends FeatureProcess {

    private static final Logger LOG = LogManager.getLogger(ToiletDetails_Assam.class);

    /**
     * Validates the building plan for toilet requirements.
     * Currently performs no validation and returns the plan as-is.
     *
     * @param pl The building plan to validate
     * @return The unmodified plan
     */
    @Override
    public Plan validate(Plan pl) {
        return pl;
    }

    @Autowired
	MDMSCacheManager cache;

    /**
     * Processes toilet requirements for all blocks in the building plan.
     * Creates scrutiny details and validates each block's toilet specifications
     * against minimum area, width, and ventilation requirements.
     *
     * @param pl The building plan to process
     * @return The processed plan with scrutiny details added
     */
    @Override
    public Plan process(Plan pl) {
        ScrutinyDetail scrutinyDetail = createToiletScrutinyDetail();

        for (Block block : pl.getBlocks()) {
            processBlockToilets(pl, block, scrutinyDetail);
        }

        pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
        return pl;
    }

    /**
     * Creates and initializes a scrutiny detail object for toilet validation reporting.
     * Sets up column headings and key for the toilet scrutiny report.
     *
     * @return Configured ScrutinyDetail object with appropriate headings and key
     */
    private ScrutinyDetail createToiletScrutinyDetail() {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.setKey(Common_Toilet);
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, FLOOR_NO);
        scrutinyDetail.addColumnHeading(4, UNIT);
        scrutinyDetail.addColumnHeading(5, REQUIRED);
        scrutinyDetail.addColumnHeading(6, PROVIDED);
        scrutinyDetail.addColumnHeading(7, STATUS);
        return scrutinyDetail;
    }

    /**
     * Processes all toilets within a specific building block.
     * Iterates through floors and toilet measurements to validate each toilet
     * against the required specifications.
     *
     * @param pl The building plan
     * @param block The building block containing toilets
     * @param scrutinyDetail The scrutiny detail object to add results to
     */
    private void processBlockToilets(Plan pl, Block block, ScrutinyDetail scrutinyDetail) {
        if (block.getBuilding() == null || block.getBuilding().getFloors() == null) return;

        for (Floor floor : block.getBuilding().getFloors()) {
        	 for (FloorUnit unit : floor.getUnits()){
            if (unit.getToilet() == null || unit.getToilet().isEmpty()) continue;

            for (Toilet toilet : unit.getToilet()) {
                if (toilet.getToilets() == null || toilet.getToilets().isEmpty()) continue;

                for (Measurement toiletMeasurement : toilet.getToilets()) {
                    evaluateToiletMeasurement(pl, floor, unit, toilet, toiletMeasurement, scrutinyDetail);
                }
                
                evaluateToiletVentilation(pl, floor, unit, block, toilet, toilet.getToilets(), scrutinyDetail);

            }
     
        }
     
      }
    }

    /**
     * Evaluates a single toilet measurement against minimum requirements.
     * Validates area, width, and ventilation height against MDMS rules and
     * generates compliance status for the scrutiny report.
     *
     * @param pl The building plan
     * @param floor The floor containing the toilet
     * @param toilet The toilet object being evaluated
     * @param measurement The specific toilet measurement to validate
     * @param scrutinyDetail The scrutiny detail object to add results to
     */
    private void evaluateToiletMeasurement(Plan pl, Floor floor, FloorUnit unit, Toilet toilet, Measurement measurement,
                                           ScrutinyDetail scrutinyDetail) {
        BigDecimal area = measurement.getArea().setScale(2, RoundingMode.HALF_UP);
        BigDecimal width = measurement.getWidth().setScale(2, RoundingMode.HALF_UP);
        BigDecimal height = unit.getCommonHeight()
                .stream()
                .min(Comparator.naturalOrder())
                .get()
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        
        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(RULE_41_5_5);
        detail.setDescription(TOILET_DESCRIPTION);
        detail.setFloorNo(String.valueOf(floor.getNumber()));
        detail.setUnitNumber(unit.getUnitNumber());
        

        Optional<ToiletRequirement> toiletRule = getToiletRule(pl);
        if (toiletRule == null) return;
        ToiletRequirement rule = toiletRule.get();
        BigDecimal minArea = rule.getMinToiletArea();
        BigDecimal minWidth = rule.getMinToiletWidth();
    	BigDecimal minHeight = rule.getMinToiletHeight();

      

        String required = TOTAL_AREA_STRING + GREATER_THAN_EQUAL + minArea + COMMA_WIDTH_STRING + GREATER_THAN_EQUAL + minWidth  + COMMA_HEIGHT_STRING + GREATER_THAN_EQUAL + minHeight;
        String provided = TOTAL_AREA_STRING + IS_EQUAL_TO + area + COMMA_WIDTH_STRING + IS_EQUAL_TO + width + COMMA_HEIGHT_STRING + IS_EQUAL_TO + height;

        detail.setRequired(required);
        detail.setProvided(provided);
        if (area.compareTo(minArea) >= 0 && width.compareTo(minWidth) >= 0 ) {
            detail.setStatus(Result.Accepted.getResultVal());
        } else {
            detail.setStatus(Result.Not_Accepted.getResultVal());
        }

        Map<String, String> details = mapReportDetails(detail);
        scrutinyDetail.getDetail().add(details);
    }
    
    /**
     * Evaluates the ventilation adequacy of a toilet based on its floor area and provided window openings.
     * <p>
     * The method checks if the total provided ventilation area (sum of all window widths × height) 
     * meets or exceeds the required ventilation area. By default, the requirement is 1/6th of the total 
     * toilet floor area, unless rules specify otherwise. It records the evaluation in the scrutiny report.
     * </p>
     *
     * <b>Logic:</b>
     * <ul>
     *   <li>Skips evaluation if no toilet, window width, or ventilation height is provided.</li>
     *   <li>Calculates total toilet area = sum of all toilet room areas.</li>
     *   <li>Calculates provided ventilation area = Σ(width × windowHeight).</li>
     *   <li>Calculates required ventilation area = toiletArea ÷ 6.</li>
     *   <li>Compares provided vs required and marks result as Accepted or Not Accepted.</li>
     *   <li>Appends the result into {@link ScrutinyDetail} for reporting.</li>
     * </ul>
     *
     * @param pl              The complete {@link Plan} being evaluated.
     * @param floor           The {@link Floor} that contains the toilet(s).
     * @param block           The {@link Block} in which the floor resides.
     * @param toilet          The {@link Toilet} object with ventilation details (widths and height).
     * @param toilets         The list of all toilets (with their areas) in the floor.
     * @param scrutinyDetail  The {@link ScrutinyDetail} object to which evaluation results will be added.
     */
    
    private void evaluateToiletVentilation(Plan pl, Floor floor, FloorUnit unit, Block block,
			            Toilet toilet, List<Measurement> toilets,
			            ScrutinyDetail scrutinyDetail) {
			if (toilet == null
			|| toilet.getToiletWindowWidth() == null
			|| toilet.getToiletWindowWidth().isEmpty()
			|| toilet.getToiletVentilation() == null) {
			return;
			}

		// Toilet floor area = sum of all toilet room areas
		BigDecimal toiletArea = toilets.stream()
		.map(Measurement::getArea)
		.reduce(BigDecimal.ZERO, BigDecimal::add)
		.setScale(2, RoundingMode.HALF_UP);
		
		// Window height
		BigDecimal windowHeight = toilet.getToiletVentilation().setScale(2, RoundingMode.HALF_UP);

	
		// Provided ventilation area = sum(width × height)
		BigDecimal providedVentilationArea = toilet.getToiletWindowWidth().stream()
			    .map(width -> width.multiply(windowHeight))
			    .reduce(BigDecimal.ZERO, BigDecimal::add)
			    .setScale(2, RoundingMode.HALF_UP);

		
		// Required ventilation area = 1/6th of toilet floor area (adjust if rule says otherwise)
		BigDecimal requiredVentilationArea = toiletArea.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
		
		LOG.info("Evaluating toilet ventilation for Floor: {}. ToiletArea: {}, RequiredVentilationArea: {}, ProvidedWindowArea: {}",
		floor.getNumber(), toiletArea, requiredVentilationArea, providedVentilationArea);
		
		// --- Build scrutiny report row ---
		ReportScrutinyDetail detail = new ReportScrutinyDetail();
		detail.setRuleNo(SUB_RULE_53_5);  // Create/use the right constant for ventilation
		detail.setDescription("Toilet Ventilation");
		detail.setFloorNo(String.valueOf(floor.getNumber()));
		detail.setUnitNumber(unit.getUnitNumber());
		
		String required = "Ventilation Area ≥ " + requiredVentilationArea + " m²";
		String provided = "Ventilation Area = " + providedVentilationArea + " m²";
		
		detail.setRequired(required);
		detail.setProvided(provided);
		
		if (providedVentilationArea.compareTo(requiredVentilationArea) >= 0) {
		detail.setStatus(Result.Accepted.getResultVal());
		} else {
		detail.setStatus(Result.Not_Accepted.getResultVal());
		}
		
		Map<String, String> details = mapReportDetails(detail);
		scrutinyDetail.getDetail().add(details);
		}



    /**
     * Retrieves toilet requirement rules from MDMS cache.
     * Fetches the first matching toilet requirement rule based on plan configuration.
     *
     * @param pl The building plan containing configuration details
     * @return Optional containing ToiletRequirement rule if found, empty otherwise
     */
    private Optional<ToiletRequirement> getToiletRule(Plan pl) {
    	List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.TOILET.getValue(), false);
       return rules.stream()
            .filter(ToiletRequirement.class::isInstance)
            .map(ToiletRequirement.class::cast)
            .findFirst();
    }

    /**
     * Returns amendment dates for toilet requirement rules.
     * Currently returns an empty map as no amendments are defined.
     *
     * @return Empty LinkedHashMap of amendment dates
     */
    @Override
    public Map<String, Date> getAmendments() {
        return new LinkedHashMap<>();
    }
}
