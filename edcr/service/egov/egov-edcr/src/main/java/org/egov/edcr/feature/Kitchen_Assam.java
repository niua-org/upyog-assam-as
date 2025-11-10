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

import static org.egov.edcr.constants.CommonFeatureConstants.FLOOR;
import static org.egov.edcr.constants.CommonKeyConstants.BLOCK;
import static org.egov.edcr.constants.CommonKeyConstants.FLOOR_SPACED;
import static org.egov.edcr.constants.CommonKeyConstants.HEIGHT_OF_ROOM;
import static org.egov.edcr.constants.CommonKeyConstants.IS_TYPICAL_REP_FLOOR;
import static org.egov.edcr.constants.CommonKeyConstants.TYPICAL_FLOOR;
import static org.egov.edcr.constants.CommonKeyConstants.U_KITCHEN;
import static org.egov.edcr.constants.DxfFileConstants.A;
import static org.egov.edcr.constants.DxfFileConstants.F;
import static org.egov.edcr.constants.EdcrReportConstants.KITCHEN;
import static org.egov.edcr.constants.EdcrReportConstants.KITCHEN_DINING;
import static org.egov.edcr.constants.EdcrReportConstants.KITCHEN_STORE;
import static org.egov.edcr.constants.EdcrReportConstants.LAYER_ROOM_HEIGHT;
import static org.egov.edcr.constants.EdcrReportConstants.ROOM_HEIGHT_NOTDEFINED;
import static org.egov.edcr.constants.EdcrReportConstants.SUBRULE_41_III;
import static org.egov.edcr.constants.EdcrReportConstants.SUBRULE_41_III_AREA_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.SUBRULE_41_III_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.SUBRULE_41_III_TOTAL_WIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.KICTHEN_VENT_DESC;
import static org.egov.edcr.constants.EdcrReportConstants.KITCHEN_DOOR_WIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.KITCHEN_DOOR_HEIGHT;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.service.ProcessHelper;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Kitchen_Assam extends Kitchen {

	public static final Logger LOG = LogManager.getLogger(Kitchen_Assam.class);

	@Autowired
	MDMSCacheManager cache;

	@Override
	public Plan validate(Plan pl) {
		return pl;
	}

	/**
	 * Processes the given Plan to validate kitchen room requirements like height,
	 * area, and width based on occupancy type and rules defined in MDMS. Applies
	 * checks only for Residential (A) and Commercial (F) occupancy types.
	 *
	 * @param pl The Plan object containing blocks, buildings, and room
	 *           measurements.
	 * @return The updated Plan object after processing and applying kitchen
	 *         validations.
	 */
	@Override
	public Plan process(Plan pl) {
		LOG.info("Starting Plan processing for Plan ID");
		validate(pl);
		Map<String, Integer> heightOfRoomFeaturesColor = pl.getSubFeatureColorCodesMaster().get(HEIGHT_OF_ROOM);
		LOG.info("Retrieved heightOfRoomFeaturesColor map with {} entries",
				heightOfRoomFeaturesColor != null ? heightOfRoomFeaturesColor.size() : 0);

		if (pl == null || pl.getBlocks() == null) {
			LOG.warn("Plan or Plan blocks are null, returning plan as is");
			return pl;
		}

		OccupancyTypeHelper mostRestrictiveOccupancy = pl.getVirtualBuilding() != null
				? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
				: null;
		LOG.info("Most restrictive occupancy determined: {}",
				mostRestrictiveOccupancy != null ? mostRestrictiveOccupancy.getType().getCode() : "null");

		if (mostRestrictiveOccupancy == null || mostRestrictiveOccupancy.getSubtype() == null) {
			LOG.warn("Most restrictive occupancy or its subtype is null, returning plan as is");
			return pl;
		}

		String occupancyCode = mostRestrictiveOccupancy.getType().getCode();
		LOG.info("Occupancy code of most restrictive occupancy: {}", occupancyCode);

		if (!(A.equalsIgnoreCase(occupancyCode) || F.equalsIgnoreCase(occupancyCode))) {
			LOG.warn("Occupancy code {} is not A or F, skipping processing", occupancyCode);
			return pl;
		}

		for (Block block : pl.getBlocks()) {
			LOG.info("Processing kitchens for Block number: {}", block.getNumber());
			processKitchenForBlock(block, pl, mostRestrictiveOccupancy, heightOfRoomFeaturesColor);
		}

		LOG.info("Completed Plan processing for Plan");
		return pl;
	}

	/**
	 * Processes kitchen requirements for all floors in a given block. Initializes
	 * the scrutiny detail report and validates kitchen-related room parameters for
	 * each floor.
	 *
	 * @param block        The Block object representing a section of the building.
	 * @param pl           The complete Plan object.
	 * @param occupancy    The most restrictive occupancy type applicable to the
	 *                     Plan.
	 * @param heightColors Map of room height feature names to their corresponding
	 *                     color codes.
	 */
	private void processKitchenForBlock(Block block, Plan pl, OccupancyTypeHelper occupancy,
			Map<String, Integer> heightColors) {
		if (block.getBuilding() == null || block.getBuilding().getFloors().isEmpty())
			return;

		scrutinyDetail = new ScrutinyDetail();
		scrutinyDetail.addColumnHeading(1, RULE_NO);
		scrutinyDetail.addColumnHeading(2, DESCRIPTION);
		scrutinyDetail.addColumnHeading(3, FLOOR);
        scrutinyDetail.addColumnHeading(4, UNIT);
		scrutinyDetail.addColumnHeading(5, REQUIRED);
		scrutinyDetail.addColumnHeading(6, PROVIDED);
		scrutinyDetail.addColumnHeading(7, STATUS);
		scrutinyDetail.setKey(BLOCK + block.getNumber() + U_KITCHEN);

		for (Floor floor : block.getBuilding().getFloors()) {
            if (floor.getUnits() != null && !floor.getUnits().isEmpty())
                for (FloorUnit floorUnit : floor.getUnits())
                    processKitchenForFloor(floor, floorUnit, block, pl, occupancy, heightColors);
        }

		pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);
	}

	/**
	 * Validates and processes kitchen rooms on a specific floor. It checks kitchen
	 * height, area, and width against the expected values defined in MDMS feature
	 * rules.
	 *
	 * @param floor        The Floor object to be validated.
	 * @param block        The block that the floor belongs to.
	 * @param pl           The Plan object.
	 * @param occupancy    The most restrictive occupancy type.
	 * @param heightColors Map of height feature color codes for different room
	 *                     types.
	 */
	private void processKitchenForFloor(Floor floor, FloorUnit floorUnit, Block block, Plan pl, OccupancyTypeHelper occupancy,
			Map<String, Integer> heightColors) {
        LOG.info("Processing kitchen for Floor: {}, FloorUnit: {}, Block: {}", floor.getNumber(), floorUnit.getUnitNumber(), block.getNumber());

		if (floorUnit.getKitchen() == null)
			return;

		// Room Color Codes
		String occ = occupancy.getType().getCode();
		String kitchenColor = A.equalsIgnoreCase(occ) ? DxfFileConstants.RESIDENTIAL_KITCHEN_ROOM_COLOR
				: DxfFileConstants.COMMERCIAL_KITCHEN_ROOM_COLOR;
		String kitchenStoreColor = A.equalsIgnoreCase(occ) ? DxfFileConstants.RESIDENTIAL_KITCHEN_STORE_ROOM_COLOR
				: DxfFileConstants.COMMERCIAL_KITCHEN_STORE_ROOM_COLOR;
		String kitchenDiningColor = A.equalsIgnoreCase(occ) ? DxfFileConstants.RESIDENTIAL_KITCHEN_DINING_ROOM_COLOR
				: DxfFileConstants.COMMERCIAL_KITCHEN_DINING_ROOM_COLOR;

		LOG.info("Processing kitchen for Floor: {}, FloorUnit: {}, Block: {}, Occupancy: {}", floor.getNumber(), floorUnit.getUnitNumber(), block.getNumber(),
				occ);

		// Extract rooms and heights
		List<Measurement> rooms = floorUnit.getKitchen().getRooms();
		//List<RoomHeight> heights = floorUnit.getKitchen().getHeights();
		List<BigDecimal> kitchenHeights = floorUnit.getCommonHeight();


		LOG.info("Kitchen heights extracted: {}", kitchenHeights);

		// Extract feature rules
		List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.KITCHEN.getValue(), false);
		Optional<KitchenRequirement> matchedRule = rules.stream().filter(KitchenRequirement.class::isInstance)
				.map(KitchenRequirement.class::cast).findFirst();
		if (!matchedRule.isPresent()) {
			LOG.warn("No KitchenRequirement rule found for Floor: {}, FloorUnit: {}, Block: {}", floor.getNumber(), floorUnit.getUnitNumber(), block.getNumber());
			return;
		}
		KitchenRequirement rule = matchedRule.get();
		LOG.info("KitchenRequirement rule found: Height = {}, Area = {}, Width = {}", rule.getKitchenHeight(),
				rule.getKitchenArea(), rule.getKitchenWidth());

        // Validate height
        if (!kitchenHeights.isEmpty()) {
            BigDecimal minHeight = kitchenHeights.stream().min(Comparator.naturalOrder()).get().setScale(2, BigDecimal.ROUND_HALF_UP);
            buildResult(pl, floor, floorUnit, rule.getKitchenHeight(), SUBRULE_41_III, SUBRULE_41_III_DESC, minHeight, false, ProcessHelper.getTypicalFloorValues(block, floor, false));
        } else {
            String layerName = String.format(LAYER_ROOM_HEIGHT, block.getNumber(), floor.getNumber(), floorUnit.getUnitNumber(), "KITCHEN");
            pl.addError(layerName, ROOM_HEIGHT_NOTDEFINED + layerName);
        }
        
     // Validate door width
        if (floorUnit.getKitchen().getKitchenDoorWidth() != null && !floorUnit.getKitchen().getKitchenDoorWidth().isEmpty()) {
            BigDecimal minDoorWidth = floorUnit.getKitchen().getKitchenDoorWidth()
                .stream().min(Comparator.naturalOrder()).get().setScale(2, BigDecimal.ROUND_HALF_UP);

            buildResult(pl, floor, floorUnit, rule.getKitchenDoorWidth(), SUBRULE_41_III,
            		KITCHEN_DOOR_WIDTH, minDoorWidth, false,
                ProcessHelper.getTypicalFloorValues(block, floor, false));
        }
        
        
     //  Validate door height
        if (floorUnit.getCommonHeight() != null && !floorUnit.getCommonHeight().isEmpty()) {
            BigDecimal minDoorHeight = floorUnit.getCommonHeight()
                    .stream()
                    .min(Comparator.naturalOrder())
                    .get()
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

            buildResult(pl, floor, floorUnit, rule.getKitchenDoorHeight(), SUBRULE_41_III,
                    KITCHEN_DOOR_HEIGHT, minDoorHeight, false,
                    ProcessHelper.getTypicalFloorValues(block, floor, false));
        }

        
        // Process Room Types
		LOG.info("Processing Kitchen room type with color: {}", kitchenColor);

        processRoomType(rooms, heightColors, kitchenColor, rule.getKitchenArea(), rule.getKitchenWidth(), KITCHEN, floor, floorUnit, block, pl);
        
		LOG.info("Processing Kitchen Store room type with color: {}", kitchenStoreColor);

        processRoomType(rooms, heightColors, kitchenStoreColor, rule.getKitchenStoreArea(), rule.getKitchenStoreWidth(), KITCHEN_STORE, floor, floorUnit, block, pl);
        
		LOG.info("Processing Kitchen Dining room type with color: {}", kitchenDiningColor);

        processRoomType(rooms, heightColors, kitchenDiningColor, rule.getKitchenDiningArea(), rule.getKitchenDiningWidth(), KITCHEN_DINING, floor, floorUnit, block, pl);
        
		LOG.info("Processing kitchen and dining ventilation for Floor: {}", floor.getNumber());

        evaluateKitchenVentilation(pl, floor, floorUnit, block, rooms);

    }

	/**
	 * Filters rooms based on the specified color code and validates each room type
	 * (kitchen, kitchen store, kitchen dining) for minimum required area and width.
	 * Adds the result to the report.
	 *
	 * @param rooms        List of room measurements on the floor.
	 * @param heightColors Map of feature color names and their respective integer
	 *                     codes.
	 * @param color        The specific color code for the room type.
	 * @param minArea      Minimum required area for the room type.
	 * @param minWidth     Minimum required width for the room type.
	 * @param roomName     Name of the room type being validated.
	 * @param floor        The floor where the rooms are located.
	 * @param block        The block that the floor belongs to.
	 * @param pl           The Plan object.
	 */
	private void processRoomType(List<Measurement> rooms, Map<String, Integer> heightColors, String color,
			BigDecimal minArea, BigDecimal minWidth, String roomName, Floor floor, FloorUnit floorUnit, Block block, Plan pl) {

		LOG.info("Processing room type: {} for Floor: {} in Block: {}", roomName, floor.getNumber(), block.getNumber());

		List<BigDecimal> areas = new ArrayList<>();
		List<BigDecimal> widths = new ArrayList<>();
		List<BigDecimal> kitchenWidth = floorUnit.getKitchen().getKitchenWidth();

		for (Measurement room : rooms) {
			if (heightColors.get(color) != null && heightColors.get(color) == room.getColorCode()) {
				areas.add(room.getArea());
				if(kitchenWidth != null) {
				widths.addAll(kitchenWidth);
				}
				LOG.info("Added room with Area: {} and Width: {} for color code: {}", room.getArea(), room.getWidth(),
						color);
			}
		}

		if (!areas.isEmpty()) {
			BigDecimal totalArea = areas.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2,
					BigDecimal.ROUND_HALF_UP);
			LOG.info("Total area calculated for room type {}: {}", roomName, totalArea);
			buildResult(pl, floor, floorUnit, minArea, SUBRULE_41_III, String.format(SUBRULE_41_III_AREA_DESC, roomName),
					totalArea, false, ProcessHelper.getTypicalFloorValues(block, floor, false));
		}

        if (!widths.isEmpty()) {
            BigDecimal minRoomWidth = widths.stream().min(Comparator.naturalOrder()).get().setScale(2, BigDecimal.ROUND_HALF_UP);
            buildResult(pl, floor, floorUnit, minWidth, SUBRULE_41_III, String.format(SUBRULE_41_III_TOTAL_WIDTH, roomName), minRoomWidth, false, ProcessHelper.getTypicalFloorValues(block, floor, false));
        }
    }


    /**
     * Builds the validation result for a given parameter (height, area, width) and appends it to the 
     * scrutiny detail report. Checks if the actual value meets or exceeds the expected value.
     *
     * @param pl                   The Plan object to which the result is added.
     * @param floor                The floor for which the rule is being validated.
     * @param expected             The expected value for the parameter (height/area/width).
     * @param subRule              Sub-rule identifier from the regulations.
     * @param subRuleDesc          Description of the sub-rule being checked.
     * @param actual               Actual value extracted from the Plan.
     * @param valid                Boolean flag representing whether the value is valid.
     * @param typicalFloorValues   Map containing information about typical floor applicability.
     */
    private void buildResult(Plan pl, Floor floor, FloorUnit floorUnit, BigDecimal expected, String subRule, String subRuleDesc,
            BigDecimal actual, boolean valid, Map<String, Object> typicalFloorValues) {
        if (!(Boolean) typicalFloorValues.get(IS_TYPICAL_REP_FLOOR)
                && expected.compareTo(BigDecimal.valueOf(0)) > 0 &&
                subRule != null && subRuleDesc != null) {
            if (actual.compareTo(expected) >= 0) {
                valid = true;
            }
            String value = typicalFloorValues.get(TYPICAL_FLOOR) != null
                    ? (String) typicalFloorValues.get(TYPICAL_FLOOR)
                    : FLOOR_SPACED + floor.getNumber();
            if (valid) {
                setReportOutputDetails(pl, subRule, subRuleDesc, value, floorUnit,
                        expected + DcrConstants.IN_METER,
                        actual + DcrConstants.IN_METER, Result.Accepted.getResultVal());
            } else {
                setReportOutputDetails(pl, subRule, subRuleDesc, value, floorUnit,
                        expected + DcrConstants.IN_METER,
                        actual + DcrConstants.IN_METER, Result.Not_Accepted.getResultVal());
            }
        }
    }
    
    /**
     * Evaluates the kitchen ventilation requirement for a given floor.
     * <p>
     * As per building regulations, every kitchen must have adequate light and ventilation.
     * The total window opening area (width × height for all kitchen windows) must be at least
     * one-sixth (1/6) of the total kitchen floor area.
     * </p>
     *
     * <ul>
     *   <li>Calculates total kitchen window area = Σ(width × height).</li>
     *   <li>Calculates total kitchen area = Σ(room areas inside kitchen).</li>
     *   <li>Derives required ventilation area = (kitchen area / 6).</li>
     *   <li>Compares the provided window area with the required area and records the result.</li>
     * </ul>
     *
     * @param pl    the {@link Plan} object containing overall building details
     * @param floor the {@link Floor} object for which kitchen ventilation is being evaluated
     * @param block the {@link Block} object to which the floor belongs
     * @param rooms list of {@link Measurement} objects representing kitchen rooms and their areas
     */
    
    private void evaluateKitchenVentilation(Plan pl, Floor floor, FloorUnit floorUnit, Block block, List<Measurement> rooms) {
        if (floorUnit.getKitchen() == null
                || floorUnit.getKitchen().getKitchenWindowWidth() == null
                || floorUnit.getKitchen().getKitchenWindowWidth().isEmpty()
                || floorUnit.getCommonHeight() == null) {
            return;
        }
        
        // Kitchen floor area = sum of all kitchen room areas
        BigDecimal kitchenArea = rooms.stream()
                .map(Measurement::getArea)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Get kitchen window height
        
            BigDecimal windowHeight = floorUnit.getCommonHeight()
                    .stream()
                    .min(Comparator.naturalOrder())
                    .get()
                    .setScale(2, BigDecimal.ROUND_HALF_UP);

        
        // Provided window area = sum of (each window width × window height)
        BigDecimal providedKitchenArea = floorUnit.getKitchen().getKitchenWindowWidth().stream()
                .map(width -> width.multiply(windowHeight))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Required ventilation area = 1/6th of kitchen floor area
        BigDecimal requiredVentilationArea = kitchenArea.divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);

        LOG.info("Evaluating kitchen ventilation for Floor: {}. KitchenFloorArea: {}, RequiredVentilationArea: {}, ProvidedWindowArea: {}",
                floor.getNumber(), kitchenArea, requiredVentilationArea, providedKitchenArea);

        buildResult(pl, floor, floorUnit, requiredVentilationArea, SUBRULE_41_III,
                KICTHEN_VENT_DESC,
                providedKitchenArea,
                providedKitchenArea.compareTo(requiredVentilationArea) >= 0,
                ProcessHelper.getTypicalFloorValues(block, floor, false));
    }


    /**
     * Populates a single scrutiny detail entry and appends it to the report output of the Plan.
     *
     * @param pl         The Plan object that contains the report.
     * @param ruleNo     Rule number being validated.
     * @param ruleDesc   Description of the rule.
     * @param floor      Floor number or name where the rule is applied.
     * @param expected   Expected value of the parameter (height, area, width).
     * @param actual     Actual value found in the Plan.
     * @param status     Result of the validation ("Accepted" or "Not Accepted").
     */
    private void setReportOutputDetails(Plan pl, String ruleNo, String ruleDesc, String floor, FloorUnit floorUnit, String expected, String actual,
            String status) {
        LOG.info("Setting report details: RuleNo={}, RuleDesc={}, Floor={}, Unit={}, Expected={}, Actual={}, Status={}",
                ruleNo, ruleDesc, floor, floorUnit.getUnitNumber(), expected, actual, status);
        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(ruleNo);
        detail.setDescription(ruleDesc);
        detail.setFloorNo(floor);
        detail.setUnitNumber(floorUnit.getUnitNumber());
        detail.setRequired(expected);
        detail.setProvided(actual);
        detail.setStatus(status);

        Map<String, String> details = mapReportDetails(detail);
        addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
    }

    @Override
    public Map<String, Date> getAmendments() {
        return new LinkedHashMap<>();
    }
}