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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.*;
import org.egov.common.entity.edcr.RainWaterHarvesting;
import org.egov.edcr.constants.CommonKeyConstants;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static org.egov.edcr.constants.CommonFeatureConstants.*;
import static org.egov.edcr.constants.CommonKeyConstants.*;
import static org.egov.edcr.constants.EdcrReportConstants.*;
import static org.egov.edcr.constants.RuleKeyConstants.*;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;
import static org.egov.edcr.utility.DcrConstants.*;
import static org.egov.edcr.utility.DcrConstants.YES;

@Service
public class AdditionalFeature_Assam extends FeatureProcess {
    private static final Logger LOG = LogManager.getLogger(AdditionalFeature_Assam.class);

    @Autowired
    MDMSCacheManager cache;

    /**
     * Validates the plan by checking if building heights are defined for all blocks.
     * Adds errors to the plan if any block has zero building height.
     *
     * @param pl The plan to validate
     * @return The validated plan with any errors added
     */
    @Override
    public Plan validate(Plan pl) {
        HashMap<String, String> errors = new HashMap<>();

        List<Block> blocks = pl.getBlocks();

        for (Block block : blocks) {
            if (block.getBuilding() != null) {
                if (block.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) == 0) {
                    errors.put(String.format(DcrConstants.BLOCK_BUILDING_HEIGHT, block.getNumber()),
                            edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                    new String[] {
                                            String.format(DcrConstants.BLOCK_BUILDING_HEIGHT, block.getNumber()) },
                                    LocaleContextHolder.getLocale()));
                    pl.addErrors(errors);
                }
            }
        }
        return pl;
    }

    /**
     * Main processing method that validates various building regulations for Assam state.
     * Retrieves rules from cache and validates number of floors, building height, floor height,
     * plinth height, barrier-free access, basement, green buildings, and fire declarations.
     *
     * @param pl The plan to process
     * @return The processed plan with validation results
     */
    @Override
    public Plan process(Plan pl) {
        HashMap<String, String> errors = new HashMap<>();
        validate(pl);

        String typeOfArea = pl.getPlanInformation().getTypeOfArea();
        BigDecimal roadWidth = pl.getPlanInformation().getRoadWidth();

        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.ADDITIONAL_FEATURE.getValue(), false);
        Optional<AdditionalFeatureRequirement> matchedRule = rules.stream()
                .filter(AdditionalFeatureRequirement.class::isInstance)
                .map(AdditionalFeatureRequirement.class::cast)
                .findFirst();

        if (!matchedRule.isPresent()) {
            LOG.error("No matching rule found for Additional Feature");
            return pl;
        }

        AdditionalFeatureRequirement rule = matchedRule.get();
        BigDecimal additionalFeatureMinRequiredFloorHeight = rule.getAdditionalFeatureMinRequiredFloorHeight();
        BigDecimal additionalFeatureMaxPermissibleFloorHeight = rule.getAdditionalFeatureMaxPermissibleFloorHeight();
        BigDecimal additionalFeatureStiltFloor = rule.getAdditionalFeatureStiltFloor();
        BigDecimal additionalFeatureRoadWidthA = rule.getAdditionalFeatureRoadWidthA();
        BigDecimal additionalFeatureRoadWidthB = rule.getAdditionalFeatureRoadWidthB();
        BigDecimal additionalFeatureRoadWidthC = rule.getAdditionalFeatureRoadWidthC();
        BigDecimal additionalFeatureRoadWidthD = rule.getAdditionalFeatureRoadWidthD();
        BigDecimal additionalFeatureRoadWidthE = rule.getAdditionalFeatureRoadWidthE();
        BigDecimal additionalFeatureRoadWidthF = rule.getAdditionalFeatureRoadWidthF();
        BigDecimal additionalFeatureNewRoadWidthA = rule.getAdditionalFeatureNewRoadWidthA();
        BigDecimal additionalFeatureNewRoadWidthB = rule.getAdditionalFeatureNewRoadWidthB();
        BigDecimal additionalFeatureNewRoadWidthC = rule.getAdditionalFeatureNewRoadWidthC();

        BigDecimal additionalFeatureBuildingHeightA = rule.getAdditionalFeatureBuildingHeightA();
        BigDecimal additionalFeatureBuildingHeightB = rule.getAdditionalFeatureBuildingHeightB();
        BigDecimal additionalFeatureBuildingHeightServiceFloor = rule.getAdditionalFeatureBuildingHeightServiceFloor();
        BigDecimal additionalFeatureTotalBuildingHeight = rule.getAdditionalFeatureTotalBuildingHeight();
        BigDecimal additionalFeatureBuildingHeightStiltParking = rule.getAdditionalFeatureBuildingHeightStiltParking();
        BigDecimal additionalFeatureBuildingHeightRoofTanks = rule.getAdditionalFeatureBuildingHeightRoofTanks();
        BigDecimal additionalFeatureBuildingHeightChimney = rule.getAdditionalFeatureBuildingHeightChimney();
        BigDecimal additionalFeatureBuildingHeightServiceRooms = rule.getAdditionalFeatureBuildingHeightServiceRooms();
        BigDecimal additionalFeatureBuildingHeightStairCovers = rule.getAdditionalFeatureBuildingHeightStairCovers();
        BigDecimal additionalFeatureBuildingHeightRoofArea = rule.getAdditionalFeatureBuildingHeightRoofArea();
        BigDecimal additionalFeatureBuildingHeightRWH = rule.getAdditionalFeatureBuildingHeightRWH();
        BigDecimal additionalFeatureBuildingHeightCappedPermitted = rule.getAdditionalFeatureBuildingHeightCappedPermitted();
        BigDecimal additionalFeatureBuildingHeightMaxPermitted = rule.getAdditionalFeatureBuildingHeightMaxPermitted();
        BigDecimal additionalFeatureFloorsAcceptedBC = rule.getAdditionalFeatureFloorsAcceptedBC();
        BigDecimal additionalFeatureFloorsAcceptedCD = rule.getAdditionalFeatureFloorsAcceptedCD();
        BigDecimal additionalFeatureFloorsAcceptedDE = rule.getAdditionalFeatureFloorsAcceptedDE();
        BigDecimal additionalFeatureFloorsAcceptedEF = rule.getAdditionalFeatureFloorsAcceptedEF();
        BigDecimal additionalFeatureFloorsNewAcceptedAB = rule.getAdditionalFeatureFloorsNewAcceptedAB();
        BigDecimal additionalFeatureFloorsNewAcceptedBC = rule.getAdditionalFeatureFloorsNewAcceptedBC();
        BigDecimal additionalFeatureBasementPlotArea = rule.getAdditionalFeatureBasementPlotArea();
        BigDecimal additionalFeatureBasementAllowed = rule.getAdditionalFeatureBasementAllowed();
        BigDecimal additionalFeatureBarrierValue = rule.getAdditionalFeatureBarrierValue();
        BigDecimal afGreenBuildingValueA = rule.getAfGreenBuildingValueA();
        BigDecimal afGreenBuildingValueB = rule.getAfGreenBuildingValueB();
        BigDecimal afGreenBuildingValueC = rule.getAfGreenBuildingValueC();
        BigDecimal afGreenBuildingValueD = rule.getAfGreenBuildingValueD();

        if (StringUtils.isNotBlank(typeOfArea) && roadWidth != null) {
            validateNumberOfFloors(pl, errors, typeOfArea, roadWidth,
                    additionalFeatureRoadWidthA,
                    additionalFeatureRoadWidthB,
                    additionalFeatureRoadWidthC,
                    additionalFeatureRoadWidthD,
                    additionalFeatureRoadWidthE,
                    additionalFeatureRoadWidthF,
                    additionalFeatureNewRoadWidthA,
                    additionalFeatureNewRoadWidthB,
                    additionalFeatureNewRoadWidthC,
                    additionalFeatureFloorsAcceptedBC,
                    additionalFeatureFloorsAcceptedCD,
                    additionalFeatureFloorsAcceptedDE,
                    additionalFeatureFloorsAcceptedEF,
                    additionalFeatureFloorsNewAcceptedAB,
                    additionalFeatureFloorsNewAcceptedBC
            );
            validateHeightOfBuilding(pl, errors, roadWidth,
                    additionalFeatureBuildingHeightA,
                    additionalFeatureBuildingHeightB,
                    additionalFeatureBuildingHeightServiceFloor,
                    additionalFeatureTotalBuildingHeight,
                    additionalFeatureBuildingHeightStiltParking,
                    additionalFeatureBuildingHeightRoofTanks,
                    additionalFeatureBuildingHeightChimney,
                    additionalFeatureBuildingHeightServiceRooms,
                    additionalFeatureBuildingHeightStairCovers,
                    additionalFeatureBuildingHeightRoofArea,
                    additionalFeatureBuildingHeightRWH,
                    additionalFeatureBuildingHeightCappedPermitted,
                    additionalFeatureBuildingHeightMaxPermitted
            );
            validateHeightOfFloors(pl, errors, additionalFeatureMinRequiredFloorHeight, additionalFeatureMaxPermissibleFloorHeight, additionalFeatureStiltFloor);
        }

        validatePlinthHeight(pl, errors);
        validateBarrierFreeAccess(pl, errors, additionalFeatureBarrierValue);
        validateBasement(pl, errors, additionalFeatureBasementPlotArea, additionalFeatureBasementAllowed);
        validateGreenBuildingsAndSustainability(pl, errors,
                afGreenBuildingValueA,
                afGreenBuildingValueB,
                afGreenBuildingValueC,
                afGreenBuildingValueD
        );
        validateFireDeclaration(pl, errors);

        return pl;
    }

    /**
     * Validates fire protection and safety requirements for high-rise buildings or
     * commercial buildings above 750 sqm.
     *
     * @param pl The plan to validate
     * @param errors Map to store validation errors
     */
    private void validateFireDeclaration(Plan pl, HashMap<String, String> errors) {
        ScrutinyDetail scrutinyDetail = getNewScrutinyDetail(CommonKeyConstants.FIRE_PROTEC_SAFETY_REQ);
        OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding() != null
                ? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
                : null;
        if (pl.getBlocks() != null && !pl.getBlocks().isEmpty()) {
            for (Block b : pl.getBlocks()) {
                if (b.getBuilding() != null && (b.getBuilding().getIsHighRise()
                        || isCommercialAbv750sqm(pl, mostRestrictiveOccupancyType))) {
                    ReportScrutinyDetail detail = new ReportScrutinyDetail();
                    detail.setRuleNo(RULE_56);
                    detail.setDescription(FIRE_PROTECTION_AND_FIRE_SAFETY_REQUIREMENTS_DESC);
                    detail.setPermissible(YES_NO_NA);
                    detail.setProvided(pl.getPlanInformation().getFireProtectionAndFireSafetyRequirements());

                    if (pl.getPlanInformation() != null && !pl.getPlanInformation().getFireProtectionAndFireSafetyRequirements().isEmpty()) {
                        detail.setStatus(Result.Accepted.getResultVal());
                    } else {
                        detail.setStatus(Result.Not_Accepted.getResultVal());
                    }

                    Map<String, String> details = mapReportDetails(detail);
                    addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
                }
            }
        }

    }

    /**
     * Checks if a building is commercial type with area above 750 sqm.
     *
     * @param pl The plan to check
     * @param mostRestrictiveOccupancyType The occupancy type helper
     * @return true if commercial building above 750 sqm, false otherwise
     */
    private boolean isCommercialAbv750sqm(Plan pl, OccupancyTypeHelper mostRestrictiveOccupancyType) {
        return pl.getVirtualBuilding() != null && mostRestrictiveOccupancyType != null
                && mostRestrictiveOccupancyType.getType() != null
                && DxfFileConstants.F.equalsIgnoreCase(mostRestrictiveOccupancyType.getType().getCode())
                && pl.getVirtualBuilding().getTotalCoverageArea().compareTo(BigDecimal.valueOf(750)) > 0;
    }

    /**
     * Validates barrier-free access requirements for physically challenged people.
     * Required for non-residential buildings with plot area above specified threshold.
     *
     * @param pl The plan to validate
     * @param errors Map to store validation errors
     * @param additionalFeatureBarrierValue Minimum plot area threshold for barrier-free access
     */
    private void validateBarrierFreeAccess(Plan pl, HashMap<String, String> errors, BigDecimal additionalFeatureBarrierValue) {
        ScrutinyDetail scrutinyDetail = getNewScrutinyDetail(BARRIER_FREE_ACCESS_FOR_PHYSICALLY_CHALLENGED_PEOPLE_DESC);
        if (pl.getVirtualBuilding() != null && pl.getVirtualBuilding().getMostRestrictiveFarHelper() != null
                && pl.getVirtualBuilding().getMostRestrictiveFarHelper().getSubtype() != null && !DxfFileConstants.A_R
                .equals(pl.getVirtualBuilding().getMostRestrictiveFarHelper().getSubtype().getCode())
                && pl.getPlot() != null && pl.getPlot().getArea().compareTo(additionalFeatureBarrierValue) > 0) {

            ReportScrutinyDetail detail = new ReportScrutinyDetail();
            detail.setRuleNo(RULE_50);
            detail.setDescription(BARRIER_FREE_ACCESS_FOR_PHYSICALLY_CHALLENGED_PEOPLE_DESC);
            detail.setPermissible(DcrConstants.YES);

            if (pl.getPlanInformation() != null
                    && !pl.getPlanInformation().getBarrierFreeAccessForPhyChlngdPpl().isEmpty()
                    && DcrConstants.YES.equals(pl.getPlanInformation().getBarrierFreeAccessForPhyChlngdPpl())) {
                detail.setProvided(DcrConstants.YES);
                detail.setStatus(Result.Accepted.getResultVal());
            } else {
                detail.setProvided(pl.getPlanInformation().getBarrierFreeAccessForPhyChlngdPpl());
                detail.setStatus(Result.Not_Accepted.getResultVal());
            }

            Map<String, String> details = mapReportDetails(detail);
            addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
        }

    }

    /**
     * Validates the number of floors allowed based on area type (old/new) and road width.
     * Checks against different road width categories and their corresponding floor limits.
     *
     * @param pl The plan to validate
     * @param errors Map to store validation errors
     * @param typeOfArea Type of area (old/new)
     * @param roadWidth Width of the road
     * @param additionalFeatureRoadWidthA-F Road width thresholds for old areas
     * @param additionalFeatureNewRoadWidthA-C Road width thresholds for new areas
     */
    private void validateNumberOfFloors(Plan pl, HashMap<String, String> errors, String typeOfArea, BigDecimal roadWidth,
                                        BigDecimal additionalFeatureRoadWidthA,
                                        BigDecimal additionalFeatureRoadWidthB,
                                        BigDecimal additionalFeatureRoadWidthC,
                                        BigDecimal additionalFeatureRoadWidthD,
                                        BigDecimal additionalFeatureRoadWidthE,
                                        BigDecimal additionalFeatureRoadWidthF,
                                        BigDecimal additionalFeatureNewRoadWidthA,
                                        BigDecimal additionalFeatureNewRoadWidthB,
                                        BigDecimal additionalFeatureNewRoadWidthC,
                                        BigDecimal additionalFeatureFloorsAcceptedBC,
                                        BigDecimal additionalFeatureFloorsAcceptedCD,
                                        BigDecimal additionalFeatureFloorsAcceptedDE,
                                        BigDecimal additionalFeatureFloorsAcceptedEF,
                                        BigDecimal additionalFeatureFloorsNewAcceptedAB,
                                        BigDecimal additionalFeatureFloorsNewAcceptedBC
    ) {
        for (Block block : pl.getBlocks()) {

            boolean isAccepted = false;
            ScrutinyDetail scrutinyDetail = getNewScrutinyDetailRoadArea(
                    BLOCK + block.getNumber() + UNDERSCORE + NUMBER_OF_FLOORS);
            BigDecimal floorAbvGround = block.getBuilding().getFloorsAboveGround();
            String requiredFloorCount = StringUtils.EMPTY;

            if (typeOfArea.equalsIgnoreCase(OLD)) {
                if (roadWidth.compareTo(additionalFeatureRoadWidthA) < 0) {
                    errors.put(OLD_AREA_ERROR, OLD_AREA_ERROR_MSG);
                    pl.addErrors(errors);
                } else if (roadWidth.compareTo(additionalFeatureRoadWidthB) >= 0
                        && roadWidth.compareTo(additionalFeatureRoadWidthC) < 0) {
                    isAccepted = floorAbvGround.compareTo(additionalFeatureFloorsAcceptedBC) <= 0;
                    requiredFloorCount = LESS_THAN_EQUAL_TO + additionalFeatureFloorsAcceptedBC;
                } else if (roadWidth.compareTo(additionalFeatureRoadWidthC) >= 0
                        && roadWidth.compareTo(additionalFeatureRoadWidthD) < 0) {
                    isAccepted = floorAbvGround.compareTo(additionalFeatureFloorsAcceptedCD) <= 0;
                    requiredFloorCount = LESS_THAN_EQUAL_TO + additionalFeatureFloorsAcceptedCD;
                } else if (roadWidth.compareTo(additionalFeatureRoadWidthD) >= 0
                        && roadWidth.compareTo(additionalFeatureRoadWidthE) < 0) {
                    isAccepted = floorAbvGround.compareTo(additionalFeatureFloorsAcceptedDE) <= 0;
                    requiredFloorCount = LESS_THAN_EQUAL_TO + additionalFeatureFloorsAcceptedDE;
                } else if (roadWidth.compareTo(additionalFeatureRoadWidthE) >= 0
                        && roadWidth.compareTo(additionalFeatureRoadWidthF) < 0) {
                    isAccepted = floorAbvGround.compareTo(additionalFeatureFloorsAcceptedEF) <= 0;
                    requiredFloorCount = LESS_THAN_EQUAL_TO + additionalFeatureFloorsAcceptedEF;
                }
            }

            if (typeOfArea.equalsIgnoreCase(NEW)) {
                if (roadWidth.compareTo(additionalFeatureNewRoadWidthA) < 0) {
                    errors.put(NEW_AREA_ERROR, NEW_AREA_ERROR_MSG);
                    pl.addErrors(errors);
                } else if (roadWidth.compareTo(additionalFeatureNewRoadWidthA) >= 0
                        && roadWidth.compareTo(additionalFeatureNewRoadWidthB) < 0) {
                    isAccepted = floorAbvGround.compareTo(additionalFeatureFloorsNewAcceptedAB) <= 0;
                    requiredFloorCount = LESS_THAN_EQUAL_TO + additionalFeatureFloorsNewAcceptedAB;
                } else if (roadWidth.compareTo(additionalFeatureNewRoadWidthB) >= 0
                        && roadWidth.compareTo(additionalFeatureNewRoadWidthC) < 0) {
                    isAccepted = floorAbvGround.compareTo(additionalFeatureFloorsNewAcceptedBC) <= 0;
                    requiredFloorCount = LESS_THAN_EQUAL_TO + additionalFeatureFloorsNewAcceptedBC;
                }
            }

            if (errors.isEmpty() && StringUtils.isNotBlank(requiredFloorCount)) {
                ReportScrutinyDetail detail = new ReportScrutinyDetail();
                detail.setRuleNo(RULE_4_4_4);
                detail.setDescription(NO_OF_FLOORS);
                detail.setAreaType(typeOfArea);
                detail.setRoadWidth(roadWidth.toString());
                detail.setPermissible(requiredFloorCount);
                detail.setProvided(String.valueOf(block.getBuilding().getFloorsAboveGround()));
                detail.setStatus(isAccepted ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());

                Map<String, String> details = mapReportDetails(detail);
                addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
            }
        }
    }

    /**
     * Validates the height of individual floors against minimum and maximum requirements.
     * Checks different requirements for regular floors vs stilt floors.
     *
     * @param pl The plan to validate
     * @param errors Map to store validation errors
     * @param additionalFeatureMinRequiredFloorHeight Minimum required floor height
     * @param additionalFeatureMaxPermissibleFloorHeight Maximum permissible floor height
     * @param additionalFeatureStiltFloor Required height for stilt floors
     */
    private void validateHeightOfFloors(Plan pl, HashMap<String, String> errors, BigDecimal additionalFeatureMinRequiredFloorHeight, BigDecimal additionalFeatureMaxPermissibleFloorHeight, BigDecimal additionalFeatureStiltFloor) {
        LOG.info(INSIDE_HIEGHT_OF_FLOOR);
        for (Block block : pl.getBlocks()) {

            boolean isAccepted = false;
            ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
            scrutinyDetail.addColumnHeading(1, RULE_NO);
            scrutinyDetail.addColumnHeading(2, FLOOR_NO);
            scrutinyDetail.addColumnHeading(3, MIN_REQUIRED);
            scrutinyDetail.addColumnHeading(4, PROVIDED);
            scrutinyDetail.addColumnHeading(5, STATUS);
            scrutinyDetail.setKey(BLOCK + block.getNumber() + UNDERSCORE + HEIGHT_OF_FLOOR);
            OccupancyTypeHelper occupancyTypeHelper = block.getBuilding().getMostRestrictiveFarHelper();
            for (Floor floor : block.getBuilding().getFloors()) {
                BigDecimal floorHeight = floor.getFloorHeights() != null ? floor.getFloorHeights().get(0)
                        : BigDecimal.ZERO;

                int floorNumber = floor.getNumber();

                String minRequiredFloorHeight = StringUtils.EMPTY;
                String maxPermissibleFloorHeight = StringUtils.EMPTY;

                minRequiredFloorHeight = additionalFeatureMinRequiredFloorHeight.toString() + DcrConstants.IN_METER;
                maxPermissibleFloorHeight = additionalFeatureMaxPermissibleFloorHeight.toString() + DcrConstants.IN_METER;
                floor.setIsStiltFloor(false);

                if (floor.getIsStiltFloor() == false) {
                    if (floorHeight.compareTo(additionalFeatureMinRequiredFloorHeight) >= 0
                    ) {
                        isAccepted = true;
                    }
                } else if (floor.getIsStiltFloor() == true) {
                    if (floorHeight.compareTo(additionalFeatureStiltFloor) >= 0
                    ) {
                        isAccepted = true;
                    }
                }

                if (errors.isEmpty() && StringUtils.isNotBlank(minRequiredFloorHeight)
                        && StringUtils.isNotBlank(maxPermissibleFloorHeight)) {
                    ReportScrutinyDetail detail = new ReportScrutinyDetail();
                    detail.setRuleNo(RULEROOMHT);
                    detail.setFloorNo(String.valueOf(floorNumber));
                    detail.setMinRequired(minRequiredFloorHeight);
                    detail.setProvided(floorHeight.toString() + DcrConstants.IN_METER);
                    detail.setStatus(isAccepted ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());

                    Map<String, String> details = mapReportDetails(detail);
                    addScrutinyDetailtoPlan(scrutinyDetail, pl, details);

                }
            }

        }
    }

    /**
     * Validates the total building height against regulations based on road width and setbacks.
     * Includes lift requirements for buildings ≥12m or ≥4 floors.
     *
     * @param pl The plan to validate
     * @param errors Map to store validation errors
     * @param roadWidth Width of the road
     */
    private void validateHeightOfBuilding(Plan pl, HashMap<String, String> errors, BigDecimal roadWidth,
                                          BigDecimal additionalFeatureBuildingHeightA,
                                          BigDecimal additionalFeatureBuildingHeightB,
                                          BigDecimal additionalFeatureBuildingHeightServiceFloor,
                                          BigDecimal additionalFeatureTotalBuildingHeight,
                                          BigDecimal additionalFeatureBuildingHeightStiltParking,
                                          BigDecimal additionalFeatureBuildingHeightRoofTanks,
                                          BigDecimal additionalFeatureBuildingHeightChimney,
                                          BigDecimal additionalFeatureBuildingHeightServiceRooms,
                                          BigDecimal additionalFeatureBuildingHeightStairCovers,
                                          BigDecimal additionalFeatureBuildingHeightRoofArea,
                                          BigDecimal additionalFeatureBuildingHeightRWH,
                                          BigDecimal additionalFeatureBuildingHeightCappedPermitted,
                                          BigDecimal additionalFeatureBuildingHeightMaxPermitted
    ) {

        for (Block block : pl.getBlocks()) {
            boolean isAccepted = false;
            boolean isServiceFloorAccepted = true;
            String ruleNo = RULE_4_4_4;
            LOG.info("Validating height of building for block: " + block.getNumber());

            if (block.getBuilding() != null && block.getBuilding().getBuildingHeight() != null && !block.getBuilding().getFloors().isEmpty())
                if (block.getBuilding().getBuildingHeight().compareTo(additionalFeatureBuildingHeightA) >= 0
                        || block.getBuilding().getFloors().size() >= additionalFeatureBuildingHeightB.intValue()) {
                    ScrutinyDetail scrutinyDetail = getNewScrutinyDetailBuildingHeight(
                            BLOCK + block.getNumber() + UNDERSCORE + HEIGHT_OF_BUILDING);
                    String requiredBuildingHeight = StringUtils.EMPTY;

                    // Check if building requires lift (≥12m or ≥4 floors)
                    validateLiftRequirement(pl, errors, block);

                    // Get building height excluding exempted components
                    BigDecimal buildingHeight = calculateEffectiveBuildingHeight(pl, block,
                            additionalFeatureBuildingHeightStiltParking,
                            additionalFeatureBuildingHeightRoofTanks,
                            additionalFeatureBuildingHeightChimney,
                            additionalFeatureBuildingHeightServiceRooms,
                            additionalFeatureBuildingHeightStairCovers,
                            additionalFeatureBuildingHeightRoofArea,
                            additionalFeatureBuildingHeightRWH
                    );

                    BigDecimal serviceFloorHeight = BigDecimal.ZERO;
                    if (!block.getBuilding().getServiceFloors().isEmpty()) {
                        LOG.info("Checking Service Floor Height...");
                        for (ServiceFloor serviceFloor : block.getBuilding().getServiceFloors()) {
                            if (serviceFloorHeight.compareTo(serviceFloor.getHeight()) < 0) {
                                serviceFloorHeight = serviceFloor.getHeight();
                            }
                        }
                        if (serviceFloorHeight.compareTo(additionalFeatureBuildingHeightServiceFloor) > 0) {
                            isServiceFloorAccepted = false;
                            errors.put(SERVICE_FLOOR_ERROR, SERVICE_FLOOR_ERROR_DESC);
                            pl.addErrors(errors);
                        }
                    }

                    // Calculate maximum permitted height based on road width and setback
                    BigDecimal maxPermittedHeight = validateMaxPermittedHeight(pl, block, roadWidth, additionalFeatureBuildingHeightCappedPermitted, additionalFeatureBuildingHeightMaxPermitted);

                    // Validate height against maximum permitted
                    isAccepted = buildingHeight.compareTo(maxPermittedHeight) <= 0;
                    requiredBuildingHeight = LESS_THAN_EQUAL_TO + maxPermittedHeight.toString();

                    if (errors.isEmpty() && StringUtils.isNotBlank(requiredBuildingHeight)) {
                        ReportScrutinyDetail detail = new ReportScrutinyDetail();
                        detail.setRuleNo(ruleNo);
                        detail.setDescription(HEIGHT_BUILDING);
                        detail.setPermissible(requiredBuildingHeight);
                        detail.setProvided(String.valueOf(buildingHeight));
                        detail.setStatus(isAccepted && isServiceFloorAccepted ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());
                        detail.setRemarks((buildingHeight.compareTo(additionalFeatureTotalBuildingHeight) > 0
                                ? CLEARANCE_FROM_STATE_SERVICE_MANDATORY + "\n"
                                : EMPTY_STRING)
                                + (pl.getPlanInformation().getNocNearDefenceAerodomes().equals(YES)
                                ? HEIGHT_SUBJECT_TO_CIVIL_AVIATION_AUTHORITY
                                : EMPTY_STRING)
                        );

                        Map<String, String> details = mapReportDetails(detail);
                        addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
                    }
                }
        }
    }

    /**
     * Calculates effective building height by excluding exempted components like
     * stilt parking, roof tanks, chimneys, service rooms, stair covers, and RWH structures.
     *
     * @param pl The plan being processed
     * @param block The building block
     * @return Effective building height after exemptions
     */
    private BigDecimal calculateEffectiveBuildingHeight(Plan pl, Block block,
                                                        BigDecimal additionalFeatureBuildingHeightStiltParking,
                                                        BigDecimal additionalFeatureBuildingHeightRoofTanks,
                                                        BigDecimal additionalFeatureBuildingHeightChimney,
                                                        BigDecimal additionalFeatureBuildingHeightServiceRooms,
                                                        BigDecimal additionalFeatureBuildingHeightStairCovers,
                                                        BigDecimal additionalFeatureBuildingHeightRoofArea,
                                                        BigDecimal additionalFeatureBuildingHeightRWH

    ) {
        BigDecimal totalHeight = block.getBuilding().getBuildingHeight();
        LOG.info("Calculating effective building height for block: " + block.getNumber());

        if(totalHeight != null) {
            // Exclude stilt/ground-level parking (max 3.0m) if earthquake resistance measures adopted
            if (!pl.getParkingDetails().getStilts().isEmpty() && pl.getPlanInformation().isEarthquakeResistant()) {
                BigDecimal parkingHeight = pl.getParkingDetails().getStilts().get(0).getHeight();
                if (parkingHeight.compareTo(additionalFeatureBuildingHeightStiltParking) <= 0) {
                    LOG.info("Excluding stilt parking height: " + parkingHeight);
                    totalHeight = totalHeight.subtract(parkingHeight);
                }
            }

            // Roof tanks and supports ≤ 2.0 m height.
            if(block.getRoofTanks().isEmpty()){
                pl.addError(NO_ROOF_TANKS_FOUND, NO_ROOF_TANK_DESC);
            } else {
                BigDecimal maxTankHeight = block.getRoofTanks().stream().reduce(BigDecimal::max).get();
                if (maxTankHeight.compareTo(additionalFeatureBuildingHeightRoofTanks) <= 0) {
                    LOG.info("Excluding roof tank height: " + maxTankHeight);
                    totalHeight = totalHeight.subtract(maxTankHeight);
                }
            }

            // Calulating maximum architecture's height as well as service room's height also
            BigDecimal maxArchitectureHeight = BigDecimal.ZERO;
            BigDecimal maxServiceRoomHeight = BigDecimal.ZERO;

            for (Floor floor : block.getBuilding().getFloors()) {
                if (!floor.getArchitecturalFeature().isEmpty())
                    for (ArchitecturalFeature architecturalFeature : floor.getArchitecturalFeature()) {
                        for (RoomHeight roomHeight : architecturalFeature.getHeights()) {
                            if (maxArchitectureHeight.compareTo(roomHeight.getHeight()) < 0) {
                                LOG.info("Found architectural feature height: " + roomHeight.getHeight());
                                maxArchitectureHeight = roomHeight.getHeight();
                            }
                        }
                    }
                if (floor.getUnits() != null && !floor.getUnits().isEmpty()) {
                    for (FloorUnit floorUnit : floor.getUnits())
                        if (!floorUnit.getServiceRooms().isEmpty())
                            for (ServiceRoom serviceRoom : floorUnit.getServiceRooms()) {
                                for (RoomHeight roomHeight : serviceRoom.getHeights()) {
                                    if (maxServiceRoomHeight.compareTo(roomHeight.getHeight()) < 0) {
                                        LOG.info("Found service room height: " + roomHeight.getHeight());
                                        maxServiceRoomHeight = roomHeight.getHeight();
                                    }
                                }
                            }
                }

            }

            // Chimneys and architectural features ≤ 1.5 m height.
            if(block.getChimneys().isEmpty()){
                pl.addError(NO_CHIMNEYS_FOUND, NO_CHIMNEYS_DESC);
            } else {
                if(!block.getChimneys().isEmpty()) {
                    BigDecimal maxChimneyHeight = block.getChimneys().stream().reduce(BigDecimal::max).get();
                    if (maxChimneyHeight.compareTo(additionalFeatureBuildingHeightChimney) <= 0) {
                        LOG.info("Excluding chimney height: " + maxChimneyHeight);
                        totalHeight = totalHeight.subtract(maxChimneyHeight);
                    }
                }

                if (maxArchitectureHeight.compareTo(additionalFeatureBuildingHeightChimney) <= 0) {
                    LOG.info("Excluding chimney height: " + maxArchitectureHeight);
                    totalHeight = totalHeight.subtract(maxArchitectureHeight);
                }
                // Subtracting servicerooms heights such as ventilation, ac and lift rooms
                if (maxServiceRoomHeight.compareTo(additionalFeatureBuildingHeightServiceRooms) <= 0) {
                    LOG.info("Excluding service room height: " + maxServiceRoomHeight);
                    totalHeight = totalHeight.subtract(maxServiceRoomHeight);
                }
            }

            // Subtracting staircovers height if less than or equalto 3.0
            if(!block.getStairCovers().isEmpty()) {
                BigDecimal maxStairCoverHeight = block.getStairCovers().stream().reduce(BigDecimal::max).get();
                if (maxStairCoverHeight.compareTo(additionalFeatureBuildingHeightStairCovers) <= 0) {
                    LOG.info("Excluding stair cover height: " + maxStairCoverHeight);
                    totalHeight = totalHeight.subtract(maxStairCoverHeight);
                }
            }

            // Rooftop Assam Type structures (e.g., rainwater harvesting) covering ≤ 50% of roof area, restricted to 2.1 m height.
            if(!pl.getUtility().getRainWaterHarvest().isEmpty()) {
                BigDecimal rainWaterHarvestArea = BigDecimal.ZERO;
                BigDecimal rwhHeight = BigDecimal.ZERO;
                for (RainWaterHarvesting rwh : pl.getUtility().getRainWaterHarvest()) {
                    if(rwh.getArea() != null) {
                        rainWaterHarvestArea = rainWaterHarvestArea.add(rwh.getArea());
                        if (rwhHeight.compareTo(rwh.getHeight()) < 0) {
                            LOG.info("Found RWH structure height: " + rwh.getHeight());
                            rwhHeight = rwh.getHeight();
                        }
                    }
                }

                BigDecimal maxRoofArea = BigDecimal.ZERO;
                for (Floor floor : block.getBuilding().getFloors()) {
                    if(floor.getRoofAreas() != null) {
                        for (RoofArea roofArea : floor.getRoofAreas()) {
                            if (maxRoofArea.compareTo(roofArea.getArea()) < 0) {
                                LOG.info("Found roof area: " + roofArea.getArea());
                                maxRoofArea = roofArea.getArea();
                            }
                        }
                    }
                }

                if (maxRoofArea.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal coverageRatio = rainWaterHarvestArea
                            .divide(maxRoofArea, 2, RoundingMode.HALF_UP);

                    boolean roofAreaCheck = coverageRatio.compareTo(additionalFeatureBuildingHeightRoofArea) <= 0;
                    boolean rwhHeightCheck = pl.getUtility().getRainWaterHarvest().stream()
                            .allMatch(rwh -> rwh.getHeight().compareTo(additionalFeatureBuildingHeightRWH) <= 0);

                    if (roofAreaCheck && rwhHeightCheck) {
                        LOG.info("Found roof area: " + coverageRatio + " and RWH height: " + rwhHeight);
                        totalHeight = totalHeight.subtract(rwhHeight);
                    }
                }
            }

        }
        return totalHeight;
    }

    /**
     * Calculates maximum permitted building height based on road width and front setback.
     * Uses formula: 1.5 × (road width + capped front setback).
     *
     * @param roadWidth Width of the road
     * @param additionalFeatureBuildingHeightCappedPermitted Maximum front setback considered (16m)
     * @param additionalFeatureBuildingHeightMaxPermitted Multiplier factor (1.5)
     * @return Maximum permitted building height
     */
    private BigDecimal validateMaxPermittedHeight(Plan pl, Block block, BigDecimal roadWidth, BigDecimal additionalFeatureBuildingHeightCappedPermitted, BigDecimal additionalFeatureBuildingHeightMaxPermitted) {
        BigDecimal frontSetback = BigDecimal.ZERO;
        LOG.info("Calculating maximum permitted height based on road width: " + roadWidth);

        // Get front setback
        if(!block.getSetBacks().isEmpty() || block.getSetBacks() != null) {
            List<SetBack> setBacks = block.getSetBacks();
            for (SetBack setBack : setBacks) {
                Yard frontYard = setBack.getFrontYard();
                if (frontYard != null && frontYard.getMinimumDistance() != null) {
                    frontSetback = frontYard.getMinimumDistance();
                    break;
                }
            }
        }else{
            pl.addError(NO_SETBACKS_FOUND, NO_SETBACK_DESC);
        }

        // Cap front setback at 16m as per regulations
        BigDecimal cappedFrontSetback = frontSetback.min(additionalFeatureBuildingHeightCappedPermitted);
        LOG.info("Capped front setback: " + cappedFrontSetback);
        BigDecimal maxHeight = additionalFeatureBuildingHeightMaxPermitted.multiply(roadWidth.add(cappedFrontSetback));

        return maxHeight.setScale(DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS);
    }

    /**
     * Validates lift requirement for buildings ≥12m height or ≥4 floors.
     * Adds error if lift is not provided when required.
     *
     * @param pl The plan to validate
     * @param errors Map to store validation errors
     * @param block The building block to check
     */
    private void validateLiftRequirement(Plan pl, HashMap<String, String> errors, Block block) {
        LOG.info("Validating lift requirement for block: " + block.getNumber());
        boolean hasLift = false;
        for (Floor floor : block.getBuilding().getFloors()) {
            if (floor.getLifts() != null && !floor.getLifts().isEmpty()) {
                hasLift = true;
                break;
            }
        }

        if (!hasLift) {
            LOG.info("Lift not found in building requiring lift.");
            errors.put(LIFT_ERROR, LIFT_ERROR_DESC);
            pl.addErrors(errors);
        }
    }

    /**
     * Validates minimum plinth height requirements for all blocks.
     * Retrieves plinth height rules from cache and validates against provided heights.
     *
     * @param pl The plan to validate
     * @param errors Map to store validation errors
     */
    private void validatePlinthHeight(Plan pl, HashMap<String, String> errors) {
        for (Block block : pl.getBlocks()) {

            BigDecimal	plintHeight = BigDecimal.ZERO;
            boolean isAccepted = false;
            BigDecimal minPlinthHeight = BigDecimal.ZERO;
            String blkNo = block.getNumber();
            ScrutinyDetail scrutinyDetail = getNewScrutinyDetail(BLOCK + blkNo + UNDERSCORE + PLINTH);
            List<BigDecimal> plinthHeights = block.getPlinthHeight();

            List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.PLINTH_HEIGHT.getValue(), false);
            Optional<PlinthHeightRequirement> matchedRule = rules.stream()
                    .filter(PlinthHeightRequirement.class::isInstance)
                    .map(PlinthHeightRequirement.class::cast)
                    .findFirst();

            if (matchedRule.isPresent()) {
                plintHeight = matchedRule.get().getPermissible();
            } else {
                plintHeight = BigDecimal.ZERO;
            }

            if (!plinthHeights.isEmpty()) {
                minPlinthHeight = plinthHeights.stream().reduce(BigDecimal::min).get().setScale(2, BigDecimal.ROUND_HALF_UP);
                if (minPlinthHeight.compareTo(plintHeight) <= 0) {
                    isAccepted = true;
                }
            } else {
                String plinthHeightLayer = String.format(DxfFileConstants.LAYER_PLINTH_HEIGHT, block.getNumber());
                errors.put(plinthHeightLayer, PLINTH_HEIGHT_IS_NOT_DEFINED_IN_LAYER + plinthHeightLayer);
                pl.addErrors(errors);
            }

            if (errors.isEmpty()) {
                ReportScrutinyDetail detail = new ReportScrutinyDetail();
                detail.setRuleNo(RULE);
                detail.setDescription(MIN_PLINTH_HEIGHT_DESC);
                detail.setPermissible(EMPTY_STRING + plintHeight);
                detail.setProvided(String.valueOf(minPlinthHeight));
                detail.setStatus(isAccepted ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());

                Map<String, String> details = mapReportDetails(detail);
                addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
            }
        }
    }

    /**
     * Validates basement/cellar restrictions based on occupancy type and plot area.
     * Different limits apply for residential vs commercial buildings and plot sizes.
     *
     * @param pl The plan to validate
     * @param errors Map to store validation errors
     * @param additionalFeatureBasementPlotArea Plot area threshold for basement restrictions
     * @param additionalFeatureBasementAllowed Maximum number of basements allowed
     */
    private void validateBasement(Plan pl, HashMap<String, String> errors, BigDecimal additionalFeatureBasementPlotArea, BigDecimal additionalFeatureBasementAllowed) {
        for (Block block : pl.getBlocks()) {

            boolean isAccepted = false;
            String allowedBsmnt = null;
            String blkNo = block.getNumber();
            ScrutinyDetail scrutinyDetail = getNewScrutinyDetail(BLOCK + blkNo + UNDERSCORE + BASEMENT_CELLAR);
            List<SetBack> setBacks = block.getSetBacks();
            List<SetBack> basementSetbacks = setBacks.stream().filter(setback -> setback.getLevel() < 0)
                    .collect(Collectors.toList());
            OccupancyTypeHelper mostRestrictiveFarHelper = pl.getVirtualBuilding() != null
                    ? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
                    : null;

            if (!basementSetbacks.isEmpty()) {
                if (mostRestrictiveFarHelper != null && mostRestrictiveFarHelper.getType() != null
                        && (DxfFileConstants.A_AF.equalsIgnoreCase(mostRestrictiveFarHelper.getSubtype().getCode())
                        || DxfFileConstants.A_R
                        .equalsIgnoreCase(mostRestrictiveFarHelper.getSubtype().getCode())
                        || DxfFileConstants.F.equalsIgnoreCase(mostRestrictiveFarHelper.getType().getCode()))
                        && pl.getPlot() != null
                        && pl.getPlot().getArea().compareTo(additionalFeatureBasementPlotArea) <= 0) {
                    isAccepted = basementSetbacks.size() <= 1 ? true : false;
                    allowedBsmnt = additionalFeatureBasementAllowed.toString();
                } else if (mostRestrictiveFarHelper != null && mostRestrictiveFarHelper.getType() != null
                        && mostRestrictiveFarHelper.getSubtype() != null
                        && (DxfFileConstants.A_AF.equalsIgnoreCase(mostRestrictiveFarHelper.getSubtype().getCode())
                        || DxfFileConstants.A_R
                        .equalsIgnoreCase(mostRestrictiveFarHelper.getSubtype().getCode())
                        || DxfFileConstants.F.equalsIgnoreCase(mostRestrictiveFarHelper.getType().getCode()))) {
                    isAccepted = basementSetbacks.size() <= 2 ? true : false;
                    allowedBsmnt = additionalFeatureBasementAllowed.toString();
                }

                ReportScrutinyDetail detail = new ReportScrutinyDetail();
                detail.setRuleNo(RULE_47);
                detail.setDescription(MAX_BSMNT_CELLAR);
                detail.setPermissible(allowedBsmnt);
                detail.setProvided(String.valueOf(basementSetbacks.size()));
                detail.setStatus(isAccepted ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());

                Map<String, String> details = mapReportDetails(detail);
                addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
            }
        }
    }

    /**
     * Validates green building and sustainability provisions for plots above specified area.
     * Checks different requirements based on plot area ranges and occupancy types.
     *
     * @param pl The plan to validate
     * @param errors Map to store validation errors
     * @param afGreenBuildingValueA-D Plot area thresholds for different green building requirements
     */
    private void validateGreenBuildingsAndSustainability(Plan pl, HashMap<String, String> errors,
                                                         BigDecimal afGreenBuildingValueA,
                                                         BigDecimal afGreenBuildingValueB,
                                                         BigDecimal afGreenBuildingValueC,
                                                         BigDecimal afGreenBuildingValueD
    ) {
        OccupancyTypeHelper mostRestrictiveFarHelper = pl.getVirtualBuilding() != null
                ? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
                : null;
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.setKey(COM_GREEN_BUILDINGS_SUSTAINABILITY);
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, REQUIRED);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        if (pl.getPlot() != null && pl.getPlot().getArea().compareTo(afGreenBuildingValueA) >= 0) {

            if (StringUtils.isNotBlank(pl.getPlanInformation().getProvisionsForGreenBuildingsAndSustainability())
                    && pl.getPlanInformation().getProvisionsForGreenBuildingsAndSustainability().equals(YES)) {

                if (mostRestrictiveFarHelper != null && mostRestrictiveFarHelper.getType() != null
                        && DxfFileConstants.A.equalsIgnoreCase(mostRestrictiveFarHelper.getType().getCode())) {

                    if (pl.getPlot().getArea().compareTo(afGreenBuildingValueA) >= 0
                            && pl.getPlot().getArea().compareTo(afGreenBuildingValueB) < 0) {
                        validate1a(pl, scrutinyDetail);
                        validate2a(pl, scrutinyDetail);
                        validate2b(pl, scrutinyDetail);
                        validate4a(pl, scrutinyDetail);

                    } else if (pl.getPlot().getArea().compareTo(afGreenBuildingValueB) >= 0
                            && pl.getPlot().getArea().compareTo(afGreenBuildingValueC) < 0) {
                        validate1a(pl, scrutinyDetail);
                        validate2b(pl, scrutinyDetail);
                        validate4a(pl, scrutinyDetail);

                    } else if (pl.getPlot().getArea().compareTo(afGreenBuildingValueC) >= 0
                            && pl.getPlot().getArea().compareTo(afGreenBuildingValueD) < 0) {
                        validate1a(pl, scrutinyDetail);
                        validate2a(pl, scrutinyDetail);
                        validate2b(pl, scrutinyDetail);
                        validate4a(pl, scrutinyDetail);

                    } else {
                        validate1a(pl, scrutinyDetail);
                        validate2a(pl, scrutinyDetail);
                        validate2b(pl, scrutinyDetail);
                        validate4a(pl, scrutinyDetail);
                    }
                } else {

                    if (pl.getPlot().getArea().compareTo(afGreenBuildingValueA) >= 0
                            && pl.getPlot().getArea().compareTo(afGreenBuildingValueB) < 0) {
                        validate1a(pl, scrutinyDetail);
                        validate2b(pl, scrutinyDetail);
                        validate4a(pl, scrutinyDetail);

                    } else if (pl.getPlot().getArea().compareTo(afGreenBuildingValueB) >= 0
                            && pl.getPlot().getArea().compareTo(afGreenBuildingValueC) < 0) {
                        validate1a(pl, scrutinyDetail);
                        validate2a(pl, scrutinyDetail);
                        validate2b(pl, scrutinyDetail);
                        validate4a(pl, scrutinyDetail);

                    } else if (pl.getPlot().getArea().compareTo(afGreenBuildingValueC) >= 0
                            && pl.getPlot().getArea().compareTo(afGreenBuildingValueD) < 0) {
                        validate1a(pl, scrutinyDetail);
                        validate2a(pl, scrutinyDetail);
                        validate2b(pl, scrutinyDetail);
                        validate4a(pl, scrutinyDetail);
                    } else {
                        validate1a(pl, scrutinyDetail);
                        validate2a(pl, scrutinyDetail);
                        validate2b(pl, scrutinyDetail);
                        validate4a(pl, scrutinyDetail);
                    }
                }
                pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail);

            } else {
                errors.put(GREEN_BUILDINGS_AND_SUSTAINABILITY_PROVISIONS_ERROR_CODE,
                        GREEN_BUILDINGS_AND_SUSTAINABILITY_PROVISIONS_ERROR_MSG);
                pl.addErrors(errors);
            }
        }

    }

    /**
     * Validates segregation of waste requirement (Rule 55.4a).
     *
     * @param pl The plan to validate
     * @param scrutinyDetail Scrutiny detail object to add results
     */
    private void validate4a(Plan pl, ScrutinyDetail scrutinyDetail) {
        if (pl.getUtility().getSegregationOfWaste() != null && !pl.getUtility().getSegregationOfWaste().isEmpty()) {
            addDetails(scrutinyDetail, FIFTY_FIVE_FOUR_A, SEG_OF_WASTE, SEG_OF_WASTE_DETAILS,
                    PROVIDED_SEG_OF_WASTE_DETAILS, Result.Accepted.getResultVal());
        }
    }

    /**
     * Validates solar water heating system requirement (Rule 55.2b).
     *
     * @param pl The plan to validate
     * @param scrutinyDetail Scrutiny detail object to add results
     */
    private void validate2b(Plan pl, ScrutinyDetail scrutinyDetail) {
        if (pl.getUtility().getSolarWaterHeatingSystems() != null
                && !pl.getUtility().getSolarWaterHeatingSystems().isEmpty()) {
            addDetails(scrutinyDetail, FIFTY_FIVE_TWO_B, INSTALL_SOLAR_ASSISTED_WATER_HEATING_SYSTEM,
                    SOLAR_ASSISTED_WATER_HEATING_SYSTEM_DETAILS,
                    PROVIDED_SOLAR_ASSISTED_WATER_HEATING_SYSTEM_DETAILS, Result.Accepted.getResultVal());
        }
    }

    /**
     * Validates solar photovoltaic panel requirement (Rule 55.2a).
     *
     * @param pl The plan to validate
     * @param scrutinyDetail Scrutiny detail object to add results
     */
    private void validate2a(Plan pl, ScrutinyDetail scrutinyDetail) {
        if (pl.getUtility().getSolar() != null && !pl.getUtility().getSolar().isEmpty()) {
            addDetails(scrutinyDetail, FIFTY_FIVE_TWO_A, INSTALL_SOLAR_PHOTOVOLTAIC_PANELS,
                    SOLAR_PHOTOVOLTAIC_PANEL_DETAILS, PROVIDED_SOLAR_PHOTOVOLTAIC_PANELS,
                    Result.Accepted.getResultVal());
        }
    }

    /**
     * Validates rainwater harvesting requirement (Rule 10.3).
     *
     * @param pl The plan to validate
     * @param scrutinyDetail Scrutiny detail object to add results
     */
    private void validate1a(Plan pl, ScrutinyDetail scrutinyDetail) {
        if (pl.getUtility().getRainWaterHarvest() != null && !pl.getUtility().getRainWaterHarvest().isEmpty()) {
            addDetails(scrutinyDetail, TEN_3, RAIN_WATER_HARVESTING, RAIN_WATER_HARVESTING_DETAILS,
                    PROVIDED_RAIN_WATER_HARVESTING, Result.Accepted.getResultVal());
        } else {
            addDetails(scrutinyDetail, TEN_3, RAIN_WATER_HARVESTING, RAIN_WATER_HARVESTING_DETAILS,
                    NOT_PROVIDED_RAIN_WATER_HARVESTING, Result.Not_Accepted.getResultVal());
        }
    }

    /**
     * Adds validation details to scrutiny detail object.
     *
     * @param scrutinyDetail Scrutiny detail object
     * @param rule Rule number
     * @param description Description of the requirement
     * @param required Required provision
     * @param provided Provided provision
     * @param status Validation status (Accepted/Not_Accepted)
     */
    private void addDetails(ScrutinyDetail scrutinyDetail, String rule, String description, String required,
                            String provided, String status) {

        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(rule);
        detail.setDescription(description);
        detail.setRequired(required);
        detail.setProvided(provided);
        detail.setStatus(status);
        Map<String, String> details = mapReportDetails(detail);

        scrutinyDetail.getDetail().add(details);
    }

    /**
     * Creates new scrutiny detail object with road area specific column headings.
     *
     * @param key Unique key for the scrutiny detail
     * @return Configured ScrutinyDetail object
     */
    private ScrutinyDetail getNewScrutinyDetailRoadArea(String key) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, DxfFileConstants.AREA_TYPE);
        scrutinyDetail.addColumnHeading(4, DxfFileConstants.ROAD_WIDTH);
        scrutinyDetail.addColumnHeading(5, PERMISSIBLE);
        scrutinyDetail.addColumnHeading(6, PROVIDED);
        scrutinyDetail.addColumnHeading(7, STATUS);
        scrutinyDetail.setKey(key);
        return scrutinyDetail;
    }

    /**
     * Creates new scrutiny detail object with building height specific column headings.
     *
     * @param key Unique key for the scrutiny detail
     * @return Configured ScrutinyDetail object
     */
    private ScrutinyDetail getNewScrutinyDetailBuildingHeight(String key) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, PERMISSIBLE);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        scrutinyDetail.addColumnHeading(6, REMARKS);
        scrutinyDetail.setKey(key);
        return scrutinyDetail;
    }

    /**
     * Creates new scrutiny detail object with standard column headings.
     *
     * @param key Unique key for the scrutiny detail
     * @return Configured ScrutinyDetail object
     */
    private ScrutinyDetail getNewScrutinyDetail(String key) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        scrutinyDetail.addColumnHeading(3, PERMISSIBLE);
        scrutinyDetail.addColumnHeading(4, PROVIDED);
        scrutinyDetail.addColumnHeading(5, STATUS);
        scrutinyDetail.setKey(key);
        return scrutinyDetail;
    }

    /**
     * Returns amendments map (empty for this implementation).
     *
     * @return Empty LinkedHashMap of amendments
     */
    @Override
    public Map<String, Date> getAmendments() {
        return new LinkedHashMap<>();
    }

}
