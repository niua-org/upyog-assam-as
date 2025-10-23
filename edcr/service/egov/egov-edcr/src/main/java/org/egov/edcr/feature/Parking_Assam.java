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

import static org.egov.edcr.constants.CommonFeatureConstants.BASEMENT_PARKING_AREA;
import static org.egov.edcr.constants.CommonFeatureConstants.CLOSING_BRACKET;
import static org.egov.edcr.constants.CommonFeatureConstants.COVER_PARKING_AREA;
import static org.egov.edcr.constants.CommonFeatureConstants.ECS_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.EMPTY_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.LOAD_UNLOAD;
import static org.egov.edcr.constants.CommonFeatureConstants.LOAD_UNLOAD_PARKING_SPACE_NOT_CONTAIN_30M2;
import static org.egov.edcr.constants.CommonFeatureConstants.NO_MECHANICAL_PARKING_SLOT_POLYGON_NOT_4_PTS;
import static org.egov.edcr.constants.CommonFeatureConstants.NO_NOT_HAVING_4_PTS;
import static org.egov.edcr.constants.CommonFeatureConstants.NO_TWO_WHEELER_PARKING_SLOT_POLYGON_4_PTS;
import static org.egov.edcr.constants.CommonFeatureConstants.N_OF_DA_PARKING_SLOT_POLYGON_NOT_HAVING_4_POINTS;
import static org.egov.edcr.constants.CommonFeatureConstants.OPENING_BRACKET;
import static org.egov.edcr.constants.CommonFeatureConstants.OPEN_PARKING_AREA;
import static org.egov.edcr.constants.CommonFeatureConstants.PARKING_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.PLOTAREA_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.SINGLE_SPACE_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.STILT_PARKING_AREA;
import static org.egov.edcr.constants.CommonKeyConstants.COMMON_PARKING;
import static org.egov.edcr.constants.DxfFileConstants.A;
import static org.egov.edcr.constants.DxfFileConstants.J;
import static org.egov.edcr.constants.EdcrReportConstants.EWS;
import static org.egov.edcr.constants.EdcrReportConstants.LIG;
import static org.egov.edcr.constants.DxfFileConstants.C;
import static org.egov.edcr.constants.DxfFileConstants.D_M;
import static org.egov.edcr.constants.DxfFileConstants.E_CLG;
import static org.egov.edcr.constants.DxfFileConstants.E_NS;
import static org.egov.edcr.constants.DxfFileConstants.F;
import static org.egov.edcr.constants.DxfFileConstants.F_CB;
import static org.egov.edcr.constants.DxfFileConstants.F_HB;
import static org.egov.edcr.constants.DxfFileConstants.F_HWB;
import static org.egov.edcr.constants.DxfFileConstants.F_LD;
import static org.egov.edcr.constants.DxfFileConstants.F_PB;
import static org.egov.edcr.constants.DxfFileConstants.G;
import static org.egov.edcr.constants.DxfFileConstants.H;
import static org.egov.edcr.constants.DxfFileConstants.K;
import static org.egov.edcr.constants.DxfFileConstants.PARKING_SLOT;
import static org.egov.edcr.constants.DxfFileConstants.S_BH;
import static org.egov.edcr.constants.DxfFileConstants.S_CRC;
import static org.egov.edcr.constants.DxfFileConstants.S_ECFG;
import static org.egov.edcr.constants.DxfFileConstants.S_SAS;
import static org.egov.edcr.constants.EdcrReportConstants.AREA_UNIT_SQM;
import static org.egov.edcr.constants.EdcrReportConstants.BSMNT_ECS;
import static org.egov.edcr.constants.EdcrReportConstants.BSMNT_PARKING_DIM_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.COVER_ECS;
import static org.egov.edcr.constants.EdcrReportConstants.COVER_PARKING_DIM_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.DA_PARKING_MIN_AREA;
import static org.egov.edcr.constants.EdcrReportConstants.EV_PARKING_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.EV_PARKING_PROVIDED;
import static org.egov.edcr.constants.EdcrReportConstants.EV_PARKING_REQUIRED;
import static org.egov.edcr.constants.EdcrReportConstants.LABEL_CAR_PARKING;
import static org.egov.edcr.constants.EdcrReportConstants.LABEL_TWO_WHEELER_PARKING;
import static org.egov.edcr.constants.EdcrReportConstants.LABEL_VISITOR_PARKING;
import static org.egov.edcr.constants.EdcrReportConstants.LOADING_UNLOADING_AREA;
import static org.egov.edcr.constants.EdcrReportConstants.MECHANICAL_PARKING;
import static org.egov.edcr.constants.EdcrReportConstants.MECH_PARKING_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.CARPETAREA_THRESHHOLD;
import static org.egov.edcr.constants.EdcrReportConstants.MECH_PARKING_DIM_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.MECH_PARKING_DIM_DESC_NA;
import static org.egov.edcr.constants.EdcrReportConstants.MECH_PARKING_HEIGHT;
import static org.egov.edcr.constants.EdcrReportConstants.MECH_PARKING_WIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.MINIMUM_AREA_OF_EACH_DA_PARKING;
import static org.egov.edcr.constants.EdcrReportConstants.MIN_AREA_EACH_CAR_PARKING;
import static org.egov.edcr.constants.EdcrReportConstants.NO_VIOLATION_OF_AREA;
import static org.egov.edcr.constants.EdcrReportConstants.NUMBERS;
import static org.egov.edcr.constants.EdcrReportConstants.OPEN_ECS;
import static org.egov.edcr.constants.EdcrReportConstants.OPEN_PARKING_DIM_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.OUT_OF;
import static org.egov.edcr.constants.EdcrReportConstants.PARKING;
import static org.egov.edcr.constants.EdcrReportConstants.PARKING_AREA_DIM;
import static org.egov.edcr.constants.EdcrReportConstants.PARKING_MIN_AREA;
import static org.egov.edcr.constants.EdcrReportConstants.PARKING_SLOT_HEIGHT;
import static org.egov.edcr.constants.EdcrReportConstants.PARKING_SLOT_WIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.PARKING_VIOLATED_DIM;
import static org.egov.edcr.constants.EdcrReportConstants.PARKING_VIOLATED_MINIMUM_AREA;
import static org.egov.edcr.constants.EdcrReportConstants.RULE117;
import static org.egov.edcr.constants.EdcrReportConstants.RULE__DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.SECTION_CAR_PARKING;
import static org.egov.edcr.constants.EdcrReportConstants.SLOT_HAVING_GT_4_PTS;
import static org.egov.edcr.constants.EdcrReportConstants.SPECIAL_PARKING_DIM_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.SP_PARKING;
import static org.egov.edcr.constants.EdcrReportConstants.SP_PARKING_SLOT_AREA;
import static org.egov.edcr.constants.EdcrReportConstants.SP_PARK_SLOT_MIN_SIDE;
import static org.egov.edcr.constants.EdcrReportConstants.STILT_ECS;
import static org.egov.edcr.constants.EdcrReportConstants.SUB_RULE_34_1_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.SUB_RULE_34_2;
import static org.egov.edcr.constants.EdcrReportConstants.SUB_RULE_40;
import static org.egov.edcr.constants.EdcrReportConstants.SUB_RULE_40_10;
import static org.egov.edcr.constants.EdcrReportConstants.SUB_RULE_40_10_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.SUB_RULE_40_8;
import static org.egov.edcr.constants.EdcrReportConstants.TWO_WHEELER_DIM_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.TWO_WHEELER_PARK_AREA;
import static org.egov.edcr.constants.EdcrReportConstants.TWO_WHEEL_PARKING_AREA_HEIGHT;
import static org.egov.edcr.constants.EdcrReportConstants.TWO_WHEEL_PARKING_AREA_WIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.T_RULE;
import static org.egov.edcr.constants.RuleKeyConstants.FOUR_P_TWO_P_ONE;
import static org.egov.edcr.utility.DcrConstants.SQMTRS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.FeatureEnum;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.FloorUnit;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.Occupancy;
import org.egov.common.entity.edcr.OccupancyType;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.ParkingDetails;
import org.egov.common.entity.edcr.ParkingHelper;
import org.egov.common.entity.edcr.ParkingRequirement;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Parking_Assam extends Parking {

	private static final Logger LOGGER = LogManager.getLogger(Parking_Assam.class);

	@Autowired
	MDMSCacheManager cache;

	@Override
	public Plan validate(Plan pl) {
		// validateDimensions(pl);
		return pl;
	}

	@Override
	public Plan process(Plan pl) {
		validate(pl);
		scrutinyDetail = new ScrutinyDetail();
		scrutinyDetail.setKey(COMMON_PARKING);
		scrutinyDetail.addColumnHeading(1, RULE_NO);
		scrutinyDetail.addColumnHeading(2, DESCRIPTION);
		scrutinyDetail.addColumnHeading(3, REQUIRED);
		scrutinyDetail.addColumnHeading(4, PROVIDED);
		scrutinyDetail.addColumnHeading(5, STATUS);

		// processParking(pl);
		 OccupancyTypeHelper mostRestrictiveOccupancy = pl.getVirtualBuilding() != null
		            ? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
		            : null;

		    if (mostRestrictiveOccupancy != null) {
		        String typeCode = mostRestrictiveOccupancy.getType().getCode();

		        if (A.equals(typeCode)) {
		            processParking(pl, OccupancyType.OCCUPANCY_A1.getOccupancyType());
		        } else if (F.equals(typeCode)) {
		            processParking(pl, OccupancyType.OCCUPANCY_F.getOccupancyType());
		        } 
		        else if (G.equals(typeCode)) {
		            processParking(pl, OccupancyType.OCCUPANCY_G1.getOccupancyType());
		        } else {
		            pl.addError("Parking", "Unsupported occupancy type for parking: " + typeCode);
		        }
		    } else {
		        pl.addError("Parking", "Most restrictive occupancy could not be determined.");
		    }
		 // processMechanicalParking(pl);
		    return pl;
	}
	private void validateDimensions(Plan pl) {
		ParkingDetails parkDtls = pl.getParkingDetails();
		if (!parkDtls.getCars().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getCars())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(PARKING_SLOT, PARKING_SLOT + count + SLOT_HAVING_GT_4_PTS);
		}

		if (!parkDtls.getOpenCars().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getOpenCars())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(OPEN_PARKING_DIM_DESC, OPEN_PARKING_DIM_DESC + count + SLOT_HAVING_GT_4_PTS);
		}

		if (!parkDtls.getCoverCars().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getCoverCars())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(COVER_PARKING_DIM_DESC, COVER_PARKING_DIM_DESC + count + SLOT_HAVING_GT_4_PTS);
		}

		if (!parkDtls.getCoverCars().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getBasementCars())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(BSMNT_PARKING_DIM_DESC, BSMNT_PARKING_DIM_DESC + count + SLOT_HAVING_GT_4_PTS);
		}

		if (!parkDtls.getSpecial().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getDisabledPersons())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(SPECIAL_PARKING_DIM_DESC,
						SPECIAL_PARKING_DIM_DESC + count + N_OF_DA_PARKING_SLOT_POLYGON_NOT_HAVING_4_POINTS);
		}

		if (!parkDtls.getLoadUnload().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getLoadUnload())
				if (m.getArea().compareTo(BigDecimal.valueOf(30)) < 0)
					count++;
			if (count > 0)
				pl.addError(LOAD_UNLOAD, count + LOAD_UNLOAD_PARKING_SPACE_NOT_CONTAIN_30M2);
		}

		if (!parkDtls.getMechParking().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getMechParking())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(MECHANICAL_PARKING, count + NO_MECHANICAL_PARKING_SLOT_POLYGON_NOT_4_PTS);
		}

		if (!parkDtls.getTwoWheelers().isEmpty()) {
			int count = 0;
			for (Measurement m : parkDtls.getTwoWheelers())
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					count++;
			if (count > 0)
				pl.addError(TWO_WHEELER_DIM_DESC,
						TWO_WHEELER_DIM_DESC + count + NO_TWO_WHEELER_PARKING_SLOT_POLYGON_4_PTS);
		}
	}

	public void processParking(Plan pl) {
		ParkingHelper helper = new ParkingHelper();
		BigDecimal plotArea = pl.getPlot() != null ? pl.getPlot().getArea() : BigDecimal.ZERO;
		ScrutinyDetail scrutinyDetail1 = initializeScrutinyDetail();

		OccupancyTypeHelper mostRestrictiveOccupancy = pl.getVirtualBuilding() != null
				? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
				: null;
		BigDecimal totalBuiltupArea = pl.getOccupancies().stream().map(Occupancy::getBuiltUpArea)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		ParkingAreas parkingAreas = calculateParkingAreas(pl);

		double totalECS = calculateTotalECS(helper, parkingAreas);

		Double requiredCarParkArea = 0d;
		Double requiredVisitorParkArea = 0d;
		BigDecimal providedVisitorParkArea = BigDecimal.ZERO;

		validateSpecialParking(pl, helper, totalBuiltupArea);

		ParkingRuleResult ruleResult = fetchApplicableRule(pl, plotArea);

		if (mostRestrictiveOccupancy != null && A.equals(mostRestrictiveOccupancy.getType().getCode())) {
			requiredCarParkArea = calculateRequiredParkingArea(parkingAreas, ruleResult.permissibleCar,
					ruleResult.noOfRequiredParking);
		}

		BigDecimal requiredCarParkingArea = Util.roundOffTwoDecimal(BigDecimal.valueOf(requiredCarParkArea));
		BigDecimal totalProvidedCarParkingArea = Util.roundOffTwoDecimal(parkingAreas.getTotal());
		BigDecimal requiredVisitorParkingArea = Util.roundOffTwoDecimal(BigDecimal.valueOf(requiredVisitorParkArea));
		BigDecimal roundedVisitorParkingArea = Util.roundOffTwoDecimal(providedVisitorParkArea);

		if (parkingAreas.getTotal().doubleValue() == 0) {
			pl.addError(RULE__DESCRIPTION, getLocaleMessage("msg.error.not.defined", RULE__DESCRIPTION));
		} else if (requiredCarParkArea > 0 && totalProvidedCarParkingArea.compareTo(requiredCarParkingArea) < 0) {
			setReportOutputDetails1(pl, FOUR_P_TWO_P_ONE, PARKING_STRING,
					ruleResult.noOfRequiredParking + ECS_STRING + PLOTAREA_STRING + plotArea + CLOSING_BRACKET,
					totalECS + ECS_STRING, Result.Not_Accepted.getResultVal());
		} else {
			setReportOutputDetails1(pl, FOUR_P_TWO_P_ONE, PARKING_STRING,
					ruleResult.noOfRequiredParking + ECS_STRING + PLOTAREA_STRING + plotArea + CLOSING_BRACKET,
					totalECS + ECS_STRING, Result.Accepted.getResultVal());
		}

		if (requiredVisitorParkArea > 0 && roundedVisitorParkingArea.compareTo(requiredVisitorParkingArea) < 0) {
			setReportOutputDetails(pl, SUB_RULE_40_10, SUB_RULE_40_10_DESCRIPTION, requiredVisitorParkingArea + SQMTRS,
					roundedVisitorParkingArea + SQMTRS, Result.Not_Accepted.getResultVal());
		} else if (requiredVisitorParkArea > 0) {
			setReportOutputDetails(pl, SUB_RULE_40_10, SUB_RULE_40_10_DESCRIPTION, requiredVisitorParkingArea + SQMTRS,
					roundedVisitorParkingArea + SQMTRS, Result.Accepted.getResultVal());
		}

		addIndividualParkingReports(pl, parkingAreas);
		LOGGER.info("******************Require no of Car Parking***************" + helper.totalRequiredCarParking);
	}
	
	/**
	 * Processes the EV parking area and adds report output details based on whether
	 * the EV parking area meets the minimum required percentage (20%) of the total provided parking area.
	 * 
	 * The method calculates the total EV parking area from the plan details and compares it
	 * with the sum of provided car, two-wheeler, and visitor parking areas. If the EV parking
	 * area is less than 20% of the total, a "Not Accepted" report entry is added; otherwise,
	 * an "Accepted" entry is added.
	 * 
	 * If no EV parking area is available or the calculated total parking area is zero,
	 * the method logs the condition and returns without adding report details.
	 * 
	 * @param providedCarParkingArea        Total area provided for car parking.
	 * @param providedTwoWheelerParkingArea Total area provided for two-wheeler parking.
	 * @param providedVisitorsParkingArea   Total area provided for visitor parking.
	 * @param pl                           The plan object containing parking details and report output.
	 */
	
	public void evParkingProcess(
	        double providedCarParkingArea,
	        double providedTwoWheelerParkingArea,
	        double providedVisitorsParkingArea,
	        Plan pl
	) {
	    BigDecimal evParking = pl.getParkingDetails().getEvParking().stream()
	            .map(Measurement::getArea)
	            .reduce(BigDecimal.ZERO, BigDecimal::add);

	    // Proceed only if evParking is not null and greater than zero
	    if (evParking == null || evParking.compareTo(BigDecimal.ZERO) <= 0) {
	        LOGGER.info("No EV parking area available, skipping report output.");
	        return;
	    }

	    double evParkingArea = evParking.doubleValue();
	    double totalProvidedArea = providedCarParkingArea + providedTwoWheelerParkingArea + providedVisitorsParkingArea;
	    double totalArea = totalProvidedArea + evParkingArea;

	    double evPercentage = evParkingArea / totalArea;
	    LOGGER.info(String.format("EV parking area: %.2f, Total parking area: %.2f, Percentage: %.2f%%", evParkingArea, totalArea, evPercentage * 100));

	    if (evPercentage < 0.20) {
	        setReportOutputDetails(pl, RULE117, EV_PARKING_DESCRIPTION,
	                EV_PARKING_REQUIRED,
	                EV_PARKING_PROVIDED,
	                Result.Not_Accepted.getResultVal());
	        LOGGER.info("EV parking below 20% threshold, report marked Not Accepted.");
	    } else {
	        setReportOutputDetails(pl, RULE117, EV_PARKING_DESCRIPTION,
	                 EV_PARKING_REQUIRED,
	                 EV_PARKING_PROVIDED,
	                Result.Accepted.getResultVal());
	        LOGGER.info("EV parking meets or exceeds 20% threshold, report marked Accepted.");
	    }
	}


	/**
	 * Calculates the total number of car parkings required based on unit carpet areas.
	 * 
	 * Rule:
	 * - 1 per dwelling unit measuring 66 sqm and above.
	 * - 2 per dwelling unit measuring 120 sqm and above.
	 * 
	 * @param pl the plan object containing blocks, floors, and units
	 * @return total number of required car parkings
	 */
	private double calculateRequiredCarParkingByCarpetArea(Plan pl) {
	    int noOfUnits66 = 0;
	    int noOfUnits120 = 0;

	    for (Block block : pl.getBlocks()) {
	        if (block.getBuilding() != null && block.getBuilding().getFloors() != null) {
	            for (Floor floor : block.getBuilding().getFloors()) {
	                if (floor.getUnits() != null) {
	                    for (FloorUnit unit : floor.getUnits()) {
	                        BigDecimal totalCarpetAreaUnit = BigDecimal.ZERO;

	                        if (unit.getOccupancies() != null) {
	                            for (Occupancy occ : unit.getOccupancies()) {
	                                if (occ.getCarpetArea() != null) {
	                                    totalCarpetAreaUnit = totalCarpetAreaUnit.add(occ.getCarpetArea());
	                                }
	                            }
	                        }

	                        // --- Categorize based on thresholds ---
	                        if (totalCarpetAreaUnit.compareTo(BigDecimal.valueOf(120)) >= 0) {
	                        	noOfUnits120++; // 2 car parks per unit
	                        } else if (totalCarpetAreaUnit.compareTo(BigDecimal.valueOf(66)) >= 0) {
	                        	noOfUnits66++; // 1 car park per unit
	                        }
	                    }
	                }
	            }
	        }
	        
	    }
	    LOGGER.debug("Units between 66–120 sqm: {}", noOfUnits66);
	    LOGGER.debug("Units ≥120 sqm: {}", noOfUnits120);

	    // --- Total car parking count ---
	    return (noOfUnits66 * 1) + (noOfUnits120 * 2);
	}

	private double processCarParking(Plan pl, String occupancyType) {
		BigDecimal totalCarpetArea = getTotalCarpetAreaByOccupancy(pl, occupancyType);
		BigDecimal totalBuiltupArea = pl.getOccupancies().stream().map(Occupancy::getBuiltUpArea)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal basement = BigDecimal.ZERO;
		BigDecimal open = BigDecimal.ZERO;
		BigDecimal stilt = BigDecimal.ZERO;
		double totalNoOfUnits = 0;
		double ecsArea = 0d;

		double builtupArea = totalBuiltupArea.doubleValue();

		if (totalCarpetArea == null || totalCarpetArea.compareTo(BigDecimal.ZERO) == 0) {
			pl.addError("Car parking", "No carpet area found for occupancy type: " + occupancyType);
			return 0;
		}
		
	 	open = pl.getParkingDetails().getOpenCars().stream().map(Measurement::getArea).reduce(BigDecimal.ZERO,
				BigDecimal::add);

	   stilt = pl.getParkingDetails().getStilts().stream().map(Measurement::getArea).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		
	   for (Block block : pl.getBlocks()) {
			for (Floor floor : block.getBuilding().getFloors()) {
				
					double unitsInFloor = floor.getUnits().size();
					totalNoOfUnits += unitsInFloor;
				
				basement = basement.add(floor.getParking().getBasementCars().stream().map(Measurement::getArea)
						.reduce(BigDecimal.ZERO, BigDecimal::add)).setScale(2, RoundingMode.UP);
			}
		}
	 

		double carpetArea = totalCarpetArea.doubleValue();
		BigDecimal plotArea = pl.getPlot() != null ? pl.getPlot().getArea() : BigDecimal.ZERO;

		ParkingRuleResult ruleResult = fetchApplicableRule(pl, plotArea);

		OccupancyTypeHelper mostRestrictiveOccupancy = pl.getVirtualBuilding() != null
				? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
				: null;

		if (mostRestrictiveOccupancy == null) {
			return 0;
		}

		String typeCode = mostRestrictiveOccupancy.getType() != null ? mostRestrictiveOccupancy.getType().getCode()
				: null;
		String subtypeCode = mostRestrictiveOccupancy.getSubtype() != null
				? mostRestrictiveOccupancy.getSubtype().getCode()
				: null;

		double requiredCarParkingArea = 0d;
		

		// Helper lambda for common calculation
		java.util.function.BiFunction<Double, Double, Double> calculateRequiredECS = (units, perUnit) -> {
			if (perUnit == 0)
				return 0d;
			return Math.ceil(units / perUnit);
		};

		if (A.equals(typeCode)) { // Residential
		    double noOfCarParking = 0d;

		    // --- Determine ECS area based on parking type ---
		    if (open.doubleValue() > 0) {
		        ecsArea = ruleResult.getPermissibleCarOpen();
		    } else if (stilt.doubleValue() > 0) {
		        ecsArea = ruleResult.getPermissibleCarStilt();
		    } else if (basement.doubleValue() > 0) {
		        ecsArea = ruleResult.getPermissibleCarBasement();
		    }

		  
		    // --- Calculate required car parking count ---
		    double noOfCarParkingRequired = calculateRequiredCarParkingByCarpetArea(pl);

		
		    // --- Calculate total required car parking area ---
		    requiredCarParkingArea = noOfCarParkingRequired * ecsArea;

		   
		    LOGGER.debug("Total car parking slots required: {}", noOfCarParking);
		    LOGGER.debug("ECS Area used: {}", ecsArea);
		    LOGGER.debug("Total required car parking area: {}", requiredCarParkingArea);
		}

            else if (F.equals(typeCode)) {
			double noOfParking = ruleResult.getNoOfRequiredParking();
			if (open.doubleValue() > 0 && basement.doubleValue() > 0 ) {
				
				 ecsArea = ruleResult.getPermissibleCarBasement();
				}else if (open.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarOpen();
				}
				else if (stilt.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarStilt();
				}
				else if (basement.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarBasement();
				}
			
			if (F_CB.equals(subtypeCode)) {
				double perArea = ruleResult.getPerAreaCommercialShopsCar();
				double ecsPerUnit = builtupArea / perArea;
				double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
				requiredCarParkingArea = requiredECS * ecsArea;

			} else if (F_PB.equals(subtypeCode)) {
				double perArea = ruleResult.getPerAreaCommercialBusinessCar();
				double ecsPerUnit = builtupArea / perArea;
				double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
				requiredCarParkingArea = requiredECS * ecsArea;

			} else if (F_LD.equals(subtypeCode)) {
				BigDecimal noOfRoom = pl.getPlanInformation().getNoOfRoom();
				if (noOfRoom != null) {
					double perRoom = ruleResult.getPerRoomHotelsCar();
					double ecsPerUnit = noOfRoom.doubleValue() / perRoom;
					double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
					requiredCarParkingArea = requiredECS * ecsArea;
				}

			} else if (F_HB.equals(subtypeCode)) {
				BigDecimal noOfRoom = pl.getPlanInformation().getNoOfRoom();
				double noOfParkingPerRule = ruleResult.getNoOfRequiredParking();
				double ecsAreaVal = ruleResult.getPermissibleCar();

				double requiredCarParkingAreaPerRoom = 0d;
				double requiredCarParkingAreaPerArea = 0d;

				if (noOfRoom != null && ruleResult.getPerRoomHotelBanquetCar() > 0) {
					double ecsPerUnitRoom = noOfRoom.doubleValue() / ruleResult.getPerRoomHotelBanquetCar();
					double requiredECSRoom = Math.ceil(ecsPerUnitRoom) * noOfParkingPerRule;
					requiredCarParkingAreaPerRoom = requiredECSRoom * ecsAreaVal;
				}

				if (ruleResult.getPerAreaHotelBanquetCar() > 0) {
					double ecsPerUnitArea = builtupArea / ruleResult.getPerAreaHotelBanquetCar();
					double requiredECSArea = Math.ceil(ecsPerUnitArea) * noOfParkingPerRule;
					requiredCarParkingAreaPerArea = requiredECSArea * ecsAreaVal;
				}

				requiredCarParkingArea = requiredCarParkingAreaPerRoom + requiredCarParkingAreaPerArea;

			} else if (F_HWB.equals(subtypeCode)) {
				BigDecimal noOfRoom = pl.getPlanInformation().getNoOfRoom();
				double noOfParkingPerRule = ruleResult.getNoOfRequiredParking();
				double ecsAreaVal = ruleResult.getPermissibleCar();

				double requiredCarParkingAreaPerRoom = 0d;
				double requiredCarParkingAreaPerArea = 0d;

				if (noOfRoom != null && ruleResult.getPerRoomHotelWithoutBanquetCar() > 0) {
					double ecsPerUnitRoom = noOfRoom.doubleValue() / ruleResult.getPerRoomHotelWithoutBanquetCar();
					double requiredECSRoom = Math.ceil(ecsPerUnitRoom) * noOfParkingPerRule;
					requiredCarParkingAreaPerRoom = requiredECSRoom * ecsAreaVal;
				}

				if (ruleResult.getPerAreaHotelWithoutBanquetCar() > 0) {
					double ecsPerUnitArea = builtupArea / ruleResult.getPerAreaHotelWithoutBanquetCar();
					double requiredECSArea = Math.ceil(ecsPerUnitArea) * noOfParkingPerRule;
					requiredCarParkingAreaPerArea = requiredECSArea * ecsAreaVal;
				}

				requiredCarParkingArea = requiredCarParkingAreaPerRoom + requiredCarParkingAreaPerArea;
			}
		} else if (J.equals(typeCode)) { // Govt
			if (open.doubleValue() > 0 && basement.doubleValue() > 0 ) {
				
				 ecsArea = ruleResult.getPermissibleCarBasement();
				}else if (open.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarOpen();
				}
				else if (stilt.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarStilt();
				}
				else if (basement.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarBasement();
				}
			double noOfParking = ruleResult.getNoOfRequiredParking();
			double perArea = ruleResult.getPerAreaInstitutionalPSPCar();

			double ecsPerUnit = builtupArea / perArea;
			double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
			requiredCarParkingArea = requiredECS * ecsArea;

		} else if (C.equals(typeCode)) { // Medical / Hospital
			double noOfParking = ruleResult.getNoOfRequiredParking();
			if (open.doubleValue() > 0 && basement.doubleValue() > 0 ) {
				
				 ecsArea = ruleResult.getPermissibleCarBasement();
				}else if (open.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarOpen();
				}
				else if (stilt.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarStilt();
				}
				else if (basement.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarBasement();
				}

			double perArea = ruleResult.getPerAreaInstitutionalMedicalCar();
			double ecsPerUnitArea = builtupArea / perArea;
			double requiredECSByArea = Math.ceil(ecsPerUnitArea) * noOfParking;

			BigDecimal noOfBeds = pl.getPlanInformation().getNoOfBeds();
			double requiredECSByBeds = 0;
			if (noOfBeds != null && noOfBeds.doubleValue() > 0) {
				double perBeds = ruleResult.getPerBedInstitutionalMedicalCar();
				double ecsPerUnitBed = noOfBeds.doubleValue() / perBeds;
				requiredECSByBeds = Math.ceil(ecsPerUnitBed) * noOfParking;
			}

			requiredCarParkingArea = (requiredECSByArea + requiredECSByBeds) * ecsArea;

		} else if (E_NS.equals(subtypeCode)) { // Educational Nursery
			double noOfParking = ruleResult.getNoOfRequiredParking();
			if (open.doubleValue() > 0 && basement.doubleValue() > 0 ) {
				
				 ecsArea = ruleResult.getPermissibleCarBasement();
				}else if (open.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarOpen();
				}
				else if (stilt.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarStilt();
				}
				else if (basement.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarBasement();
				}
			double perArea = ruleResult.getPerAreaEducationalNurseryCar();

			double ecsPerUnit = (builtupArea * perArea / 100.0) / ecsArea;
			double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
			requiredCarParkingArea = requiredECS * ecsArea;

		} else if (E_CLG.equals(subtypeCode)) { // Educational College
			double noOfParking = ruleResult.getNoOfRequiredParking();
			if (open.doubleValue() > 0 && basement.doubleValue() > 0 ) {
				
				 ecsArea = ruleResult.getPermissibleCarBasement();
				}else if (open.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarOpen();
				}
				else if (stilt.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarStilt();
				}
				else if (basement.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarBasement();
				}
			double perArea = ruleResult.getPerAreaEducationalSchoolsCar();

			double ecsPerUnit = (builtupArea * perArea / 100.0) / ecsArea;
			double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
			requiredCarParkingArea = requiredECS * ecsArea;

		} else if (D_M.equals(subtypeCode)) { // Assembly - Cinema/Multiplex
			Integer seatCount = pl.getPlanInformation().getNoOfSeats();
			BigDecimal noOfSeats = seatCount != null ? BigDecimal.valueOf(seatCount) : BigDecimal.ZERO;
			if (noOfSeats != null) {
				double noOfParking = ruleResult.getNoOfRequiredParking();
				if (open.doubleValue() > 0 && basement.doubleValue() > 0 ) {
					
					 ecsArea = ruleResult.getPermissibleCarBasement();
					}else if (open.doubleValue() > 0) {
						 ecsArea = ruleResult.getPermissibleCarOpen();
					}
					else if (stilt.doubleValue() > 0) {
						 ecsArea = ruleResult.getPermissibleCarStilt();
					}
					else if (basement.doubleValue() > 0) {
						 ecsArea = ruleResult.getPermissibleCarBasement();
					}
				double perSeat = ruleResult.getPerSeatAssemblyCinemaCar();

				double ecsPerUnit = noOfSeats.doubleValue() / perSeat;
				double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
				requiredCarParkingArea = requiredECS * ecsArea;
			}

		} else if (S_BH.equals(subtypeCode) || S_CRC.equals(subtypeCode)) { // Assembly - Community Hall/Banquet
			double noOfParking = ruleResult.getNoOfRequiredParking();
			if (open.doubleValue() > 0 && basement.doubleValue() > 0 ) {
				
				 ecsArea = ruleResult.getPermissibleCarBasement();
				}else if (open.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarOpen();
				}
				else if (stilt.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarStilt();
				}
				else if (basement.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarBasement();
				}
			double perArea = ruleResult.getPerPlotAreaAssemblyCommunityCar();

			double ecsPerUnit = plotArea.doubleValue() / perArea;
			double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
			requiredCarParkingArea = requiredECS * ecsArea;

		} else if (S_ECFG.equals(subtypeCode) || S_SAS.equals(subtypeCode)) { // Assembly - Stadium
			Integer seatCount = pl.getPlanInformation().getNoOfSeats();
			BigDecimal noOfSeats = seatCount != null ? BigDecimal.valueOf(seatCount) : BigDecimal.ZERO;
			if (noOfSeats != null) {
				double noOfParking = ruleResult.getNoOfRequiredParking();
				if (open.doubleValue() > 0 && basement.doubleValue() > 0 ) {
					
					 ecsArea = ruleResult.getPermissibleCarBasement();
					}else if (open.doubleValue() > 0) {
						 ecsArea = ruleResult.getPermissibleCarOpen();
					}
					else if (stilt.doubleValue() > 0) {
						 ecsArea = ruleResult.getPermissibleCarStilt();
					}
					else if (basement.doubleValue() > 0) {
						 ecsArea = ruleResult.getPermissibleCarBasement();
					}
				double perSeat = ruleResult.getPerSeatAssemblyStadiumCar();

				double ecsPerUnit = noOfSeats.doubleValue() / perSeat;
				double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
				requiredCarParkingArea = requiredECS * ecsArea;
			}

		} else if (G.equals(typeCode)) { // Industrial
			double noOfParking = ruleResult.getNoOfRequiredParking();
			if (open.doubleValue() > 0 && basement.doubleValue() > 0 ) {
				
				 ecsArea = ruleResult.getPermissibleCarBasement();
				}else if (open.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarOpen();
				}
				else if (stilt.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarStilt();
				}
				else if (basement.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarBasement();
				}
			double perArea = ruleResult.getPerAreaIndustrialCar();

			double ecsPerUnit = builtupArea / perArea;
			double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
			requiredCarParkingArea = requiredECS * ecsArea;

		} else if (H.equals(typeCode)) { // Wholesale
			double noOfParking = ruleResult.getNoOfRequiredParking();
			if (open.doubleValue() > 0 && basement.doubleValue() > 0 ) {
				
				 ecsArea = ruleResult.getPermissibleCarBasement();
				}else if (open.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarOpen();
				}
				else if (stilt.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarStilt();
				}
				else if (basement.doubleValue() > 0) {
					 ecsArea = ruleResult.getPermissibleCarBasement();
				}
			double perArea = ruleResult.getPerAreaWholesaleCar();

			double ecsPerUnit = builtupArea / perArea;
			double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
			requiredCarParkingArea = requiredECS * ecsArea;
		}

		return requiredCarParkingArea;
	}

	/**
	 * Counts total number of units where carpet area <= given threshold.
	 *
	 * @param pl         Plan object
	 * @param threshold  Carpet area threshold (e.g., 66 sqm)
	 * @return Number of qualifying units
	 */
	private int countUnitsByCarpetArea(Plan pl, double threshold) {
	    int noOfUnits = 0;

	    for (Block block : pl.getBlocks()) {
	        if (block.getBuilding() != null && block.getBuilding().getFloors() != null) {
	            for (Floor floor : block.getBuilding().getFloors()) {
	                if (floor.getUnits() != null) {
	                    for (FloorUnit unit : floor.getUnits()) {
	                        BigDecimal totalCarpetAreaUnit = BigDecimal.ZERO;

	                        if (unit.getOccupancies() != null) {
	                            for (Occupancy occ : unit.getOccupancies()) {
	                                if (occ.getCarpetArea() != null) {
	                                    totalCarpetAreaUnit = totalCarpetAreaUnit.add(occ.getCarpetArea());
	                                }
	                            }
	                        }

	                        // --- Check condition: carpet area <= threshold ---
	                        if (totalCarpetAreaUnit.compareTo(BigDecimal.valueOf(threshold)) <= 0) {
	                        	noOfUnits++;
	                        }
	                    }
	                }
	            }
	        }
	    }

	    return noOfUnits;
	}

	private double processTwoWheelerParking(Plan pl, String occupancyType) {

		BigDecimal totalCarpetArea = getTotalCarpetAreaByOccupancy(pl, occupancyType);

		double requiredTwoWheelerParkingArea = BigDecimal.ZERO.doubleValue();

		OccupancyTypeHelper mostRestrictiveOccupancy = pl.getVirtualBuilding() != null
				? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
				: null;
		BigDecimal totalBuiltupArea = pl.getOccupancies().stream().map(Occupancy::getBuiltUpArea)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		double builtupArea = totalBuiltupArea.doubleValue();

		if (totalCarpetArea == null || totalCarpetArea.compareTo(BigDecimal.ZERO) == 0) {
			pl.addError("TwoWheelerParking", "No carpet area found for occupancy type: ");
			return 0;
		}

		String typeCode = mostRestrictiveOccupancy.getType() != null ? mostRestrictiveOccupancy.getType().getCode()
				: null;
		String subtypeCode = mostRestrictiveOccupancy.getSubtype() != null
				? mostRestrictiveOccupancy.getSubtype().getCode()
				: null;

		double carpetArea = totalCarpetArea.doubleValue();

		BigDecimal plotArea = pl.getPlot() != null ? pl.getPlot().getArea() : BigDecimal.ZERO;
		ParkingRuleResult ruleResult = fetchApplicableRule(pl, plotArea);

		double perArea = ruleResult.perAreaTwoWheeler;
		double noOfParking = ruleResult.noOfRequiredParking;
		double ecsArea = ruleResult.permissibleTwoWheeler;
		double carpetAreaThreshhold = CARPETAREA_THRESHHOLD.doubleValue();

		
		int noOfUnits = countUnitsByCarpetArea(pl, carpetAreaThreshhold);

		if (A.equals(typeCode)) {
			
			double requiredECS = noOfUnits * 1;
 			requiredTwoWheelerParkingArea = requiredECS * ecsArea;
		} else if (F.equals(typeCode)) {

			double ecsPerUnit = builtupArea / perArea;
			double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
			requiredTwoWheelerParkingArea = requiredECS * ecsArea;

		} else if (D_M.equals(subtypeCode)) { // Assembly - Cinema/Multiplex
			Integer seatCount = pl.getPlanInformation().getNoOfSeats();
			BigDecimal noOfSeats = seatCount != null ? BigDecimal.valueOf(seatCount) : BigDecimal.ZERO;
			if (noOfSeats != null) {
				noOfParking = ruleResult.getNoOfRequiredParking();
				ecsArea = ruleResult.getPermissibleTwoWheeler();
				double perSeat = ruleResult.getPerSeatAssemblyCinemaTwoWheeler();

				double ecsPerUnit = noOfSeats.doubleValue() / perSeat;
				double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
				requiredTwoWheelerParkingArea = requiredECS * ecsArea;
			}
		} else if (J.equals(typeCode)) { // Govt
			ecsArea = ruleResult.getPermissibleTwoWheeler();
			noOfParking = ruleResult.getNoOfRequiredParking();
			perArea = ruleResult.getPerAreaInstitutionalPSPTwoWheeler();

			double ecsPerUnit = builtupArea / perArea;
			double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
			requiredTwoWheelerParkingArea = requiredECS * ecsArea;

		} else if (C.equals(typeCode)) { // Medical / Hospital
			noOfParking = ruleResult.getNoOfRequiredParking();
			ecsArea = ruleResult.getPermissibleTwoWheeler();
			BigDecimal noOfBeds = pl.getPlanInformation().getNoOfBeds();
			double requiredECSByBeds = 0;
			if (noOfBeds != null && noOfBeds.doubleValue() > 0) {
				double perBeds = ruleResult.getPerBedInstitutionalMedicalTwoWheeler();
				double ecsPerUnitBed = noOfBeds.doubleValue() / perBeds;
				requiredECSByBeds = Math.ceil(ecsPerUnitBed) * noOfParking;
			}

			requiredTwoWheelerParkingArea = requiredECSByBeds * ecsArea;

		} else if (G.equals(typeCode)) { // Industrial
			noOfParking = ruleResult.getNoOfRequiredParking();
			ecsArea = ruleResult.getPermissibleTwoWheeler();
			perArea = ruleResult.getPerAreaIndustrialTwoWheeler();

			double ecsPerUnit = builtupArea / perArea;
			double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
			requiredTwoWheelerParkingArea = requiredECS * ecsArea;
			}
		return requiredTwoWheelerParkingArea;
	}


	/**
	 * Counts the number of units within a given carpet area range (inclusive).
	 *
	 * @param pl        Plan object
	 * @param minArea   Minimum carpet area (sqm)
	 * @param maxArea   Maximum carpet area (sqm)
	 * @return Number of qualifying units
	 */
	private int countUnitsByCarpetAreaRange(Plan pl, double minArea, double maxArea) {
	    int qualifyingUnits = 0;

	    for (Block block : pl.getBlocks()) {
	        if (block.getBuilding() != null && block.getBuilding().getFloors() != null) {
	            for (Floor floor : block.getBuilding().getFloors()) {
	                if (floor.getUnits() != null) {
	                    for (FloorUnit unit : floor.getUnits()) {
	                        BigDecimal totalCarpetAreaUnit = BigDecimal.ZERO;

	                        if (unit.getOccupancies() != null) {
	                            for (Occupancy occ : unit.getOccupancies()) {
	                                if (occ.getCarpetArea() != null) {
	                                    totalCarpetAreaUnit = totalCarpetAreaUnit.add(occ.getCarpetArea());
	                                }
	                            }
	                        }

	                        double area = totalCarpetAreaUnit.doubleValue();
	                        if (area >= minArea && area <= maxArea) {
	                            qualifyingUnits++;
	                        }
	                    }
	                }
	            }
	        }
	    }

	    return qualifyingUnits;
	}


	private double processVisitorsParking(Plan pl, String occupancyType) {

		BigDecimal totalCarpetArea = getTotalCarpetAreaByOccupancy(pl, occupancyType);

		BigDecimal totalBuiltupArea = pl.getOccupancies().stream().map(Occupancy::getBuiltUpArea)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		double builtupArea = totalBuiltupArea.doubleValue();

		double requiredVisitorsParkingArea = BigDecimal.ZERO.doubleValue();

		if (totalCarpetArea == null || totalCarpetArea.compareTo(BigDecimal.ZERO) == 0) {
			pl.addError("Visitors parking", "No carpet area found for occupancy type: ");
			return 0;
		}

		double carpetArea = totalCarpetArea.doubleValue();

		BigDecimal plotArea = pl.getPlot() != null ? pl.getPlot().getArea() : BigDecimal.ZERO;
		ParkingRuleResult ruleResult = fetchApplicableRule(pl, plotArea);

		OccupancyTypeHelper mostRestrictiveOccupancy = pl.getVirtualBuilding() != null
				? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
				: null;
		String typeCode = mostRestrictiveOccupancy.getType() != null ? mostRestrictiveOccupancy.getType().getCode()
				: null;
		String subtypeCode = mostRestrictiveOccupancy.getSubtype() != null
				? mostRestrictiveOccupancy.getSubtype().getCode()
				: null;

		   
	    // --- Count dwelling units by carpet area range ---
	    int unitsUpTo66 = 0;
	    int unitsAbove66 = countUnitsByCarpetAreaRange(pl, 66.01, Double.MAX_VALUE); // default for >66 sqm
	    double visitorSlotsForUpTo66 = 0.0;

	    if (pl.getPlanInformation() != null && pl.getPlanInformation().getPlotType() != null &&
	        (pl.getPlanInformation().getPlotType().equalsIgnoreCase(EWS)
	        || pl.getPlanInformation().getPlotType().equalsIgnoreCase(LIG))) {

	        unitsUpTo66 = countUnitsByCarpetAreaRange(pl, 0, 66);
	    }

	    // --- Calculate required visitor parking slots ---
	    if (unitsUpTo66 > 0) {
	        visitorSlotsForUpTo66 = Math.ceil(unitsUpTo66 / 10.0);
	    }
	    double visitorSlotsForAbove66 = Math.ceil(unitsAbove66 / 10.0); // 1 per 10 units

		if (A.equals(typeCode)) {
			double ecsArea = ruleResult.permissibleVisitor;
			double totalVisitorSlots = visitorSlotsForUpTo66 + visitorSlotsForAbove66;
			requiredVisitorsParkingArea = totalVisitorSlots * ecsArea;
		} else if (F.equals(typeCode)) {
			double perArea = ruleResult.perAreaVisitor;
			double noOfParking = ruleResult.noOfRequiredParking;
			double ecsArea = ruleResult.permissibleVisitor;

			double ecsPerUnit = builtupArea / perArea;
			double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
			requiredVisitorsParkingArea = requiredECS * ecsArea;

		} else if (J.equals(typeCode)) { // Govt
			double ecsArea = ruleResult.getPermissibleVisitor();
			double noOfParking = ruleResult.getNoOfRequiredParking();
			double perArea = ruleResult.getPerAreaInstitutionalPSPVisitor();

			double ecsPerUnit = builtupArea / perArea;
			double requiredECS = Math.ceil(ecsPerUnit) * noOfParking;
			requiredVisitorsParkingArea = requiredECS * ecsArea;

		} else if (C.equals(typeCode)) { // Medical / Hospital
			double noOfParking = ruleResult.getNoOfRequiredParking();
			double ecsArea = ruleResult.getPermissibleVisitor();

			double perArea = ruleResult.getPerAreaInstitutionalMedicalVisitor();
			double ecsPerUnitArea = builtupArea / perArea;
			double requiredECSByArea = Math.ceil(ecsPerUnitArea) * noOfParking;
			requiredVisitorsParkingArea = requiredECSByArea * ecsArea;

		}
		return requiredVisitorsParkingArea;
	}

	private ScrutinyDetail initializeScrutinyDetail() {
		ScrutinyDetail detail = new ScrutinyDetail();
		detail.addColumnHeading(1, RULE_NO);
		detail.addColumnHeading(2, DESCRIPTION);
		detail.addColumnHeading(3, EMPTY_STRING);
		detail.addColumnHeading(4, REQUIRED);
		detail.addColumnHeading(5, PROVIDED);
		detail.addColumnHeading(6, STATUS);
		return detail;
	}

	private ParkingAreas calculateParkingAreas(Plan pl) {
		BigDecimal cover = BigDecimal.ZERO;
		BigDecimal basement = BigDecimal.ZERO;

		for (Block block : pl.getBlocks()) {
			for (Floor floor : block.getBuilding().getFloors()) {
				cover = cover.add(floor.getParking().getCoverCars().stream().map(Measurement::getArea)
						.reduce(BigDecimal.ZERO, BigDecimal::add)).setScale(2, RoundingMode.UP);
				basement = basement.add(floor.getParking().getBasementCars().stream().map(Measurement::getArea)
						.reduce(BigDecimal.ZERO, BigDecimal::add)).setScale(2, RoundingMode.UP);
			}
		}

		BigDecimal open = pl.getParkingDetails().getOpenCars().stream().map(Measurement::getArea)
				.reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.UP);
		BigDecimal stilt = pl.getParkingDetails().getStilts().stream().map(Measurement::getArea)
				.reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.UP);

		return new ParkingAreas(open, cover, basement, stilt);
	}

	// For Assam
	private ParkingAreas1 calculateParkingAreas1(Plan pl) {
		BigDecimal cover = BigDecimal.ZERO;
		BigDecimal basement = BigDecimal.ZERO;
		BigDecimal open = BigDecimal.ZERO;
		BigDecimal stilt = BigDecimal.ZERO;
		BigDecimal twoWheeler = BigDecimal.ZERO;
		BigDecimal visitor = BigDecimal.ZERO;

		for (Block block : pl.getBlocks()) {
			for (Floor floor : block.getBuilding().getFloors()) {
				if (floor.getParking() != null) {
					cover = cover.add(floor.getParking().getCoverCars().stream().map(Measurement::getArea)
							.reduce(BigDecimal.ZERO, BigDecimal::add));

					basement = basement.add(floor.getParking().getBasementCars().stream().map(Measurement::getArea)
							.reduce(BigDecimal.ZERO, BigDecimal::add));
				}
			}
		}

		if (pl.getParkingDetails() != null) {
			open = pl.getParkingDetails().getOpenCars().stream().map(Measurement::getArea).reduce(BigDecimal.ZERO,
					BigDecimal::add);

			stilt = pl.getParkingDetails().getStilts().stream().map(Measurement::getArea).reduce(BigDecimal.ZERO,
					BigDecimal::add);

			twoWheeler = pl.getParkingDetails().getTwoWheelers().stream().map(Measurement::getArea)
					.reduce(BigDecimal.ZERO, BigDecimal::add);

			visitor = pl.getParkingDetails().getVisitors().stream().map(Measurement::getArea).reduce(BigDecimal.ZERO,
					BigDecimal::add);
		}

		cover = cover.setScale(2, RoundingMode.UP);
		basement = basement.setScale(2, RoundingMode.UP);
		open = open.setScale(2, RoundingMode.UP);
		stilt = stilt.setScale(2, RoundingMode.UP);
		twoWheeler = twoWheeler.setScale(2, RoundingMode.UP);
		visitor = visitor.setScale(2, RoundingMode.UP);

		BigDecimal total = open.add(cover).add(basement).add(stilt).add(twoWheeler).add(visitor).setScale(2,
				RoundingMode.UP);

		return new ParkingAreas1(open, cover, basement, stilt, twoWheeler, visitor, total);
	}

	public void processParking(Plan pl, String occupancyType) {

		BigDecimal totalCarpetArea = getTotalCarpetAreaByOccupancy(pl, occupancyType);

		if (totalCarpetArea == null || totalCarpetArea.compareTo(BigDecimal.ZERO) == 0) {
			pl.addError("Parking", "No carpet area found for occupancy type: ");
			return;
		}

		double requiredCarParkingArea = processCarParking(pl, occupancyType);
		double requiredTwoWheelerParkingArea = processTwoWheelerParking(pl, occupancyType);
		double requiredVisitorsParkingArea = processVisitorsParking(pl, occupancyType);

		ParkingAreas1 parkingAreas = calculateParkingAreas1(pl);

		// Provided areas
		double providedCarParkingArea = parkingAreas.getOpen().doubleValue() + parkingAreas.getCover().doubleValue()
				+ parkingAreas.getBasement().doubleValue() + parkingAreas.getStilt().doubleValue();
		double providedTwoWheelerParkingArea = parkingAreas.getTwoWheeler().doubleValue();
		double providedVisitorsParkingArea = parkingAreas.getVisitor().doubleValue();
	
		
		evParkingProcess(providedCarParkingArea, providedTwoWheelerParkingArea, providedVisitorsParkingArea, pl);
		
		// Check each requirement
		boolean carOk = providedCarParkingArea >= requiredCarParkingArea;
		boolean twoWheelerOk = providedTwoWheelerParkingArea >= requiredTwoWheelerParkingArea;
		boolean visitorOk = providedVisitorsParkingArea >= requiredVisitorsParkingArea;

		// Compile overall status
		boolean overallOk = true;

		// Car report - only if provided > 0
		if (providedCarParkingArea > 0) {
			setReportOutputDetails1(pl, SECTION_CAR_PARKING, LABEL_CAR_PARKING, requiredCarParkingArea + " sqm",
					providedCarParkingArea + AREA_UNIT_SQM,
					carOk ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());
			if (!carOk)
				overallOk = false;
		}

		// Two-wheeler report - only if provided > 0
		if (providedTwoWheelerParkingArea > 0) {
			setReportOutputDetails1(pl, SECTION_CAR_PARKING, LABEL_TWO_WHEELER_PARKING, requiredTwoWheelerParkingArea + AREA_UNIT_SQM,
					providedTwoWheelerParkingArea + AREA_UNIT_SQM,
					twoWheelerOk ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());
			if (!twoWheelerOk)
				overallOk = false;
		}

		// Visitor parking report - only if provided > 0
		if (providedVisitorsParkingArea > 0) {
			setReportOutputDetails1(pl, SECTION_CAR_PARKING, LABEL_VISITOR_PARKING, requiredVisitorsParkingArea + AREA_UNIT_SQM,
					providedVisitorsParkingArea + AREA_UNIT_SQM,
					visitorOk ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());
			if (!visitorOk)
				overallOk = false;
		}

		// Final Acceptance or Error
		if (!overallOk) {
			StringBuilder err = new StringBuilder("Parking not sufficient for: ");
			if (providedCarParkingArea > 0 && !carOk)
				err.append("Car; ");
			if (providedTwoWheelerParkingArea > 0 && !twoWheelerOk)
				err.append("Two-Wheeler; ");
			if (providedVisitorsParkingArea > 0 && !visitorOk)
				err.append("Visitor; ");
			pl.addError("Parking", err.toString());
		}
		addIndividualParkingReports1(pl, parkingAreas);
	}

	private double calculateTotalECS(ParkingHelper helper, ParkingAreas areas) {
		helper.totalRequiredCarParking += areas.open.doubleValue() / OPEN_ECS;
		helper.totalRequiredCarParking += areas.cover.doubleValue() / COVER_ECS;
		helper.totalRequiredCarParking += areas.basement.doubleValue() / BSMNT_ECS;
		helper.totalRequiredCarParking += areas.stilt.doubleValue() / STILT_ECS;

		return roundECS(areas.open.doubleValue() / OPEN_ECS) + roundECS(areas.cover.doubleValue() / COVER_ECS)
				+ roundECS(areas.basement.doubleValue() / BSMNT_ECS) + roundECS(areas.stilt.doubleValue() / STILT_ECS);
	}

	private double calculateTotalECS1(ParkingHelper helper, ParkingAreas1 areas) {
		helper.totalRequiredCarParking += areas.open.doubleValue() / OPEN_ECS;
		helper.totalRequiredCarParking += areas.cover.doubleValue() / COVER_ECS;
		helper.totalRequiredCarParking += areas.basement.doubleValue() / BSMNT_ECS;
		helper.totalRequiredCarParking += areas.stilt.doubleValue() / STILT_ECS;

		return roundECS(areas.open.doubleValue() / OPEN_ECS) + roundECS(areas.cover.doubleValue() / COVER_ECS)
				+ roundECS(areas.basement.doubleValue() / BSMNT_ECS) + roundECS(areas.stilt.doubleValue() / STILT_ECS);
	}

	private double roundECS(double val) {
		return Double.parseDouble(String.format("%.2f", val));
	}

	private ParkingRuleResult fetchApplicableRule(Plan pl, BigDecimal plotArea) {
		List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.PARKING.getValue(), false);

		// Initialize all variables with null to detect first non-null value
		Double noOfParking = null;
		Double permissibleCar = null;
		Double permissibleCarOpen = null;
		Double permissibleCarBasement = null;
		Double permissibleCarStilt = null;
		Double permissibleTwoWheeler = null;
		Double permissibleVisitor = null;
		Double perAreaCar = null;
		Double perAreaTwoWheeler = null;
		Double perAreaVisitor = null;

		Double perAreaHotelWithoutBanquetCar = null;
		Double perRoomHotelWithoutBanquetCar = null;
		Double perAreaHotelBanquetCar = null;
		Double perRoomHotelBanquetCar = null;
		Double perRoomHotelsCar = null;

		Double perAreaCommercialBusinessCar = null;
		Double perAreaCommercialShopsCar = null;

		Double perAreaInstitutionalPSPCar = null;
		Double perAreaInstitutionalMedicalCar = null;
		Double perBedInstitutionalMedicalCar = null;

		Double perAreaEducationalNurseryCar = null;
		Double perAreaEducationalSchoolsCar = null;

		Double perSeatAssemblyCinemaCar = null;
		Double perPlotAreaAssemblyCommunityCar = null;
		Double perSeatAssemblyStadiumCar = null;

		Double perAreaIndustrialCar = null;
		Double perAreaWholesaleCar = null;

		// New two-wheeler and visitor fields
		Double perAreaIndustrialTwoWheeler = null;
		Double perAreaInstitutionalPSPTwoWheeler = null;
		Double perBedInstitutionalMedicalTwoWheeler = null;
		Double perSeatAssemblyCinemaTwoWheeler = null;
		Double perAreaInstitutionalMedicalVisitor = null;
		Double perAreaInstitutionalPSPVisitor = null;

		for (Object obj : rules) {
			if (obj instanceof ParkingRequirement) {
				ParkingRequirement pr = (ParkingRequirement) obj;

				if (noOfParking == null && pr.getNoOfParking() != null)
					noOfParking = pr.getNoOfParking().doubleValue();
				if (permissibleCar == null && pr.getPermissibleCar() != null)
					permissibleCar = pr.getPermissibleCar().doubleValue();
				if (permissibleCarOpen == null && pr.getPermissibleCarOpen() != null)
					permissibleCarOpen = pr.getPermissibleCarOpen().doubleValue();
				if (permissibleCarStilt == null && pr.getPermissibleCarStilt() != null)
					permissibleCarStilt = pr.getPermissibleCarStilt().doubleValue();
				if (permissibleCarBasement == null && pr.getPermissibleCarBasement() != null)
					permissibleCarBasement = pr.getPermissibleCarBasement().doubleValue();
				if (permissibleTwoWheeler == null && pr.getPermissibleTwoWheeler() != null)
					permissibleTwoWheeler = pr.getPermissibleTwoWheeler().doubleValue();
				if (permissibleVisitor == null && pr.getPermissibleVisitor() != null)
					permissibleVisitor = pr.getPermissibleVisitor().doubleValue();
				if (perAreaCar == null && pr.getPerAreaCar() != null)
					perAreaCar = pr.getPerAreaCar().doubleValue();
				if (perAreaTwoWheeler == null && pr.getPerAreaTwoWheeler() != null)
					perAreaTwoWheeler = pr.getPerAreaTwoWheeler().doubleValue();
				if (perAreaVisitor == null && pr.getPerAreaVisitor() != null)
					perAreaVisitor = pr.getPerAreaVisitor().doubleValue();

				if (perAreaHotelWithoutBanquetCar == null && pr.getPerAreaHotelWithoutBanquetCar() != null)
					perAreaHotelWithoutBanquetCar = pr.getPerAreaHotelWithoutBanquetCar().doubleValue();
				if (perRoomHotelWithoutBanquetCar == null && pr.getPerRoomHotelWithoutBanquetCar() != null)
					perRoomHotelWithoutBanquetCar = pr.getPerRoomHotelWithoutBanquetCar().doubleValue();
				if (perAreaHotelBanquetCar == null && pr.getPerAreaHotelBanquetCar() != null)
					perAreaHotelBanquetCar = pr.getPerAreaHotelBanquetCar().doubleValue();
				if (perRoomHotelBanquetCar == null && pr.getPerRoomHotelBanquetCar() != null)
					perRoomHotelBanquetCar = pr.getPerRoomHotelBanquetCar().doubleValue();
				if (perRoomHotelsCar == null && pr.getPerRoomHotelsCar() != null)
					perRoomHotelsCar = pr.getPerRoomHotelsCar().doubleValue();

				if (perAreaCommercialBusinessCar == null && pr.getPerAreaCommercialBusinessCar() != null)
					perAreaCommercialBusinessCar = pr.getPerAreaCommercialBusinessCar().doubleValue();
				if (perAreaCommercialShopsCar == null && pr.getPerAreaCommercialShopsCar() != null)
					perAreaCommercialShopsCar = pr.getPerAreaCommercialShopsCar().doubleValue();

				if (perAreaInstitutionalPSPCar == null && pr.getPerAreaInstitutionalPSPCar() != null)
					perAreaInstitutionalPSPCar = pr.getPerAreaInstitutionalPSPCar().doubleValue();
				if (perAreaInstitutionalMedicalCar == null && pr.getPerAreaInstitutionalMedicalCar() != null)
					perAreaInstitutionalMedicalCar = pr.getPerAreaInstitutionalMedicalCar().doubleValue();
				if (perBedInstitutionalMedicalCar == null && pr.getPerBedInstitutionalMedicalCar() != null)
					perBedInstitutionalMedicalCar = pr.getPerBedInstitutionalMedicalCar().doubleValue();

				if (perAreaEducationalNurseryCar == null && pr.getPerAreaEducationalNurseryCar() != null)
					perAreaEducationalNurseryCar = pr.getPerAreaEducationalNurseryCar().doubleValue();
				if (perAreaEducationalSchoolsCar == null && pr.getPerAreaEducationalSchoolsCar() != null)
					perAreaEducationalSchoolsCar = pr.getPerAreaEducationalSchoolsCar().doubleValue();

				if (perSeatAssemblyCinemaCar == null && pr.getPerSeatAssemblyCinemaCar() != null)
					perSeatAssemblyCinemaCar = pr.getPerSeatAssemblyCinemaCar().doubleValue();
				if (perPlotAreaAssemblyCommunityCar == null && pr.getPerPlotAreaAssemblyCommunityCar() != null)
					perPlotAreaAssemblyCommunityCar = pr.getPerPlotAreaAssemblyCommunityCar().doubleValue();
				if (perSeatAssemblyStadiumCar == null && pr.getPerSeatAssemblyStadiumCar() != null)
					perSeatAssemblyStadiumCar = pr.getPerSeatAssemblyStadiumCar().doubleValue();

				if (perAreaIndustrialCar == null && pr.getPerAreaIndustrialCar() != null)
					perAreaIndustrialCar = pr.getPerAreaIndustrialCar().doubleValue();
				if (perAreaWholesaleCar == null && pr.getPerAreaWholesaleCar() != null)
					perAreaWholesaleCar = pr.getPerAreaWholesaleCar().doubleValue();

				// New two-wheeler and visitor fields
				if (perAreaIndustrialTwoWheeler == null && pr.getPerAreaIndustrialTwoWheeler() != null)
					perAreaIndustrialTwoWheeler = pr.getPerAreaIndustrialTwoWheeler().doubleValue();
				if (perAreaInstitutionalPSPTwoWheeler == null && pr.getPerAreaInstitutionalPSPTwoWheeler() != null)
					perAreaInstitutionalPSPTwoWheeler = pr.getPerAreaInstitutionalPSPTwoWheeler().doubleValue();
				if (perBedInstitutionalMedicalTwoWheeler == null
						&& pr.getPerBedInstitutionalMedicalTwoWheeler() != null)
					perBedInstitutionalMedicalTwoWheeler = pr.getPerBedInstitutionalMedicalTwoWheeler().doubleValue();
				if (perSeatAssemblyCinemaTwoWheeler == null && pr.getPerSeatAssemblyCinemaTwoWheeler() != null)
					perSeatAssemblyCinemaTwoWheeler = pr.getPerSeatAssemblyCinemaTwoWheeler().doubleValue();
				if (perAreaInstitutionalMedicalVisitor == null && pr.getPerAreaInstitutionalMedicalVisitor() != null)
					perAreaInstitutionalMedicalVisitor = pr.getPerAreaInstitutionalMedicalVisitor().doubleValue();
				if (perAreaInstitutionalPSPVisitor == null && pr.getPerAreaInstitutionalPSPVisitor() != null)
					perAreaInstitutionalPSPVisitor = pr.getPerAreaInstitutionalPSPVisitor().doubleValue();
			}
		}

		return new ParkingRuleResult(noOfParking != null ? noOfParking : 0d,
				permissibleCar != null ? permissibleCar : 0d,
				permissibleCarOpen != null ? permissibleCarOpen : 0d,				
				permissibleCarStilt != null ? permissibleCarStilt : 0d,
				permissibleTwoWheeler != null ? permissibleTwoWheeler : 0d,
			    permissibleCarBasement != null ? permissibleCarBasement : 0d,
				permissibleVisitor != null ? permissibleVisitor : 0d, perAreaCar != null ? perAreaCar : 0d,
				perAreaTwoWheeler != null ? perAreaTwoWheeler : 0d, perAreaVisitor != null ? perAreaVisitor : 0d,
				perAreaHotelWithoutBanquetCar != null ? perAreaHotelWithoutBanquetCar : 0d,
				perRoomHotelWithoutBanquetCar != null ? perRoomHotelWithoutBanquetCar : 0d,
				perAreaHotelBanquetCar != null ? perAreaHotelBanquetCar : 0d,
				perRoomHotelBanquetCar != null ? perRoomHotelBanquetCar : 0d,
				perRoomHotelsCar != null ? perRoomHotelsCar : 0d,
				perAreaCommercialBusinessCar != null ? perAreaCommercialBusinessCar : 0d,
				perAreaCommercialShopsCar != null ? perAreaCommercialShopsCar : 0d,
				perAreaInstitutionalPSPCar != null ? perAreaInstitutionalPSPCar : 0d,
				perAreaInstitutionalMedicalCar != null ? perAreaInstitutionalMedicalCar : 0d,
				perBedInstitutionalMedicalCar != null ? perBedInstitutionalMedicalCar : 0d,
				perAreaEducationalNurseryCar != null ? perAreaEducationalNurseryCar : 0d,
				perAreaEducationalSchoolsCar != null ? perAreaEducationalSchoolsCar : 0d,
				perSeatAssemblyCinemaCar != null ? perSeatAssemblyCinemaCar : 0d,
				perPlotAreaAssemblyCommunityCar != null ? perPlotAreaAssemblyCommunityCar : 0d,
				perSeatAssemblyStadiumCar != null ? perSeatAssemblyStadiumCar : 0d,
				perAreaIndustrialCar != null ? perAreaIndustrialCar : 0d,
				perAreaWholesaleCar != null ? perAreaWholesaleCar : 0d,
				perAreaIndustrialTwoWheeler != null ? perAreaIndustrialTwoWheeler : 0d,
				perAreaInstitutionalPSPTwoWheeler != null ? perAreaInstitutionalPSPTwoWheeler : 0d,
				perBedInstitutionalMedicalTwoWheeler != null ? perBedInstitutionalMedicalTwoWheeler : 0d,
				perSeatAssemblyCinemaTwoWheeler != null ? perSeatAssemblyCinemaTwoWheeler : 0d,
				perAreaInstitutionalMedicalVisitor != null ? perAreaInstitutionalMedicalVisitor : 0d,
				perAreaInstitutionalPSPVisitor != null ? perAreaInstitutionalPSPVisitor : 0d);
	}

	private double calculateRequiredParkingArea(ParkingAreas areas, double ecs, double noOfRequiredParking) {
		if (areas.open.doubleValue() > 0 || areas.stilt.doubleValue() > 0 || areas.basement.doubleValue() > 0
				|| areas.cover.doubleValue() > 0) {
			return ecs * noOfRequiredParking;
		}
		return 0d;
	}

	private void addIndividualParkingReports(Plan pl, ParkingAreas areas) {
		if (areas.open.doubleValue() > 0) {
			setReportOutputDetails(pl, FOUR_P_TWO_P_ONE, OPEN_PARKING_AREA, EMPTY_STRING,
					roundECS(areas.open.doubleValue() / OPEN_ECS) + ECS_STRING + OPENING_BRACKET + areas.open + SQMTRS
							+ CLOSING_BRACKET,
					EMPTY_STRING);
		}
		if (areas.cover.doubleValue() > 0) {
			setReportOutputDetails(pl, FOUR_P_TWO_P_ONE, COVER_PARKING_AREA, SINGLE_SPACE_STRING,
					roundECS(areas.cover.doubleValue() / COVER_ECS) + ECS_STRING + OPENING_BRACKET + areas.cover
							+ SQMTRS + CLOSING_BRACKET,
					EMPTY_STRING);
		}
		if (areas.basement.doubleValue() > 0) {
			setReportOutputDetails(pl, FOUR_P_TWO_P_ONE, BASEMENT_PARKING_AREA, EMPTY_STRING,
					roundECS(areas.basement.doubleValue() / BSMNT_ECS) + ECS_STRING + OPENING_BRACKET + areas.basement
							+ SQMTRS + CLOSING_BRACKET,
					EMPTY_STRING);
		}
		if (areas.stilt.doubleValue() > 0) {
			setReportOutputDetails(pl, FOUR_P_TWO_P_ONE, STILT_PARKING_AREA, EMPTY_STRING,
					roundECS(areas.stilt.doubleValue() / STILT_ECS) + ECS_STRING + OPENING_BRACKET + areas.stilt
							+ SQMTRS + CLOSING_BRACKET,
					EMPTY_STRING);
		}
	}

	private void addIndividualParkingReports1(Plan pl, ParkingAreas1 areas) {
		if (areas.open.doubleValue() > 0) {
			setReportOutputDetails(pl, "4.2.1", "Open Parking Area", "",
					roundECS(areas.open.doubleValue() / OPEN_ECS) + " ECS " + "(" + areas.open + SQMTRS + ")", "");
		}
		if (areas.cover.doubleValue() > 0) {
			setReportOutputDetails(pl, "4.2.1", "Cover Parking Area", " ",
					roundECS(areas.cover.doubleValue() / COVER_ECS) + " ECS " + "(" + areas.cover + SQMTRS + ")", "");
		}
		if (areas.basement.doubleValue() > 0) {
			setReportOutputDetails(pl, "4.2.1", "Basement Parking Area", "",
					roundECS(areas.basement.doubleValue() / BSMNT_ECS) + " ECS " + "(" + areas.basement + SQMTRS + ")",
					"");
		}
		if (areas.stilt.doubleValue() > 0) {
			setReportOutputDetails(pl, "4.2.1", "Stilt Parking Area", "",
					roundECS(areas.stilt.doubleValue() / STILT_ECS) + " ECS " + "(" + areas.stilt + SQMTRS + ")", "");
		}
	}

	private static class ParkingAreas {
		BigDecimal open, cover, basement, stilt;

		ParkingAreas(BigDecimal open, BigDecimal cover, BigDecimal basement, BigDecimal stilt) {
			this.open = open;
			this.cover = cover;
			this.basement = basement;
			this.stilt = stilt;
		}

		BigDecimal getTotal() {
			return open.add(cover).add(basement).add(stilt);
		}
	}

	private static class ParkingAreas1 {
		BigDecimal open, cover, basement, stilt, twoWheeler, visitor, total;

		ParkingAreas1(BigDecimal open, BigDecimal cover, BigDecimal basement, BigDecimal stilt, BigDecimal twoWheeler,
				BigDecimal visitor, BigDecimal total) {
			this.open = open;
			this.cover = cover;
			this.basement = basement;
			this.stilt = stilt;
			this.twoWheeler = twoWheeler;
			this.visitor = visitor;
			this.total = total;
		}

		// Add these getters:
		BigDecimal getOpen() {
			return open;
		}

		BigDecimal getCover() {
			return cover;
		}

		BigDecimal getBasement() {
			return basement;
		}

		BigDecimal getStilt() {
			return stilt;
		}

		BigDecimal getTwoWheeler() {
			return twoWheeler;
		}

		BigDecimal getVisitor() {
			return visitor;
		}

		BigDecimal getTotal() {
			return total;
		}
	}

	public class ParkingRuleResult {

		// ----- Institutional -----
		private double perAreaInstitutionalPSPCar;
		private double perAreaInstitutionalMedicalCar;
		private double perBedInstitutionalMedicalCar;

		// ----- Educational -----
		private double perAreaEducationalNurseryCar; // Nursery/Creche → 10% built-up
		private double perAreaEducationalSchoolsCar; // Schools/Colleges → 20% built-up

		// ----- Assembly -----
		private double perSeatAssemblyCinemaCar; // Cinema/Multiplex → 1 per 10 seats
		private double perPlotAreaAssemblyCommunityCar; // Community Hall / Banquet → 1 per 50 sqm plot area
		private double perSeatAssemblyStadiumCar; // Stadium/Exhibition → 1 per 30 seats

		// ----- Industrial -----
		private double perAreaIndustrialCar; // Industrial → 1 per 200 sqm

		// ----- Wholesale / Storage -----
		private double perAreaWholesaleCar; // Wholesale/Storage → 1 per 250 sqm

		// ----- Hotels (already existed but normalized) -----
		private double perAreaHotelWithoutBanquetCar;
		private double perRoomHotelWithoutBanquetCar;
		private double perAreaHotelBanquetCar;
		private double perRoomHotelBanquetCar;
		private double perRoomHotelsCar;

		// ----- Commercial (existing) -----
		private double perAreaCommercialBusinessCar;
		private double perAreaCommercialShopsCar;

		// ----- Common / Configurable Values -----
		private double noOfRequiredParking;
		private double permissibleCar;
		private double permissibleCarOpen;
		private double permissibleCarStilt;
		private double permissibleCarBasement;
		private double permissibleTwoWheeler;
		private double permissibleVisitor;

		private double perAreaCar;
		private double perAreaTwoWheeler;
		private double perAreaVisitor;

		private double perAreaIndustrialTwoWheeler;
		private double perAreaInstitutionalPSPTwoWheeler;
		private double perBedInstitutionalMedicalTwoWheeler;
		private double perSeatAssemblyCinemaTwoWheeler;
		private double perAreaInstitutionalMedicalVisitor;
		private double perAreaInstitutionalPSPVisitor;

		// ----- Constructor -----
		public ParkingRuleResult(double noOfRequiredParking, double permissibleCar,  double permissibleCarOpen,  double permissibleCarStilt,
				double permissibleTwoWheeler,  double permissibleCarBasement,
				double permissibleVisitor, double perAreaCar, double perAreaTwoWheeler, double perAreaVisitor,

				// Hotel
				double perAreaHotelWithoutBanquetCar, double perRoomHotelWithoutBanquetCar,
				double perAreaHotelBanquetCar, double perRoomHotelBanquetCar, double perRoomHotelsCar,

				// Commercial
				double perAreaCommercialBusinessCar, double perAreaCommercialShopsCar,

				// Institutional
				double perAreaInstitutionalPSPCar, double perAreaInstitutionalMedicalCar,
				double perBedInstitutionalMedicalCar,

				// Educational
				double perAreaEducationalNurseryCar, double perAreaEducationalSchoolsCar,

				// Assembly
				double perSeatAssemblyCinemaCar, double perPlotAreaAssemblyCommunityCar,
				double perSeatAssemblyStadiumCar,

				// Industrial
				double perAreaIndustrialCar,

				// Wholesale
				double perAreaWholesaleCar,

				double perAreaIndustrialTwoWheeler, double perAreaInstitutionalPSPTwoWheeler,
				double perBedInstitutionalMedicalTwoWheeler, double perSeatAssemblyCinemaTwoWheeler,
				double perAreaInstitutionalMedicalVisitor, double perAreaInstitutionalPSPVisitor) {
			this.noOfRequiredParking = noOfRequiredParking;
			this.permissibleCar = permissibleCar;
			this.permissibleCarOpen = permissibleCarOpen;
			this.permissibleCarStilt = permissibleCarStilt;
			this.permissibleCarBasement = permissibleCarBasement;
			this.permissibleTwoWheeler = permissibleTwoWheeler;
			this.permissibleVisitor = permissibleVisitor;

			this.perAreaCar = perAreaCar;
			this.perAreaTwoWheeler = perAreaTwoWheeler;
			this.perAreaVisitor = perAreaVisitor;

			this.perAreaHotelWithoutBanquetCar = perAreaHotelWithoutBanquetCar;
			this.perRoomHotelWithoutBanquetCar = perRoomHotelWithoutBanquetCar;
			this.perAreaHotelBanquetCar = perAreaHotelBanquetCar;
			this.perRoomHotelBanquetCar = perRoomHotelBanquetCar;
			this.perRoomHotelsCar = perRoomHotelsCar;

			this.perAreaCommercialBusinessCar = perAreaCommercialBusinessCar;
			this.perAreaCommercialShopsCar = perAreaCommercialShopsCar;

			this.perAreaInstitutionalPSPCar = perAreaInstitutionalPSPCar;
			this.perAreaInstitutionalMedicalCar = perAreaInstitutionalMedicalCar;
			this.perBedInstitutionalMedicalCar = perBedInstitutionalMedicalCar;

			this.perAreaEducationalNurseryCar = perAreaEducationalNurseryCar;
			this.perAreaEducationalSchoolsCar = perAreaEducationalSchoolsCar;

			this.perSeatAssemblyCinemaCar = perSeatAssemblyCinemaCar;
			this.perPlotAreaAssemblyCommunityCar = perPlotAreaAssemblyCommunityCar;
			this.perSeatAssemblyStadiumCar = perSeatAssemblyStadiumCar;

			this.perAreaIndustrialCar = perAreaIndustrialCar;
			this.perAreaWholesaleCar = perAreaWholesaleCar;

			this.perAreaIndustrialTwoWheeler = perAreaIndustrialTwoWheeler;
			this.perAreaInstitutionalPSPTwoWheeler = perAreaInstitutionalPSPTwoWheeler;
			this.perBedInstitutionalMedicalTwoWheeler = perBedInstitutionalMedicalTwoWheeler;
			this.perSeatAssemblyCinemaTwoWheeler = perSeatAssemblyCinemaTwoWheeler;
			this.perAreaInstitutionalMedicalVisitor = perAreaInstitutionalMedicalVisitor;
			this.perAreaInstitutionalPSPVisitor = perAreaInstitutionalPSPVisitor;
		}

		// ----- Getters & Setters -----

		public double getPerAreaInstitutionalPSPCar() {
			return perAreaInstitutionalPSPCar;
		}

		public void setPerAreaInstitutionalPSPCar(double v) {
			this.perAreaInstitutionalPSPCar = v;
		}

		public double getPerAreaInstitutionalMedicalCar() {
			return perAreaInstitutionalMedicalCar;
		}

		public void setPerAreaInstitutionalMedicalCar(double v) {
			this.perAreaInstitutionalMedicalCar = v;
		}

		public double getPerBedInstitutionalMedicalCar() {
			return perBedInstitutionalMedicalCar;
		}

		public void setPerBedInstitutionalMedicalCar(double v) {
			this.perBedInstitutionalMedicalCar = v;
		}

		public double getPerAreaEducationalNurseryCar() {
			return perAreaEducationalNurseryCar;
		}

		public void setPerAreaEducationalNurseryCar(double v) {
			this.perAreaEducationalNurseryCar = v;
		}

		public double getPerAreaEducationalSchoolsCar() {
			return perAreaEducationalSchoolsCar;
		}

		public void setPerAreaEducationalSchoolsCar(double v) {
			this.perAreaEducationalSchoolsCar = v;
		}

		public double getPerSeatAssemblyCinemaCar() {
			return perSeatAssemblyCinemaCar;
		}

		public void setPerSeatAssemblyCinemaCar(double v) {
			this.perSeatAssemblyCinemaCar = v;
		}

		public double getPerPlotAreaAssemblyCommunityCar() {
			return perPlotAreaAssemblyCommunityCar;
		}

		public void setPerPlotAreaAssemblyCommunityCar(double v) {
			this.perPlotAreaAssemblyCommunityCar = v;
		}

		public double getPerSeatAssemblyStadiumCar() {
			return perSeatAssemblyStadiumCar;
		}

		public void setPerSeatAssemblyStadiumCar(double v) {
			this.perSeatAssemblyStadiumCar = v;
		}

		public double getPerAreaIndustrialCar() {
			return perAreaIndustrialCar;
		}

		public void setPerAreaIndustrialCar(double v) {
			this.perAreaIndustrialCar = v;
		}

		public double getPerAreaWholesaleCar() {
			return perAreaWholesaleCar;
		}

		public void setPerAreaWholesaleCar(double v) {
			this.perAreaWholesaleCar = v;
		}

		public double getPerAreaHotelWithoutBanquetCar() {
			return perAreaHotelWithoutBanquetCar;
		}

		public void setPerAreaHotelWithoutBanquetCar(double v) {
			this.perAreaHotelWithoutBanquetCar = v;
		}

		public double getPerRoomHotelWithoutBanquetCar() {
			return perRoomHotelWithoutBanquetCar;
		}

		public void setPerRoomHotelWithoutBanquetCar(double v) {
			this.perRoomHotelWithoutBanquetCar = v;
		}

		public double getPerAreaHotelBanquetCar() {
			return perAreaHotelBanquetCar;
		}

		public void setPerAreaHotelBanquetCar(double v) {
			this.perAreaHotelBanquetCar = v;
		}

		public double getPerRoomHotelBanquetCar() {
			return perRoomHotelBanquetCar;
		}

		public void setPerRoomHotelBanquetCar(double v) {
			this.perRoomHotelBanquetCar = v;
		}

		public double getPerRoomHotelsCar() {
			return perRoomHotelsCar;
		}

		public void setPerRoomHotelsCar(double v) {
			this.perRoomHotelsCar = v;
		}

		public double getPerAreaCommercialBusinessCar() {
			return perAreaCommercialBusinessCar;
		}

		public void setPerAreaCommercialBusinessCar(double v) {
			this.perAreaCommercialBusinessCar = v;
		}

		public double getPerAreaCommercialShopsCar() {
			return perAreaCommercialShopsCar;
		}

		public void setPerAreaCommercialShopsCar(double v) {
			this.perAreaCommercialShopsCar = v;
		}

		public double getNoOfRequiredParking() {
			return noOfRequiredParking;
		}

		public void setNoOfRequiredParking(double v) {
			this.noOfRequiredParking = v;
		}

		public double getPermissibleCar() {
			return permissibleCar;
		}

		public void setPermissibleCar(double v) {
			this.permissibleCar = v;
		}
		
		public double getPermissibleCarOpen() {
			return permissibleCarOpen;
		}

		public void setPermissibleCarOpen(double v) {
			this.permissibleCarOpen = v;
		}

		public double getPermissibleCarStilt() {
			return permissibleCarStilt;
		}

		public void setPermissibleCarStilt(double v) {
			this.permissibleCarStilt = v;
		}

		public double getPermissibleCarBasement() {
			return permissibleCarBasement;
		}

		public void setPermissibleCarBasement(double v) {
			this.permissibleCarBasement = v;
		}

		
		public double getPermissibleTwoWheeler() {
			return permissibleTwoWheeler;
		}

		public void setPermissibleTwoWheeler(double v) {
			this.permissibleTwoWheeler = v;
		}

		public double getPermissibleVisitor() {
			return permissibleVisitor;
		}

		public void setPermissibleVisitor(double v) {
			this.permissibleVisitor = v;
		}

		public double getPerAreaCar() {
			return perAreaCar;
		}

		public void setPerAreaCar(double v) {
			this.perAreaCar = v;
		}

		public double getPerAreaTwoWheeler() {
			return perAreaTwoWheeler;
		}

		public void setPerAreaTwoWheeler(double v) {
			this.perAreaTwoWheeler = v;
		}

		public double getPerAreaVisitor() {
			return perAreaVisitor;
		}

		public void setPerAreaVisitor(double v) {
			this.perAreaVisitor = v;
		}

		// Getters and Setters for new fields
		public double getPerAreaIndustrialTwoWheeler() {
			return perAreaIndustrialTwoWheeler;
		}

		public void setPerAreaIndustrialTwoWheeler(double perAreaIndustrialTwoWheeler) {
			this.perAreaIndustrialTwoWheeler = perAreaIndustrialTwoWheeler;
		}

		public double getPerAreaInstitutionalPSPTwoWheeler() {
			return perAreaInstitutionalPSPTwoWheeler;
		}

		public void setPerAreaInstitutionalPSPTwoWheeler(double perAreaInstitutionalPSPTwoWheeler) {
			this.perAreaInstitutionalPSPTwoWheeler = perAreaInstitutionalPSPTwoWheeler;
		}

		public double getPerBedInstitutionalMedicalTwoWheeler() {
			return perBedInstitutionalMedicalTwoWheeler;
		}

		public void setPerBedInstitutionalMedicalTwoWheeler(double perBedInstitutionalMedicalTwoWheeler) {
			this.perBedInstitutionalMedicalTwoWheeler = perBedInstitutionalMedicalTwoWheeler;
		}

		public double getPerSeatAssemblyCinemaTwoWheeler() {
			return perSeatAssemblyCinemaTwoWheeler;
		}

		public void setPerSeatAssemblyCinemaTwoWheeler(double perSeatAssemblyCinemaTwoWheeler) {
			this.perSeatAssemblyCinemaTwoWheeler = perSeatAssemblyCinemaTwoWheeler;
		}

		public double getPerAreaInstitutionalMedicalVisitor() {
			return perAreaInstitutionalMedicalVisitor;
		}

		public void setPerAreaInstitutionalMedicalVisitor(double perAreaInstitutionalMedicalVisitor) {
			this.perAreaInstitutionalMedicalVisitor = perAreaInstitutionalMedicalVisitor;
		}

		public double getPerAreaInstitutionalPSPVisitor() {
			return perAreaInstitutionalPSPVisitor;
		}

		public void setPerAreaInstitutionalPSPVisitor(double perAreaInstitutionalPSPVisitor) {
			this.perAreaInstitutionalPSPVisitor = perAreaInstitutionalPSPVisitor;
		}
	}

	private void setReportOutputDetails(Plan pl, String ruleNo, String ruleDesc, String expected, String actual,
			String status) {
		Map<String, String> details = new HashMap<>();
		details.put(RULE_NO, ruleNo);
		details.put(DESCRIPTION, ruleDesc);
		details.put(REQUIRED, expected);
		details.put(PROVIDED, actual);
		details.put(STATUS, status);
		scrutinyDetail.getDetail().add(details);
		pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
	}

	private void setReportOutputDetails1(Plan pl, String ruleNo, String ruleDesc, String expected, String actual,
			String status) {

		ScrutinyDetail scrutinyDetail1 = new ScrutinyDetail();
		scrutinyDetail1.addColumnHeading(1, RULE_NO);
		scrutinyDetail1.addColumnHeading(2, DESCRIPTION);
		scrutinyDetail1.addColumnHeading(3, EMPTY_STRING);
		scrutinyDetail1.addColumnHeading(4, REQUIRED);
		scrutinyDetail1.addColumnHeading(5, PROVIDED);
		scrutinyDetail1.addColumnHeading(6, STATUS);

		Map<String, String> details = new HashMap<>();
		details.put(RULE_NO, ruleNo);
		details.put(DESCRIPTION, ruleDesc);
		details.put(REQUIRED, expected);
		details.put(PROVIDED, actual);
		details.put(STATUS, status);
		scrutinyDetail.getDetail().add(details);
		pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
	}

	private void validateSpecialParking(Plan pl, ParkingHelper helper, BigDecimal totalBuiltupArea) {
		BigDecimal maxHeightOfBuilding = BigDecimal.ZERO;
		int failedCount = 0;
		int success = 0;
		if (!pl.getParkingDetails().getSpecial().isEmpty()) {
			for (Measurement m : pl.getParkingDetails().getSpecial()) {
				if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0)
					failedCount++;
				else
					success++;
			}
			if (failedCount > 0)
				pl.addError(SPECIAL_PARKING_DIM_DESC, SPECIAL_PARKING_DIM_DESC + failedCount + NO_NOT_HAVING_4_PTS);
			pl.getParkingDetails().setValidSpecialSlots(success);
		}

		for (Block block : pl.getBlocks()) {
			if (block.getBuilding().getBuildingHeight() != null
					&& block.getBuilding().getBuildingHeight().compareTo(maxHeightOfBuilding) > 0) {
				maxHeightOfBuilding = block.getBuilding().getBuildingHeight();
			}
		}
		if (maxHeightOfBuilding.compareTo(new BigDecimal(15)) >= 0
				|| (pl.getPlot() != null && pl.getPlot().getArea().compareTo(new BigDecimal(500)) > 0)) {
			if (pl.getParkingDetails().getValidSpecialSlots() == 0) {
				// pl.addError(T_RULE, getLocaleMessage(DcrConstants.OBJECTNOTDEFINED,
				// SP_PARKING));
			} else {
				for (Measurement m : pl.getParkingDetails().getSpecial()) {
					if (m.getMinimumSide().compareTo(new BigDecimal(0)) > 0
							&& m.getMinimumSide().compareTo(new BigDecimal(3.6)) >= 0) {
						setReportOutputDetails(pl, T_RULE, SP_PARKING, 1 + NUMBERS,
								pl.getParkingDetails().getValidSpecialSlots() + NUMBERS,
								Result.Accepted.getResultVal());
					} else if (m.getMinimumSide().compareTo(new BigDecimal(0)) > 0) {
						setReportOutputDetails(pl, T_RULE, SP_PARKING, 1 + NUMBERS,
								pl.getParkingDetails().getValidSpecialSlots() + NUMBERS,
								Result.Not_Accepted.getResultVal());
					}
					
					if (m.getWidth().compareTo(new BigDecimal(0)) > 0
							&& m.getWidth().compareTo(new BigDecimal(3.6)) >= 0) {
						setReportOutputDetails(pl, T_RULE, SP_PARKING, 1 + NUMBERS,
								pl.getParkingDetails().getValidSpecialSlots() + NUMBERS,
								Result.Accepted.getResultVal());
					} else if (m.getWidth().compareTo(new BigDecimal(0)) > 0) {
						setReportOutputDetails(pl, T_RULE, SP_PARKING, 1 + NUMBERS,
								pl.getParkingDetails().getValidSpecialSlots() + NUMBERS,
								Result.Not_Accepted.getResultVal());
					}
					BigDecimal minDist = m.getMinimumDistance() != null ? m.getMinimumDistance() : BigDecimal.ZERO;

				    if (minDist.compareTo(BigDecimal.ZERO) > 0 && minDist.compareTo(new BigDecimal(30.0)) <= 0) {
				        setReportOutputDetails(pl, T_RULE, SP_PARKING, 1 + NUMBERS,
				                pl.getParkingDetails().getValidSpecialSlots() + NUMBERS,
				                Result.Accepted.getResultVal());
				    } else if (minDist.compareTo(BigDecimal.ZERO) > 0) {
				        setReportOutputDetails(pl, T_RULE, SP_PARKING, 1 + NUMBERS,
				                pl.getParkingDetails().getValidSpecialSlots() + NUMBERS,
				                Result.Not_Accepted.getResultVal());
				    }
				}
				
			}
		}

	}

	// For Assam - Two Wheeler Parking
	private void processTwoWheelerParking1(Plan pl, ParkingHelper helper) {
		ScrutinyDetail scrutinyDetail = initializeScrutinyDetail(); // Optional, if you’re using it

		// Step 1: Compute required area for two-wheeler parking
		double ecsArea = 2.70 * 5.50;
		double requiredTwoWheelerArea = 0.25 * helper.totalRequiredCarParking * ecsArea;
		helper.twoWheelerParking = BigDecimal.valueOf(requiredTwoWheelerArea).setScale(4, BigDecimal.ROUND_HALF_UP)
				.doubleValue();

		// Step 2: Compute provided two-wheeler parking area
		double providedArea = 0;
		if (pl.getParkingDetails() != null && pl.getParkingDetails().getTwoWheelers() != null) {
			for (Measurement measurement : pl.getParkingDetails().getTwoWheelers()) {
				if (measurement.getArea() != null) {
					providedArea += measurement.getArea().doubleValue();
				}
			}
		}

		// Step 3: Generate Report
		if (providedArea == 0) {
			pl.addError("TwoWheelerParking", "Two-wheeler parking area not defined.");
		}

		String required = BigDecimal.valueOf(helper.twoWheelerParking).setScale(2, BigDecimal.ROUND_HALF_UP) + " sqm";
		String provided = BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + " sqm";

		if (providedArea < helper.twoWheelerParking) {
			setReportOutputDetails(pl, SUB_RULE_34_2, TWO_WHEELER_PARK_AREA, required, provided,
					Result.Not_Accepted.getResultVal());
		} else {
			setReportOutputDetails(pl, SUB_RULE_34_2, TWO_WHEELER_PARK_AREA, required, provided,
					Result.Accepted.getResultVal());
		}
	}

	private void processTwoWheelerParking(Plan pl, ParkingHelper helper) {
		helper.twoWheelerParking = BigDecimal.valueOf(0.25 * helper.totalRequiredCarParking * 2.70 * 5.50)
				.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
		double providedArea = 0;
		for (Measurement measurement : pl.getParkingDetails().getTwoWheelers()) {
			providedArea = providedArea + measurement.getArea().doubleValue();
		}
		if (providedArea < helper.twoWheelerParking) {
			setReportOutputDetails(pl, SUB_RULE_34_2, TWO_WHEELER_PARK_AREA,
					helper.twoWheelerParking + SINGLE_SPACE_STRING + DcrConstants.SQMTRS,
					BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + SINGLE_SPACE_STRING
							+ DcrConstants.SQMTRS,
					Result.Not_Accepted.getResultVal());
		} else {
			setReportOutputDetails(pl, SUB_RULE_34_2, TWO_WHEELER_PARK_AREA,
					helper.twoWheelerParking + SINGLE_SPACE_STRING + DcrConstants.SQMTRS,
					BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + SINGLE_SPACE_STRING
							+ DcrConstants.SQMTRS,
					Result.Accepted.getResultVal());
		}
	}

	private void processMechanicalParking(Plan pl) {
		int count = 0;
		for (Measurement m : pl.getParkingDetails().getMechParking())
			if (m.getWidth().compareTo(BigDecimal.valueOf(MECH_PARKING_WIDTH)) < 0
					|| m.getHeight().compareTo(BigDecimal.valueOf(MECH_PARKING_HEIGHT)) < 0)
				count++;
		if (count > 0) {
			setReportOutputDetails(pl, SUB_RULE_34_2, MECH_PARKING_DESC, MECH_PARKING_DIM_DESC,
					count + MECH_PARKING_DIM_DESC_NA, Result.Not_Accepted.getResultVal());
		} else {
			setReportOutputDetails(pl, SUB_RULE_34_2, MECH_PARKING_DESC, MECH_PARKING_DIM_DESC,
					count + MECH_PARKING_DIM_DESC_NA, Result.Accepted.getResultVal());
		}
	}

	/*
	 * private double processMechanicalParking(Plan pl, ParkingHelper helper) {
	 * Integer noOfMechParkingFromPlInfo =
	 * pl.getPlanInformation().getNoOfMechanicalParking(); Integer providedSlots =
	 * pl.getParkingDetails().getMechParking().size(); double maxAllowedMechPark =
	 * BigDecimal.valueOf(helper.totalRequiredCarParking / 2).setScale(0,
	 * RoundingMode.UP) .intValue(); if (noOfMechParkingFromPlInfo > 0) { if
	 * (noOfMechParkingFromPlInfo > 0 && providedSlots == 0) {
	 * setReportOutputDetails(pl, SUB_RULE_34_2, MECHANICAL_PARKING, 1 + NUMBERS,
	 * providedSlots + NUMBERS, Result.Not_Accepted.getResultVal()); } else if
	 * (noOfMechParkingFromPlInfo > 0 && providedSlots > 0 &&
	 * noOfMechParkingFromPlInfo > maxAllowedMechPark) { setReportOutputDetails(pl,
	 * SUB_RULE_34_2, MAX_ALLOWED_MECH_PARK, maxAllowedMechPark + NUMBERS,
	 * noOfMechParkingFromPlInfo + NUMBERS, Result.Not_Accepted.getResultVal()); }
	 * else if (noOfMechParkingFromPlInfo > 0 && providedSlots > 0) {
	 * setReportOutputDetails(pl, SUB_RULE_34_2, MECHANICAL_PARKING, EMPTY_STRING,
	 * noOfMechParkingFromPlInfo + NUMBERS, Result.Accepted.getResultVal()); } }
	 * return 0; }
	 */

	/*
	 * private void buildResultForYardValidation(Plan Plan, BigDecimal
	 * parkSlotAreaInFrontYard, BigDecimal maxAllowedArea, String type) {
	 * Plan.reportOutput .add(buildRuleOutputWithSubRule(DcrConstants.RULE34,
	 * SUB_RULE_34_1,
	 * "Parking space should not exceed 50% of the area of mandatory " + type,
	 * "Parking space should not exceed 50% of the area of mandatory " + type,
	 * "Maximum allowed area for parking in " + type +SINGLE_SPACE_STRING +
	 * maxAllowedArea + DcrConstants.SQMTRS,
	 * "Parking provided in more than the allowed area " + parkSlotAreaInFrontYard +
	 * DcrConstants.SQMTRS, Result.Not_Accepted, null)); } private BigDecimal
	 * validateParkingSlotsAreWithInYard(Plan Plan, Polygon yardPolygon) {
	 * BigDecimal area = BigDecimal.ZERO; for (Measurement parkingSlot :
	 * Plan.getParkingDetails().getCars()) { Iterator parkSlotIterator =
	 * parkingSlot.getPolyLine().getVertexIterator(); while
	 * (parkSlotIterator.hasNext()) { DXFVertex dxfVertex = (DXFVertex)
	 * parkSlotIterator.next(); Point point = dxfVertex.getPoint(); if
	 * (rayCasting.contains(point, yardPolygon)) { area =
	 * area.add(parkingSlot.getArea()); } } } return area; }
	 */

	private void checkDimensionForCarParking(Plan pl, ParkingHelper helper) {

		/*
		 * for (Block block : Plan.getBlocks()) { for (SetBack setBack :
		 * block.getSetBacks()) { if (setBack.getFrontYard() != null &&
		 * setBack.getFrontYard().getPresentInDxf()) { Polygon frontYardPolygon =
		 * ProcessHelper.getPolygon(setBack.getFrontYard().getPolyLine()); BigDecimal
		 * parkSlotAreaInFrontYard = validateParkingSlotsAreWithInYard(Plan,
		 * frontYardPolygon); BigDecimal maxAllowedArea =
		 * setBack.getFrontYard().getArea().divide(BigDecimal.valueOf(2),
		 * DcrConstants.DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP); if
		 * (parkSlotAreaInFrontYard.compareTo(maxAllowedArea) > 0) {
		 * buildResultForYardValidation(Plan, parkSlotAreaInFrontYard, maxAllowedArea,
		 * "front yard space"); } } if (setBack.getRearYard() != null &&
		 * setBack.getRearYard().getPresentInDxf()) { Polygon rearYardPolygon =
		 * ProcessHelper.getPolygon(setBack.getRearYard().getPolyLine()); BigDecimal
		 * parkSlotAreaInRearYard = validateParkingSlotsAreWithInYard(Plan,
		 * rearYardPolygon); BigDecimal maxAllowedArea =
		 * setBack.getRearYard().getArea().divide(BigDecimal.valueOf(2),
		 * DcrConstants.DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP); if
		 * (parkSlotAreaInRearYard.compareTo(maxAllowedArea) > 0) {
		 * buildResultForYardValidation(Plan, parkSlotAreaInRearYard, maxAllowedArea,
		 * "rear yard space"); } } if (setBack.getSideYard1() != null &&
		 * setBack.getSideYard1().getPresentInDxf()) { Polygon sideYard1Polygon =
		 * ProcessHelper.getPolygon(setBack.getSideYard1().getPolyLine()); BigDecimal
		 * parkSlotAreaInSideYard1 = validateParkingSlotsAreWithInYard(Plan,
		 * sideYard1Polygon); BigDecimal maxAllowedArea =
		 * setBack.getSideYard1().getArea().divide(BigDecimal.valueOf(2),
		 * DcrConstants.DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP); if
		 * (parkSlotAreaInSideYard1.compareTo(maxAllowedArea) > 0) {
		 * buildResultForYardValidation(Plan, parkSlotAreaInSideYard1, maxAllowedArea,
		 * "side yard1 space"); } } if (setBack.getSideYard2() != null &&
		 * setBack.getSideYard2().getPresentInDxf()) { Polygon sideYard2Polygon =
		 * ProcessHelper.getPolygon(setBack.getSideYard2().getPolyLine()); BigDecimal
		 * parkSlotAreaInFrontYard = validateParkingSlotsAreWithInYard(Plan,
		 * sideYard2Polygon); BigDecimal maxAllowedArea =
		 * setBack.getSideYard2().getArea().divide(BigDecimal.valueOf(2),
		 * DcrConstants.DECIMALDIGITS_MEASUREMENTS, RoundingMode.HALF_UP); if
		 * (parkSlotAreaInFrontYard.compareTo(maxAllowedArea) > 0) {
		 * buildResultForYardValidation(Plan, parkSlotAreaInFrontYard, maxAllowedArea,
		 * "side yard2 space"); } } } }
		 */

		int parkingCount = pl.getParkingDetails().getCars().size();
		int failedCount = 0;
		int success = 0;
		for (Measurement slot : pl.getParkingDetails().getCars()) {
			if (slot.getHeight().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_HEIGHT
					&& slot.getWidth().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_WIDTH)
				success++;
			else
				failedCount++;
		}
		pl.getParkingDetails().setValidCarParkingSlots(parkingCount - failedCount);
		if (parkingCount > 0)
			if (failedCount > 0) {
				if (helper.totalRequiredCarParking.intValue() == pl.getParkingDetails().getValidCarParkingSlots()) {
					setReportOutputDetails(pl, SUB_RULE_40, SUB_RULE_34_1_DESCRIPTION,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + parkingCount + PARKING + failedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Accepted.getResultVal());
				} else {
					setReportOutputDetails(pl, SUB_RULE_40, SUB_RULE_34_1_DESCRIPTION,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + parkingCount + PARKING + failedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Not_Accepted.getResultVal());
				}
			} else {
				setReportOutputDetails(pl, SUB_RULE_40, SUB_RULE_34_1_DESCRIPTION,
						PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING, NO_VIOLATION_OF_AREA + parkingCount + PARKING,
						Result.Accepted.getResultVal());
			}
		int openParkCount = pl.getParkingDetails().getOpenCars().size();
		int openFailedCount = 0;
		int openSuccess = 0;
		for (Measurement slot : pl.getParkingDetails().getOpenCars()) {
			if (slot.getHeight().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_HEIGHT
					&& slot.getWidth().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_WIDTH)
				openSuccess++;
			else
				openFailedCount++;
		}
		pl.getParkingDetails().setValidOpenCarSlots(openParkCount - openFailedCount);
		if (openParkCount > 0)
			if (openFailedCount > 0) {
				if (helper.totalRequiredCarParking.intValue() == pl.getParkingDetails().getValidOpenCarSlots()) {
					setReportOutputDetails(pl, SUB_RULE_40, OPEN_PARKING_DIM_DESC,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + openParkCount + PARKING + openFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Accepted.getResultVal());
				} else {
					setReportOutputDetails(pl, SUB_RULE_40, OPEN_PARKING_DIM_DESC,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + openParkCount + PARKING + openFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Not_Accepted.getResultVal());
				}
			} else {
				setReportOutputDetails(pl, SUB_RULE_40, OPEN_PARKING_DIM_DESC,
						PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING, NO_VIOLATION_OF_AREA + openParkCount + PARKING,
						Result.Accepted.getResultVal());
			}

		int coverParkCount = pl.getParkingDetails().getCoverCars().size();
		int coverFailedCount = 0;
		int coverSuccess = 0;
		for (Measurement slot : pl.getParkingDetails().getCoverCars()) {
			if (slot.getHeight().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_HEIGHT
					&& slot.getWidth().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_WIDTH)
				coverSuccess++;
			else
				coverFailedCount++;
		}
		pl.getParkingDetails().setValidCoverCarSlots(coverParkCount - coverFailedCount);
		if (coverParkCount > 0)
			if (coverFailedCount > 0) {
				if (helper.totalRequiredCarParking.intValue() == pl.getParkingDetails().getValidCoverCarSlots()) {
					setReportOutputDetails(pl, SUB_RULE_40, COVER_PARKING_DIM_DESC,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + coverParkCount + PARKING + coverFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Accepted.getResultVal());
				} else {
					setReportOutputDetails(pl, SUB_RULE_40, COVER_PARKING_DIM_DESC,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + coverParkCount + PARKING + coverFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Not_Accepted.getResultVal());
				}
			} else {
				setReportOutputDetails(pl, SUB_RULE_40, COVER_PARKING_DIM_DESC,
						PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING, NO_VIOLATION_OF_AREA + coverParkCount + PARKING,
						Result.Accepted.getResultVal());
			}

		// Validate dimension of basement
		int bsmntParkCount = pl.getParkingDetails().getBasementCars().size();
		int bsmntFailedCount = 0;
		int bsmntSuccess = 0;
		for (Measurement slot : pl.getParkingDetails().getBasementCars()) {
			if (slot.getHeight().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_HEIGHT
					&& slot.getWidth().setScale(2, RoundingMode.UP).doubleValue() >= PARKING_SLOT_WIDTH)
				bsmntSuccess++;
			else
				bsmntFailedCount++;
		}
		pl.getParkingDetails().setValidBasementCarSlots(bsmntParkCount - bsmntFailedCount);
		if (bsmntParkCount > 0)
			if (bsmntFailedCount > 0) {
				if (helper.totalRequiredCarParking.intValue() == pl.getParkingDetails().getValidBasementCarSlots()) {
					setReportOutputDetails(pl, SUB_RULE_40, BSMNT_PARKING_DIM_DESC,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + bsmntParkCount + PARKING + bsmntFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Accepted.getResultVal());
				} else {
					setReportOutputDetails(pl, SUB_RULE_40, BSMNT_PARKING_DIM_DESC,
							PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING,
							OUT_OF + bsmntParkCount + PARKING + bsmntFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Not_Accepted.getResultVal());
				}
			} else {
				setReportOutputDetails(pl, SUB_RULE_40, BSMNT_PARKING_DIM_DESC,
						PARKING_MIN_AREA + MIN_AREA_EACH_CAR_PARKING, NO_VIOLATION_OF_AREA + bsmntParkCount + PARKING,
						Result.Accepted.getResultVal());
			}

	}

	private void checkDimensionForSpecialParking(Plan pl, ParkingHelper helper) {

		int success = 0;
		int specialFailedCount = 0;
		int specialParkCount = pl.getParkingDetails().getSpecial().size();
		for (Measurement spParkSlot : pl.getParkingDetails().getSpecial()) {
			if (spParkSlot.getMinimumSide().doubleValue() >= SP_PARK_SLOT_MIN_SIDE)
				success++;
			else
				specialFailedCount++;
		}
		pl.getParkingDetails().setValidSpecialSlots(specialParkCount - specialFailedCount);
		if (specialParkCount > 0)
			if (specialFailedCount > 0) {
				if (helper.daParking.intValue() == pl.getParkingDetails().getValidSpecialSlots()) {
					setReportOutputDetails(pl, SUB_RULE_40_8, SP_PARKING_SLOT_AREA,
							DA_PARKING_MIN_AREA + MINIMUM_AREA_OF_EACH_DA_PARKING,
							NO_VIOLATION_OF_AREA + pl.getParkingDetails().getValidSpecialSlots() + PARKING,
							Result.Accepted.getResultVal());
				} else {
					setReportOutputDetails(pl, SUB_RULE_40_8, SP_PARKING_SLOT_AREA,
							DA_PARKING_MIN_AREA + MINIMUM_AREA_OF_EACH_DA_PARKING,
							OUT_OF + specialParkCount + PARKING + specialFailedCount + PARKING_VIOLATED_MINIMUM_AREA,
							Result.Not_Accepted.getResultVal());
				}
			} else {
				setReportOutputDetails(pl, SUB_RULE_40_8, SP_PARKING_SLOT_AREA,
						DA_PARKING_MIN_AREA + MINIMUM_AREA_OF_EACH_DA_PARKING,
						NO_VIOLATION_OF_AREA + specialParkCount + PARKING, Result.Accepted.getResultVal());
			}
	}

	private void checkDimensionForTwoWheelerParking(Plan pl, ParkingHelper helper) {
		double providedArea = 0;
		int twoWheelParkingCount = pl.getParkingDetails().getTwoWheelers().size();
		int failedTwoWheelCount = 0;
		helper.twoWheelerParking = BigDecimal.valueOf(0.25 * helper.totalRequiredCarParking * 2.70 * 5.50)
				.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
		if (!pl.getParkingDetails().getTwoWheelers().isEmpty()) {
			for (Measurement m : pl.getParkingDetails().getTwoWheelers()) {
				if (m.getWidth().setScale(2, RoundingMode.UP).doubleValue() < TWO_WHEEL_PARKING_AREA_WIDTH
						|| m.getHeight().setScale(2, RoundingMode.UP).doubleValue() < TWO_WHEEL_PARKING_AREA_HEIGHT)
					failedTwoWheelCount++;

				providedArea = providedArea + m.getArea().doubleValue();
			}
		}

		if (providedArea < helper.twoWheelerParking) {
			setReportOutputDetails(pl, SUB_RULE_34_2, TWO_WHEELER_PARK_AREA,
					helper.twoWheelerParking + SINGLE_SPACE_STRING + DcrConstants.SQMTRS,
					BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + SINGLE_SPACE_STRING
							+ DcrConstants.SQMTRS,
					Result.Not_Accepted.getResultVal());
		} else {
			setReportOutputDetails(pl, SUB_RULE_34_2, TWO_WHEELER_PARK_AREA,
					helper.twoWheelerParking + SINGLE_SPACE_STRING + DcrConstants.SQMTRS,
					BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + SINGLE_SPACE_STRING
							+ DcrConstants.SQMTRS,
					Result.Accepted.getResultVal());
		}

		if (providedArea >= helper.twoWheelerParking && failedTwoWheelCount >= 0) {
			setReportOutputDetails(pl, SUB_RULE_40, TWO_WHEELER_DIM_DESC, PARKING_AREA_DIM,
					OUT_OF + twoWheelParkingCount + PARKING + failedTwoWheelCount + PARKING_VIOLATED_DIM,
					Result.Accepted.getResultVal());
		} else {
			setReportOutputDetails(pl, SUB_RULE_40, TWO_WHEELER_DIM_DESC, PARKING_AREA_DIM,
					OUT_OF + twoWheelParkingCount + PARKING + failedTwoWheelCount + PARKING_VIOLATED_DIM,
					Result.Not_Accepted.getResultVal());
		}
	}

	private BigDecimal getTotalCarpetAreaByOccupancy(Plan pl, String occupancyType) {
	    BigDecimal totalArea = BigDecimal.ZERO;

	    for (Block b : pl.getBlocks()) {
	        for (Occupancy occupancy : b.getBuilding().getTotalArea()) {
	            if (occupancy != null
	                    && occupancy.getTypeHelper() != null
	                   ) {
	                
	                totalArea = totalArea.add(
	                        occupancy.getCarpetArea() == null ? BigDecimal.ZERO : occupancy.getCarpetArea()
	                );
	            }
	        }
	    }

	    return totalArea;
	}


	private void checkAreaForLoadUnloadSpaces(Plan pl) {
		double providedArea = 0;
		BigDecimal totalBuiltupArea = pl.getOccupancies().stream().map(Occupancy::getBuiltUpArea)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		double requiredArea = Math.abs(((totalBuiltupArea.doubleValue() - 700) / 1000) * 30);
		if (!pl.getParkingDetails().getLoadUnload().isEmpty()) {
			for (Measurement m : pl.getParkingDetails().getLoadUnload()) {
				if (m.getArea().compareTo(BigDecimal.valueOf(30)) >= 0)
					providedArea = providedArea + m.getArea().doubleValue();
			}
		}
		if (providedArea < requiredArea) {
			setReportOutputDetails(pl, SUB_RULE_40, LOADING_UNLOADING_AREA,
					requiredArea + SINGLE_SPACE_STRING + DcrConstants.SQMTRS,
					BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + SINGLE_SPACE_STRING
							+ DcrConstants.SQMTRS,
					Result.Not_Accepted.getResultVal());
		} else {
			setReportOutputDetails(pl, SUB_RULE_40, LOADING_UNLOADING_AREA,
					requiredArea + SINGLE_SPACE_STRING + DcrConstants.SQMTRS,
					BigDecimal.valueOf(providedArea).setScale(2, BigDecimal.ROUND_HALF_UP) + SINGLE_SPACE_STRING
							+ DcrConstants.SQMTRS,
					Result.Accepted.getResultVal());
		}
	}

	@Override
	public Map<String, Date> getAmendments() {
		return new LinkedHashMap<>();
	}
}
