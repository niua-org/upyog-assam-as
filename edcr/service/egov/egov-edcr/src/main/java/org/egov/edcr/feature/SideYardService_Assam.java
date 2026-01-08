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

import static org.egov.edcr.constants.CommonFeatureConstants.BASEMENT_SIDE_YARD;
import static org.egov.edcr.constants.CommonFeatureConstants.BLK_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.COMMA;
import static org.egov.edcr.constants.CommonFeatureConstants.EMPTY_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.FOR_BLOCK;
import static org.egov.edcr.constants.CommonFeatureConstants.LVL_0_NOT_DEFINED_PLAN;
import static org.egov.edcr.constants.CommonFeatureConstants.LVL_SIDE_SETBACK_1_NOT_DEFINED_PLAN;
import static org.egov.edcr.constants.CommonFeatureConstants.LVL_SIDE_SETBACK_2_NOT_DEFINED_PLAN;
import static org.egov.edcr.constants.CommonFeatureConstants.MIN_AND_MEAN_VALUE;
import static org.egov.edcr.constants.CommonFeatureConstants.MIN_LESS_REQ_MIN;
import static org.egov.edcr.constants.CommonFeatureConstants.NO_CONST_PERMIT_WIDTH_10_HEIGHT_12_G_3;
import static org.egov.edcr.constants.CommonFeatureConstants.NO_CONST_PERMIT_WIDTH_10_HEIGHT_16_G_4;
import static org.egov.edcr.constants.CommonFeatureConstants.PLOT_LESS_200SQM_EXPECTED_DISTANCE_TRUE;
import static org.egov.edcr.constants.CommonFeatureConstants.SIDE_SETBACK;
import static org.egov.edcr.constants.CommonFeatureConstants.SIDE_SETBACK_1_BLOCK;
import static org.egov.edcr.constants.CommonFeatureConstants.SIXTEEN_HEIGHT_TEN_WIDTH_SIDE_YARD;
import static org.egov.edcr.constants.CommonFeatureConstants.SLASH;
import static org.egov.edcr.constants.CommonFeatureConstants.TWELVE_HEIGHT_TEN_WIDTH_SIDE_YARD;
import static org.egov.edcr.constants.CommonFeatureConstants.UNDERSCORE;
import static org.egov.edcr.constants.CommonKeyConstants.BLOCK;
import static org.egov.edcr.constants.DxfFileConstants.A;
import static org.egov.edcr.constants.DxfFileConstants.A_AF;
import static org.egov.edcr.constants.DxfFileConstants.A_PO;
import static org.egov.edcr.constants.DxfFileConstants.A_R;
import static org.egov.edcr.constants.DxfFileConstants.B;
import static org.egov.edcr.constants.DxfFileConstants.C;
import static org.egov.edcr.constants.DxfFileConstants.D;
import static org.egov.edcr.constants.DxfFileConstants.D_AW;
import static org.egov.edcr.constants.DxfFileConstants.D_M;
import static org.egov.edcr.constants.DxfFileConstants.F;
import static org.egov.edcr.constants.DxfFileConstants.G;
import static org.egov.edcr.constants.DxfFileConstants.G_LI;
import static org.egov.edcr.constants.DxfFileConstants.G_PHI;
import static org.egov.edcr.constants.DxfFileConstants.G_SI;
import static org.egov.edcr.constants.DxfFileConstants.H;
import static org.egov.edcr.constants.DxfFileConstants.I;
import static org.egov.edcr.constants.EdcrReportConstants.BSMT_SIDE_YARD_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.BUILDING_HEIGHT;
import static org.egov.edcr.constants.EdcrReportConstants.BUILDING_HEIGHT_SCHOOL;
import static org.egov.edcr.constants.EdcrReportConstants.ERR_NARROW_ROAD_RULE;
import static org.egov.edcr.constants.EdcrReportConstants.MINIMUMLABEL;
import static org.egov.edcr.constants.EdcrReportConstants.PLOTAREA_300;
import static org.egov.edcr.constants.EdcrReportConstants.PLOT_AREA_802_SQM;
import static org.egov.edcr.constants.EdcrReportConstants.ROAD_WIDTH_TWELVE_POINTTWO;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_35_T9;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_36;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_37_TWO_A;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_37_TWO_B;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_37_TWO_C;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_37_TWO_D;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_37_TWO_G;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_37_TWO_H;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_37_TWO_I;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_47;
import static org.egov.edcr.constants.EdcrReportConstants.SIDENUMBER;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_EIGHT;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_FIVE;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_FOUR;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_FOURPOINTFIVE;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_NINE;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_ONE;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_ONEPOINTEIGHT;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_ONEPOINTFIVE;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_ONE_TWO;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_SEVEN;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_SEVENTYFIVE;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_SIX;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_TEN;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_THREE;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_THREEPOINTSIX;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_TWO;
import static org.egov.edcr.constants.EdcrReportConstants.SIDEVALUE_TWOPOINTFIVE;
import static org.egov.edcr.constants.EdcrReportConstants.SIDE_YARD_1_NOTDEFINED;
import static org.egov.edcr.constants.EdcrReportConstants.SIDE_YARD_2_NOTDEFINED;
import static org.egov.edcr.constants.EdcrReportConstants.SUB_RULE_SIDE_YARD;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.SIDE_YARD1_DESC;
import static org.egov.edcr.utility.DcrConstants.SIDE_YARD2_DESC;
import static org.egov.edcr.utility.DcrConstants.SIDE_YARD_DESC;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Building;
import org.egov.common.entity.edcr.FeatureEnum;
import org.egov.common.entity.edcr.Occupancy;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Plot;
import org.egov.common.entity.edcr.ReportScrutinyDetail;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.common.entity.edcr.SetBack;
import org.egov.common.entity.edcr.SideYardServiceRequirement;
import org.egov.common.entity.edcr.Yard;
import org.egov.edcr.constants.CommonFeatureConstants;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.infra.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SideYardService_Assam extends SideYardService {

	@Autowired
	MDMSCacheManager cache;

	private static final Logger LOG = LogManager.getLogger(SideYardService.class);

	private class SideYardResult {
		String rule;
		String desc;
		String subRule;
		String blockName;
		Integer level;
		BigDecimal actualMeanDistance = BigDecimal.ZERO;
		BigDecimal actualDistance = BigDecimal.ZERO;
		String occupancy;
		BigDecimal expectedDistance = BigDecimal.ZERO;
		BigDecimal expectedmeanDistance = BigDecimal.ZERO;
		boolean status = false;
	}

	/**
	 * Main entry point for processing side yard validations for all blocks in the
	 * plan. Validates side yard rules and processes each block individually.
	 *
	 * @param pl The building plan to process
	 */
	public void processSideYard(final Plan pl) {
		LOG.info("Processing SideYard:");
		if (pl.getPlot() == null)
			return;

		HashMap<String, String> errors = new HashMap<>();
		validateSideYardRule(pl);

		for (Block block : pl.getBlocks()) {
			processBlockForSideYard(pl, block, errors);
		}
	}

	/**
	 * Processes side yard validation for a specific building block. Sets up
	 * scrutiny details and processes each setback level.
	 *
	 * @param pl     The building plan
	 * @param block  The building block to process
	 * @param errors Map to collect validation errors
	 */
	private void processBlockForSideYard(Plan pl, Block block, HashMap<String, String> errors) {
		scrutinyDetail = new ScrutinyDetail();
		scrutinyDetail.addColumnHeading(1, RULE_NO);
		scrutinyDetail.addColumnHeading(2, LEVEL);
		scrutinyDetail.addColumnHeading(3, OCCUPANCY);
		scrutinyDetail.addColumnHeading(4, SIDENUMBER);
		scrutinyDetail.addColumnHeading(5, FIELDVERIFIED);
		scrutinyDetail.addColumnHeading(6, PERMISSIBLE);
		scrutinyDetail.addColumnHeading(7, PROVIDED);
		scrutinyDetail.addColumnHeading(8, STATUS);
		scrutinyDetail.setHeading(SIDE_YARD_DESC);

		SideYardResult sideYard1Result = new SideYardResult();
		SideYardResult sideYard2Result = new SideYardResult();

		for (SetBack setback : block.getSetBacks()) {
			processSetback(pl, block, setback, sideYard1Result, sideYard2Result, errors);
		}
	}

	/**
	 * Processes individual setback levels and validates side yard requirements.
	 * Handles both basement and above-ground level validations.
	 *
	 * @param pl              The building plan
	 * @param block           The building block
	 * @param setback         The setback level being processed
	 * @param sideYard1Result Result object for side yard 1
	 * @param sideYard2Result Result object for side yard 2
	 * @param errors          Map to collect validation errors
	 */

	private void processSetback(Plan pl, Block block, SetBack setback, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result, HashMap<String, String> errors) {

		Yard sideYard1 = getValidSideYard(setback.getSideYard1(), pl, block, sideYard1Result, sideYard2Result);
		Yard sideYard2 = getValidSideYard(setback.getSideYard2(), pl, block, sideYard1Result, sideYard2Result);

		if (sideYard1 != null || sideYard2 != null) {
			BigDecimal buildingHeight = computeBuildingHeight(block, sideYard1, sideYard2);
			double[] minMax = getMinMaxDistances(sideYard1, sideYard2);

			for (Occupancy occupancy : block.getBuilding().getTotalArea()) {
				scrutinyDetail.setKey(BLOCK + block.getName() + UNDERSCORE
						+ (setback.getLevel() < 0 ? BASEMENT_SIDE_YARD : SIDE_SETBACK));

				OccupancyTypeHelper mostRestrictiveOccupancy = occupancy.getTypeHelper();
				if (setback.getLevel() < 0) {
					checkSideYardBasement(pl, block.getBuilding(), buildingHeight, block.getName(), setback.getLevel(),
							pl.getPlot(), minMax[0], minMax[1], 0, 0, occupancy.getTypeHelper(), sideYard1Result,
							sideYard2Result);
				}

//				if (isApplicableSubtype(occupancy)) {
					//if (buildingHeight.compareTo(BigDecimal.valueOf(10)) <= 0)
							//&& block.getBuilding().getFloorsAboveGround().compareTo(BigDecimal.valueOf(3)) <= 0)
					{
						checkSideYardCommon(pl, block, block.getBuilding(), buildingHeight, block.getName(),
								setback.getLevel(), pl.getPlot(), minMax[0], minMax[1], occupancy.getTypeHelper(),
								sideYard1Result, sideYard2Result, errors);
					}
				//}

				
			}
			addSideYardResult(pl, errors, sideYard1Result, sideYard2Result);
		} else if (pl.getPlanInformation() != null
				&& pl.getPlanInformation().getWidthOfPlot().compareTo(BigDecimal.valueOf(10)) <= 0) {
			// Commented logic retained
		}
	}

	/**
	 * Validates and returns a side yard if it meets minimum requirements. Exempts
	 * certain occupancy types from side yard requirements.
	 *
	 * @param yard            The yard to validate
	 * @param pl              The building plan
	 * @param block           The building block
	 * @param sideYard1Result Result object for side yard 1
	 * @param sideYard2Result Result object for side yard 2
	 * @return Valid yard or null if exempt/invalid
	 */

	private Yard getValidSideYard(Yard yard, Plan pl, Block block, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result) {
		if (yard != null && yard.getMean().compareTo(BigDecimal.ZERO) > 0) {
			return yard;
		} else {
			exemptSideYardForAAndF(pl, block, sideYard1Result, sideYard2Result);
			return null;
		}
	}

	/**
	 * Computes the effective building height for side yard calculations. Uses the
	 * maximum height from available yard heights or building height.
	 *
	 * @param block     The building block
	 * @param sideYard1 First side yard
	 * @param sideYard2 Second side yard
	 * @return The computed building height
	 */

	private BigDecimal computeBuildingHeight(Block block, Yard sideYard1, Yard sideYard2) {
		if (sideYard1 != null && sideYard1.getHeight() != null && sideYard1.getHeight().compareTo(BigDecimal.ZERO) > 0
				&& sideYard2 != null && sideYard2.getHeight() != null
				&& sideYard2.getHeight().compareTo(BigDecimal.ZERO) > 0) {
			return sideYard1.getHeight().compareTo(sideYard2.getHeight()) >= 0 ? sideYard1.getHeight()
					: sideYard2.getHeight();
		} else if (sideYard1 != null && sideYard1.getHeight() != null
				&& sideYard1.getHeight().compareTo(BigDecimal.ZERO) > 0) {
			return sideYard1.getHeight();
		} else if (sideYard2 != null && sideYard2.getHeight() != null
				&& sideYard2.getHeight().compareTo(BigDecimal.ZERO) > 0) {
			return sideYard2.getHeight();
		} else {
			return block.getBuilding().getBuildingHeight();
		}
	}
	
	/**
	 * Applies special rules for side yard requirements when the building is located 
	 * on a narrow road (specifically a 2.40m wide road).
	 * <p>
	 * The rule is applicable only when the plot area falls within specific ranges:
	 * <ul>
	 *   <li><b>53.56 – 93.73 sqm:</b> Minimum side setback of 0.90m (mean setback also 0.90m).</li>
	 *   <li><b>93.73 – 134 sqm:</b> Minimum side setback of 2.00m.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * The method validates the provided side yard distances against the permissible 
	 * values, updates the {@link SideYardResult}, and logs errors if requirements are not met.
	 * </p>
	 *
	 * @param pl The {@link Plan} object containing overall plan details.
	 * @param building The {@link Building} under consideration.
	 * @param blockName The name of the block being validated.
	 * @param level The level/floor number being checked.
	 * @param plot The {@link Plot} object containing plot details such as area.
	 * @param mostRestrictiveOccupancy The most restrictive {@link OccupancyTypeHelper} 
	 *                                 applicable to this block.
	 * @param sideYard1Result The {@link SideYardResult} for the first side yard.
	 * @param sideYard2Result The {@link SideYardResult} for the second side yard.
	 * @param buildingHeight The height of the building.
	 * @param errors A {@link HashMap} for collecting error messages (block-specific).
	 * @param roadWidth The width of the road adjacent to the plot.
	 * @param plotArea The area of the plot.
	 * @param min The minimum permissible value to be compared against.
	 * @return {@code true} if both side yards satisfy the special setback rule, 
	 *         {@code false} otherwise.
	 */
	
	private Boolean applySpecialRuleForNarrowRoadSideYard(Plan pl, Building building, String blockName, Integer level,
	        Plot plot, OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result,
	        SideYardResult sideYard2Result, BigDecimal buildingHeight, HashMap<String, String> errors,
	        BigDecimal roadWidth, BigDecimal plotArea, final double min, final double max) {

	    LOG.info("Applying special narrow road rule (Side Yard, 2.40m road) for Block: {}, Level: {}, Plot Area: {}",
	            blockName, level, plotArea);

	    BigDecimal minVal = BigDecimal.ZERO;
	    BigDecimal meanVal = BigDecimal.ZERO;
	    String subRule = "";
	    String rule = SIDE_YARD_DESC;

	    if (plotArea.compareTo(BigDecimal.valueOf(53.56)) >= 0
	            && plotArea.compareTo(BigDecimal.valueOf(134)) <= 0) {

	        minVal = BigDecimal.valueOf(0.90);
	        meanVal = BigDecimal.valueOf(0.90);
	        subRule = "Side setback: 1.50 m";
	        LOG.info("Matched Plot Area range 53.56 - 93.73 sqm → {}", subRule);

	    }
	   
	    Boolean valid = (BigDecimal.valueOf(min) != null && BigDecimal.valueOf(min).compareTo(minVal) >= 0)
	            && (BigDecimal.valueOf(max) != null && BigDecimal.valueOf(max).compareTo(minVal) >= 0);
	    compareSideYardResult(blockName, minVal, BigDecimal.valueOf(min), mostRestrictiveOccupancy,
		        subRule, rule, valid, level, sideYard1Result, sideYard2Result, BigDecimal.valueOf(max));


	    LOG.info("Special side yard rule applied → RequiredMin: {}, ProvidedMin1: {}, ProvidedMin2: {}, Status: {}",
	            minVal, BigDecimal.valueOf(min), BigDecimal.valueOf(max), valid);

	    return valid;
	}


	/**
	 * Extracts minimum and maximum distances from the two side yards.
	 *
	 * @param sideYard1 First side yard
	 * @param sideYard2 Second side yard
	 * @return Array containing [min, max] distances
	 */
	private double[] getMinMaxDistances(Yard sideYard1, Yard sideYard2) {
		double min = 0;
		double max = 0;
		if (sideYard1 != null && sideYard2 != null) {
			if (sideYard1.getMinimumDistance().doubleValue() < sideYard2.getMinimumDistance().doubleValue()) {
				min = sideYard1.getMinimumDistance().doubleValue();
				max = sideYard2.getMinimumDistance().doubleValue();
			} else {
				min = sideYard2.getMinimumDistance().doubleValue();
				max = sideYard1.getMinimumDistance().doubleValue();
			}
		} else if (sideYard1 != null) {
			max = sideYard1.getMinimumDistance().doubleValue();
		} else if (sideYard2 != null) {
			min = sideYard2.getMinimumDistance().doubleValue();
		}
		return new double[] { min, max };
	}

	/**
	 * Checks if the occupancy subtype is applicable for residential side yard
	 * rules.
	 *
	 * @param occupancy The occupancy to check
	 * @return true if applicable for residential rules
	 */
	private boolean isApplicableSubtype(Occupancy occupancy) {
		return occupancy.getTypeHelper().getSubtype() != null
				&& (A_R.equalsIgnoreCase(occupancy.getTypeHelper().getSubtype().getCode())
						|| A_AF.equalsIgnoreCase(occupancy.getTypeHelper().getSubtype().getCode())
						|| A_PO.equalsIgnoreCase(occupancy.getTypeHelper().getSubtype().getCode()));
	}

	private void checkSideYardCommon(final Plan pl, Block block, Building building, BigDecimal buildingHeight,
			String blockName, Integer level, final Plot plot, final double min, final double max,
			final OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result, HashMap<String, String> errors) {

		BigDecimal plotArea = pl.getPlot().getArea();
		String rule = SIDE_YARD_DESC;
		String subRule = RULE_35_T9;
		String occupancyCode = mostRestrictiveOccupancy.getType().getCode();
		String subOccupancyCode = mostRestrictiveOccupancy.getSubtype().getCode();
		Boolean valid = false;
		String landUse = pl.getPlanInformation().getLandUseZone();
		BigDecimal roadWidth = pl.getPlanInformation().getRoadWidth();
		if (roadWidth != null 
		        && roadWidth.compareTo(BigDecimal.valueOf(2.40)) >= 0 
		        && roadWidth.compareTo(BigDecimal.valueOf(3.60)) <= 0) {
		        LOG.info("Checking special narrow road rule (SideYard) for Block: {}, Level: {}, RoadWidth: {}",
		                blockName, level, roadWidth);

		        BigDecimal allowedFloors = BigDecimal.valueOf(2); // G + 1 floors

		        BigDecimal actualFloors = BigDecimal.ZERO;
		        if (building != null) {
		            if (building.getTotalFloors() != null) {
		                actualFloors = building.getTotalFloors();
		            } else if (building.getFloors() != null) {
		                actualFloors = BigDecimal.valueOf(building.getFloors().size());
		            }
		        }

		        if (actualFloors.compareTo(allowedFloors) > 0) {
		            errors.put("NARROW_ROAD_RULE", String.format(ERR_NARROW_ROAD_RULE, actualFloors));
		            LOG.warn("Narrow road violation (SideYard): Allowed = {}, Actual = {}", allowedFloors, actualFloors);
		            return;
		        }

		        Boolean specialValid = applySpecialRuleForNarrowRoadSideYard(
		                pl, building, blockName, level, plot,
		                mostRestrictiveOccupancy, sideYard1Result, sideYard2Result,
		                buildingHeight, errors, roadWidth, plotArea, min, max);

		        if (specialValid != null) {
		            LOG.info("Special narrow road rule applied for SideYard. Result = {}", specialValid);
		            return; 
		        } else {
		            LOG.info("Special narrow road rule not applicable for SideYard, continuing normal checks.");
		        }
		    }

		if (A.equalsIgnoreCase(occupancyCode) || H.equalsIgnoreCase(occupancyCode)) {

			processSideYardResidential(pl, blockName, level, min, mostRestrictiveOccupancy, rule, subRule,
					buildingHeight, plotArea, sideYard1Result, sideYard2Result, max);
		} else if (F.equalsIgnoreCase(occupancyCode)) {
			if (buildingHeight.compareTo(BUILDING_HEIGHT) <= 0 && plot.getArea().compareTo(PLOT_AREA_802_SQM) <= 0) {
				// Commercial
				processSideYardCommercial(pl, blockName, level, min, mostRestrictiveOccupancy, rule, subRule,
						buildingHeight, plotArea, sideYard1Result, sideYard2Result, max);

			}else {
				processSideYardResidential(pl, blockName, level, min,  mostRestrictiveOccupancy, rule, subRule,
						buildingHeight, plotArea, sideYard1Result, sideYard2Result, max);
			}
		}else if (G.equalsIgnoreCase(occupancyCode)) {
			processSideYardIndustrial(pl, blockName, level, min, mostRestrictiveOccupancy, rule, subRule, buildingHeight,
					plotArea, sideYard1Result, sideYard2Result, max);
		
		}
		else if ((D.equalsIgnoreCase(occupancyCode) &&  D_AW.equalsIgnoreCase(subOccupancyCode))) {
			processSideYardPlaceOfworship(pl, blockName, level, min, mostRestrictiveOccupancy, rule, subRule, buildingHeight,
					plotArea, sideYard1Result, sideYard2Result, max);
		
		}
		else if (B.equalsIgnoreCase(occupancyCode) && buildingHeight.compareTo(BUILDING_HEIGHT_SCHOOL) <= 0) {
			processSideYardSchool(pl, blockName, level, min, mostRestrictiveOccupancy, rule, subRule, buildingHeight,
					plotArea, sideYard1Result, sideYard2Result, max);
		
		}
		 else if (B.equalsIgnoreCase(occupancyCode) && buildingHeight.compareTo(BUILDING_HEIGHT_SCHOOL) > 0) {
			 processSideYardResidential(pl, blockName, level, min, mostRestrictiveOccupancy, rule, subRule,
						buildingHeight, plotArea, sideYard1Result, sideYard2Result, max);
		 }
		else if (D.equalsIgnoreCase(occupancyCode) &&  D_M.equalsIgnoreCase(subOccupancyCode)) {
			processSideYardMultiplex(pl, blockName, level, min, mostRestrictiveOccupancy, rule, subRule, buildingHeight,
					plotArea, sideYard1Result, sideYard2Result, max);
		
		}else if (C.equalsIgnoreCase(occupancyCode)) {
			processSideYardHospitalAndNursingHomes(pl, blockName, level, min, mostRestrictiveOccupancy, rule, subRule, buildingHeight,
					plotArea, sideYard1Result, sideYard2Result, max);
		
		}else if (D.equalsIgnoreCase(occupancyCode)) {
			processSideYardAssembly(pl, blockName, level, min, mostRestrictiveOccupancy, rule, subRule, buildingHeight,
					plotArea, sideYard1Result, sideYard2Result, max);
		
		}
	}

	/**
	 * Validates side yard requirements for residential buildings based on plot
	 * area. Uses plot area-based calculations instead of height-based rules.
	 *
	 * @param pl                       The building plan
	 * @param blockName                Name of the building block
	 * @param level                    The setback level
	 * @param min                      Minimum side yard distance
	 * @param mostRestrictiveOccupancy The occupancy type
	 * @param rule                     The applicable rule
	 * @param subRule                  The sub-rule reference
	 * @param buildingHeight           Height of the building
	 * @param plotArea                 Area of the plot
	 * @param sideYard1Result          Result object for side yard 1
	 * @param sideYard2Result          Result object for side yard 2
	 */

	private Boolean processSideYardResidential(
	        Plan pl,
	        String blockName,
	        Integer level,
	        final double min,
	        final OccupancyTypeHelper mostRestrictiveOccupancy,
	        String rule,
	        String subRule,
	        BigDecimal buildingHeight,
	        BigDecimal plotArea,
	        SideYardResult sideYard1Result,
	        SideYardResult sideYard2Result,
	        final double max) {

	    LOG.info("Processing SideYardResidential with bye-law compliant logic");

	    BigDecimal minVal = BigDecimal.ZERO;
	    BigDecimal meanVal = BigDecimal.ZERO;
	    HashMap<String, String> errors = new HashMap<>();

	    subRule = SUB_RULE_SIDE_YARD;

	    // Fetch MDMS rules
	    List<Object> rules = cache.getFeatureRules(
	            pl, FeatureEnum.SIDE_YARD_SERVICE.getValue(), false);

	    List<SideYardServiceRequirement> mdmsRules = rules.stream()
	            .filter(SideYardServiceRequirement.class::isInstance)
	            .map(SideYardServiceRequirement.class::cast)
	            .filter(r -> Boolean.TRUE.equals(r.getActive()))
	            .filter(r -> r.getFromBuildingHeight() != null
                && r.getToBuildingHeight() != null)
	            .collect(Collectors.toList());

	    if (!mdmsRules.isEmpty() && buildingHeight != null) {

	        SideYardServiceRequirement applicableRule =
	                sideYardRule(buildingHeight, mdmsRules);

	        meanVal = applicableRule.getPermissible();
	        minVal = meanVal;

	        LOG.info("Side Yard applied slab up to height {} → permissible = {}",
	                applicableRule.getToBuildingHeight(), meanVal);

	    } else {
	        LOG.warn("No Side Yard MDMS rule found for height {}", buildingHeight);
	        errors.put("MDMS_RULE_MISSING",
	                "No side yard rule found for given building height in MDMS.");
	    }

	    // Validate minimum side yard
	    boolean valid = validateMinimumAndMeanValue(
	            BigDecimal.valueOf(min), minVal, plotArea);

	    if (!valid) {
	        LOG.info("Side Yard: min value validity FALSE actual/expected : {}/{}",
	                min, minVal);
	        errors.put(MIN_AND_MEAN_VALUE,
	                MIN_LESS_REQ_MIN + min + SLASH + minVal);
	    } else {
	        LOG.info("Side Yard: min value validity TRUE actual/expected : {}/{}",
	                min, minVal);
	    }

	    // Store scrutiny result
	    compareSideYardResult(
	            blockName,
	            minVal,
	            BigDecimal.valueOf(min),
	            mostRestrictiveOccupancy,
	            subRule,
	            rule,
	            valid,
	            level,
	            sideYard1Result,
	            sideYard2Result,
	            BigDecimal.valueOf(max));

	    return valid;
	}


	
	/**
	 * Resolves the applicable Side Yard setback rule based on the building height
	 * as per the approved building bye-laws.
	 *
	 * <p>
	 * This method applies the standard height-based slab selection along with the
	 * special Note (N.B.) clause defined in the bye-laws:
	 * </p>
	 *
	 * <p>
	 * <b>N.B.:</b> If the building height falls between two specified height slabs
	 * and exceeds the lower slab height by more than the permissible tolerance
	 * (10% of the slab height subject to a maximum of 1.5 meters),
	 * the next higher slab shall be considered for side yard setbacks.
	 * </p>
	 *
	 * <p>
	 * The method intentionally does <b>not</b> rely on direct
	 * {@code fromBuildingHeight}–{@code toBuildingHeight} range matching from MDMS.
	 * Instead, MDMS is treated as a set of reference height slabs, and the legal
	 * interpretation of the bye-law is applied in code to ensure correct and
	 * audit-safe scrutiny results.
	 * </p>
	 *
	 * <p>
	 * If the building height exceeds all defined slabs, the highest available
	 * slab is applied.
	 * </p>
	 *
	 * @param buildingHeight the total building height extracted from the drawing (in meters)
	 * @param rules          the list of active {@link SideYardServiceRequirement}
	 *                       rules fetched from MDMS
	 * @return the applicable {@link SideYardServiceRequirement} based on the
	 *         building height and bye-law tolerance rules
	 */
	private SideYardServiceRequirement sideYardRule(
	        BigDecimal buildingHeight,
	        List<SideYardServiceRequirement> rules) {

	    rules.sort(Comparator.comparing(SideYardServiceRequirement::getFromBuildingHeight));

	    for (int i = 0; i < rules.size(); i++) {

	        SideYardServiceRequirement current = rules.get(i);
	        BigDecimal upperHeight = current.getToBuildingHeight();

	        // Case 1: Normal slab match
	        if (buildingHeight.compareTo(upperHeight) <= 0) {
	            return current;
	        }

	        // Case 2: NB clause check
	        BigDecimal tenPercent = upperHeight.multiply(BigDecimal.valueOf(0.10));
	        BigDecimal tolerance = tenPercent.min(BigDecimal.valueOf(1.5));
	        BigDecimal maxAllowedHeight = upperHeight.add(tolerance);

	        if (buildingHeight.compareTo(upperHeight) > 0
	                && buildingHeight.compareTo(maxAllowedHeight) <= 0
	                && i + 1 < rules.size()) {

	            // Escalate to next slab
	            return rules.get(i + 1);
	        }
	    }

	    // If height exceeds all slabs → take highest slab
	    return rules.get(rules.size() - 1);
	}

	/**
	 * Processes and validates the side yard requirement for Industrial buildings based on MDMS rules.
	 * 
	 * <p>This method determines the permissible side yard width depending on the industrial subtype 
	 * (e.g., Small, Light, or Heavy Industry) retrieved from MDMS configuration. It then compares 
	 * the measured side yard values with the permissible limits and logs the results for scrutiny.</p>
	 * 
	 * @param pl                        The Plan object containing all building details.
	 * @param blockName                 The name of the block being processed.
	 * @param level                     The floor or level number being validated.
	 * @param min                       The minimum measured side yard value.
	 * @param mostRestrictiveOccupancy  The most restrictive occupancy type helper.
	 * @param rule                      The main rule reference.
	 * @param subRule                   The sub-rule reference.
	 * @param buildingHeight            The total height of the building.
	 * @param plotArea                  The total plot area of the building.
	 * @param sideYard1Result           The result object for the first side yard.
	 * @param sideYard2Result           The result object for the second side yard.
	 * @param max                       The maximum measured side yard value.
	 * @return                          True if validation passes based on MDMS permissible values; otherwise false.
	 */
	private Boolean processSideYardIndustrial(Plan pl, String blockName, Integer level, final double min,
	        final OccupancyTypeHelper mostRestrictiveOccupancy, String rule, String subRule, 
	        BigDecimal buildingHeight, BigDecimal plotArea,
	        SideYardResult sideYard1Result, SideYardResult sideYard2Result, final double max) {

	    LOG.info("Processing SideYardIndustrial with MDMS rules for Block: {}, Level: {}", blockName, level);

	    // Initialize variables
	    BigDecimal minVal = BigDecimal.ZERO;
	    BigDecimal meanVal = BigDecimal.ZERO;
	    subRule = SUB_RULE_SIDE_YARD;
	    Map<String, String> errors = new HashMap<>();

	    // Fetch rules from MDMS cache for SIDE_YARD_SERVICE
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.SIDE_YARD_SERVICE.getValue(), false);

	    // Identify applicable rule (active rule)
	    Optional<SideYardServiceRequirement> matchedRule = rules.stream()
	            .filter(SideYardServiceRequirement.class::isInstance)
	            .map(SideYardServiceRequirement.class::cast)
	            .filter(SideYardServiceRequirement::getActive)
	            .findFirst();

	    if (matchedRule.isPresent()) {
	        SideYardServiceRequirement mdmsRule = matchedRule.get();

	        // Determine subtype-specific permissible values
	        String subtypeCode = mostRestrictiveOccupancy.getSubtype() != null
	                ? mostRestrictiveOccupancy.getSubtype().getCode()
	                : null;

	        if (G_SI.equalsIgnoreCase(subtypeCode)) {
	            meanVal = mdmsRule.getPermissibleLight();
	        } else if (G_LI.equalsIgnoreCase(subtypeCode)) {
	            meanVal = mdmsRule.getPermissibleMedium();
	        } else if (G_PHI.equalsIgnoreCase(subtypeCode)) {
	            meanVal = mdmsRule.getPermissibleFlattered();
	        } else {
	            meanVal = mdmsRule.getPermissible();
	        }

	        minVal = meanVal;
	        LOG.info("Matched MDMS rule for Industrial subtype '{}': Permissible Side Yard = {}", subtypeCode, meanVal);

	    } else {
	        LOG.warn("No matching MDMS rule found for building height: {}", buildingHeight);
	        errors.put("MDMS_RULE_MISSING", "No setback rule found for given building height in MDMS.");
	    }

	    // Validate actual min value against permissible value
	    boolean valid = validateMinimumAndMeanValue(BigDecimal.valueOf(min), minVal, plotArea);

	    if (!valid) {
	        LOG.info("Side Yard Industrial: Validation FAILED — Actual: {}, Required: {}", min, minVal);
	        errors.put(MIN_AND_MEAN_VALUE, MIN_LESS_REQ_MIN + min + SLASH + minVal);
	    } else {
	        LOG.info("Side Yard Industrial: Validation PASSED — Actual: {}, Required: {}", min, minVal);
	    }

	    // Compare and record results
	    compareSideYardResult(blockName, minVal, BigDecimal.valueOf(min), mostRestrictiveOccupancy,
	            subRule, rule, valid, level, sideYard1Result, sideYard2Result, BigDecimal.valueOf(max));

	    return valid;
	}

	
	private Boolean processSideYardPlaceOfworship(Plan pl, String blockName, Integer level, final double min,
			final OccupancyTypeHelper mostRestrictiveOccupancy, String rule, String subRule, BigDecimal buildingHeight,
			BigDecimal plotArea, SideYardResult sideYard1Result, SideYardResult sideYard2Result, final double max) {

		LOG.info("Processing SideYardResidential with MDMS rules:");

		// Initialize values
		BigDecimal minVal = BigDecimal.ZERO;
		BigDecimal meanVal = BigDecimal.ZERO;
		HashMap<String, String> errors = new HashMap<>();

		
		subRule = SUB_RULE_SIDE_YARD;

		// Fetch rule set from cache
		List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.SIDE_YARD_SERVICE.getValue(), false);

	    Optional<SideYardServiceRequirement> matchedRule = rules.stream()
	        .filter(SideYardServiceRequirement.class::isInstance)
	        .map(SideYardServiceRequirement.class::cast)
	        .filter(ruleFeature ->
	                ruleFeature.getFromPlotDepth() != null && ruleFeature.getToPlotDepth() != null
	                && buildingHeight.compareTo(ruleFeature.getFromPlotDepth()) >= 0
	                && buildingHeight.compareTo(ruleFeature.getToPlotDepth()) < 0
	                && Boolean.TRUE.equals(ruleFeature.getActive()))
	        .findFirst();


		if (matchedRule.isPresent()) {
			SideYardServiceRequirement mdmsRule = matchedRule.get();
			meanVal = mdmsRule.getPermissible();
			minVal = meanVal; // Keeping minVal same as permissible from MDMS
		} else {
			LOG.warn("No matching MDMS rule found for building height: {}", buildingHeight);
			errors.put("MDMS_RULE_MISSING", "No setback rule found for given building height in MDMS.");
		}

		// Validate actual min value against expected values
		boolean valid = validateMinimumAndMeanValue(BigDecimal.valueOf(min), minVal, plotArea);

		// Compare results and store
		compareSideYardResult(blockName, minVal, BigDecimal.valueOf(min), mostRestrictiveOccupancy, subRule, rule,
				valid, level, sideYard1Result, sideYard2Result, BigDecimal.valueOf(max));

		return valid;
	}
	
	/**
	 * Processes and validates side yard requirements specifically for hospitals and nursing homes
	 * based on the plot depth and corresponding MDMS (Master Data Management System) rules.
	 * <p>
	 * The method retrieves permissible side yard distances from the cached MDMS configuration,
	 * compares them against the actual measured values from the plan, and updates the 
	 * {@link SideYardResult} objects with the validation details.
	 * </p>
	 *
	 * @param pl                       The {@link Plan} object containing building and plot details.
	 * @param blockName                The name of the block being processed.
	 * @param level                    The floor level for which the validation is being done.
	 * @param min                      The minimum actual side yard distance measured on-site.
	 * @param mostRestrictiveOccupancy  The most restrictive {@link OccupancyTypeHelper} for the block.
	 * @param rule                     The main rule number or identifier.
	 * @param subRule                  The sub-rule reference applicable to side yard validation.
	 * @param buildingHeight            The height of the building in meters.
	 * @param plotArea                 The total plot area in square meters.
	 * @param sideYard1Result          The result object to store the first side yard validation outcome.
	 * @param sideYard2Result          The result object to store the second side yard validation outcome.
	 * @param max                      The maximum actual side yard distance measured on-site.
	 *
	 * @return {@code true} if the side yard distances meet or exceed the permissible requirement
	 *         from the MDMS rule, {@code false} otherwise.
	 *
	 * @see SideYardResult
	 * @see SideYardServiceRequirement
	 * @see #compareSideYardResult(String, BigDecimal, BigDecimal, OccupancyTypeHelper, String, String, Boolean, Integer, SideYardResult, SideYardResult, BigDecimal)
	 */
	
	private Boolean processSideYardHospitalAndNursingHomes(Plan pl, String blockName, Integer level, final double min,
			final OccupancyTypeHelper mostRestrictiveOccupancy, String rule, String subRule, BigDecimal buildingHeight,
			BigDecimal plotArea, SideYardResult sideYard1Result, SideYardResult sideYard2Result, final double max) {

		LOG.info("Processing SideYardResidential with MDMS rules:");

		
		BigDecimal minVal = BigDecimal.ZERO;
		BigDecimal meanVal = BigDecimal.ZERO;
		HashMap<String, String> errors = new HashMap<>();
		BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();
	
		subRule = SUB_RULE_SIDE_YARD;

		// Fetch rule set from cache
		List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.SIDE_YARD_SERVICE.getValue(), false);

		 Optional<SideYardServiceRequirement> matchedRule = rules.stream()
			        .filter(SideYardServiceRequirement.class::isInstance)
			        .map(SideYardServiceRequirement.class::cast)
			        .filter(ruleFeature ->
			                ruleFeature.getFromPlotDepth() != null && ruleFeature.getToPlotDepth() != null
			                && depthOfPlot.compareTo(ruleFeature.getFromPlotDepth()) >= 0
			                && depthOfPlot.compareTo(ruleFeature.getToPlotDepth()) < 0
			                && Boolean.TRUE.equals(ruleFeature.getActive()))
			        .findFirst();
		 
	    

		if (matchedRule.isPresent()) {
			SideYardServiceRequirement mdmsRule = matchedRule.get();
			meanVal = mdmsRule.getPermissible();
			minVal = meanVal; // Keeping minVal same as permissible from MDMS
		} 
		// Validate actual min value against expected values
		boolean valid = validateMinimumAndMeanValue(BigDecimal.valueOf(min), minVal, plotArea);
		

		// Compare results and store
		compareSideYardResult(blockName, minVal, BigDecimal.valueOf(min), mostRestrictiveOccupancy, subRule, rule,
				valid, level, sideYard1Result, sideYard2Result, BigDecimal.valueOf(max));

		return valid;
	}
	
	/**
	 * Processes and validates the side yard requirements for various types of schools 
	 * (e.g., Nursery, Primary, High School, College) based on occupancy subtype and 
	 * corresponding MDMS (Master Data Management System) rules.
	 * <p>
	 * This method determines the permissible side yard distances from the MDMS configuration,
	 * validates the actual measured distances from the building plan against these permissible values,
	 * and records the results in {@link SideYardResult} objects for reporting and scrutiny.
	 * </p>
	 *
	 * @param pl                       The {@link Plan} object containing building and plot details.
	 * @param blockName                The name of the block being validated.
	 * @param level                    The floor level being processed.
	 * @param min                      The minimum actual side yard distance measured from the plan.
	 * @param mostRestrictiveOccupancy  The most restrictive {@link OccupancyTypeHelper} for the block.
	 * @param rule                     The main rule number or identifier applicable to side yard validation.
	 * @param subRule                  The specific sub-rule reference (e.g., "Side Yard Sub Rule").
	 * @param buildingHeight           The height of the building in meters.
	 * @param plotArea                 The total plot area in square meters.
	 * @param sideYard1Result          The result object storing the validation result for the first side yard.
	 * @param sideYard2Result          The result object storing the validation result for the second side yard.
	 * @param max                      The maximum actual side yard distance measured from the plan.
	 *
	 * @return {@code true} if the side yard distances meet or exceed the permissible requirement
	 *         as per MDMS rules; {@code false} otherwise.
	 *
	 * @see SideYardServiceRequirement
	 * @see SideYardResult
	 * @see #compareSideYardResult(String, BigDecimal, BigDecimal, OccupancyTypeHelper, String, String, Boolean, Integer, SideYardResult, SideYardResult, BigDecimal)
	 */
	private Boolean processSideYardSchool(Plan pl, String blockName, Integer level, final double min,
	        final OccupancyTypeHelper mostRestrictiveOccupancy, String rule, String subRule, BigDecimal buildingHeight,
	        BigDecimal plotArea, SideYardResult sideYard1Result, SideYardResult sideYard2Result, final double max) {

	    LOG.info("Processing Side Yard for School/Nursery/College buildings using MDMS rules for block: {}", blockName);

	    // Initialize values
	    BigDecimal minVal = BigDecimal.ZERO;
	    BigDecimal meanVal = BigDecimal.ZERO;
	    HashMap<String, String> errors = new HashMap<>();

	    subRule = SUB_RULE_SIDE_YARD;

	    // Fetch the rule set from cache
	    LOG.info("Fetching SIDE_YARD_SERVICE rules from cache for block: {}", blockName);
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.SIDE_YARD_SERVICE.getValue(), false);

	    Optional<SideYardServiceRequirement> matchedRule = rules.stream()
	            .filter(SideYardServiceRequirement.class::isInstance)
	            .map(SideYardServiceRequirement.class::cast)
	            .filter(ruleFeature -> Boolean.TRUE.equals(ruleFeature.getActive()))
	            .findFirst();

	    if (matchedRule.isPresent()) {
	        SideYardServiceRequirement mdmsRule = matchedRule.get();
	        LOG.info("Matched active SIDE_YARD_SERVICE rule from MDMS for block: {}", blockName);

	            meanVal = mdmsRule.getPermissible();
	            LOG.info("Occupancy subtype: COLLEGE | Permissible Side Yard: {}", meanVal);
	       
	        minVal = meanVal;
	    } else {
	        LOG.warn("No active MDMS rule found for SIDE_YARD_SERVICE. Setting permissible values to 0.");
	        meanVal = BigDecimal.ZERO;
	        minVal = BigDecimal.ZERO;
	    }

	    // Validate the actual minimum distance against expected permissible values
	    boolean valid = validateMinimumAndMeanValue(BigDecimal.valueOf(min), minVal, plotArea);
	    LOG.info("Side Yard validation result for block: {} | Actual Min: {} | Expected: {} | Valid: {}", 
	            blockName, min, minVal, valid);

	    // Compare and store results
	    compareSideYardResult(blockName, minVal, BigDecimal.valueOf(min), mostRestrictiveOccupancy, subRule, rule,
	            valid, level, sideYard1Result, sideYard2Result, BigDecimal.valueOf(max));

	    LOG.info("Side Yard comparison completed for block: {} | Level: {} | Status: {}", blockName, level, valid);

	    return valid;
	}

	
	/**
	 * Processes and validates the side yard requirements for assembly buildings
	 * (such as auditoriums, community halls, or similar structures) based on the 
	 * plot depth and building height using MDMS (Master Data Management System) rules.
	 * <p>
	 * The method retrieves permissible side yard distances from the cached MDMS configuration,
	 * validates the actual measured side yard distances against the permissible values,
	 * and records the results in {@link SideYardResult} objects for reporting.
	 * </p>
	 *
	 * @param pl                       The {@link Plan} object containing the building and plot details.
	 * @param blockName                The name of the block being processed.
	 * @param level                    The floor level under consideration.
	 * @param min                      The minimum actual side yard distance measured.
	 * @param mostRestrictiveOccupancy  The most restrictive {@link OccupancyTypeHelper} for the block.
	 * @param rule                     The main rule number or identifier for this validation.
	 * @param subRule                  The sub-rule reference specific to side yard validation.
	 * @param buildingHeight           The height of the building in meters.
	 * @param plotArea                 The total plot area in square meters.
	 * @param sideYard1Result          The result object to store the first side yard validation outcome.
	 * @param sideYard2Result          The result object to store the second side yard validation outcome.
	 * @param max                      The maximum actual side yard distance measured.
	 *
	 * @return {@code true} if the side yard distances meet or exceed the permissible requirement
	 *         as per MDMS rule; {@code false} otherwise.
	 *
	 * @see SideYardResult
	 * @see SideYardServiceRequirement
	 * @see #compareSideYardResult(String, BigDecimal, BigDecimal, OccupancyTypeHelper, String, String, Boolean, Integer, SideYardResult, SideYardResult, BigDecimal)
	 */
	private Boolean processSideYardAssembly(Plan pl, String blockName, Integer level, final double min,
	        final OccupancyTypeHelper mostRestrictiveOccupancy, String rule, String subRule, BigDecimal buildingHeight,
	        BigDecimal plotArea, SideYardResult sideYard1Result, SideYardResult sideYard2Result, final double max) {

	    LOG.info("Processing Side Yard for Assembly Occupancy using MDMS rules for block: {}", blockName);

	    // Initialize values
	    BigDecimal minVal = BigDecimal.ZERO;
	    BigDecimal meanVal = BigDecimal.ZERO;
	    HashMap<String, String> errors = new HashMap<>();
	    BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();

	    subRule = SUB_RULE_SIDE_YARD;

	    // Fetch rule set from cache
	    LOG.info("Fetching SIDE_YARD_SERVICE rules from cache for block: {}", blockName);
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.SIDE_YARD_SERVICE.getValue(), false);

	    LOG.debug("Filtering active SIDE_YARD_SERVICE rules based on building height: {}", buildingHeight);
	    Optional<SideYardServiceRequirement> matchedRule = rules.stream()
	            .filter(SideYardServiceRequirement.class::isInstance)
	            .map(SideYardServiceRequirement.class::cast)
	            .filter(ruleFeature ->
	                    ruleFeature.getFromPlotDepth() != null && ruleFeature.getToPlotDepth() != null
	                            && buildingHeight.compareTo(ruleFeature.getFromPlotDepth()) >= 0
	                            && buildingHeight.compareTo(ruleFeature.getToPlotDepth()) < 0
	                            && Boolean.TRUE.equals(ruleFeature.getActive()))
	            .findFirst();

	    if (matchedRule.isPresent()) {
	        SideYardServiceRequirement mdmsRule = matchedRule.get();
	        meanVal = mdmsRule.getPermissible();
	        minVal = meanVal; // Keeping minVal same as permissible from MDMS
	        LOG.info("Matched MDMS rule found for block: {} | Permissible Side Yard: {}", blockName, meanVal);
	    } else {
	        LOG.warn("No matching active SIDE_YARD_SERVICE rule found for block: {}. Defaulting permissible value to 0.", blockName);
	    }

	    // Validate actual min value against expected permissible value
	    boolean valid = validateMinimumAndMeanValue(BigDecimal.valueOf(min), minVal, plotArea);
	    LOG.info("Side Yard validation result for block: {} | Actual Min: {} | Expected: {} | Valid: {}", 
	            blockName, min, minVal, valid);

	    // Compare and store results
	    LOG.debug("Comparing and storing side yard results for block: {} | Level: {}", blockName, level);
	    compareSideYardResult(blockName, minVal, BigDecimal.valueOf(min), mostRestrictiveOccupancy, subRule, rule,
	            valid, level, sideYard1Result, sideYard2Result, BigDecimal.valueOf(max));

	    LOG.info("Side Yard comparison completed for Assembly Occupancy | Block: {} | Level: {} | Status: {}", 
	            blockName, level, valid);

	    return valid;
	}

	
	/**
	 * Processes and validates the side yard requirement for Multiplex buildings based on MDMS rules.
	 * 
	 * <p>This method fetches the permissible side yard values from MDMS for a given plot depth, 
	 * compares them with the actual measured values from the plan, validates the results, 
	 * and records them for reporting and scrutiny output.</p>
	 * 
	 * @param pl                        The Plan object containing all details of the building plan.
	 * @param blockName                 The name of the block being processed.
	 * @param level                     The floor or level number under consideration.
	 * @param min                       The minimum side yard measured value.
	 * @param mostRestrictiveOccupancy  The most restrictive occupancy type helper.
	 * @param rule                      The main rule reference.
	 * @param subRule                   The sub-rule reference.
	 * @param buildingHeight            The total building height.
	 * @param plotArea                  The total plot area.
	 * @param sideYard1Result           The result object for the first side yard.
	 * @param sideYard2Result           The result object for the second side yard.
	 * @param max                       The maximum side yard measured value.
	 * @return                          True if validation passes based on permissible values, otherwise false.
	 */
	private Boolean processSideYardMultiplex(Plan pl, String blockName, Integer level, final double min,
	        final OccupancyTypeHelper mostRestrictiveOccupancy, String rule, String subRule, 
	        BigDecimal buildingHeight, BigDecimal plotArea, 
	        SideYardResult sideYard1Result, SideYardResult sideYard2Result, final double max) {

	    LOG.info("Processing SideYardMultiplex with MDMS rules for Block: {}, Level: {}", blockName, level);

	    // Initialize permissible and minimum values
	    BigDecimal minVal = BigDecimal.ZERO;
	    BigDecimal meanVal = BigDecimal.ZERO;
	    BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();
	    subRule = SUB_RULE_SIDE_YARD;

	    // Fetch rules from cache for the Side Yard Service feature
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.SIDE_YARD_SERVICE.getValue(), false);

	    // Find applicable rule based on plot depth range and active flag
	    Optional<SideYardServiceRequirement> matchedRule = rules.stream()
	            .filter(SideYardServiceRequirement.class::isInstance)
	            .map(SideYardServiceRequirement.class::cast)
	            .filter(ruleFeature -> ruleFeature.getFromPlotDepth() != null 
	                    && ruleFeature.getToPlotDepth() != null
	                    && depthOfPlot.compareTo(ruleFeature.getFromPlotDepth()) >= 0
	                    && depthOfPlot.compareTo(ruleFeature.getToPlotDepth()) < 0
	                    && Boolean.TRUE.equals(ruleFeature.getActive()))
	            .findFirst();

	    if (matchedRule.isPresent()) {
	        SideYardServiceRequirement mdmsRule = matchedRule.get();
	        meanVal = mdmsRule.getPermissible();
	        minVal = meanVal; // For multiplex, permissible value acts as both mean and minimum
	        LOG.info("Matched MDMS Rule for depth {}: Permissible Side Yard = {}", depthOfPlot, meanVal);
	    } else {
	        LOG.warn("No matching MDMS rule found for depth: {}", depthOfPlot);
	    }

	    // Validate the actual minimum side yard value against permissible value
	    boolean valid = validateMinimumAndMeanValue(BigDecimal.valueOf(min), minVal, plotArea);
	    LOG.debug("Validation result for Side Yard (Min: {}, Required: {}): {}", min, minVal, valid);

	    // Compare and record results for reporting
	    compareSideYardResult(blockName, minVal, BigDecimal.valueOf(min), mostRestrictiveOccupancy, 
	            subRule, rule, valid, level, sideYard1Result, sideYard2Result, BigDecimal.valueOf(max));

	    return valid;
	}



	/**
	 * Validates minimum side yard distance against required values. Plots ≤200 sqm
	 * are exempt from minimum distance requirements.
	 *
	 * @param min      Actual minimum distance
	 * @param minval   Required minimum distance
	 * @param plotArea Area of the plot
	 * @return true if validation passes
	 */
	private Boolean validateMinimumAndMeanValue(final BigDecimal min, final BigDecimal minval, BigDecimal plotArea) {
		Boolean valid = false;

		if (min.compareTo(minval) >= 0) {
			valid = true;
		}

		return valid;
	}

	/**
	 * Compares and updates side yard results with validation outcomes. Sets the
	 * same values for both side yards assuming symmetric requirements.
	 *
	 * @param blockName                Name of the building block
	 * @param exptDistance             Expected minimum distance
	 * @param actualDistance           Actual provided distance
	 * @param mostRestrictiveOccupancy The occupancy type
	 * @param subRule                  The sub-rule reference
	 * @param rule                     The applicable rule
	 * @param valid                    Validation result
	 * @param level                    The setback level
	 * @param sideYard1Result          Result object for side yard 1
	 * @param sideYard2Result          Result object for side yard 2
	 */
	// Added by Bimal 18-March-2924 to check Side yard based on plot are not on
	// height
	private void compareSideYardResult(String blockName, BigDecimal exptDistance, BigDecimal actualDistance,
	        OccupancyTypeHelper mostRestrictiveOccupancy, String subRule, String rule, Boolean valid, Integer level,
	        SideYardResult sideYard1Result, SideYardResult sideYard2Result, BigDecimal actualDistance2) {

	    String occupancyName;
	    if (mostRestrictiveOccupancy.getSubtype() != null)
	        occupancyName = mostRestrictiveOccupancy.getSubtype().getName();
	    else
	        occupancyName = mostRestrictiveOccupancy.getType().getName();

	    // SideYard1
	    sideYard1Result.rule = rule;
	    sideYard1Result.occupancy = occupancyName;
	    sideYard1Result.subRule = subRule;
	    sideYard1Result.blockName = blockName;
	    sideYard1Result.level = level;
	    sideYard1Result.actualDistance = actualDistance;
	    sideYard1Result.expectedDistance = exptDistance;
	    sideYard1Result.status = valid;
	    sideYard1Result.desc = PLOT_LESS_200SQM_EXPECTED_DISTANCE_TRUE;

	    LOG.info("SideYard1Result: actualDistance/expectedDistance and status:" + sideYard1Result.actualDistance + "/"
				+ sideYard1Result.expectedDistance + "and " + sideYard1Result.status);
	    // SideYard2 
	    if (sideYard2Result != null) {
	        sideYard2Result.rule = rule;
	        sideYard2Result.occupancy = occupancyName;
	        sideYard2Result.subRule = subRule;
	        sideYard2Result.blockName = blockName;
	        sideYard2Result.level = level;
	        sideYard2Result.actualDistance = actualDistance2; 
	        sideYard2Result.expectedDistance = exptDistance;
	        sideYard2Result.status = valid;
	        sideYard2Result.desc = PLOT_LESS_200SQM_EXPECTED_DISTANCE_TRUE;
	    }
	    LOG.info("sideYard2Result: actualDistance/expectedDistance and status:" + sideYard2Result.actualDistance + "/"
				+ sideYard2Result.expectedDistance + "and " + sideYard2Result.status);
	}

	/**
	 * Adds side yard validation results to the plan's scrutiny details. Creates
	 * detailed reports for both side yards with acceptance status.
	 *
	 * @param pl              The building plan
	 * @param errors          Map of validation errors
	 * @param sideYard1Result Result object for side yard 1
	 * @param sideYard2Result Result object for side yard 2
	 */

	private void addSideYardResult(final Plan pl, HashMap<String, String> errors, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result) {
		if (sideYard1Result != null) {
			ReportScrutinyDetail detail = new ReportScrutinyDetail();
			detail.setRuleNo(sideYard1Result.subRule);
			detail.setLevel(sideYard1Result.level != null ? sideYard1Result.level.toString() : EMPTY_STRING);
			detail.setOccupancy(sideYard1Result.occupancy);
			detail.setFieldVerified(MINIMUMLABEL);
			detail.setPermissible(sideYard1Result.expectedDistance.toString());
			detail.setProvided(sideYard1Result.actualDistance.toString());
			detail.setSideNumber(SIDE_YARD1_DESC);
			if (sideYard1Result.status) {
				detail.setStatus(Result.Accepted.getResultVal());
			} else {
				detail.setStatus(Result.Not_Accepted.getResultVal());
			}

			Map<String, String> details = mapReportDetails(detail);
			addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
		}

		if (errors.isEmpty()) {
			if (sideYard2Result != null) {
				ReportScrutinyDetail detail2 = new ReportScrutinyDetail();
				detail2.setRuleNo(sideYard2Result.subRule);
				detail2.setLevel(sideYard2Result.level != null ? sideYard2Result.level.toString() : EMPTY_STRING);
				detail2.setOccupancy(sideYard2Result.occupancy);
				detail2.setFieldVerified(MINIMUMLABEL);
				detail2.setPermissible(sideYard2Result.expectedDistance.toString());
				detail2.setProvided(sideYard2Result.actualDistance.toString());
				detail2.setSideNumber(SIDE_YARD2_DESC);
				if (sideYard2Result.status) {
					detail2.setStatus(Result.Accepted.getResultVal());
				} else {
					detail2.setStatus(Result.Not_Accepted.getResultVal());
				}

				Map<String, String> details = mapReportDetails(detail2);
				addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
			}
		}
	}

	/**
	 * Exempts certain occupancy types (A and F) from side yard requirements.
	 * Removes related validation errors for exempt occupancies.
	 *
	 * @param pl              The building plan
	 * @param block           The building block
	 * @param sideYard1Result Result object for side yard 1
	 * @param sideYard2Result Result object for side yard 2
	 */
	private void exemptSideYardForAAndF(final Plan pl, Block block, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result) {
		for (final Occupancy occupancy : block.getBuilding().getTotalArea()) {
			scrutinyDetail.setKey(BLOCK + block.getName() + UNDERSCORE + SIDE_SETBACK);
			if (occupancy.getTypeHelper().getType() != null
					&& A.equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())
					|| F.equalsIgnoreCase(occupancy.getTypeHelper().getType().getCode())) {
				if (pl.getErrors().containsKey(SIDE_YARD_2_NOTDEFINED)) {
					pl.getErrors().remove(SIDE_YARD_2_NOTDEFINED);
				}
				if (pl.getErrors().containsKey(SIDE_YARD_1_NOTDEFINED)) {
					pl.getErrors().remove(SIDE_YARD_1_NOTDEFINED);
				}
				if (pl.getErrors().containsKey(SIDE_YARD_DESC)) {
					pl.getErrors().remove(SIDE_YARD_DESC);
				}
				if (pl.getErrors()
						.containsValue(BLK_STRING + block.getNumber() + LVL_SIDE_SETBACK_1_NOT_DEFINED_PLAN)) {
					pl.getErrors().remove(EMPTY_STRING,
							BLK_STRING + block.getNumber() + LVL_SIDE_SETBACK_1_NOT_DEFINED_PLAN);
				}
				if (pl.getErrors()
						.containsValue(BLK_STRING + block.getNumber() + LVL_SIDE_SETBACK_2_NOT_DEFINED_PLAN)) {
					pl.getErrors().remove(EMPTY_STRING,
							BLK_STRING + block.getNumber() + LVL_SIDE_SETBACK_2_NOT_DEFINED_PLAN);
				}
				if (pl.getErrors().containsValue(SIDE_SETBACK_1_BLOCK + block.getNumber() + LVL_0_NOT_DEFINED_PLAN)) {
					pl.getErrors().remove(EMPTY_STRING,
							SIDE_SETBACK_1_BLOCK + block.getNumber() + LVL_0_NOT_DEFINED_PLAN);
				}

			}

			compareSideYard2Result(block.getName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
					occupancy.getTypeHelper(), sideYard2Result, true, RULE_35_T9, SIDE_YARD_DESC, 0);
			compareSideYard1Result(block.getName(), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
					occupancy.getTypeHelper(), sideYard1Result, true, RULE_35_T9, SIDE_YARD_DESC, 0);
		}
	}

	/**
	 * Validates side yard requirements for buildings up to 10 meters height.
	 * Handles different occupancy types (residential, commercial) and applies
	 * appropriate rules based on plot width and land use zone.
	 *
	 * @param pl                       The building plan containing plot and plan
	 *                                 information
	 * @param building                 The building being validated
	 * @param buildingHeight           Height of the building
	 * @param blockName                Name of the building block
	 * @param level                    Floor level being checked
	 * @param plot                     The plot containing the building
	 * @param min                      Minimum side yard distance measured
	 * @param max                      Maximum side yard distance measured
	 * @param minMeanlength            Mean length for minimum side
	 * @param maxMeanLength            Mean length for maximum side
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard1Result          Result object for side yard 1 validation
	 * @param sideYard2Result          Result object for side yard 2 validation
	 */

	private void checkSideYardUptoTenMts(final Plan pl, Building building, BigDecimal buildingHeight, String blockName,
			Integer level, final Plot plot, final double min, final double max, double minMeanlength,
			double maxMeanLength, final OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result) {

		String rule = SIDE_YARD_DESC;
		String subRule = RULE_35_T9;
		Boolean valid2 = false;
		Boolean valid1 = false;
		BigDecimal side2val = BigDecimal.ZERO;
		BigDecimal side1val = BigDecimal.ZERO;

		BigDecimal widthOfPlot = pl.getPlanInformation().getWidthOfPlot();

		if (mostRestrictiveOccupancy.getSubtype() != null
				&& (A_R.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
						|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
						|| A_PO.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()))) {
			if (pl.getPlanInformation() != null && pl.getPlanInformation().getRoadWidth() != null
					&& StringUtils.isNotBlank(pl.getPlanInformation().getLandUseZone())
					&& DxfFileConstants.COMMERCIAL.equalsIgnoreCase(pl.getPlanInformation().getLandUseZone())
					&& pl.getPlanInformation().getRoadWidth().compareTo(ROAD_WIDTH_TWELVE_POINTTWO) < 0) {
				checkCommercialUptoSixteen(blockName, level, min, max, minMeanlength, maxMeanLength,
						mostRestrictiveOccupancy, sideYard1Result, sideYard2Result, rule, DxfFileConstants.RULE_28,
						valid2, valid1, side2val, side1val, widthOfPlot);
			} else {
				checkResidentialUptoTenMts(pl, blockName, level, min, max, minMeanlength, maxMeanLength,
						mostRestrictiveOccupancy, sideYard1Result, sideYard2Result, rule, subRule, valid2, valid1,
						side2val, side1val, widthOfPlot);
			}
		} else if (F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			checkCommercialUptoSixteen(blockName, level, min, max, minMeanlength, maxMeanLength,
					mostRestrictiveOccupancy, sideYard1Result, sideYard2Result, rule, subRule, valid2, valid1, side2val,
					side1val, widthOfPlot);
		}
	}

	/**
	 * Validates residential side yard requirements for buildings up to 10 meters
	 * height. Sets side yard values based on plot width ranges and validates
	 * against measured distances.
	 *
	 * @param pl                       The building plan
	 * @param blockName                Name of the building block
	 * @param level                    Floor level being checked
	 * @param min                      Minimum side yard distance measured
	 * @param max                      Maximum side yard distance measured
	 * @param minMeanlength            Mean length for minimum side
	 * @param maxMeanLength            Mean length for maximum side
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard1Result          Result object for side yard 1 validation
	 * @param sideYard2Result          Result object for side yard 2 validation
	 * @param rule                     Main rule description
	 * @param subRule                  Specific sub-rule reference
	 * @param valid2                   Validation status for side yard 2
	 * @param valid1                   Validation status for side yard 1
	 * @param side2val                 Required value for side yard 2
	 * @param side1val                 Required value for side yard 1
	 * @param widthOfPlot              Width of the plot
	 */
	private void checkResidentialUptoTenMts(Plan pl, String blockName, Integer level, final double min,
			final double max, double minMeanlength, double maxMeanLength,
			final OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result, String rule, String subRule, Boolean valid2, Boolean valid1,
			BigDecimal side2val, BigDecimal side1val, BigDecimal widthOfPlot) {
		if (widthOfPlot.compareTo(BigDecimal.valueOf(10)) <= 0) {
			if (pl.getErrors().containsKey(SIDE_YARD_2_NOTDEFINED)) {
				pl.getErrors().remove(SIDE_YARD_2_NOTDEFINED);
			}
			if (pl.getErrors().containsKey(SIDE_YARD_1_NOTDEFINED)) {
				pl.getErrors().remove(SIDE_YARD_1_NOTDEFINED);
			}
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(10)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(15)) <= 0) {
			side2val = SIDEVALUE_SEVENTYFIVE;
			side1val = SIDEVALUE_SEVENTYFIVE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(15)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(21)) <= 0) {
			side2val = SIDEVALUE_ONE;
			side1val = SIDEVALUE_ONE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(21)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(27)) <= 0) {
			side2val = SIDEVALUE_ONEPOINTFIVE;
			side1val = SIDEVALUE_ONEPOINTFIVE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(27)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(33)) <= 0) {
			side2val = SIDEVALUE_ONEPOINTFIVE;
			side1val = SIDEVALUE_ONEPOINTFIVE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(33)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(39)) <= 0) {
			side2val = SIDEVALUE_TWO;
			side1val = SIDEVALUE_TWO;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(39)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(45)) <= 0) {
			side2val = SIDEVALUE_TWO;
			side1val = SIDEVALUE_TWO;
		}

		if (max >= side1val.doubleValue())
			valid1 = true;
		if (min >= side2val.doubleValue())
			valid2 = true;

		compareSideYard2Result(blockName, side2val, BigDecimal.valueOf(min), BigDecimal.ZERO,
				BigDecimal.valueOf(minMeanlength), mostRestrictiveOccupancy, sideYard2Result, valid2, subRule, rule,
				level);
		compareSideYard1Result(blockName, side1val, BigDecimal.valueOf(max), BigDecimal.ZERO,
				BigDecimal.valueOf(maxMeanLength), mostRestrictiveOccupancy, sideYard1Result, valid1, subRule, rule,
				level);
	}

	/**
	 * Validates basement side yard requirements for residential and commercial
	 * occupancies. Applies specific rules for plots up to 300 sqm area.
	 *
	 * @param pl                       The building plan
	 * @param building                 The building being validated
	 * @param buildingHeight           Height of the building
	 * @param blockName                Name of the building block
	 * @param level                    Floor level being checked
	 * @param plot                     The plot containing the building
	 * @param min                      Minimum side yard distance measured
	 * @param max                      Maximum side yard distance measured
	 * @param minMeanlength            Mean length for minimum side
	 * @param maxMeanLength            Mean length for maximum side
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard1Result          Result object for side yard 1 validation
	 * @param sideYard2Result          Result object for side yard 2 validation
	 */
	private void checkSideYardBasement(final Plan pl, Building building, BigDecimal buildingHeight, String blockName,
			Integer level, final Plot plot, final double min, final double max, double minMeanlength,
			double maxMeanLength, final OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result) {

		String rule = SIDE_YARD_DESC;
		String subRule = RULE_47;
		Boolean valid2 = false;
		Boolean valid1 = false;
		BigDecimal side2val = BigDecimal.ZERO;
		BigDecimal side1val = BigDecimal.ZERO;

		if ((mostRestrictiveOccupancy.getSubtype() != null
				&& A_R.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_PO.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode()))
				|| F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			if (plot.getArea().compareTo(BigDecimal.valueOf(PLOTAREA_300)) <= 0) {
				side2val = SIDEVALUE_THREE;
				side1val = SIDEVALUE_THREE;

				if (max >= side1val.doubleValue())
					valid1 = true;
				if (min >= side2val.doubleValue())
					valid2 = true;

				rule = BSMT_SIDE_YARD_DESC;

				compareSideYard2Result(blockName, side2val, BigDecimal.valueOf(min), BigDecimal.ZERO,
						BigDecimal.valueOf(minMeanlength), mostRestrictiveOccupancy, sideYard2Result, valid2, subRule,
						rule, level);
				compareSideYard1Result(blockName, side1val, BigDecimal.valueOf(max), BigDecimal.ZERO,
						BigDecimal.valueOf(maxMeanLength), mostRestrictiveOccupancy, sideYard1Result, valid1, subRule,
						rule, level);
			}
		}
	}

	/**
	 * Processes and validates the Side Yard requirements for Commercial buildings based on MDMS rules.
	 *
	 * <p>This method retrieves the permissible side yard width from MDMS configuration 
	 * using the plot depth as a reference. It then compares the actual measured values 
	 * against the permissible minimum values, logs the results, and updates the 
	 * corresponding {@link SideYardResult} objects for further scrutiny or reporting.</p>
	 *
	 * @param pl                        The {@link Plan} object containing building and plot details.
	 * @param blockName                 The name of the block being processed.
	 * @param level                     The floor or level number being validated.
	 * @param min                       The minimum measured side yard width.
	 * @param mostRestrictiveOccupancy  The most restrictive occupancy type helper.
	 * @param rule                      The main rule reference from the building regulation.
	 * @param subRule                   The sub-rule identifier for the side yard.
	 * @param buildingHeight            The total height of the building.
	 * @param plotArea                  The total plot area being considered.
	 * @param sideYard1Result           The result object to store evaluation details for the first side yard.
	 * @param sideYard2Result           The result object to store evaluation details for the second side yard.
	 * @param max                       The maximum measured side yard width.
	 * 
	 * @return                          {@code true} if the side yard measurement meets or exceeds 
	 *                                  the permissible requirement; {@code false} otherwise.
	 */
	private Boolean processSideYardCommercial(Plan pl, String blockName, Integer level, final double min,
	        final OccupancyTypeHelper mostRestrictiveOccupancy, String rule, String subRule,
	        BigDecimal buildingHeight, BigDecimal plotArea,
	        SideYardResult sideYard1Result, SideYardResult sideYard2Result, final double max) {

	    LOG.info("Processing SideYardCommercial with MDMS rules:");

	    // Initialize values
	    BigDecimal minVal = BigDecimal.ZERO;
	    BigDecimal meanVal = BigDecimal.ZERO;
	    HashMap<String, String> errors = new HashMap<>();

	    // Override subRule description
	    subRule = SUB_RULE_SIDE_YARD;

	    // Get plot depth
	    BigDecimal depthOfPlot = pl.getPlanInformation().getDepthOfPlot();

	    // Fetch MDMS rules for SIDE_YARD_SERVICE
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.SIDE_YARD_SERVICE.getValue(), false);

	    // Match appropriate rule from MDMS based on plot depth
	    Optional<SideYardServiceRequirement> matchedRule = rules.stream()
	            .filter(SideYardServiceRequirement.class::isInstance)
	            .map(SideYardServiceRequirement.class::cast)
	            .filter(ruleFeature ->
	                    ruleFeature.getFromPlotDepth() != null &&
	                    ruleFeature.getToPlotDepth() != null &&
	                    depthOfPlot.compareTo(ruleFeature.getFromPlotDepth()) >= 0 &&
	                    depthOfPlot.compareTo(ruleFeature.getToPlotDepth()) <= 0 &&
	                    Boolean.TRUE.equals(ruleFeature.getActive()))
	            .findFirst();

	    if (matchedRule.isPresent()) {
	        SideYardServiceRequirement mdmsRule = matchedRule.get();
	        meanVal = mdmsRule.getPermissible();
	        minVal = meanVal; // If MDMS provides separate min, adjust accordingly
	    } else {
	        LOG.warn("No matching MDMS rule found for plot depth: {}", depthOfPlot);
	        errors.put("MDMS_RULE_MISSING", "No setback rule found for given plot depth in MDMS.");
	    }

	    // Validate actual min value against expected values
	    boolean valid = validateMinimumAndMeanValue(BigDecimal.valueOf(min), minVal, plotArea);
	    if (!valid) {
	        LOG.info("Commercial Side Yard Service: min value validity False: actual/expected : {}/{}", min, minVal);
	        errors.put(MIN_AND_MEAN_VALUE, MIN_LESS_REQ_MIN + min + SLASH + minVal);
	    } else {
	        LOG.info("Commercial Side Yard Service: min value validity True: actual/expected : {}/{}", min, minVal);
	    }

	    // Compare results and store
	    compareSideYardResult(blockName, minVal, BigDecimal.valueOf(min),
	            mostRestrictiveOccupancy, subRule, rule, valid, level, sideYard1Result, sideYard2Result, BigDecimal.valueOf(max));

	    return valid;
	}


	/**
	 * Validates side yard requirements for industrial occupancy buildings. Sets
	 * side yard values based on plot area and width combinations.
	 *
	 * @param pl                       The building plan
	 * @param building                 The building being validated
	 * @param buildingHeight           Height of the building
	 * @param blockName                Name of the building block
	 * @param level                    Floor level being checked
	 * @param plot                     The plot containing the building
	 * @param min                      Minimum side yard distance measured
	 * @param max                      Maximum side yard distance measured
	 * @param minMeanlength            Mean length for minimum side
	 * @param maxMeanLength            Mean length for maximum side
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard1Result          Result object for side yard 1 validation
	 * @param sideYard2Result          Result object for side yard 2 validation
	 */

	private void checkSideYardForIndustrial(final Plan pl, Building building, BigDecimal buildingHeight,
			String blockName, Integer level, final Plot plot, final double min, final double max, double minMeanlength,
			double maxMeanLength, final OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result) {

		String rule = SIDE_YARD_DESC;
		String subRule = RULE_35_T9;
		Boolean valid2 = false;
		Boolean valid1 = false;
		BigDecimal side2val = BigDecimal.ZERO;
		BigDecimal side1val = BigDecimal.ZERO;

		BigDecimal widthOfPlot = pl.getPlanInformation().getWidthOfPlot();
		BigDecimal plotArea = pl.getPlot().getArea();

		if (plotArea.compareTo(BigDecimal.valueOf(550)) < 0) {
			if (widthOfPlot.compareTo(BigDecimal.valueOf(10)) <= 0) {
				side2val = SIDEVALUE_ONEPOINTFIVE;
				side1val = SIDEVALUE_ONEPOINTFIVE;
			} else if (widthOfPlot.compareTo(BigDecimal.valueOf(12)) <= 0) {
				side2val = SIDEVALUE_TWO;
				side1val = SIDEVALUE_TWO;
			} else if (widthOfPlot.compareTo(BigDecimal.valueOf(15)) <= 0) {
				side2val = SIDEVALUE_THREE;
				side1val = SIDEVALUE_THREE;
			} else if (widthOfPlot.compareTo(BigDecimal.valueOf(18)) <= 0) {
				side2val = SIDEVALUE_FOUR;
				side1val = SIDEVALUE_FOUR;
			} else if (widthOfPlot.compareTo(BigDecimal.valueOf(18)) > 0) {
				side2val = SIDEVALUE_FOURPOINTFIVE;
				side1val = SIDEVALUE_FOURPOINTFIVE;
			}
		} else if (plotArea.compareTo(BigDecimal.valueOf(550)) > 0
				&& plotArea.compareTo(BigDecimal.valueOf(1000)) <= 0) {
			side2val = SIDEVALUE_FOURPOINTFIVE;
			side1val = SIDEVALUE_FOURPOINTFIVE;
		} else if (plotArea.compareTo(BigDecimal.valueOf(1000)) > 0
				&& plotArea.compareTo(BigDecimal.valueOf(5000)) <= 0) {
			side2val = SIDEVALUE_SIX;
			side1val = SIDEVALUE_SIX;
		} else if (plotArea.compareTo(BigDecimal.valueOf(5000)) > 0
				&& plotArea.compareTo(BigDecimal.valueOf(30000)) <= 0) {
			side2val = SIDEVALUE_NINE;
			side1val = SIDEVALUE_NINE;
		} else if (plotArea.compareTo(BigDecimal.valueOf(30000)) > 0) {
			side2val = SIDEVALUE_TEN;
			side1val = SIDEVALUE_TEN;
		}

		if (max >= side1val.doubleValue())
			valid1 = true;
		if (min >= side2val.doubleValue())
			valid2 = true;

		compareSideYard2Result(blockName, side2val, BigDecimal.valueOf(min), BigDecimal.ZERO,
				BigDecimal.valueOf(minMeanlength), mostRestrictiveOccupancy, sideYard2Result, valid2, subRule, rule,
				level);
		compareSideYard1Result(blockName, side1val, BigDecimal.valueOf(max), BigDecimal.ZERO,
				BigDecimal.valueOf(maxMeanLength), mostRestrictiveOccupancy, sideYard1Result, valid1, subRule, rule,
				level);

	}

	/**
	 * Validates side yard requirements for various occupancy types including
	 * educational, institutional, assembly, malls, hazardous, affordable housing,
	 * and IT/ITES.
	 *
	 * @param pl                       The building plan
	 * @param building                 The building being validated
	 * @param buildingHeight           Height of the building
	 * @param blockName                Name of the building block
	 * @param level                    Floor level being checked
	 * @param plot                     The plot containing the building
	 * @param min                      Minimum side yard distance measured
	 * @param max                      Maximum side yard distance measured
	 * @param minMeanlength            Mean length for minimum side
	 * @param maxMeanLength            Mean length for maximum side
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard1Result          Result object for side yard 1 validation
	 * @param sideYard2Result          Result object for side yard 2 validation
	 */

	private void checkSideYardForOtherOccupancies(final Plan pl, Building building, BigDecimal buildingHeight,
			String blockName, Integer level, final Plot plot, final double min, final double max, double minMeanlength,
			double maxMeanLength, final OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result) {

		String rule = SIDE_YARD_DESC;
		String subRule = RULE_35_T9;
		Boolean valid2 = false;
		Boolean valid1 = false;
		BigDecimal side2val = BigDecimal.ZERO;
		BigDecimal side1val = BigDecimal.ZERO;

		// Educational
		if (mostRestrictiveOccupancy.getType() != null
				&& B.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			side2val = SIDEVALUE_SIX;
			side1val = SIDEVALUE_SIX;
			subRule = RULE_37_TWO_A;
		} // Institutional
		if (mostRestrictiveOccupancy.getType() != null
				&& B.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			side2val = SIDEVALUE_SIX;
			side1val = SIDEVALUE_SIX;
			subRule = RULE_37_TWO_B;
		} // Assembly
		if (mostRestrictiveOccupancy.getType() != null
				&& D.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			side2val = SIDEVALUE_SIX;
			side1val = SIDEVALUE_SIX;
			subRule = RULE_37_TWO_C;
		} // Malls and multiplex
		if (mostRestrictiveOccupancy.getType() != null
				&& D.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			side2val = SIDEVALUE_SEVEN;
			side1val = SIDEVALUE_SEVEN;
			subRule = RULE_37_TWO_D;
		} // Hazardous
		if (mostRestrictiveOccupancy.getType() != null
				&& I.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			side2val = SIDEVALUE_NINE;
			side1val = SIDEVALUE_NINE;
			subRule = RULE_37_TWO_G;
		} // Affordable
		if (mostRestrictiveOccupancy.getType() != null
				&& A.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			side2val = SIDEVALUE_THREE;
			side1val = SIDEVALUE_THREE;
			subRule = RULE_37_TWO_H;
		}
		// IT,ITES
		if (mostRestrictiveOccupancy.getType() != null
				&& F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			// nil as per commercial
			subRule = RULE_37_TWO_I;
		}

		if (max >= side1val.doubleValue())
			valid1 = true;
		if (min >= side2val.doubleValue())
			valid2 = true;

		compareSideYard2Result(blockName, side2val, BigDecimal.valueOf(min), BigDecimal.ZERO,
				BigDecimal.valueOf(minMeanlength), mostRestrictiveOccupancy, sideYard2Result, valid2, subRule, rule,
				level);
		compareSideYard1Result(blockName, side1val, BigDecimal.valueOf(max), BigDecimal.ZERO,
				BigDecimal.valueOf(maxMeanLength), mostRestrictiveOccupancy, sideYard1Result, valid1, subRule, rule,
				level);

	}

	/**
	 * Validates side yard requirements for buildings up to 12 meters height.
	 * Handles residential and commercial occupancies with specific width
	 * restrictions.
	 *
	 * @param pl                       The building plan
	 * @param building                 The building being validated
	 * @param buildingHeight           Height of the building
	 * @param blockName                Name of the building block
	 * @param level                    Floor level being checked
	 * @param plot                     The plot containing the building
	 * @param min                      Minimum side yard distance measured
	 * @param max                      Maximum side yard distance measured
	 * @param minMeanlength            Mean length for minimum side
	 * @param maxMeanLength            Mean length for maximum side
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard1Result          Result object for side yard 1 validation
	 * @param sideYard2Result          Result object for side yard 2 validation
	 * @param errors                   Map to collect validation errors
	 */
	private void checkSideYardUptoTwelveMts(final Plan pl, Building building, BigDecimal buildingHeight,
			String blockName, Integer level, final Plot plot, final double min, final double max, double minMeanlength,
			double maxMeanLength, final OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result, HashMap<String, String> errors) {

		String rule = SIDE_YARD_DESC;
		String subRule = RULE_35_T9;
		Boolean valid2 = false;
		Boolean valid1 = false;
		BigDecimal side2val = BigDecimal.ZERO;
		BigDecimal side1val = BigDecimal.ZERO;

		BigDecimal widthOfPlot = pl.getPlanInformation().getWidthOfPlot();

		if (mostRestrictiveOccupancy.getSubtype() != null
				&& A_R.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_PO.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())) {
			if (pl.getPlanInformation() != null && pl.getPlanInformation().getRoadWidth() != null
					&& StringUtils.isNotBlank(pl.getPlanInformation().getLandUseZone())
					&& DxfFileConstants.COMMERCIAL.equalsIgnoreCase(pl.getPlanInformation().getLandUseZone())
					&& pl.getPlanInformation().getRoadWidth().compareTo(ROAD_WIDTH_TWELVE_POINTTWO) < 0) {
				checkCommercialUptoSixteen(blockName, level, min, max, minMeanlength, maxMeanLength,
						mostRestrictiveOccupancy, sideYard1Result, sideYard2Result, rule, DxfFileConstants.RULE_28,
						valid2, valid1, side2val, side1val, widthOfPlot);
			} else {
				checkResidentialUptoTwelveMts(pl, blockName, level, min, max, minMeanlength, maxMeanLength,
						mostRestrictiveOccupancy, sideYard1Result, sideYard2Result, errors, rule, subRule, valid2,
						valid1, side2val, side1val, widthOfPlot);
			}
		} else if (F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			checkCommercialUptoSixteen(blockName, level, min, max, minMeanlength, maxMeanLength,
					mostRestrictiveOccupancy, sideYard1Result, sideYard2Result, rule, subRule, valid2, valid1, side2val,
					side1val, widthOfPlot);
		}
	}

	/**
	 * Validates residential side yard requirements for buildings up to 12 meters
	 * height. Prohibits construction for plots less than 10m width and sets
	 * progressive side yard values.
	 *
	 * @param pl                       The building plan
	 * @param blockName                Name of the building block
	 * @param level                    Floor level being checked
	 * @param min                      Minimum side yard distance measured
	 * @param max                      Maximum side yard distance measured
	 * @param minMeanlength            Mean length for minimum side
	 * @param maxMeanLength            Mean length for maximum side
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard1Result          Result object for side yard 1 validation
	 * @param sideYard2Result          Result object for side yard 2 validation
	 * @param errors                   Map to collect validation errors
	 * @param rule                     Main rule description
	 * @param subRule                  Specific sub-rule reference
	 * @param valid2                   Validation status for side yard 2
	 * @param valid1                   Validation status for side yard 1
	 * @param side2val                 Required value for side yard 2
	 * @param side1val                 Required value for side yard 1
	 * @param widthOfPlot              Width of the plot
	 */
	private void checkResidentialUptoTwelveMts(final Plan pl, String blockName, Integer level, final double min,
			final double max, double minMeanlength, double maxMeanLength,
			final OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result, HashMap<String, String> errors, String rule, String subRule, Boolean valid2,
			Boolean valid1, BigDecimal side2val, BigDecimal side1val, BigDecimal widthOfPlot) {
		if (widthOfPlot.compareTo(BigDecimal.valueOf(10)) <= 0) {
			errors.put(TWELVE_HEIGHT_TEN_WIDTH_SIDE_YARD, NO_CONST_PERMIT_WIDTH_10_HEIGHT_12_G_3);
			pl.addErrors(errors);
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(10)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(15)) <= 0) {
			side2val = SIDEVALUE_ONEPOINTFIVE;
			side1val = SIDEVALUE_ONEPOINTFIVE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(15)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(21)) <= 0) {
			side2val = SIDEVALUE_ONEPOINTFIVE;
			side1val = SIDEVALUE_ONEPOINTFIVE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(21)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(27)) <= 0) {
			side2val = SIDEVALUE_TWO;
			side1val = SIDEVALUE_TWO;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(27)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(33)) <= 0) {
			side2val = SIDEVALUE_TWOPOINTFIVE;
			side1val = SIDEVALUE_TWOPOINTFIVE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(33)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(39)) <= 0) {
			side2val = SIDEVALUE_THREE;
			side1val = SIDEVALUE_THREE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(39)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(45)) <= 0) {
			side2val = SIDEVALUE_THREEPOINTSIX;
			side1val = SIDEVALUE_THREEPOINTSIX;
		}

		if (max >= side1val.doubleValue())
			valid1 = true;
		if (min >= side2val.doubleValue())
			valid2 = true;

		compareSideYard2Result(blockName, side2val, BigDecimal.valueOf(min), BigDecimal.ZERO,
				BigDecimal.valueOf(minMeanlength), mostRestrictiveOccupancy, sideYard2Result, valid2, subRule, rule,
				level);
		compareSideYard1Result(blockName, side1val, BigDecimal.valueOf(max), BigDecimal.ZERO,
				BigDecimal.valueOf(maxMeanLength), mostRestrictiveOccupancy, sideYard1Result, valid1, subRule, rule,
				level);
	}

	/**
	 * Validates side yard requirements for buildings up to 16 meters height. Routes
	 * to appropriate validation method based on occupancy type.
	 *
	 * @param pl                       The building plan
	 * @param building                 The building being validated
	 * @param buildingHeight           Height of the building
	 * @param blockName                Name of the building block
	 * @param level                    Floor level being checked
	 * @param plot                     The plot containing the building
	 * @param min                      Minimum side yard distance measured
	 * @param max                      Maximum side yard distance measured
	 * @param minMeanlength            Mean length for minimum side
	 * @param maxMeanLength            Mean length for maximum side
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard1Result          Result object for side yard 1 validation
	 * @param sideYard2Result          Result object for side yard 2 validation
	 * @param errors                   Map to collect validation errors
	 */
	private void checkSideYardUptoSixteenMts(final Plan pl, Building building, BigDecimal buildingHeight,
			String blockName, Integer level, final Plot plot, final double min, final double max, double minMeanlength,
			double maxMeanLength, final OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result, HashMap<String, String> errors) {

		String rule = SIDE_YARD_DESC;
		String subRule = RULE_35_T9;
		Boolean valid2 = false;
		Boolean valid1 = false;
		BigDecimal side2val = SIDEVALUE_ONE;
		BigDecimal side1val = SIDEVALUE_ONE_TWO;

		BigDecimal widthOfPlot = pl.getPlanInformation().getWidthOfPlot();

		if (mostRestrictiveOccupancy.getSubtype() != null
				&& A_R.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_AF.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())
				|| A_PO.equalsIgnoreCase(mostRestrictiveOccupancy.getSubtype().getCode())) {
			if (pl.getPlanInformation() != null && pl.getPlanInformation().getRoadWidth() != null
					&& StringUtils.isNotBlank(pl.getPlanInformation().getLandUseZone())
					&& DxfFileConstants.COMMERCIAL.equalsIgnoreCase(pl.getPlanInformation().getLandUseZone())
					&& pl.getPlanInformation().getRoadWidth().compareTo(ROAD_WIDTH_TWELVE_POINTTWO) < 0) {
				checkCommercialUptoSixteen(blockName, level, min, max, minMeanlength, maxMeanLength,
						mostRestrictiveOccupancy, sideYard1Result, sideYard2Result, rule, DxfFileConstants.RULE_28,
						valid2, valid1, side2val, side1val, widthOfPlot);
			} else {
				checkResidentialUptoSixteen(pl, blockName, level, min, max, minMeanlength, maxMeanLength,
						mostRestrictiveOccupancy, sideYard1Result, sideYard2Result, errors, subRule, valid2, valid1,
						side2val, side1val, widthOfPlot);
			}
		} else if (F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
			checkCommercialUptoSixteen(blockName, level, min, max, minMeanlength, maxMeanLength,
					mostRestrictiveOccupancy, sideYard1Result, sideYard2Result, rule, subRule, valid2, valid1, side2val,
					side1val, widthOfPlot);
		}
	}

	/**
	 * Validates commercial side yard requirements for buildings up to 16 meters
	 * height. Sets side yard values based on plot width ranges with no requirements
	 * for plots ≤10m.
	 *
	 * @param blockName                Name of the building block
	 * @param level                    Floor level being checked
	 * @param min                      Minimum side yard distance measured
	 * @param max                      Maximum side yard distance measured
	 * @param minMeanlength            Mean length for minimum side
	 * @param maxMeanLength            Mean length for maximum side
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard1Result          Result object for side yard 1 validation
	 * @param sideYard2Result          Result object for side yard 2 validation
	 * @param rule                     Main rule description
	 * @param subRule                  Specific sub-rule reference
	 * @param valid2                   Validation status for side yard 2
	 * @param valid1                   Validation status for side yard 1
	 * @param side2val                 Required value for side yard 2
	 * @param side1val                 Required value for side yard 1
	 * @param widthOfPlot              Width of the plot
	 */
	private void checkCommercialUptoSixteen(String blockName, Integer level, final double min, final double max,
			double minMeanlength, double maxMeanLength, final OccupancyTypeHelper mostRestrictiveOccupancy,
			SideYardResult sideYard1Result, SideYardResult sideYard2Result, String rule, String subRule, Boolean valid2,
			Boolean valid1, BigDecimal side2val, BigDecimal side1val, BigDecimal widthOfPlot) {
		if (widthOfPlot.compareTo(BigDecimal.valueOf(10)) <= 0) {
			// NIL
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(10)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(15)) <= 0) {
			side2val = SIDEVALUE_TWO;
			side1val = SIDEVALUE_TWO;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(15)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(21)) <= 0) {
			side2val = SIDEVALUE_TWOPOINTFIVE;
			side1val = SIDEVALUE_TWOPOINTFIVE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(21)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(27)) <= 0) {
			side2val = SIDEVALUE_THREE;
			side1val = SIDEVALUE_THREE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(27)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(33)) <= 0) {
			side2val = SIDEVALUE_FOUR;
			side1val = SIDEVALUE_FOUR;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(33)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(39)) <= 0) {
			side2val = SIDEVALUE_FIVE;
			side1val = SIDEVALUE_FIVE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(39)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(45)) <= 0) {
			side2val = SIDEVALUE_SIX;
			side1val = SIDEVALUE_SIX;
		}

		if (max >= side1val.doubleValue())
			valid1 = true;
		if (min >= side2val.doubleValue())
			valid2 = true;

		compareSideYard2Result(blockName, side2val, BigDecimal.valueOf(min), BigDecimal.ZERO,
				BigDecimal.valueOf(minMeanlength), mostRestrictiveOccupancy, sideYard2Result, valid2, subRule, rule,
				level);
		compareSideYard1Result(blockName, side1val, BigDecimal.valueOf(max), BigDecimal.ZERO,
				BigDecimal.valueOf(maxMeanLength), mostRestrictiveOccupancy, sideYard1Result, valid1, subRule, rule,
				level);
	}

	/**
	 * Validates residential side yard requirements for buildings up to 16 meters
	 * height. Prohibits construction for plots ≤10m width and sets progressive side
	 * yard values.
	 *
	 * @param pl                       The building plan
	 * @param blockName                Name of the building block
	 * @param level                    Floor level being checked
	 * @param min                      Minimum side yard distance measured
	 * @param max                      Maximum side yard distance measured
	 * @param minMeanlength            Mean length for minimum side
	 * @param maxMeanLength            Mean length for maximum side
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard1Result          Result object for side yard 1 validation
	 * @param sideYard2Result          Result object for side yard 2 validation
	 * @param errors                   Map to collect validation errors
	 * @param rule                     Main rule description
	 * @param valid2                   Validation status for side yard 2
	 * @param valid1                   Validation status for side yard 1
	 * @param side2val                 Required value for side yard 2
	 * @param side1val                 Required value for side yard 1
	 * @param widthOfPlot              Width of the plot
	 */
	private void checkResidentialUptoSixteen(final Plan pl, String blockName, Integer level, final double min,
			final double max, double minMeanlength, double maxMeanLength,
			final OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result, HashMap<String, String> errors, String rule, Boolean valid2, Boolean valid1,
			BigDecimal side2val, BigDecimal side1val, BigDecimal widthOfPlot) {
		if (widthOfPlot.compareTo(BigDecimal.valueOf(10)) <= 0) {
			errors.put(SIXTEEN_HEIGHT_TEN_WIDTH_SIDE_YARD, NO_CONST_PERMIT_WIDTH_10_HEIGHT_16_G_4);
			pl.addErrors(errors);
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(10)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(15)) <= 0) {
			side2val = SIDEVALUE_ONEPOINTEIGHT;
			side1val = SIDEVALUE_ONEPOINTEIGHT;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(15)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(21)) <= 0) {
			side2val = SIDEVALUE_TWO;
			side1val = SIDEVALUE_TWO;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(21)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(27)) <= 0) {
			side2val = SIDEVALUE_TWOPOINTFIVE;
			side1val = SIDEVALUE_TWOPOINTFIVE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(27)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(33)) <= 0) {
			side2val = SIDEVALUE_THREE;
			side1val = SIDEVALUE_THREE;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(33)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(39)) <= 0) {
			side2val = SIDEVALUE_THREEPOINTSIX;
			side1val = SIDEVALUE_THREEPOINTSIX;
		} else if (widthOfPlot.compareTo(BigDecimal.valueOf(39)) > 0
				&& widthOfPlot.compareTo(BigDecimal.valueOf(45)) <= 0) {
			side2val = SIDEVALUE_FOUR;
			side1val = SIDEVALUE_FOUR;
		}

		if (max >= side1val.doubleValue())
			valid1 = true;
		if (min >= side2val.doubleValue())
			valid2 = true;

		compareSideYard2Result(blockName, side2val, BigDecimal.valueOf(min), BigDecimal.ZERO,
				BigDecimal.valueOf(minMeanlength), mostRestrictiveOccupancy, sideYard2Result, valid2, rule, rule,
				level);
		compareSideYard1Result(blockName, side1val, BigDecimal.valueOf(max), BigDecimal.ZERO,
				BigDecimal.valueOf(maxMeanLength), mostRestrictiveOccupancy, sideYard1Result, valid1, rule, rule,
				level);
	}

	/**
	 * Validates side yard requirements for buildings above 16 meters height. Sets
	 * progressive side yard values based on building height ranges.
	 *
	 * @param pl                       The building plan
	 * @param building                 The building being validated
	 * @param blockBuildingHeight      Height of the building block
	 * @param blockName                Name of the building block
	 * @param level                    Floor level being checked
	 * @param plot                     The plot containing the building
	 * @param min                      Minimum side yard distance measured
	 * @param max                      Maximum side yard distance measured
	 * @param minMeanlength            Mean length for minimum side
	 * @param maxMeanLength            Mean length for maximum side
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard1Result          Result object for side yard 1 validation
	 * @param sideYard2Result          Result object for side yard 2 validation
	 */
	private void checkSideYardAboveSixteenMts(final Plan pl, Building building, BigDecimal blockBuildingHeight,
			String blockName, Integer level, final Plot plot, final double min, final double max, double minMeanlength,
			double maxMeanLength, final OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result,
			SideYardResult sideYard2Result) {

		String rule = SIDE_YARD_DESC;
		String subRule = RULE_36;
		Boolean valid2 = false;
		Boolean valid1 = false;
		BigDecimal side2val = SIDEVALUE_ONE;
		BigDecimal side1val = SIDEVALUE_ONE_TWO;

		if (blockBuildingHeight.compareTo(BigDecimal.valueOf(16)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(19)) <= 0) {
			side2val = SIDEVALUE_FOURPOINTFIVE;
			side1val = SIDEVALUE_FOURPOINTFIVE;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(19)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(22)) <= 0) {
			side2val = SIDEVALUE_FOURPOINTFIVE;
			side1val = SIDEVALUE_FOURPOINTFIVE;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(22)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(25)) <= 0) {
			side2val = SIDEVALUE_FIVE;
			side1val = SIDEVALUE_FIVE;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(25)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(28)) <= 0) {
			side2val = SIDEVALUE_SIX;
			side1val = SIDEVALUE_SIX;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(28)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(31)) <= 0) {
			side2val = SIDEVALUE_SEVEN;
			side1val = SIDEVALUE_SEVEN;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(31)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(36)) <= 0) {
			side2val = SIDEVALUE_SEVEN;
			side1val = SIDEVALUE_SEVEN;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(36)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(41)) <= 0) {
			side2val = SIDEVALUE_EIGHT;
			side1val = SIDEVALUE_EIGHT;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(41)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(46)) <= 0) {
			side2val = SIDEVALUE_EIGHT;
			side1val = SIDEVALUE_EIGHT;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(46)) > 0
				&& blockBuildingHeight.compareTo(BigDecimal.valueOf(51)) <= 0) {
			side2val = SIDEVALUE_NINE;
			side1val = SIDEVALUE_NINE;
		} else if (blockBuildingHeight.compareTo(BigDecimal.valueOf(51)) > 0) {
			side2val = SIDEVALUE_NINE;
			side1val = SIDEVALUE_NINE;
		}

		if (max >= side1val.doubleValue())
			valid1 = true;
		if (min >= side2val.doubleValue())
			valid2 = true;

		compareSideYard2Result(blockName, side2val, BigDecimal.valueOf(min), BigDecimal.ZERO,
				BigDecimal.valueOf(minMeanlength), mostRestrictiveOccupancy, sideYard2Result, valid2, subRule, rule,
				level);
		compareSideYard1Result(blockName, side1val, BigDecimal.valueOf(max), BigDecimal.ZERO,
				BigDecimal.valueOf(maxMeanLength), mostRestrictiveOccupancy, sideYard1Result, valid1, subRule, rule,
				level);

	}

	/**
	 * Compares and updates side yard 1 validation results with the most restrictive
	 * requirements. Updates result object if current expected distance is greater
	 * than or equal to existing.
	 *
	 * @param blockName                Name of the building block
	 * @param exptDistance             Expected side yard distance
	 * @param actualDistance           Actual measured distance
	 * @param expectedMeanDistance     Expected mean distance (unused)
	 * @param actualMeanDistance       Actual mean distance (unused)
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard1Result          Result object to update
	 * @param valid                    Validation status
	 * @param subRule                  Specific sub-rule reference
	 * @param rule                     Main rule description
	 * @param level                    Floor level
	 */
	private void compareSideYard1Result(String blockName, BigDecimal exptDistance, BigDecimal actualDistance,
			BigDecimal expectedMeanDistance, BigDecimal actualMeanDistance,
			OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard1Result, Boolean valid, String subRule,
			String rule, Integer level) {
		String occupancyName;
		if (mostRestrictiveOccupancy.getSubtype() != null)
			occupancyName = mostRestrictiveOccupancy.getSubtype().getName();
		else
			occupancyName = mostRestrictiveOccupancy.getType().getName();
		if (exptDistance.compareTo(sideYard1Result.expectedDistance) >= 0) {
			if (exptDistance.compareTo(sideYard1Result.expectedDistance) == 0) {
				sideYard1Result.rule = sideYard1Result.rule != null ? sideYard1Result.rule + COMMA + rule : rule;
				sideYard1Result.occupancy = sideYard1Result.occupancy != null
						? sideYard1Result.occupancy + COMMA + occupancyName
						: occupancyName;
			} else {
				sideYard1Result.rule = rule;
				sideYard1Result.occupancy = occupancyName;
			}

			sideYard1Result.subRule = subRule;
			sideYard1Result.blockName = blockName;
			sideYard1Result.level = level;
			sideYard1Result.actualDistance = actualDistance;
			sideYard1Result.expectedDistance = exptDistance;
			sideYard1Result.status = valid;
		}
	}

	/**
	 * Compares and updates side yard 2 validation results with the most restrictive
	 * requirements. Updates result object if current expected distance is greater
	 * than or equal to existing.
	 *
	 * @param blockName                Name of the building block
	 * @param exptDistance             Expected side yard distance
	 * @param actualDistance           Actual measured distance
	 * @param expectedMeanDistance     Expected mean distance (unused)
	 * @param actualMeanDistance       Actual mean distance (unused)
	 * @param mostRestrictiveOccupancy The most restrictive occupancy type
	 * @param sideYard2Result          Result object to update
	 * @param valid                    Validation status
	 * @param subRule                  Specific sub-rule reference
	 * @param rule                     Main rule description
	 * @param level                    Floor level
	 */
	private void compareSideYard2Result(String blockName, BigDecimal exptDistance, BigDecimal actualDistance,
			BigDecimal expectedMeanDistance, BigDecimal actualMeanDistance,
			OccupancyTypeHelper mostRestrictiveOccupancy, SideYardResult sideYard2Result, Boolean valid, String subRule,
			String rule, Integer level) {
		String occupancyName;
		if (mostRestrictiveOccupancy.getSubtype() != null)
			occupancyName = mostRestrictiveOccupancy.getSubtype().getName();
		else
			occupancyName = mostRestrictiveOccupancy.getType().getName();
		if (exptDistance.compareTo(sideYard2Result.expectedDistance) >= 0) {
			if (exptDistance.compareTo(sideYard2Result.expectedDistance) == 0) {
				sideYard2Result.rule = sideYard2Result.rule != null ? sideYard2Result.rule + COMMA + rule : rule;
				sideYard2Result.occupancy = sideYard2Result.occupancy != null
						? sideYard2Result.occupancy + COMMA + occupancyName
						: occupancyName;
			} else {
				sideYard2Result.rule = rule;
				sideYard2Result.occupancy = occupancyName;
			}

			sideYard2Result.subRule = subRule;
			sideYard2Result.blockName = blockName;
			sideYard2Result.level = level;
			sideYard2Result.actualDistance = actualDistance;
			sideYard2Result.expectedDistance = exptDistance;
			sideYard2Result.status = valid;
		}
	}

	/**
	 * Validates that at least one side yard is defined for non-existing blocks.
	 * Adds validation errors if no side yards are found.
	 *
	 * @param pl The building plan
	 */
	private void validateSideYardRule(final Plan pl) {

		for (Block block : pl.getBlocks()) {
			if (!block.getCompletelyExisting()) {
				Boolean sideYardDefined = false;
				for (SetBack setback : block.getSetBacks()) {
					if (setback.getSideYard1() != null
							&& setback.getSideYard1().getMean().compareTo(BigDecimal.valueOf(0)) > 0) {
						sideYardDefined = true;
					} else if (setback.getSideYard2() != null
							&& setback.getSideYard2().getMean().compareTo(BigDecimal.valueOf(0)) > 0) {
						sideYardDefined = true;
					}
				}
				if (!sideYardDefined) {
					HashMap<String, String> errors = new HashMap<>();
					errors.put(SIDE_YARD_DESC,
							prepareMessage(OBJECTNOTDEFINED, SIDE_YARD_DESC + FOR_BLOCK + block.getName()));
					pl.addErrors(errors);
				}
			}

		}

	}

}
