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

import static org.egov.edcr.constants.CommonFeatureConstants.AREA;
import static org.egov.edcr.constants.CommonFeatureConstants.COMMA_WIDTH_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.EMPTY_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.FLOOR;
import static org.egov.edcr.constants.CommonFeatureConstants.GREATER_THAN_EQUAL;
import static org.egov.edcr.constants.CommonFeatureConstants.HEIGHT_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.IS_EQUAL_TO;
import static org.egov.edcr.constants.CommonFeatureConstants.REGULAR_ROOM;
import static org.egov.edcr.constants.CommonFeatureConstants.SUB_RULE_DESC_1;
import static org.egov.edcr.constants.CommonFeatureConstants.SUB_RULE_DESC_2;
import static org.egov.edcr.constants.CommonFeatureConstants.SUB_RULE_DESC_3;
import static org.egov.edcr.constants.CommonFeatureConstants.SUB_RULE_DESC_6;
import static org.egov.edcr.constants.CommonFeatureConstants.UNDERSCORE;
import static org.egov.edcr.constants.CommonFeatureConstants.WIDTH_STRING;
import static org.egov.edcr.constants.CommonKeyConstants.BLOCK;
import static org.egov.edcr.constants.CommonKeyConstants.DOORS_AND_WINDOWS;
import static org.egov.edcr.constants.CommonKeyConstants.FLOOR_SPACED;
import static org.egov.edcr.constants.CommonKeyConstants.HEIGHT_OF_ROOM;
import static org.egov.edcr.constants.CommonKeyConstants.IS_TYPICAL_REP_FLOOR;
import static org.egov.edcr.constants.CommonKeyConstants.TYPICAL_FLOOR;
import static org.egov.edcr.constants.DxfFileConstants.A;
import static org.egov.edcr.constants.DxfFileConstants.F;
import static org.egov.edcr.constants.DxfFileConstants.G;
import static org.egov.edcr.constants.EdcrReportConstants.HEIGHT;
import static org.egov.edcr.constants.EdcrReportConstants.LAYER_ROOM_HEIGHT;
import static org.egov.edcr.constants.EdcrReportConstants.MAXIMUM_AREA_46_45;
import static org.egov.edcr.constants.EdcrReportConstants.MINIMUM_AREA_7_5;
import static org.egov.edcr.constants.EdcrReportConstants.MINIMUM_AREA_9_5;
import static org.egov.edcr.constants.EdcrReportConstants.MINIMUM_WIDTH_2_1;
import static org.egov.edcr.constants.EdcrReportConstants.MINIMUM_WIDTH_2_4;
import static org.egov.edcr.constants.EdcrReportConstants.ROOM_HEIGHT_NOTDEFINED;
import static org.egov.edcr.constants.EdcrReportConstants.RULE;
import static org.egov.edcr.constants.EdcrReportConstants.RULE9;
import static org.egov.edcr.constants.EdcrReportConstants.RULEROOMHT;
import static org.egov.edcr.constants.EdcrReportConstants.RULEROOMHTC;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_4_4_4_I;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_AC_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_REGULAR_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_REGULAR_ROOM;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_ROOM_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.SUBRULE_41_II_B;
import static org.egov.edcr.constants.EdcrReportConstants.subRuleDesc4;
import static org.egov.edcr.constants.EdcrReportConstants.subRuleDoor;
import static org.egov.edcr.constants.RuleKeyConstants.FOUR_P_FOUR_P_FOUR;
import static org.egov.edcr.constants.RuleKeyConstants.SIX_FOUR_ONE;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.constants.MdmsFeatureConstants;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.Door;
import org.egov.common.entity.edcr.DoorsRequirement;
import org.egov.common.entity.edcr.FeatureEnum;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.FloorUnit;
import org.egov.common.entity.edcr.MdmsFeatureRule;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.NonHabitationalDoorsRequirement;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.ReportScrutinyDetail;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.Room;
import org.egov.common.entity.edcr.RoomAreaRequirement;
import org.egov.common.entity.edcr.RoomHeight;
import org.egov.common.entity.edcr.RoomWiseDoorAreaRequirement;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.common.entity.edcr.Window;
import org.egov.common.entity.edcr.WindowsRequirement;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.service.ProcessHelper;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HeightOfRoom_Assam extends HeightOfRoom {
	private static final Logger LOG = LogManager.getLogger(HeightOfRoom_Assam.class);
	
	@Override
	public Plan validate(Plan pl) {
		return pl;
	}

	@Autowired
	MDMSCacheManager cache;

	@Override
	public Plan process(Plan pl) {

		Map<String, Integer> heightOfRoomFeaturesColor = pl.getSubFeatureColorCodesMaster().get(HEIGHT_OF_ROOM);
		validate(pl);
		HashMap<String, String> errors = new HashMap<>();
		if (pl != null && pl.getBlocks() != null) {
			OccupancyTypeHelper mostRestrictiveOccupancy = pl.getVirtualBuilding() != null
					? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
					: null;
			 BigDecimal ventilationPercentage = BigDecimal.ZERO;
			String occupancyName = EMPTY_STRING;
			if (mostRestrictiveOccupancy != null && mostRestrictiveOccupancy.getType() != null
					&& (A.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())
							|| (G.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())
									|| F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())))) {
				for (Block block : pl.getBlocks()) {
					if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {
						scrutinyDetail = new ScrutinyDetail();
						scrutinyDetail.addColumnHeading(1, RULE_NO);
						scrutinyDetail.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail.addColumnHeading(3, FLOOR);
						scrutinyDetail.addColumnHeading(4, UNIT);
						scrutinyDetail.addColumnHeading(5, Room);
						scrutinyDetail.addColumnHeading(6, REQUIRED);
						scrutinyDetail.addColumnHeading(7, PROVIDED);
						scrutinyDetail.addColumnHeading(8, STATUS);

						scrutinyDetail.setKey(BLOCK + block.getNumber() + UNDERSCORE + MdmsFeatureConstants.ROOM);

						ScrutinyDetail scrutinyDetail1 = new ScrutinyDetail();
						scrutinyDetail.addColumnHeading(1, RULE_NO);
						scrutinyDetail.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail.addColumnHeading(3, FLOOR);
						scrutinyDetail.addColumnHeading(4, UNIT);
						scrutinyDetail.addColumnHeading(5, Room);
						scrutinyDetail.addColumnHeading(6, REQUIRED);
						scrutinyDetail.addColumnHeading(7, PROVIDED);
						scrutinyDetail.addColumnHeading(8, STATUS);
						scrutinyDetail.setKey(BLOCK + block.getNumber() + UNDERSCORE + MdmsFeatureConstants.ROOM_AREA);

						ScrutinyDetail scrutinyDetail2 = new ScrutinyDetail();
						scrutinyDetail2.addColumnHeading(1, RULE_NO);
						scrutinyDetail2.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail2.addColumnHeading(3, FLOOR);
						scrutinyDetail2.addColumnHeading(4, UNIT);
						scrutinyDetail2.addColumnHeading(5, Room);
						scrutinyDetail2.addColumnHeading(6, REQUIRED);
						scrutinyDetail2.addColumnHeading(7, PROVIDED);
						scrutinyDetail2.addColumnHeading(8, STATUS);
						scrutinyDetail2.setKey(BLOCK + block.getNumber() + UNDERSCORE + MdmsFeatureConstants.DOORS);

						ScrutinyDetail scrutinyDetail5 = new ScrutinyDetail();
						scrutinyDetail5.addColumnHeading(1, RULE_NO);
						scrutinyDetail5.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail5.addColumnHeading(3, FLOOR);
						scrutinyDetail5.addColumnHeading(4, UNIT);
						scrutinyDetail5.addColumnHeading(5, Room);
						scrutinyDetail5.addColumnHeading(6, REQUIRED);
						scrutinyDetail5.addColumnHeading(7, PROVIDED);
						scrutinyDetail5.addColumnHeading(8, STATUS);
						scrutinyDetail5.setKey(
								BLOCK + block.getNumber() + UNDERSCORE + MdmsFeatureConstants.NON_HABITATIONAL_DOORS);

						ScrutinyDetail scrutinyDetail3 = new ScrutinyDetail();
						scrutinyDetail3.addColumnHeading(1, RULE_NO);
						scrutinyDetail3.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail3.addColumnHeading(3, FLOOR);
						scrutinyDetail2.addColumnHeading(4, UNIT);
						scrutinyDetail3.addColumnHeading(5, Room);
						scrutinyDetail3.addColumnHeading(6, REQUIRED);
						scrutinyDetail3.addColumnHeading(7, PROVIDED);
						scrutinyDetail3.addColumnHeading(8, STATUS);
						scrutinyDetail3.setKey(BLOCK + block.getNumber() + UNDERSCORE + MdmsFeatureConstants.WINDOW);

						ScrutinyDetail scrutinyDetail4 = new ScrutinyDetail();
						scrutinyDetail4.addColumnHeading(1, RULE_NO);
						scrutinyDetail4.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail4.addColumnHeading(3, FLOOR);
						scrutinyDetail4.addColumnHeading(4, UNIT);
						scrutinyDetail4.addColumnHeading(5, Room);
						scrutinyDetail4.addColumnHeading(6, REQUIRED);
						scrutinyDetail4.addColumnHeading(7, PROVIDED);
						scrutinyDetail4.addColumnHeading(8, STATUS);
						scrutinyDetail4.setKey(BLOCK + block.getNumber() + UNDERSCORE
								+ MdmsFeatureConstants.ROOM_WISE_VENTILATION + DOORS_AND_WINDOWS);

						ScrutinyDetail scrutinyDetail6 = new ScrutinyDetail();
						scrutinyDetail6.addColumnHeading(1, RULE_NO);
						scrutinyDetail6.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail6.addColumnHeading(3, FLOOR);
						scrutinyDetail6.addColumnHeading(4, UNIT);
						scrutinyDetail6.addColumnHeading(5, Room);
						scrutinyDetail6.addColumnHeading(6, REQUIRED);
						scrutinyDetail6.addColumnHeading(7, PROVIDED);
						scrutinyDetail6.addColumnHeading(8, STATUS);
						scrutinyDetail6.setKey(
								BLOCK + block.getNumber() + UNDERSCORE + MdmsFeatureConstants.ROOM_WISE_WINDOW_AREA);

						ScrutinyDetail scrutinyDetail7 = new ScrutinyDetail();
						scrutinyDetail7.addColumnHeading(1, RULE_NO);
						scrutinyDetail7.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail7.addColumnHeading(3, FLOOR);
						scrutinyDetail7.addColumnHeading(4, UNIT);
						scrutinyDetail7.addColumnHeading(5, Room);
						scrutinyDetail7.addColumnHeading(6, REQUIRED);
						scrutinyDetail7.addColumnHeading(7, PROVIDED);
						scrutinyDetail7.addColumnHeading(8, STATUS);
						scrutinyDetail7
								.setKey(BLOCK + block.getNumber() + UNDERSCORE + MdmsFeatureConstants.DOOR_VENTILATION);

						ScrutinyDetail scrutinyDetail8 = new ScrutinyDetail();
						scrutinyDetail8.addColumnHeading(1, RULE_NO);
						scrutinyDetail8.addColumnHeading(2, DESCRIPTION);
						scrutinyDetail8.addColumnHeading(3, FLOOR);
						scrutinyDetail8.addColumnHeading(4, UNIT);
						scrutinyDetail8.addColumnHeading(5, Room);
						scrutinyDetail8.addColumnHeading(6, REQUIRED);
						scrutinyDetail8.addColumnHeading(7, PROVIDED);
						scrutinyDetail8.addColumnHeading(8, STATUS);
						scrutinyDetail8
								.setKey(BLOCK + block.getNumber() + UNDERSCORE + MdmsFeatureConstants.ROOM_WISE_DOOR_AREA);

						for (Floor floor : block.getBuilding().getFloors()) {
						    // iterate units inside each floor
						    for (FloorUnit unit : floor.getUnits()) {
						        // Your processing logic goes here
						        LOG.debug("Processing Floor [{}], Unit [{}]", floor.getNumber(), unit.getUnitNumber());

						    LOG.info("Processing Floor: {} for Block: {}", floor.getNumber(), block.getName());

						    List<BigDecimal> roomAreas = new ArrayList<>();
						    List<BigDecimal> roomWidths = new ArrayList<>();
						    BigDecimal minimumHeight = BigDecimal.ZERO;
						    BigDecimal totalArea = BigDecimal.ZERO;
						    BigDecimal minWidth = BigDecimal.ZERO;
						    BigDecimal maxArea = BigDecimal.ZERO;
						    String subRule = FOUR_P_FOUR_P_FOUR;
						    String color = EMPTY_STRING;

						    if (A.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode()))
						        color = DxfFileConstants.COLOR_RESIDENTIAL_ROOM;
						    else if (F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode()))
						        color = DxfFileConstants.COLOR_COMMERCIAL_ROOM;
						    else if (G.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode()))
						        color = DxfFileConstants.COLOR_INDUSTRIAL_ROOM;

						    LOG.info("Using room color: {} for Occupancy: {}", color, mostRestrictiveOccupancy.getType().getCode());

						    if (unit.getAcRooms() != null && !unit.getAcRooms().isEmpty()) {
						        LOG.info("Processing AC rooms for Floor: {}", floor.getNumber());
						        processAcRooms(floor, unit, block, color, mostRestrictiveOccupancy, heightOfRoomFeaturesColor,
						                roomAreas, roomWidths, pl, errors);
						    }

						    LOG.info("Processing Regular rooms for Floor: {}", floor.getNumber());
						    processRegularRooms(pl, floor, unit, block, color, mostRestrictiveOccupancy,
						            heightOfRoomFeaturesColor, scrutinyDetail, errors);

						    LOG.info("Processing Non-Inhabitational rooms for Floor: {}", floor.getNumber());
						    processNonInhabitationalRooms(pl, floor, unit, block, color, mostRestrictiveOccupancy,
						            heightOfRoomFeaturesColor, scrutinyDetail8, errors);

						    if (!roomAreas.isEmpty()) {
						        totalArea = roomAreas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
						        BigDecimal minRoomWidth = roomWidths.stream().reduce(BigDecimal::min).get();
						        LOG.info("Floor: {} total room area: {}, minimum room width: {}", floor.getNumber(), totalArea, minRoomWidth);

						        evaluateFirstRoomDetails(pl, floor, unit, roomAreas, roomWidths, subRule, RULE_REGULAR_ROOM,
						                scrutinyDetail);

						        evaluateRemainingRoomDetails(pl, floor, unit, roomAreas, roomWidths, subRule, RULE_REGULAR_ROOM,
						                scrutinyDetail1);
						    }

						    LOG.info("Evaluating doors for Floor: {}", floor.getNumber());
						    evaluateDoorsForFloor(pl, floor, unit, scrutinyDetail2);

						    LOG.info("Evaluating Non-Habitational doors for Floor: {}", floor.getNumber());
						    evaluateNonHabitationalDoors(pl, floor, unit, scrutinyDetail5);

						    LOG.info("Evaluating windows for Floor: {}", floor.getNumber());
						    evaluateWindows(pl, floor, unit, scrutinyDetail3);

						    if (unit.getRegularRooms() != null) {
						        for (Room room : unit.getRegularRooms()) {
						            LOG.info("Evaluating room ventilation for Room: {} on Floor: {}", floor.getNumber());
						            evaluateRoomVentilation(pl, floor, unit, room, scrutinyDetail4, scrutinyDetail6);
						        }
						    }

						    for (Room room : unit.getRegularRooms()) {
						        BigDecimal roomArea = calculateRoomArea(room);
						        BigDecimal requiredVentilationArea = getRequiredVentilationArea(roomArea, ventilationPercentage);
						        BigDecimal totalDoorArea = calculateTotalDoorArea(room);

						        LOG.info("Evaluating door dimensions for Room: {}. RoomArea: {}, RequiredVentArea: {}, TotalDoorArea: {}",
						                roomArea, requiredVentilationArea, totalDoorArea);

						        evaluateDoorDimensions(pl, room, floor, unit, requiredVentilationArea, totalDoorArea, scrutinyDetail8);
						    }

						    LOG.info("Completed processing Floor: {} for Block: {}", floor.getNumber(), block.getName());
						}

					}
				}
				}
			}
		}

		return pl;

	}
	
	/**
	 * Evaluates all doors on a floor for minimum width compliance.
	 *
	 * @param pl              The plan being evaluated.
	 * @param floor           The floor object containing door details.
	 * @param scrutinyDetail  The scrutiny detail to which results are recorded.
	 */
	private void evaluateDoorsForFloor(Plan pl, Floor floor, FloorUnit unit, ScrutinyDetail scrutinyDetail) {
	    if (unit.getDoors() == null || unit.getDoors().isEmpty()) return;

	    for (Door door : unit.getDoors()) {
	        if (door != null) {
	            evaluateSingleDoor(pl, floor, unit, door, scrutinyDetail);
	        }
	    }
	}
	
	/**
	 * Evaluates a single door for compliance with minimum width rules.
	 *
	 * @param pl              The plan being evaluated.
	 * @param floor           The floor the door belongs to.
	 * @param door            The door object to evaluate.
	 * @param scrutinyDetail  The scrutiny detail to record the result.
	 */
	private void evaluateSingleDoor(Plan pl, Floor floor, FloorUnit unit, Door door, ScrutinyDetail scrutinyDetail) {
	    BigDecimal doorWidth = door.getDoorWidth();
	    LOG.info("Evaluating door on Floor: {}. Provided door width: {}", floor.getNumber(), doorWidth);

	    Optional<DoorsRequirement> matchedRule = getMinimumDoorWidth(pl);

	    BigDecimal minDoorWidth = BigDecimal.ZERO;
	    if (matchedRule.isPresent()) {
	        DoorsRequirement rule = matchedRule.get();
	        minDoorWidth = rule.getPermissible();
	        LOG.info("Matched minimum door width from rules: {}", minDoorWidth);
	    } else {
	        LOG.warn("No minimum door width rule found, defaulting to {}", minDoorWidth);
	    }

	    String subRule = SUBRULE_41_II_B;
	    String subRuleDesc = SUBRULE_41_II_B;

	    String requirement = WIDTH_STRING + GREATER_THAN_EQUAL + minDoorWidth;
	    String provided = WIDTH_STRING + IS_EQUAL_TO + doorWidth + DcrConstants.IN_METER;

	    String result = (doorWidth != null && doorWidth.compareTo(minDoorWidth) >= 0)
	            ? Result.Accepted.getResultVal()
	            : Result.Not_Accepted.getResultVal();

	    LOG.info("Door evaluation result: {}. Requirement: {}, Provided: {}", result, requirement, provided);

	    setReportOutputDetails(pl, subRule, subRuleDesc, floor.getNumber().toString(), unit.getUnitNumber(), requirement, provided, EMPTY_STRING, result, scrutinyDetail);
	}


	
	/**
	 * Calculates the total area of a room by summing up areas of its measurements.
	 *
	 * @param room The room object containing measurements.
	 * @return     The total area of the room.
	 */
	private BigDecimal calculateRoomArea(Room room) {
	    BigDecimal roomArea = BigDecimal.ZERO;
	    if (room.getRooms() != null) {
	        for (Measurement measurement : room.getRooms()) {
	            roomArea = roomArea.add(measurement.getArea());
	        }
	    }
	    return roomArea;
	}

	/**
	 * Calculates the required ventilation area based on room area and ventilation percentage.
	 *
	 * @param roomArea              The area of the room.
	 * @param ventilationPercentage The required ventilation percentage.
	 * @return                      The required ventilation area.
	 */
	private BigDecimal getRequiredVentilationArea(BigDecimal roomArea, BigDecimal ventilationPercentage) {
	    return roomArea.multiply(ventilationPercentage)
	            .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * Calculates total area of all doors in a room.
	 *
	 * @param room The room object containing doors.
	 * @return     The total door area.
	 */
	private BigDecimal calculateTotalDoorArea(Room room) {
	    BigDecimal totalDoorArea = BigDecimal.ZERO;
	    if (room.getDoors() != null) {
	        for (Door door : room.getDoors()) {
	            BigDecimal doorArea = door.getDoorHeight().multiply(door.getDoorWidth())
	                    .setScale(2, BigDecimal.ROUND_HALF_UP);
	            totalDoorArea = totalDoorArea.add(doorArea);
	        }
	    }
	    return totalDoorArea;
	}
	
	/**
	 * Evaluates each door in a room for compliance with minimum height and width rules.
	 *
	 * @param pl                    The plan being evaluated.
	 * @param room                  The room containing doors.
	 * @param floor                 The floor the room belongs to.
	 * @param requiredVentilationArea Required ventilation area for the room.
	 * @param totalDoorArea         Total area of all doors in the room.
	 * @param scrutinyDetail8       The scrutiny detail to record the result.
	 */

	private void evaluateDoorDimensions(Plan pl, Room room, Floor floor, FloorUnit unit, BigDecimal requiredVentilationArea,
	        BigDecimal totalDoorArea, ScrutinyDetail scrutinyDetail8) {

	    BigDecimal minDoorWidth = BigDecimal.ZERO;
	    BigDecimal minDoorHeight = BigDecimal.ZERO;

	    for (Door door : room.getDoors()) {
	        BigDecimal doorHeight = door.getDoorHeight().setScale(2, BigDecimal.ROUND_HALF_UP);
	        BigDecimal doorWidth = door.getDoorWidth().setScale(2, BigDecimal.ROUND_HALF_UP);

	        LOG.info("Evaluating door in Room: {}, Floor: {}. Door Width: {}, Door Height: {}",
	                 room.getNumber(), floor.getNumber(), doorWidth, doorHeight);

	        minDoorHeight = BigDecimal.ZERO;
	        minDoorWidth = BigDecimal.ZERO;
	        String subRule = RULE_4_4_4_I;

	        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.ROOM_WISE_DOOR_AREA.getValue(), false);
	        Optional<RoomWiseDoorAreaRequirement> matchedRule = rules.stream()
	            .filter(RoomWiseDoorAreaRequirement.class::isInstance)
	            .map(RoomWiseDoorAreaRequirement.class::cast)
	            .findFirst();

	        if (matchedRule.isPresent()) {
	            RoomWiseDoorAreaRequirement rule = matchedRule.get();
	            minDoorHeight = rule.getMinDoorHeight();
	            minDoorWidth = rule.getMinDoorWidth();
	            LOG.info("Matched minimum door dimensions: Width = {}, Height = {}", minDoorWidth, minDoorHeight);
	        }

	        String result;
	        if (doorHeight.compareTo(minDoorHeight) >= 0 && doorWidth.compareTo(minDoorWidth) >= 0) {
	            result = Result.Accepted.getResultVal();
	        } else {
	            result = Result.Not_Accepted.getResultVal();
	        }

	        LOG.info("Door evaluation result for Room {}: {}", room.getNumber(), result);

	        if (doorHeight.compareTo(minDoorHeight) >= 0 && doorWidth.compareTo(minDoorWidth) >= 0) {
	            setReportOutputDetails(pl, subRuleDoor, subRuleDesc4, floor.getNumber().toString(), unit.getUnitNumber(), room.getNumber(),
	                    HEIGHT + minDoorHeight + COMMA_WIDTH_STRING + GREATER_THAN_EQUAL + minDoorWidth,
	                    HEIGHT_STRING + IS_EQUAL_TO + doorHeight + COMMA_WIDTH_STRING + IS_EQUAL_TO + doorWidth,
	                    result, scrutinyDetail8);
	        } else {
	            setReportOutputDetails(pl, subRule, subRuleDesc4, floor.getNumber().toString(), unit.getUnitNumber(), room.getNumber(),
	                    HEIGHT + minDoorHeight + COMMA_WIDTH_STRING + GREATER_THAN_EQUAL + minDoorWidth,
	                    HEIGHT_STRING + IS_EQUAL_TO + doorHeight + COMMA_WIDTH_STRING + IS_EQUAL_TO + doorWidth,
	                    result, scrutinyDetail8);
	        }
	    }
	}


	/**
	 * Fetches the minimum permissible door width from feature rules.
	 *
	 * @param pl The plan being evaluated.
	 * @return   The minimum permissible door width.
	 */
	private Optional<DoorsRequirement> getMinimumDoorWidth(Plan pl) {
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.DOORS.getValue(), false);
	    LOG.info("Fetching minimum door width rules from MDMS. Total rules found: {}", rules.size());

	    Optional<DoorsRequirement> result = rules.stream()
	        .filter(DoorsRequirement.class::isInstance)
	        .map(DoorsRequirement.class::cast)
	        .findFirst();

	    result.ifPresent(rule -> LOG.info("Matched DoorsRequirement with permissible width: {}", rule.getPermissible()));

	    return result;
	}
	
	
	private Optional<WindowsRequirement> getMinimumWidowsRequirement(Plan pl) {
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.WINDOWS.getValue(), false);
	    LOG.info("Fetching minimum door width rules from MDMS. Total rules found: {}", rules.size());

	    Optional<WindowsRequirement> result = rules.stream()
	        .filter(WindowsRequirement.class::isInstance)
	        .map(WindowsRequirement.class::cast)
	        .findFirst();

	    result.ifPresent(rule -> LOG.info("Matched WindowsRequirement with permissible width: {}", rule.getMinWindowWidth()));

	    return result;
	}

	/**
	 * Evaluates all windows on a floor for basic dimension compliance.
	 *
	 * @param pl              The plan being evaluated.
	 * @param floor           The floor containing windows.
	 * @param scrutinyDetail  The scrutiny detail to record the results.
	 */
	private void evaluateWindows(Plan pl, Floor floor, FloorUnit unit, ScrutinyDetail scrutinyDetail) {
	    if (unit.getWindows() == null || unit.getWindows().isEmpty()) return;

	    for (Window window : unit.getWindows()) {
	        if (window != null) {
	            evaluateSingleWindow(pl, floor, unit, window, scrutinyDetail);
	        }
	    }
	}
	
	/**
	 * Evaluates room ventilation by calculating required ventilation and comparing with actual provided area.
	 * Also validates individual window dimensions.
	 *
	 * @param pl               The plan being evaluated.
	 * @param floor            The floor containing the room.
	 * @param room             The room to be evaluated.
	 * @param ventilationDetail Scrutiny detail for ventilation compliance.
	 * @param windowDetail      Scrutiny detail for individual window checks.
	 */
	private void evaluateRoomVentilation(Plan pl, Floor floor, FloorUnit unit, Room room, ScrutinyDetail ventilationDetail,
	        ScrutinyDetail windowDetail) {

	    BigDecimal roomArea = calculateRoomArea(room);
	    BigDecimal ventilationPercentage = getVentilationPercentage(pl);
	    BigDecimal requiredVentilationArea = roomArea.multiply(ventilationPercentage).divide(BigDecimal.valueOf(100))
	            .setScale(2, BigDecimal.ROUND_HALF_UP);
	    String subRuleDesc1 = SUB_RULE_DESC_1;

	    BigDecimal totalWindowArea = calculateWindowArea(room);
	  //  BigDecimal totalDoorArea = calculateDoorArea(room);
	   // BigDecimal combinedArea = totalWindowArea.add(totalDoorArea);

	    LOG.info("Evaluating ventilation for Room: {}, Floor: {}. RoomArea: {}, RequiredVentilationArea: {}, TotalWindowArea: {}",
	            room.getNumber(), floor.getNumber(), roomArea, requiredVentilationArea, totalWindowArea);

	    if (requiredVentilationArea.compareTo(BigDecimal.ZERO) != 0 && totalWindowArea.compareTo(BigDecimal.ZERO) != 0) {
	        String result = totalWindowArea.compareTo(requiredVentilationArea) >= 0 ? Result.Accepted.getResultVal()
	                : Result.Not_Accepted.getResultVal();

	        setReportOutputDetails(pl, RULE9, subRuleDesc1, floor.getNumber().toString(), unit.getUnitNumber(), EMPTY_STRING + room.getNumber(),
	                VENTILATION_REQUIRED + GREATER_THAN_EQUAL + requiredVentilationArea, AREA_PROVIDED + totalWindowArea, result,
	                ventilationDetail);
	    }

	    validateIndividualWindows(pl, floor, unit, room, windowDetail);
	}


	/**
	 * Retrieves the ventilation percentage required from the feature rules.
	 *
	 * @param pl The plan being evaluated.
	 * @return   The ventilation percentage.
	 */
	private BigDecimal getVentilationPercentage(Plan pl) {
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.ROOM_WISE_VENTILATION.getValue(), false);
	    Optional<MdmsFeatureRule> matchedRule = rules.stream()
	            .map(obj -> (MdmsFeatureRule) obj)
	            .findFirst();

	    BigDecimal ventilationPercentage = matchedRule.map(MdmsFeatureRule::getPermissible).orElse(BigDecimal.ZERO);
	    LOG.info("Ventilation percentage for plan", ventilationPercentage);
	    return ventilationPercentage;
	}

	
	/**
	 * Calculates the total area of all windows in a room.
	 *
	 * @param room The room object containing windows.
	 * @return     The total window area.
	 */
	private BigDecimal calculateWindowArea(Room room) {
	    BigDecimal area = BigDecimal.ZERO;
	    if (room.getWindows() != null) {
	        for (Window window : room.getWindows()) {
	            BigDecimal height = window.getWindowHeight();
	            BigDecimal width = window.getWindowWidth();
	            BigDecimal windowArea = height.multiply(width).setScale(2, BigDecimal.ROUND_HALF_UP);
	            area = area.add(windowArea);
	            LOG.info("Window {}x{} m contributes area: {} m²", height, width, windowArea);
	        }
	    }
	    LOG.info("Total window area for Room {}: {} m²", room.getNumber(), area);
	    return area;
	}

	/**
	 * Calculates the total area of all doors in a room.
	 *
	 * @param room The room object containing doors.
	 * @return     The total door area.
	 */
	private BigDecimal calculateDoorArea(Room room) {
	    BigDecimal area = BigDecimal.ZERO;
	    if (room.getDoors() != null) {
	        for (Door door : room.getDoors()) {
	            BigDecimal height = door.getDoorHeight();
	            BigDecimal width = door.getDoorWidth();
	            BigDecimal doorArea = height.multiply(width).setScale(2, BigDecimal.ROUND_HALF_UP);
	            area = area.add(doorArea);
	            LOG.info("Door {}x{} m contributes area: {} m²", height, width, doorArea);
	        }
	    }
	    LOG.info("Total door area for Room {}: {} m²", room.getNumber(), area);
	    return area;
	}


	
	/**
	 * Validates individual window dimensions in a room.
	 *
	 * @param pl              The plan being evaluated.
	 * @param floor           The floor containing the room.
	 * @param room            The room to evaluate.
	 * @param scrutinyDetail  The scrutiny detail to record the result.
	 */
	private void validateIndividualWindows(Plan pl, Floor floor, FloorUnit unit, Room room, ScrutinyDetail scrutinyDetail) {
	    if (room.getWindows() == null)
	        return;
	    String subRuleDesc2 = SUB_RULE_DESC_2;
	    String subRule = RULE_4_4_4_I;

	    for (Window window : room.getWindows()) {
	        BigDecimal height = window.getWindowHeight().setScale(2, BigDecimal.ROUND_HALF_UP);
	        BigDecimal width = window.getWindowWidth().setScale(2, BigDecimal.ROUND_HALF_UP);

	        LOG.info("Validating window for Room: {}, Floor: {} - Height: {}, Width: {}", 
	                 room.getNumber(), floor.getNumber(), height, width);

	        setReportOutputDetails(pl, subRule, subRuleDesc2, floor.getNumber().toString(), unit.getUnitNumber(), room.getNumber(), EMPTY_STRING,
	                HEIGHT_STRING + IS_EQUAL_TO + height + COMMA_WIDTH_STRING + IS_EQUAL_TO + width, Result.Accepted.getResultVal(), scrutinyDetail);
	    }
	}



	/**
	 * Evaluates a single window for minimum height and width compliance.
	 *
	 * @param pl              The plan being evaluated.
	 * @param floor           The floor the window belongs to.
	 * @param window          The window to evaluate.
	 * @param scrutinyDetail  The scrutiny detail to record the result.
	 */
	private void evaluateSingleWindow(Plan pl, Floor floor, FloorUnit unit, Window window, ScrutinyDetail scrutinyDetail) {
	    BigDecimal windowHeight = window.getWindowHeight().setScale(2, BigDecimal.ROUND_HALF_UP);
	    BigDecimal windowWidth = window.getWindowWidth().setScale(2, BigDecimal.ROUND_HALF_UP);

	    BigDecimal minWindowHeight = BigDecimal.ZERO;
	    BigDecimal minWindowWidth = BigDecimal.ZERO;
	    
	    Optional<WindowsRequirement> matchedRule = getMinimumWidowsRequirement(pl);
	    
	    BigDecimal minDoorWidth = BigDecimal.ZERO;
	    if (matchedRule.isPresent()) {
	        WindowsRequirement rule = matchedRule.get();
	        minWindowWidth = rule.getMinWindowWidth();
	        minWindowHeight = rule.getMinWindowHeight();
	        LOG.info("Matched minimum door width from rules: {}", minDoorWidth);
	    } else {
	        LOG.warn("No minimum door width rule found, defaulting to {}", minDoorWidth);
	    }

	    LOG.info("Evaluating single window on Floor: {} - Height: {}, Width: {}, MinHeight: {}, MinWidth: {}",
	             floor.getNumber(), windowHeight, windowWidth, minWindowHeight, minWindowWidth);

	    String subRuleDesc3 = SUB_RULE_DESC_3;
	    
	    String requirement = "-";  
	    
	    String provided = HEIGHT_STRING + IS_EQUAL_TO + windowHeight + COMMA_WIDTH_STRING + IS_EQUAL_TO + windowWidth;

	    String result = Result.Accepted.getResultVal();
	    setReportOutputDetails(pl, EMPTY_STRING, subRuleDesc3, floor.getNumber().toString(),
	            unit.getUnitNumber(), "-", requirement, provided, result, scrutinyDetail);
	}


	/**
	 * Evaluates the first room in a list for compliance with area and width rules.
	 *
	 * @param pl              The plan being evaluated.
	 * @param floor           The floor the room belongs to.
	 * @param roomAreas       List of room areas on the floor.
	 * @param roomWidths      List of room widths on the floor.
	 * @param subRule         The subrule reference.
	 * @param subRuleDesc     The subrule description.
	 * @param scrutinyDetail  The scrutiny detail to record the result.
	 */
	private void evaluateFirstRoomDetails(Plan pl, Floor floor, FloorUnit unit, List<BigDecimal> roomAreas, List<BigDecimal> roomWidths,
	        String subRule, String subRuleDesc, ScrutinyDetail scrutinyDetail) {

	    if (roomAreas.size() >= 1) {
	        BigDecimal minimumArea = MINIMUM_AREA_9_5;
	        BigDecimal minWidth = MINIMUM_WIDTH_2_4;
	        BigDecimal maxArea = MAXIMUM_AREA_46_45;

	        BigDecimal area = roomAreas.get(0);
	        BigDecimal width = roomWidths.get(0);

	        LOG.info("Evaluating first room: Area = {}, Width = {}, Minimum Area = {}, Minimum Width = {}, Max Area = {}", 
	                  area, width, minimumArea, minWidth, maxArea);

	        String requirement = AREA + GREATER_THAN_EQUAL + minimumArea + COMMA_WIDTH_STRING + GREATER_THAN_EQUAL + minWidth;
	        String provided = AREA + IS_EQUAL_TO + area + COMMA_WIDTH_STRING + IS_EQUAL_TO + width + DcrConstants.IN_METER;

	        String result = (area.compareTo(maxArea) <= 0 && area.compareTo(minimumArea) >= 0
	                && width.compareTo(minWidth) >= 0) ? Result.Accepted.getResultVal()
	                        : Result.Not_Accepted.getResultVal();

	        LOG.info("First room evaluation result: {}", result);

	        setReportOutputDetails(pl, subRule, subRuleDesc, floor.getNumber().toString(), unit.getUnitNumber(), EMPTY_STRING, requirement, provided,
	                result, scrutinyDetail);
	    }
	}


	/**
	 * Evaluates remaining room details (other than the first room) on a floor.
	 * Checks if area and width are within permissible limits and records results in scrutiny.
	 *
	 * @param pl              the Plan object being validated
	 * @param floor           the floor containing the rooms
	 * @param roomAreas       list of room areas
	 * @param roomWidths      list of room widths
	 * @param subRule         sub-rule number being applied
	 * @param subRuleDesc     description of the sub-rule
	 * @param scrutinyDetail  scrutiny details where results are to be recorded
	 */
	private void evaluateRemainingRoomDetails(Plan pl, Floor floor, FloorUnit unit,  List<BigDecimal> roomAreas,
	        List<BigDecimal> roomWidths, String subRule, String subRuleDesc, ScrutinyDetail scrutinyDetail) {

	    BigDecimal minimumArea = MINIMUM_AREA_7_5;
	    BigDecimal minWidth = MINIMUM_WIDTH_2_1;
	    BigDecimal maxArea = MAXIMUM_AREA_46_45;

	    for (int i = 1; i < roomAreas.size(); i++) {
	        BigDecimal area = roomAreas.get(i);
	        BigDecimal width = roomWidths.get(i);

	        LOG.info("Evaluating remaining room {} on Floor: {} - Area: {}, Width: {}, MinArea: {}, MinWidth: {}, MaxArea: {}",
	                i, floor.getNumber(), area, width, minimumArea, minWidth, maxArea);

	        String requirement = AREA + GREATER_THAN_EQUAL + minimumArea + COMMA_WIDTH_STRING + GREATER_THAN_EQUAL + minWidth;
	        String provided = AREA + IS_EQUAL_TO + area + COMMA_WIDTH_STRING + IS_EQUAL_TO + width + DcrConstants.IN_METER;

	        String result = (area.compareTo(maxArea) <= 0 && area.compareTo(minimumArea) >= 0
	                && width.compareTo(minWidth) >= 0) ? Result.Accepted.getResultVal()
	                        : Result.Not_Accepted.getResultVal();

	        setReportOutputDetails(pl, subRule, subRuleDesc, floor.getNumber().toString(), unit.getUnitNumber(), EMPTY_STRING, requirement, provided,
	                result, scrutinyDetail);
	    }
	}

	
	/**
	 * Evaluates non-habitational doors present on the given floor.
	 * Delegates evaluation to a method for each individual door.
	 *
	 * @param pl              the Plan object
	 * @param floor           the floor being evaluated
	 * @param scrutinyDetail  scrutiny object to collect validation results
	 */
	private void evaluateNonHabitationalDoors(Plan pl, Floor floor, FloorUnit unit, ScrutinyDetail scrutinyDetail) {
	    if (unit.getNonaHabitationalDoors() == null || unit.getNonaHabitationalDoors().isEmpty()) return;

	    for (Door door : unit.getNonaHabitationalDoors()) {
	        if (door != null) {
	            evaluateSingleNonHabitationalDoor(pl, floor, unit, door, scrutinyDetail);
	        }
	    }
	}

	/**
	 * Evaluates a single non-habitational door against minimum width and height.
	 * The requirements are fetched from MDMS feature rules.
	 *
	 * @param pl              the Plan object
	 * @param floor           the floor containing the door
	 * @param door            the Door object to evaluate
	 * @param scrutinyDetail  scrutiny object to record the results
	 */
	private void evaluateSingleNonHabitationalDoor(Plan pl, Floor floor, FloorUnit unit, Door door, ScrutinyDetail scrutinyDetail) {
	    BigDecimal doorHeight = door.getNonHabitationDoorHeight().setScale(2, BigDecimal.ROUND_HALF_UP);
	    BigDecimal doorWidth = door.getNonHabitationDoorWidth().setScale(2, BigDecimal.ROUND_HALF_UP);

	    BigDecimal minDoorWidth = BigDecimal.ZERO;
	    BigDecimal minDoorHeight = BigDecimal.ZERO;

	    List<Object> rules = cache.getFeatureRules(pl, MdmsFeatureConstants.NON_HABITATIONAL_DOORS, false);
	    Optional<NonHabitationalDoorsRequirement> matchedRule = rules.stream()
	        .filter(NonHabitationalDoorsRequirement.class::isInstance)
	        .map(NonHabitationalDoorsRequirement.class::cast)
	        .findFirst();

	    if (matchedRule.isPresent()) {
	        NonHabitationalDoorsRequirement rule = matchedRule.get();
	        minDoorWidth = rule.getMinDoorWidth();
	        minDoorHeight = rule.getMinDoorHeight();
	    }

	    LOG.info("Evaluating Non-Habitational Door on Floor: {} - DoorHeight: {}, DoorWidth: {}, MinDoorHeight: {}, MinDoorWidth: {}",
	            floor.getNumber(), doorHeight, doorWidth, minDoorHeight, minDoorWidth);

	    String subRuleDoor = SIX_FOUR_ONE;
	    String subRuleDesc6 = SUB_RULE_DESC_6;

	    String requirement = HEIGHT + minDoorHeight + COMMA_WIDTH_STRING + GREATER_THAN_EQUAL + minDoorWidth;
	    String provided = HEIGHT_STRING + IS_EQUAL_TO + doorHeight + COMMA_WIDTH_STRING + IS_EQUAL_TO + doorWidth;

	    String result = (doorHeight.compareTo(minDoorHeight) >= 0 && doorWidth.compareTo(minDoorWidth) >= 0)
	            ? Result.Accepted.getResultVal()
	            : Result.Not_Accepted.getResultVal();

	    setReportOutputDetails(pl, subRuleDoor, subRuleDesc6, floor.getNumber().toString(), unit.getUnitNumber(),  "-", requirement, provided, result, scrutinyDetail);
	}

	/**
	 * Processes AC rooms on the floor by collecting their height and area details.
	 * Validates the minimum height based on occupancy type and stores results or errors.
	 *
	 * @param floor                      the floor to process
	 * @param block                      the block to which the floor belongs
	 * @param color                      color code used for identification
	 * @param mostRestrictiveOccupancy  most restrictive occupancy type
	 * @param heightOfRoomFeaturesColor map linking feature types to color codes
	 * @param roomAreas                  list to store collected room areas
	 * @param roomWidths                 list to store collected room widths
	 * @param pl                         the Plan object
	 * @param errors                     map to record any processing errors
	 */
	private void processAcRooms(Floor floor, FloorUnit unit,  Block block, String color, OccupancyTypeHelper mostRestrictiveOccupancy,
	        Map<String, Integer> heightOfRoomFeaturesColor, List<BigDecimal> roomAreas, List<BigDecimal> roomWidths,
	        Plan pl, Map<String, String> errors) {

	    LOG.info("Processing AC Rooms for Floor: {}, Block: {}, Unit: {}, Color: {}", floor.getNumber(), unit.getUnitNumber(), block.getNumber(), unit.getUnitNumber(), color);

	    List<BigDecimal> residentialAcRoomHeights = new ArrayList<>();
	    List<RoomHeight> acHeights = new ArrayList<>();
	    List<Measurement> acRooms = new ArrayList<>();

	    BigDecimal acRoomHeight = BigDecimal.ZERO;
	    BigDecimal commercialACRoomHeight = BigDecimal.ZERO;

	    // Collect all AC room heights and measurements
		/*
		 * for (Room room : floor.getAcRooms()) { if (room.getHeights() != null) {
		 * acHeights.addAll(room.getHeights()); } if (room.getRooms() != null) {
		 * acRooms.addAll(room.getRooms()); } }
		 */
	    
	    for (Room room : unit.getAcRooms()) {
	        if (room.getHeights() != null) {
	            acHeights.addAll(room.getHeights());
	        }
	        if (room.getRooms() != null) {
	            acRooms.addAll(room.getRooms());
	        }
	    }

	    for (RoomHeight roomHeight : acHeights) {
	        if (heightOfRoomFeaturesColor.get(color) == roomHeight.getColorCode()) {
	            residentialAcRoomHeights.add(roomHeight.getHeight());
	        }
	    }

	    for (Measurement acRoom : acRooms) {
	        if (heightOfRoomFeaturesColor.get(color) == acRoom.getColorCode()) {
	            roomAreas.add(acRoom.getArea());
	            roomWidths.add(acRoom.getWidth());
	        }
	    }

	    LOG.info("Collected Residential AC Room Heights: {}", residentialAcRoomHeights);
	    LOG.info("Collected AC Room Areas: {}, Room Widths: {}", roomAreas, roomWidths);

	    // Fetch rules
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.ROOM_AREA.getValue(), false);
	    Optional<RoomAreaRequirement> matchedRule = rules.stream()
	            .filter(RoomAreaRequirement.class::isInstance)
	            .map(RoomAreaRequirement.class::cast)
	            .findFirst();

	    if (matchedRule.isPresent()) {
	        RoomAreaRequirement rule = matchedRule.get();
	        acRoomHeight = rule.getAcRoomHeight();
	        commercialACRoomHeight = rule.getCommercialACroomHeight();
	    }

	    LOG.info("Fetched AC Room Heights from Rules - Residential: {}, Commercial: {}", acRoomHeight, commercialACRoomHeight);

	    if (!residentialAcRoomHeights.isEmpty()) {
	        BigDecimal minHeight = residentialAcRoomHeights.stream().reduce(BigDecimal::min).get();
	        BigDecimal minimumHeight;

	        if (mostRestrictiveOccupancy != null && mostRestrictiveOccupancy.getType() != null
	                && DxfFileConstants.F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {
	            minimumHeight = commercialACRoomHeight;
	        } else {
	            minimumHeight = acRoomHeight;
	        }

	        LOG.info("Minimum AC Room Height determined: {}", minimumHeight);

	        String subRule = RULE;
	        String subRuleDesc = RULE_AC_DESC;
	        boolean valid = false;
	        boolean isTypicalRepititiveFloor = false;

	        Map<String, Object> typicalFloorValues = ProcessHelper.getTypicalFloorValues(block, floor,
	                isTypicalRepititiveFloor);

	        buildResult(pl, floor, unit, minimumHeight, subRule, subRuleDesc, minHeight, valid, typicalFloorValues);
	        buildRoomHeightResult(pl, floor, unit, block, mostRestrictiveOccupancy, residentialAcRoomHeights, heightOfRoomFeaturesColor,
	        		color, errors);

	    } else {
	        String layerName = String.format(LAYER_ROOM_HEIGHT, block.getNumber(), floor.getNumber(), "AC_ROOM");
	        errors.put(layerName, ROOM_HEIGHT_NOTDEFINED + layerName);
	        LOG.warn("AC Room Heights not defined for layer: {}", layerName);
	        pl.addErrors(errors);
	    }
	}


	
	/**
	 * Processes Nonhabitational rooms on the floor by collecting their height and area details.
	 * Validates the minimum height based on occupancy type and stores results or errors.
	 *
	 * @param floor                      the floor to process
	 * @param block                      the block to which the floor belongs
	 * @param color                      color code used for identification
	 * @param mostRestrictiveOccupancy  most restrictive occupancy type
	 * @param heightOfRoomFeaturesColor map linking feature types to color codes
	 * @param roomAreas                  list to store collected room areas
	 * @param roomWidths                 list to store collected room widths
	 * @param pl                         the Plan object
	 * @param errors                     map to record any processing errors
	 */
	
	private void processNonInhabitationalRooms(Plan pl, Floor floor, FloorUnit unit, Block block, String color, OccupancyTypeHelper mostRestrictiveOccupancy,
	        Map<String, Integer> heightOfRoomFeaturesColor, ScrutinyDetail scrutinyDetail, Map<String, String> errors) {

	    if (unit.getNonInhabitationalRooms() == null || unit.getNonInhabitationalRooms().isEmpty())
	        return;

	    LOG.info("Processing Non-Inhabitational Rooms for Floor: {}, Unit: {}, Block: {}, Color: {}", floor.getNumber(), unit.getUnitNumber(), block.getNumber(), color);

	    List<BigDecimal> residentialRoomHeights = new ArrayList<>();
	    List<RoomHeight> heights = new ArrayList<>();
	    List<Measurement> rooms = new ArrayList<>();

	   // collectHeightsAndRoomsFromNonInhabitationalRooms(floor, unit, heights, rooms);
	    populateRoomNumberForMeasurements(unit.getNonInhabitationalRooms());

	    LOG.info("Collected Heights: {}, Rooms: {}", heights, rooms);

		/*
		 * for (RoomHeight roomHeight : heights) { if
		 * (heightOfRoomFeaturesColor.get(color) == roomHeight.getColorCode()) {
		 * residentialRoomHeights.add(roomHeight.getHeight()); } }
		 */

	    buildRoomHeightResult(pl, floor, unit, block, mostRestrictiveOccupancy, unit.getCommonHeight(),
	    		   heightOfRoomFeaturesColor, color, errors);
		   
	    boolean roomArea2Satisfied = false;
	    BigDecimal roomArea1 = BigDecimal.ZERO, roomArea2 = BigDecimal.ZERO;
	    BigDecimal roomWidth1 = BigDecimal.ZERO, roomWidth2 = BigDecimal.ZERO;

	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.ROOM_AREA.getValue(), false);
	    Optional<RoomAreaRequirement> matchedRule = rules.stream()
	        .filter(RoomAreaRequirement.class::isInstance)
	        .map(RoomAreaRequirement.class::cast)
	        .findFirst();

	    if (matchedRule.isPresent()) {
	        RoomAreaRequirement rule = matchedRule.get();
	        roomArea1 = rule.getNonInhabitationalRoomArea();
	        roomArea2 = rule.getRoomArea2();
	        roomWidth1 = rule.getRoomWidth1();
	        roomWidth2 = rule.getRoomWidth2();

	        LOG.info("Fetched Non-Inhabitational Room Rules - Area1: {}, Area2: {}, Width1: {}, Width2: {}", roomArea1, roomArea2, roomWidth1, roomWidth2);

	        // Validate minimum area and width per room
	        for (Measurement room : rooms) {
	            BigDecimal roomArea = room.getArea().setScale(2, BigDecimal.ROUND_HALF_UP);
	            BigDecimal roomWidth = room.getWidth().setScale(2, BigDecimal.ROUND_HALF_UP);

	            BigDecimal minArea = roomArea1;
	            BigDecimal minWidth = roomWidth2;

	            LOG.info("Validating Non-Inhabitational Room: {} with Area: {}, Width: {}, MinArea: {}, MinWidth: {}", room.getRoomNumber(), roomArea, roomWidth, minArea, minWidth);

	            validateAndReportNonInhabitattionalRoom(pl, floor, unit, room, scrutinyDetail, minArea, minWidth, roomArea, roomWidth);
	        }
	    }
	}


	/**
	 * Processes regular rooms on a floor by validating their area, width, and height
	 * against rules from MDMS. Builds scrutiny results accordingly.
	 *
	 * @param pl                         the Plan object
	 * @param floor                      the floor containing the rooms
	 * @param block                      block that includes the floor
	 * @param color                      color code used to identify elements
	 * @param mostRestrictiveOccupancy  occupancy type used for validation
	 * @param heightOfRoomFeaturesColor map of feature type to color codes
	 * @param scrutinyDetail             scrutiny detail to store validation output
	 * @param errors                     map to record processing errors
	 */
	private void processRegularRooms(Plan pl, Floor floor,  FloorUnit unit, Block block, String color, OccupancyTypeHelper mostRestrictiveOccupancy,
	        Map<String, Integer> heightOfRoomFeaturesColor, ScrutinyDetail scrutinyDetail, Map<String, String> errors) {

//	    if (floor.getRegularRooms() == null || floor.getRegularRooms().isEmpty())
//	        return;
	    
	    if (unit.getRegularRooms() == null || unit.getRegularRooms().isEmpty())
	        return;

	    LOG.info("Processing Regular Rooms for Floor: {}, Block: {}, Color: {}", floor.getNumber(), block.getNumber(), unit.getUnitNumber(), color);

	    List<BigDecimal> residentialRoomHeights = new ArrayList<>();
	    List<RoomHeight> heights = new ArrayList<>();
	    List<Measurement> rooms = new ArrayList<>();

	   // collectHeightsAndRoomsFromRegularRooms(floor, unit, heights, rooms);
	    populateRoomNumberForMeasurements(unit.getRegularRooms());

	    LOG.info("Collected Heights: {}, Rooms: {}", heights, rooms);

		/*
		 * for (RoomHeight roomHeight : heights) { if
		 * (heightOfRoomFeaturesColor.get(color) == roomHeight.getColorCode()) {
		 * residentialRoomHeights.add(roomHeight.getHeight()); } }
		 */
	    

       buildRoomHeightResult(pl, floor, unit, block, mostRestrictiveOccupancy, unit.getCommonHeight(),
    		   heightOfRoomFeaturesColor, color, errors);
	    LOG.info("Filtered Residential Regular Room Heights: {}", residentialRoomHeights);

	    boolean roomArea2Satisfied = false;
	    BigDecimal roomArea1 = BigDecimal.ZERO, roomArea2 = BigDecimal.ZERO;
	    BigDecimal roomWidth1 = BigDecimal.ZERO, roomWidth2 = BigDecimal.ZERO;

	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.ROOM_AREA.getValue(), false);
	    Optional<RoomAreaRequirement> matchedRule = rules.stream()
	        .filter(RoomAreaRequirement.class::isInstance)
	        .map(RoomAreaRequirement.class::cast)
	        .findFirst();

	    if (matchedRule.isPresent()) {
	        RoomAreaRequirement rule = matchedRule.get();
	        roomArea1 = rule.getRoomArea1();
	        roomArea2 = rule.getRoomArea2();
	        roomWidth1 = rule.getRoomWidth1();
	        roomWidth2 = rule.getRoomWidth2();

	        LOG.info("Fetched Regular Room Rules - Area1: {}, Area2: {}, Width1: {}, Width2: {}", roomArea1, roomArea2, roomWidth1, roomWidth2);
	    }

	  
	 // Validate minimum area and width per room
	   // for (Room regularRoom : floor.getRegularRooms()) {
	      for (Room regularRoom : unit.getRegularRooms()) {
	        for (Measurement room : regularRoom.getRooms()) {
	            BigDecimal roomArea = room.getArea().setScale(2, BigDecimal.ROUND_HALF_UP);

	            // Default to ZERO if width not present
	            BigDecimal roomWidth = BigDecimal.ZERO;

	            // Pick width from Room
	            if (regularRoom.getRoomWidth() != null && !regularRoom.getRoomWidth().isEmpty()) {
	                // Take the first width (or loop if multiple widths are expected)
	                roomWidth = regularRoom.getRoomWidth().get(0).setScale(2, BigDecimal.ROUND_HALF_UP);
	            }

	            BigDecimal minArea = roomArea1;
	            BigDecimal minWidth = roomWidth2;

	            LOG.info("Validating Regular Room: {} with Area: {}, Width(from Room): {}, MinArea: {}, MinWidth: {}",
	                    room.getRoomNumber(), roomArea, roomWidth, minArea, minWidth);

	            validateAndReportRoom(pl, floor, unit, room, scrutinyDetail, minArea, minWidth, roomArea, roomWidth);
	        }
	      }
	    }



	/**
	 * Collects room heights and room measurements from regular rooms on the floor.
	 *
	 * @param floor  the floor containing regular rooms
	 * @param heights output list to collect RoomHeight objects
	 * @param rooms   output list to collect Measurement objects (room area/width)
	 */
	/*
	 * private void collectHeightsAndRoomsFromRegularRooms(Floor floor, FloorUnit
	 * unit, List<RoomHeight> heights, List<Measurement> rooms) {
	 * 
	 * if (room.getHeights() != null) heights.addAll(room.getHeights()); if
	 * (room.getRooms() != null) rooms.addAll(room.getRooms());
	 * 
	 * }
	 */

	/**
	 * Populates room numbers for each Measurement object inside rooms.
	 *
	 * @param rooms list of Room objects whose measurements will be updated
	 */
	private void populateRoomNumberForMeasurements(List<Room> rooms) {
		for (Room room : rooms) {
			for (Measurement m : room.getRooms()) {
				m.setRoomNumber(room.getNumber());
			}
		}
	}

	
	/**
	 * Collects room heights and room measurements from regular rooms on the floor.
	 *
	 * @param floor  the floor containing regular rooms
	 * @param heights output list to collect RoomHeight objects
	 * @param rooms   output list to collect Measurement objects (room area/width)
	 */
	/*
	 * private void collectHeightsAndRoomsFromNonInhabitationalRooms(Floor floor,
	 * FloorUnit unit, List<RoomHeight> heights, List<Measurement> rooms) { for
	 * (Room room : unit.getNonInhabitationalRooms()) { if (room.getHeights() !=
	 * null) heights.addAll(room.getHeights()); if (room.getRooms() != null)
	 * rooms.addAll(room.getRooms()); } }
	 */
	
	/**
	 * Validates a room against minimum area and width requirements and records result.
	 *
	 * @param pl             the Plan object
	 * @param floor          the floor containing the room
	 * @param room           the Measurement object representing the room
	 * @param scrutinyDetail object used to record results
	 * @param minArea        minimum permissible area
	 * @param minWidth       minimum permissible width
	 * @param roomArea       actual room area
	 * @param roomWidth      actual room width
	 */
	private void validateAndReportRoom(Plan pl, Floor floor,  FloorUnit unit, Measurement room, ScrutinyDetail scrutinyDetail,
			BigDecimal minArea, BigDecimal minWidth, BigDecimal roomArea, BigDecimal roomWidth) {

		String result = (roomArea.compareTo(minArea) >= 0 && roomWidth.compareTo(minWidth) >= 0)
				? Result.Accepted.getResultVal()
				: Result.Not_Accepted.getResultVal();

		setReportOutputDetails(pl, RULE_4_4_4_I, RULE_REGULAR_ROOM, floor.getNumber().toString(), unit.getUnitNumber(),
				String.valueOf(room.getRoomNumber()), AREA + GREATER_THAN_EQUAL + minArea + COMMA_WIDTH_STRING + GREATER_THAN_EQUAL + minWidth,
				AREA + IS_EQUAL_TO+ roomArea + COMMA_WIDTH_STRING + IS_EQUAL_TO + roomWidth, result, scrutinyDetail);
	}

	
	/**
	 * Validates a room against minimum area and width requirements and records result.
	 *
	 * @param pl             the Plan object
	 * @param floor          the floor containing the room
	 * @param room           the Measurement object representing the room
	 * @param scrutinyDetail object used to record results
	 * @param minArea        minimum permissible area
	 * @param minWidth       minimum permissible width
	 * @param roomArea       actual room area
	 * @param roomWidth      actual room width
	 */
	private void validateAndReportNonInhabitattionalRoom(Plan pl, Floor floor, FloorUnit unit, Measurement room, ScrutinyDetail scrutinyDetail,
			BigDecimal minArea, BigDecimal minWidth, BigDecimal roomArea, BigDecimal roomWidth) {

		String result = (roomArea.compareTo(minArea) >= 0 && roomWidth.compareTo(minWidth) >= 0)
				? Result.Accepted.getResultVal()
				: Result.Not_Accepted.getResultVal();

		setReportOutputDetails(pl, RULE_4_4_4_I, RULE_ROOM_DESC, floor.getNumber().toString(), unit.getUnitNumber(),
				String.valueOf(room.getRoomNumber()), AREA + GREATER_THAN_EQUAL + minArea + COMMA_WIDTH_STRING + GREATER_THAN_EQUAL + minWidth,
				AREA + IS_EQUAL_TO+ roomArea + COMMA_WIDTH_STRING + IS_EQUAL_TO + roomWidth, result, scrutinyDetail);
	}
	
	/**
	 * Builds the scrutiny result for room height validation. If heights are
	 * available, evaluates them against required height based on occupancy type.
	 * Otherwise, records an error.
	 *
	 * @param pl                        the Plan object
	 * @param floor                     the floor being processed
	 * @param block                     the block containing the floor
	 * @param mostRestrictiveOccupancy  occupancy type affecting validation
	 * @param residentialRoomHeights    list of collected room heights
	 * @param heightOfRoomFeaturesColor map linking feature types to color codes
	 * @param color                     color code to match against
	 * @param errors                    map to record any errors during processing
	 */
	private void buildRoomHeightResult(Plan pl, Floor floor, FloorUnit unit, Block block, OccupancyTypeHelper mostRestrictiveOccupancy,
	        List<BigDecimal> residentialRoomHeights, Map<String, Integer> heightOfRoomFeaturesColor, String color,
	        Map<String, String> errors) {

	    BigDecimal roomHeight = BigDecimal.ZERO;
	    boolean foundHillyRoom = false;

	    // 1. Check if any room has hilly height
	    for (Room room : unit.getRegularRooms()) {
	        if (room.getHillyAreaRoomHeight() != null) {
	            foundHillyRoom = true;
	            roomHeight = room.getHillyAreaRoomHeight();
	            LOG.info("Processing Hilly Area Room Height: {}", roomHeight);

	            List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.ROOM_AREA.getValue(), false);
	            Optional<RoomAreaRequirement> matchedRule = rules.stream()
	                    .filter(RoomAreaRequirement.class::isInstance)
	                    .map(RoomAreaRequirement.class::cast)
	                    .findFirst();

	            if (matchedRule.isPresent()) {
	                RoomAreaRequirement rule = matchedRule.get();
	                if (rule.getHillyRoomHeight() != null) {
	                    roomHeight = rule.getHillyRoomHeight();
	                    LOG.info("Hilly Room Height from MDMS rule: {}", roomHeight);
	                }
	            }

	            BigDecimal minimumHeight = roomHeight;
	            Map<String, Object> typicalFloorValues = ProcessHelper.getTypicalFloorValues(block, floor, false);
	            LOG.info("Building result for Hilly Area Room with minimum height: {}", minimumHeight);
	            buildResult(pl, floor, unit, minimumHeight, RULE, "Hilly Area Room Height Rule", roomHeight, false, typicalFloorValues);
	            return; // Once hilly is handled, exit method
	        }
	    }

	    // 2. Handle Commercial Occupancy
	    if (mostRestrictiveOccupancy != null && mostRestrictiveOccupancy.getType() != null
	            && DxfFileConstants.F.equalsIgnoreCase(mostRestrictiveOccupancy.getType().getCode())) {

	        BigDecimal requiredHeight = BigDecimal.ZERO;
	        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.ROOM_AREA.getValue(), false);
	        Optional<RoomAreaRequirement> matchedRule = rules.stream()
	                .filter(RoomAreaRequirement.class::isInstance)
	                .map(RoomAreaRequirement.class::cast)
	                .findFirst();

	        if (matchedRule.isPresent()) {
	            RoomAreaRequirement rule = matchedRule.get();
	            requiredHeight = rule.getCommercialGFheight();
	            LOG.info("Commercial Occupancy Required GF Height from MDMS: {}", requiredHeight);
	        }
	        
	        BigDecimal actualMinHeight = residentialRoomHeights.stream().reduce(BigDecimal::min).orElse(BigDecimal.ZERO);
	        LOG.info("Actual minimum residential room height for commercial occupancy: {}", actualMinHeight);

	        Map<String, Object> typicalFloorValues = ProcessHelper.getTypicalFloorValues(block, floor, false);
	        buildResult(pl, floor, unit, requiredHeight, RULEROOMHTC, "Commercial Room Height Rule", actualMinHeight, false, typicalFloorValues);
	        return;
	    }

	    // 3. Residential or other occupancy
	    if (!residentialRoomHeights.isEmpty()) {
	        BigDecimal minHeight = residentialRoomHeights.stream().reduce(BigDecimal::min).get();

	        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.ROOM_AREA.getValue(), false);
	        Optional<RoomAreaRequirement> matchedRule = rules.stream()
	                .filter(RoomAreaRequirement.class::isInstance)
	                .map(RoomAreaRequirement.class::cast)
	                .findFirst();

	        if (matchedRule.isPresent()) {
	            RoomAreaRequirement rule = matchedRule.get();
	            roomHeight = rule.getRoomHeight();
	            LOG.info("Residential Room Height from MDMS: {}", roomHeight);
	        }

	        BigDecimal minimumHeight = roomHeight;
	        LOG.info("Minimum Residential Regular Room Height required is set to: {}", minimumHeight);

	        Map<String, Object> typicalFloorValues = ProcessHelper.getTypicalFloorValues(block, floor, false);
	        buildResult(pl, floor, unit, minimumHeight, RULEROOMHT, RULE_REGULAR_DESC, minHeight, false, typicalFloorValues);

		} /*
			 * else { String layerName = String.format(LAYER_ROOM_HEIGHT, block.getNumber(),
			 * floor.getNumber(), unit.getUnitNumber(), REGULAR_ROOM);
			 * LOG.warn("No Residential Room Heights defined for layer: {}", layerName);
			 * errors.put(layerName, ROOM_HEIGHT_NOTDEFINED + layerName);
			 * pl.addErrors(errors); }
			 */
	}


	private void buildResult(Plan pl, Floor floor, FloorUnit unit, BigDecimal expected, String subRule, String subRuleDesc,
			BigDecimal actual, boolean valid, Map<String, Object> typicalFloorValues) {
		if (!(Boolean) typicalFloorValues.get(IS_TYPICAL_REP_FLOOR)
				&& expected.compareTo(BigDecimal.valueOf(0)) > 0 && subRule != null && subRuleDesc != null) {
			if (actual.compareTo(expected) >= 0) {
				valid = true;
			}
			String value = typicalFloorValues.get(TYPICAL_FLOOR) != null
					? (String) typicalFloorValues.get(TYPICAL_FLOOR)
					: FLOOR_SPACED + floor.getNumber();
			if (valid) {
				setReportOutputDetails(pl, subRule, subRuleDesc, value, unit.getUnitNumber(), EMPTY_STRING, expected + DcrConstants.IN_METER,
						actual + DcrConstants.IN_METER, Result.Accepted.getResultVal(), scrutinyDetail);
				LOG.info("Room Height Validation True: (Expected/Actual) " + expected + "/" + actual);
			} else {
				setReportOutputDetails(pl, subRule, subRuleDesc, value, unit.getUnitNumber(), EMPTY_STRING, expected + DcrConstants.IN_METER,
						actual + DcrConstants.IN_METER, Result.Not_Accepted.getResultVal(), scrutinyDetail);
				LOG.info("Room Height Validation False: (Expected/Actual) " + expected + "/" + actual);
			}
		}
	}

	private void setReportOutputDetails(Plan pl, String ruleNo, String ruleDesc, String floor, Integer unit, String room,
			String expected, String actual, String status, ScrutinyDetail scrutinyDetail) {
		ReportScrutinyDetail detail = new ReportScrutinyDetail();
		detail.setRuleNo(ruleNo);
		detail.setDescription(ruleDesc);
		detail.setFloorNo(floor);
		detail.setRoom(room);
		detail.setRequired(expected);
		detail.setProvided(actual);
		detail.setStatus(status);
		detail.setUnitNumber(unit);

		Map<String, String> details = mapReportDetails(detail);
		addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
	}

	@Override
	public Map<String, Date> getAmendments() {
		return new LinkedHashMap<>();
	}
}