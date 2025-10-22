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
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.constants.MdmsFeatureConstants;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.service.FetchEdcrRulesMdms;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import static org.egov.edcr.constants.DxfFileConstants.A;

import static org.egov.edcr.constants.CommonFeatureConstants.*;
import static org.egov.edcr.constants.EdcrReportConstants.*;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;

@Service
public class Coverage_Assam extends FeatureProcess {
	// private static final String OCCUPANCY2 = "OCCUPANCY";

	private static final Logger LOG = LogManager.getLogger(Coverage_Assam.class);

	@Autowired
	FetchEdcrRulesMdms fetchEdcrRulesMdms;
	
	 @Autowired
	MDMSCacheManager cache;


	/*
	 * private static final BigDecimal FortyFive = BigDecimal.valueOf(45); private
	 * static final BigDecimal Fifty = BigDecimal.valueOf(50); private static final
	 * BigDecimal FiftyFive = BigDecimal.valueOf(55); private static final
	 * BigDecimal Sixty = BigDecimal.valueOf(60); private static final BigDecimal
	 * SixtyFive = BigDecimal.valueOf(65); private static final BigDecimal Seventy =
	 * BigDecimal.valueOf(70); private static final BigDecimal SeventyFive =
	 * BigDecimal.valueOf(75); private static final BigDecimal Eighty =
	 * BigDecimal.valueOf(80);
	 */

	public static final String RULE_38 = "38";
	public static final String RULE_7_C_1 = "Table 7-C-1";
	public static final String RULE = "4.4.4";
	
	@Override
	public Plan validate(Plan pl) {
		for (Block block : pl.getBlocks()) {
			if (block.getCoverage().isEmpty()) {
				pl.addError(COVERAGE_AREA + block.getNumber(),
						COVERAGE_AREA_BLOCK + block.getNumber() + NOT_PROVIDED);
			}
		}
		return pl;
	}


	/**
	 * Processes the Plan object to calculate and validate the coverage area.
	 *
	 * @param pl the Plan object containing plot, blocks, occupancy, etc.
	 * @return updated Plan object with coverage details set
	 */
	@Override
	public Plan process(Plan pl) {
	    LOG.info("Inside Coverage process()");
	    
	    validate(pl);

	    BigDecimal plotArea = pl.getPlot().getArea();
	    LOG.info("Plot area: {}", plotArea);

	    int noOfFloors = 0; // unused but kept as per original
	    LOG.info("Initial noOfFloors set to {}", noOfFloors);

	    Set<OccupancyTypeHelper> occupancyList = extractOccupancyList(pl);
	    LOG.info("Extracted {} occupancy types from plan", occupancyList.size());

	    OccupancyTypeHelper mostRestrictiveOccupancy = pl.getVirtualBuilding().getMostRestrictiveFarHelper();
	    LOG.info("Most restrictive occupancy: {}", 
	        mostRestrictiveOccupancy != null ? mostRestrictiveOccupancy.getType().getName() : "None");
	    String occupancy = mostRestrictiveOccupancy.getType().getCode();

	    BigDecimal totalCoverageArea = calculateCoverageAreas(pl, plotArea);
	    LOG.info("Total coverage area calculated: {}", totalCoverageArea);

	    BigDecimal totalCoverage = calculateTotalCoverage(plotArea, totalCoverageArea);
	    LOG.info("Total coverage (%): {}", totalCoverage);

	    //  Skip processing if occupancy is residential
	    if (occupancy != null && occupancy.equalsIgnoreCase(A)) {
	        LOG.info("Occupancy is Residential — skipping coverage processing.");
	        return pl;
	    }

	    pl.setCoverage(totalCoverage);
	    if (pl.getVirtualBuilding() != null) {
	        pl.getVirtualBuilding().setTotalCoverageArea(totalCoverageArea);
	        LOG.info("Set totalCoverageArea {} in virtualBuilding", totalCoverageArea);
	    }

	    BigDecimal permissibleCoverageValue = getPermissibleCoverageIfApplicable(pl, plotArea, mostRestrictiveOccupancy);
	    LOG.info("Permissible coverage value: {}", permissibleCoverageValue);

	    if (permissibleCoverageValue.compareTo(BigDecimal.ZERO) > 0) {
	        LOG.info("Processing coverage validation for occupancy: {}", mostRestrictiveOccupancy.getType().getName());
	        processCoverage(pl, mostRestrictiveOccupancy.getType().getName(), totalCoverage, permissibleCoverageValue);
	    } else {
	        LOG.warn("No permissible coverage rule found for this plan. Skipping coverage validation.");
	    }

	    LOG.info("Coverage process() completed successfully.");
	    return pl;
	}

	/**
	 * Extracts all unique OccupancyTypeHelpers from the floors of all blocks in the plan.
	 *
	 * @param pl the Plan object
	 * @return a set of OccupancyTypeHelper extracted from the plan
	 */
	private Set<OccupancyTypeHelper> extractOccupancyList(Plan pl) {
	    Set<OccupancyTypeHelper> occupancyList = new HashSet<>();
	    for (Block block : pl.getBlocks()) {
	        for (Floor flr : block.getBuilding().getFloors()) {
	            for (Occupancy occupancy : flr.getOccupancies()) {
	                if (occupancy.getTypeHelper() != null && occupancy.getTypeHelper().getType() != null) {
	                    occupancyList.add(occupancy.getTypeHelper());
	                }
	            }
	        }
	    }
	    return occupancyList;
	}

	/**
	 * Calculates the total coverage area by summing up net coverage areas of all blocks.
	 *
	 * @param pl the Plan object
	 * @param plotArea the total plot area
	 * @return total net coverage area across all blocks
	 */
	private BigDecimal calculateCoverageAreas(Plan pl, BigDecimal plotArea) {
	    BigDecimal totalCoverageArea = BigDecimal.ZERO;

	    for (Block block : pl.getBlocks()) {
	        BigDecimal coverageAreaWithoutDeduction = BigDecimal.ZERO;
	        BigDecimal coverageDeductionArea = BigDecimal.ZERO;

	        for (Measurement coverage : block.getCoverage()) {
	            coverageAreaWithoutDeduction = coverageAreaWithoutDeduction.add(coverage.getArea());
	        }
	        for (Measurement deduct : block.getCoverageDeductions()) {
	            coverageDeductionArea = coverageDeductionArea.add(deduct.getArea());
	        }

	        BigDecimal netCoverageArea = coverageAreaWithoutDeduction.subtract(coverageDeductionArea);
	        if (block.getBuilding() != null) {
	            block.getBuilding().setCoverageArea(netCoverageArea);
	            if (plotArea.doubleValue() > 0) {
	                BigDecimal coveragePercentage = netCoverageArea.multiply(BigDecimal.valueOf(100))
	                        .divide(plotArea, DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS);
	                block.getBuilding().setCoverage(coveragePercentage);
	            }
	        }

	        totalCoverageArea = totalCoverageArea.add(netCoverageArea);
	    }

	    return totalCoverageArea;
	}

	/**
	 * Calculates the percentage of total coverage area with respect to plot area.
	 *
	 * @param plotArea total plot area
	 * @param totalCoverageArea total area covered by buildings
	 * @return coverage percentage
	 */
	private BigDecimal calculateTotalCoverage(BigDecimal plotArea, BigDecimal totalCoverageArea) {
	    LOG.info("Inside calculateTotalCoverage()");
	    LOG.info("Inputs -> plotArea: {}, totalCoverageArea: {}", plotArea, totalCoverageArea);

	    if (plotArea.compareTo(BigDecimal.ZERO) > 0) {
	        BigDecimal totalCoverage = totalCoverageArea.multiply(BigDecimal.valueOf(100))
	                .divide(plotArea, DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS);
	        LOG.info("Calculated totalCoverage (%): {}", totalCoverage);
	        return totalCoverage;
	    }

	    LOG.warn("Plot area is zero or negative. Returning totalCoverage = 0");
	    return BigDecimal.ZERO;
	}

	/**
	 * Retrieves the permissible coverage based on plot area and most restrictive occupancy.
	 *
	 * @param pl the Plan object
	 * @param plotArea the plot area
	 * @param mostRestrictiveOccupancy the occupancy considered most restrictive
	 * @return permissible coverage in percentage
	 */
	private BigDecimal getPermissibleCoverageIfApplicable(Plan pl, BigDecimal plotArea, OccupancyTypeHelper mostRestrictiveOccupancy) {
	    LOG.info("Inside getPermissibleCoverageIfApplicable()");
	    LOG.info("Inputs -> plotArea: {}, mostRestrictiveOccupancy: {}", plotArea,
	            mostRestrictiveOccupancy != null ? mostRestrictiveOccupancy.getType().getName() : "null");

	    if (plotArea.compareTo(BigDecimal.ZERO) > 0 && mostRestrictiveOccupancy != null) {
	        String occupancyName = fetchEdcrRulesMdms.getOccupancyName(pl).toLowerCase();
	        String feature = MdmsFeatureConstants.COVERAGE;
	        LOG.info("Fetching permissible coverage for occupancyName: {}, feature: {}", occupancyName, feature);

	        BigDecimal permissibleCoverage = getPermissibleCoverage(pl, plotArea, feature, occupancyName);
	        LOG.info("Permissible coverage found: {}", permissibleCoverage);
	        return permissibleCoverage;
	    }

	    LOG.warn("Either plotArea <= 0 or mostRestrictiveOccupancy is null. Returning permissibleCoverage = 0");
	    return BigDecimal.ZERO;
	}

	
	/**
	 * Fetches the permissible coverage from cached rules based on plot area and occupancy name.
	 *
	 * @param pl the Plan object
	 * @param area the plot area
	 * @param feature the feature for which rule is being fetched (e.g., "Coverage")
	 * @param occupancyName the name of the occupancy
	 * @return permissible coverage value
	 */
	private BigDecimal getPermissibleCoverage(Plan pl, BigDecimal area, String feature, String occupancyName) {
	    LOG.info("Inside getPermissibleCoverage()");
	    LOG.info("Inputs -> area: {}, feature: {}, occupancyName: {}", area, feature, occupancyName);

	    BigDecimal permissibleCoverage = BigDecimal.ZERO;

	    // Fetch all rules for the given plan from the cache.
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.COVERAGE.getValue(), true);
	    LOG.info("Fetched {} rules from cache for feature: {}", rules != null ? rules.size() : 0, FeatureEnum.COVERAGE.getValue());

	    Optional<CoverageRequirement> matchedRule = rules.stream()
	        .filter(CoverageRequirement.class::isInstance)
	        .map(CoverageRequirement.class::cast)
	        .filter(rule -> {
	            boolean matches = area.compareTo(rule.getFromPlotArea()) >= 0 && area.compareTo(rule.getToPlotArea()) < 0;
	            if (matches) {
	                LOG.info("Rule matched -> fromPlotArea: {}, toPlotArea: {}, permissible: {}", 
	                    rule.getFromPlotArea(), rule.getToPlotArea(), rule.getPermissible());
	            }
	            return matches;
	        })
	        .findFirst();

	    if (matchedRule.isPresent()) {
	        CoverageRequirement rule = matchedRule.get();
	        permissibleCoverage = rule.getPermissible();
	        LOG.info("Permissible coverage found: {}", permissibleCoverage);
	    } else {
	        permissibleCoverage = BigDecimal.ZERO;
	        LOG.warn("No matching coverage rule found for area: {}", area);
	    }

	    LOG.info("Returning permissibleCoverage: {}", permissibleCoverage);
	    return permissibleCoverage;
	}



//	

	/**
	 * Adds coverage scrutiny details to the plan report.
	 *
	 * @param pl the Plan object
	 * @param occupancy occupancy name
	 * @param coverage the actual coverage percentage
	 * @param upperLimit the permissible coverage limit
	 */
	private void processCoverage(Plan pl, String occupancy, BigDecimal coverage, BigDecimal upperLimit) {
	    LOG.info("Inside processCoverage()");
	    LOG.info("Inputs -> occupancy: {}, coverage: {}, upperLimit: {}", occupancy, coverage, upperLimit);

	    ScrutinyDetail scrutinyDetail = createCoverageScrutinyDetail(occupancy);
	    LOG.info("Created ScrutinyDetail for occupancy: {}", occupancy);

	    Map<String, String> details = createCoverageDetails(occupancy, coverage, upperLimit, pl);
	    LOG.info("Created coverage details: {}", details);

	    scrutinyDetail.getDetail().add(details);
	    LOG.info("Added details to scrutinyDetail for occupancy: {}", occupancy);

	    pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
	    LOG.info("ScrutinyDetail added to Plan report output for occupancy: {}", occupancy);
	}


	private ScrutinyDetail createCoverageScrutinyDetail(String occupancy) {
	    ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
	    scrutinyDetail.setKey(COMMON_COVERAGE);
	    scrutinyDetail.setHeading(COVERAGE_HEADING);
	    
	    scrutinyDetail.addColumnHeading(1, RULE_NO);
	    scrutinyDetail.addColumnHeading(2, OCCUPANCY);
	    scrutinyDetail.addColumnHeading(3, PERMISSIBLE);
	    scrutinyDetail.addColumnHeading(4, PROVIDED);
	    scrutinyDetail.addColumnHeading(5, STATUS);
	    
	    if (!isResidentialOrCommercial(occupancy)) {
	        scrutinyDetail.addColumnHeading(6, DESCRIPTION);
	    }
	    
	    return scrutinyDetail;
	}
	
	private Map<String, String> createCoverageDetails(String occupancy, BigDecimal coverage, BigDecimal upperLimit, Plan pl) {
	    String desc = getLocaleMessage(RULE_DESCRIPTION_KEY, upperLimit.toString());
	    String actualResult = getLocaleMessage(COVERAGE_RULE_ACTUAL_KEY, coverage.toString());
	    String expectedResult = getLocaleMessage(COVERAGE_RULE_EXPECTED_KEY, upperLimit.toString());
	    boolean isCompliant = coverage.compareTo(upperLimit) <= 0 || isResidentialOrCommercial(occupancy);

		ReportScrutinyDetail detail = new ReportScrutinyDetail();
		detail.setRuleNo(RULE);
		detail.setOccupancy(occupancy);
		detail.setPermissible(expectedResult);
		detail.setProvided(actualResult);
		detail.setStatus(isCompliant ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());
		if (!isResidentialOrCommercial(occupancy)) {
			detail.setDescription(desc);
		}
		Map<String, String> details = mapReportDetails(detail);
		return details;
	}

	private boolean isResidentialOrCommercial(String occupancy) {
	    return RESIDENTIAL.equalsIgnoreCase(occupancy) || COMMERCIAL.equalsIgnoreCase(occupancy);
	}


	protected OccupancyType getMostRestrictiveCoverage(EnumSet<OccupancyType> distinctOccupancyTypes) {

		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B1))
			return OccupancyType.OCCUPANCY_B1;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B2))
			return OccupancyType.OCCUPANCY_B2;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_B3))
			return OccupancyType.OCCUPANCY_B3;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_D))
			return OccupancyType.OCCUPANCY_D;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_D1))
			return OccupancyType.OCCUPANCY_D1;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_I2))
			return OccupancyType.OCCUPANCY_I2;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_I1))
			return OccupancyType.OCCUPANCY_I1;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_C))
			return OccupancyType.OCCUPANCY_C;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A1))
			return OccupancyType.OCCUPANCY_A1;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A4))
			return OccupancyType.OCCUPANCY_A4;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_A2))
			return OccupancyType.OCCUPANCY_A2;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_G1))
			return OccupancyType.OCCUPANCY_G1;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_E))
			return OccupancyType.OCCUPANCY_E;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_F))
			return OccupancyType.OCCUPANCY_F;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_F4))
			return OccupancyType.OCCUPANCY_F4;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_G2))
			return OccupancyType.OCCUPANCY_G2;
		if (distinctOccupancyTypes.contains(OccupancyType.OCCUPANCY_H))
			return OccupancyType.OCCUPANCY_H;

		else
			return null;
	}

	@Override
	public Map<String, Date> getAmendments() {
		return new LinkedHashMap<>();
	}
}
