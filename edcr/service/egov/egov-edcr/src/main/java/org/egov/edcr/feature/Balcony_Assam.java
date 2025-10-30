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
 */

package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.constants.MdmsFeatureConstants;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.constants.EdcrReportConstants;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.egov.edcr.constants.CommonFeatureConstants.*;
import static org.egov.edcr.constants.CommonFeatureConstants.BALCONY;
import static org.egov.edcr.constants.CommonFeatureConstants.BALCONY_LENGTH_DESC;
import static org.egov.edcr.constants.CommonFeatureConstants.BALCONY_LENGTH_EXCEEDED;
import static org.egov.edcr.constants.CommonFeatureConstants.BALCONY_SETBACK_DESC;
import static org.egov.edcr.constants.CommonFeatureConstants.BUILDING_LENGTH_NOT_DEFINED;
import static org.egov.edcr.constants.CommonFeatureConstants.BUILDING_LENGTH_NULL;
import static org.egov.edcr.constants.CommonKeyConstants.*;
import static org.egov.edcr.constants.EdcrReportConstants.*;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;

@Service
public class Balcony_Assam extends FeatureProcess {
	private static final Logger LOG = LogManager.getLogger(Balcony.class);

	@Autowired
	MDMSCacheManager cache;

	@Override
	public Plan validate(Plan plan) {
	    return plan;
	}

	/**
	 * Processes the given {@link Plan} by validating balcony widths block-wise.
	 * <p>
	 * For each block in the plan, if the block contains a building, it invokes
	 * {@code processBlockBalconies} to validate balconies floor-wise, gather
	 * scrutiny details, and append them to the plan's report output.
	 * </p>
	 *
	 * @param plan the {@link Plan} object containing building blocks and their details
	 * @return the modified {@link Plan} object with updated scrutiny report
	 */
	
	@Override
    public Plan process(Plan plan) {
        LOG.info("Starting process() for plan");
        for (Block block : plan.getBlocks()) {
            LOG.info("Processing Block: {}", block.getNumber());
            if (block.getBuilding() != null) {
                LOG.info("Block {} has building. Processing balconies.", block.getNumber());
                processBlockBalconies(plan, block);
            } else {
                LOG.info("Block {} has no building. Skipping.", block.getNumber());
            }
        }

        LOG.info("Exiting process() for plan:");
        return plan;
    }

	/**
	 * Processes all balconies for a given block and prepares scrutiny details.
	 * <p>
	 * Iterates over each floor of the block's building and delegates balcony validation
	 * to {@code processFloorBalconies}. Appends the collected scrutiny details to
	 * the plan's report output.
	 * </p>
	 *
	 * @param plan  the plan being processed
	 * @param block the block whose balconies are to be processed"Block_"
	 */
	private void processBlockBalconies(Plan plan, Block block) {
        LOG.info("Processing balconies for Block {}", block.getNumber());

        ScrutinyDetail scrutinyDetail = createScrutinyDetail(
                BLOCK + block.getNumber() + UNDERSCORE + MdmsFeatureConstants.BALCONY,
                RULE_NO, FLOOR, UNIT, DESCRIPTION, PERMISSIBLE, PROVIDED, STATUS);

        for (Floor floor : block.getBuilding().getFloors()) { // length , width , setback
            validateBalconyProjection(plan, block, floor, scrutinyDetail, new BigDecimal(4), new BigDecimal(1.5), new BigDecimal(1.5));

            for(FloorUnit floorUnit : floor.getUnits()) {
                LOG.info("Processing Floor Unit {} of Floor {} in Block {}", floorUnit.getRoomNumber(), floor.getNumber(), block.getNumber());
                processFloorBalconies(plan, block, floor, scrutinyDetail, floorUnit);
            }
        }

        LOG.info("Adding scrutiny details for Block {} to plan report.", block.getNumber());
        plan.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
    }

	/**
	 * Processes all balconies on a given floor of a block.
	 * <p>
	 * Retrieves the list of balconies from the floor and validates each one
	 * using {@code validateBalcony}. Also handles typical floor LOGic.
	 * </p>
	 *
	 * @param plan           the plan being processed
	 * @param block          the block containing the floor
	 * @param floor          the floor to process
	 * @param scrutinyDetail the scrutiny detail object to which validation results are added
	 */
	private void processFloorBalconies(Plan plan, Block block, Floor floor, ScrutinyDetail scrutinyDetail, FloorUnit floorUnit) {
        LOG.info("Processing balconies for Floor {} in Block {}", floor.getNumber(), block.getNumber());

        boolean isTypicalRepititiveFloor = false;
        Map<String, Object> typicalFloorValues = Util.getTypicalFloorValues(block, floor, isTypicalRepititiveFloor);

        List<org.egov.common.entity.edcr.Balcony> balconies = floorUnit.getBalconies();
        if (balconies != null && !balconies.isEmpty()) {
            LOG.info("Found {} balconies in Floor {} of Block {}", balconies.size(), floor.getNumber(), block.getNumber());
            for (org.egov.common.entity.edcr.Balcony balcony : balconies) {
                LOG.info("Validating Balcony {}", balcony.getNumber());
                validateBalcony(plan, floor, balcony, typicalFloorValues, scrutinyDetail, floorUnit);
            }
        } else {
            LOG.info("No balconies found in Floor {} of Block {}", floor.getNumber(), block.getNumber());
        }
    }

	/**
	 * Validates the width of a single balcony against MDMS rules and adds the
	 * result to scrutiny.
	 * <p>
	 * Compares the minimum width of the balcony with the permissible value obtained
	 * from MDMS rules. Based on the comparison, a result row is created and added
	 * to the scrutiny detail.
	 * </p>
	 *
	 * @param plan               the plan being processed
	 * @param floor              the floor containing the balcony
	 * @param balcony            the balcony to validate
	 * @param typicalFloorValues a map containing details about typical floors, if
	 *                           applicable
	 * @param scrutinyDetail     the scrutiny detail object to which the validation
	 *                           result is added
	 */
	private void validateBalcony(Plan plan, Floor floor, org.egov.common.entity.edcr.Balcony balcony,
			Map<String, Object> typicalFloorValues, ScrutinyDetail scrutinyDetail, FloorUnit floorUnit) {

		BigDecimal balconyValue = BigDecimal.ZERO;
		LOG.info("Validating balcony widths for Balcony {}", balcony.getNumber());

		List<BigDecimal> widths = balcony.getWidths();
		LOG.info("Widths found for Balcony {}: {}", balcony.getNumber(), widths);

		BigDecimal minWidth = widths.isEmpty() ? BigDecimal.ZERO : widths.stream().reduce(BigDecimal::min).get();
		minWidth = minWidth.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS);
		LOG.info("Minimum width for Balcony {}: {}", balcony.getNumber(), minWidth);

		List<Object> rules = cache.getFeatureRules(plan, FeatureEnum.BALCONY.getValue(), false);
		LOG.info("Fetched {} rules from MDMS for Balcony validation.", rules.size());

		Optional<BalconyRequirement> matchedRule = rules.stream().filter(BalconyRequirement.class::isInstance)
				.map(BalconyRequirement.class::cast).findFirst();

		if (matchedRule.isPresent()) {
			balconyValue = matchedRule.get().getPermissible();
			LOG.info("Matched permissible value from MDMS: {}", balconyValue);
		} else {
			balconyValue = BigDecimal.ZERO;
			LOG.info("No rule matched. Default permissible value set to 0.");
		}

		boolean isAccepted = minWidth.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
		        DcrConstants.ROUNDMODE_MEASUREMENTS)
		    .compareTo(balconyValue.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
		        DcrConstants.ROUNDMODE_MEASUREMENTS)) <= 0;


		LOG.info("Balcony {} validation result: {}", balcony.getNumber(), isAccepted ? "ACCEPTED" : "NOT ACCEPTED");

		String floorLabel = typicalFloorValues.get(TYPICAL_FLOOR) != null
				? (String) typicalFloorValues.get(TYPICAL_FLOOR)
				: FLOOR_SPACED + floor.getNumber();

		ReportScrutinyDetail detail = new ReportScrutinyDetail();
		detail.setRuleNo(RULE45_IV);
		detail.setDescription(String.format(WIDTH_BALCONY_DESCRIPTION, balcony.getNumber()));
		detail.setPermissible(balconyValue.toString());
		detail.setProvided(minWidth.toString());
		detail.setStatus(isAccepted ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());
		detail.setFloorNo(floorLabel);
        detail.setUnitNumber(floorUnit.getUnitNumber());

		Map<String, String> details = mapReportDetails(detail);
		scrutinyDetail.getDetail().add(details);
		LOG.info("Added scrutiny detail for Balcony {} in Floor {}", balcony.getNumber(), floor.getNumber());
	}

    private void validateBalconyProjection(Plan plan, Block block, Floor floor, ScrutinyDetail scrutinyDetail, BigDecimal farBalconyLength, BigDecimal farBalconyWidth, BigDecimal farBalconySetback) {

        LOG.info("Validating Balcony Projections...");
        if(floor.getNumber() > 0) {
            // Check min setback 1.5m
            boolean isSetbackCompliant = isBalconyWithinMinSetback(floor, farBalconySetback);
            BigDecimal actualSetback = floor.getBalconyDistanceFromPlotBoundary().isEmpty() ?
                    BigDecimal.ZERO :
                    floor.getBalconyDistanceFromPlotBoundary().stream().reduce(BigDecimal::min).orElse(BigDecimal.ZERO);

            String truncatedActualSetback = actualSetback.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

            ReportScrutinyDetail setbackDetail = new ReportScrutinyDetail();
            setbackDetail.setRuleNo(RULE32_2_1);
            setbackDetail.setDescription(MIN_SETBACK_PLOT_BOUNDARY);
            setbackDetail.setPermissible(farBalconySetback.toString());
            setbackDetail.setProvided(truncatedActualSetback);
            setbackDetail.setStatus(isSetbackCompliant ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());
            setbackDetail.setFloorNo(floor.getNumber().toString());

            Map<String, String> setbackDetails = mapReportDetails(setbackDetail);
            scrutinyDetail.getDetail().add(setbackDetails);

            // Check max length 1/4 of building dimension and max width 1.5m
            boolean isWidthCompliant = isBalconyWidthCompliant(block, plan, floor, farBalconyLength, farBalconyWidth);
            BigDecimal actualWidth = BigDecimal.ZERO;
            if(floor.getFloorProjectedBalconies() != null && !floor.getFloorProjectedBalconies().isEmpty()) {
                actualWidth = floor.getFloorProjectedBalconies().stream()
                        .filter(Objects::nonNull)
                        .map(Measurement::getWidth)
                        .max(BigDecimal::compareTo)
                        .orElse(BigDecimal.ZERO);
            }

            String truncatedActualWidth = actualWidth.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

            ReportScrutinyDetail widthDetail = new ReportScrutinyDetail();
            widthDetail.setRuleNo(RULE32_2_1);
            widthDetail.setDescription("Maximum Balcony Width");
            widthDetail.setPermissible(farBalconyWidth.toString());
            widthDetail.setProvided(truncatedActualWidth);
            widthDetail.setStatus(isWidthCompliant ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());
            widthDetail.setFloorNo(floor.getNumber().toString());

            Map<String, String> widthDetails = mapReportDetails(widthDetail);
            scrutinyDetail.getDetail().add(widthDetails);
        }
    }

    private boolean isBalconyWithinMinSetback(Floor floor, BigDecimal farBalconySetback) {
        LOG.info("Checking if Balcony is within Minimum Setback...");
        for (BigDecimal balconyDistance: floor.getBalconyDistanceFromPlotBoundary()){
            if(balconyDistance.compareTo(farBalconySetback) > 0)
                return true;
        }

        return false;
    }

    private boolean isBalconyWidthCompliant(Block block, Plan plan, Floor floor, BigDecimal farBalconyLength, BigDecimal farBalconyWidth) {
        LOG.info("Checking if Balcony Width is Compliant...");
        /*
        BigDecimal buildingLength = block.getBuilding().getBuildingLength();
        BigDecimal quarterBuildingLength = BigDecimal.ZERO;

        if(buildingLength == null || buildingLength.compareTo(BigDecimal.ZERO) == 0){
            plan.addError(BUILDING_LENGTH_NULL, BUILDING_LENGTH_NOT_DEFINED + block.getNumber());
        }
        else{
            quarterBuildingLength = buildingLength.divide(farBalconyLength, 2, RoundingMode.HALF_UP);
        }
         */

        if(floor.getFloorProjectedBalconies() != null || !floor.getFloorProjectedBalconies().isEmpty())
            for (Measurement projectedBalcony : floor.getFloorProjectedBalconies()) {
                if(projectedBalcony != null && projectedBalcony.getWidth().compareTo(farBalconyWidth) > 0)
                    return false;
                /*
                if (projectedBalcony != null && projectedBalcony.getWidth().compareTo(quarterBuildingLength) > 0)
                    return false;
                 */
            }

        return true;
    }

	// Method to create ScrutinyDetail
	private ScrutinyDetail createScrutinyDetail(String key, String... headings) {
        LOG.info("Creating scrutiny detail with key: {}", key);
        ScrutinyDetail detail = new ScrutinyDetail();
        detail.setKey(key);
        for (int i = 0; i < headings.length; i++) {
            detail.addColumnHeading(i + 1, headings[i]);
        }
        return detail;
    }
	
	@Override
	public Map<String, Date> getAmendments() {
	    return new LinkedHashMap<>();
	}


}