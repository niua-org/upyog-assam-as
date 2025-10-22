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

import static org.egov.edcr.constants.CommonFeatureConstants.*;
import static org.egov.edcr.constants.CommonKeyConstants.*;
import static org.egov.edcr.constants.DxfFileConstants.*;
import static org.egov.edcr.constants.EdcrReportConstants.*;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;
import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.OBJECTNOTDEFINED;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.constants.MdmsFeatureConstants;
import org.egov.common.entity.edcr.*;
import org.egov.common.entity.edcr.Balcony;
import org.egov.edcr.constants.EdcrReportConstants;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.service.ProcessPrintHelper;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Far_Assam extends Far {
    private static final Logger LOG = LogManager.getLogger(Far_Assam.class);

    @Autowired
    MDMSCacheManager cache;

    /**
     * Validates the given Plan object to ensure the plot area is defined and greater than zero.
     *
     * @param pl The Plan object to validate.
     * @return The validated Plan object with any validation errors added.
     */
    @Override
    public Plan validate(Plan pl) {
        if (pl.getPlot() == null || (pl.getPlot() != null
                && (pl.getPlot().getArea() == null || pl.getPlot().getArea().doubleValue() == 0))) {
            pl.addError(PLOT_AREA, getLocaleMessage(OBJECTNOTDEFINED, PLOT_AREA));

        }

        return pl;
    }

    /**
     * Processes the given Plan object to calculate various building metrics like FAR,
     * total built-up area, carpet area, etc., and populate the plan details accordingly.
     *
     * @param pl The Plan object to be processed.
     * @return The updated Plan object with computed values and any processing errors.
     */
    @Override
    public Plan process(Plan pl) {
        LOG.info("Starting FAR process for Plan");

        decideNocIsRequired(pl);
        LOG.info("Inside FAR process");

        HashMap<String, String> errorMsgs = new HashMap<>();
        int initialErrorCount = pl.getErrors().size();
        LOG.info("Initial error count: {}", initialErrorCount);

        validate(pl);
        LOG.info("Validation completed. Initial error count: {}, Current error count: {}", initialErrorCount, pl.getErrors().size());
        LOG.info("Plot area: {}", pl.getPlot().getArea());

        if (validationFailed(pl, initialErrorCount)) {
            LOG.warn("Validation failed for Plan: {}. Returning plan without further processing.");
            return pl;
        }

        BigDecimal totalExistingBuiltUpArea = BigDecimal.ZERO;
        BigDecimal totalExistingFloorArea   = BigDecimal.ZERO;
        BigDecimal totalBuiltUpArea         = BigDecimal.ZERO;
        BigDecimal totalFloorArea           = BigDecimal.ZERO;
        BigDecimal totalCarpetArea          = BigDecimal.ZERO;
        BigDecimal totalExistingCarpetArea  = BigDecimal.ZERO;

        // Process block occupancies
        for (Block blk : pl.getBlocks()) {
            LOG.info("Processing Block Number: {}", blk.getNumber());

            Building building = blk.getBuilding();

            BigDecimal flrArea = BigDecimal.ZERO;
            BigDecimal bltUpArea = BigDecimal.ZERO;
            BigDecimal existingFlrArea = BigDecimal.ZERO;
            BigDecimal existingBltUpArea = BigDecimal.ZERO;
            BigDecimal carpetArea = BigDecimal.ZERO;
            BigDecimal existingCarpetArea = BigDecimal.ZERO;

            for (Floor flr : building.getFloors()) {
                for (Occupancy occupancy : flr.getOccupancies()) {
                    validate2(pl, blk, flr, occupancy);

                    bltUpArea = bltUpArea.add(
                            occupancy.getBuiltUpArea() == null ? BigDecimal.ZERO : occupancy.getBuiltUpArea());
                    existingBltUpArea = existingBltUpArea.add(
                            occupancy.getExistingBuiltUpArea() == null ? BigDecimal.ZERO : occupancy.getExistingBuiltUpArea());
                    flrArea = flrArea.add(occupancy.getFloorArea());
                    existingFlrArea = existingFlrArea.add(occupancy.getExistingFloorArea());
                    
                }
            }

            for (Floor flr : building.getFloors()) {
            	 for (FloorUnit unit : flr.getUnits()) {
                for (Occupancy occupancy : unit.getOccupancies()) {
                    carpetArea = carpetArea.add(occupancy.getCarpetArea());
                    existingCarpetArea = existingCarpetArea.add(occupancy.getExistingCarpetArea());
                }
            }
        }

            building.setTotalFloorArea(flrArea);
            building.setTotalBuitUpArea(bltUpArea);
            building.setTotalExistingBuiltUpArea(existingBltUpArea);
            building.setTotalExistingFloorArea(existingFlrArea);

            if (existingBltUpArea.compareTo(bltUpArea) == 0) {
                blk.setCompletelyExisting(Boolean.TRUE);
                LOG.info("Block Number: {} is completely existing.", blk.getNumber());
            }

            // Add to run-level totals
            totalFloorArea          = totalFloorArea.add(flrArea);
            totalBuiltUpArea        = totalBuiltUpArea.add(bltUpArea);
            totalExistingBuiltUpArea= totalExistingBuiltUpArea.add(existingBltUpArea);
            totalExistingFloorArea  = totalExistingFloorArea.add(existingFlrArea);
            totalCarpetArea         = totalCarpetArea.add(carpetArea);
            totalExistingCarpetArea = totalExistingCarpetArea.add(existingCarpetArea);

            LOG.info("Block {} Totals - FloorArea: {}, BuiltUpArea: {}, ExistingFloorArea: {}, ExistingBuiltUpArea: {}, CarpetArea: {}",
                    blk.getNumber(), flrArea, bltUpArea, existingFlrArea, existingBltUpArea, carpetArea);

            // Build occupancy types for this block
            processBlockOccupancyTypes(blk);
            LOG.info("Completed processing occupancy types for Block Number: {}", blk.getNumber());
        }

        // Distinct occupancies across plan
        Set<OccupancyTypeHelper> distinctOccupancyTypes = collectDistinctOccupancyTypes(pl);
        LOG.info("Distinct occupancy types collected: {}", distinctOccupancyTypes.size());

        List<Occupancy> occupanciesForPlan = collectOccupanciesForPlan(distinctOccupancyTypes, pl);
        pl.setOccupancies(occupanciesForPlan);
        LOG.info("Total occupancies set at Plan level: {}", occupanciesForPlan.size());

        // Populate plan & virtual building with totals
        populatePlanAndVirtualBuildingDetails(pl, distinctOccupancyTypes, distinctOccupancyTypes,
                totalFloorArea, totalCarpetArea, totalExistingBuiltUpArea,
                totalExistingFloorArea, totalExistingCarpetArea, totalBuiltUpArea);
        LOG.info("Plan and virtual building details populated with totals.");

        processOccupancyInformation(pl);
        LOG.info("Occupancy information processed.");

        // All Deductions in TotalBuiltUp Area
        totalBuiltUpArea = farDeductions(pl, totalBuiltUpArea);

        // Update virtual building with deducted built-up area
        pl.getVirtualBuilding().setTotalBuitUpArea(totalBuiltUpArea);

        BigDecimal surrenderRoadArea = calculateSurrenderRoadArea(pl);
        pl.setTotalSurrenderRoadArea(surrenderRoadArea.setScale(
                DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS));
        LOG.info("Surrender road area calculated: {}", surrenderRoadArea);

        BigDecimal plotArea = calculateTotalPlotArea(pl, surrenderRoadArea);
        LOG.info("Calculated total plot area (including surrender road): {}", plotArea);

        BigDecimal providedFar = calculateProvidedFar(pl, plotArea);
        pl.setFarDetails(new FarDetails());
        pl.getFarDetails().setProvidedFar(providedFar.doubleValue());
        LOG.info("Provided FAR calculated: {}", providedFar);

        processFarComputation(pl, providedFar, plotArea, errorMsgs);
        LOG.info("FAR computation completed.");

        ProcessPrintHelper.print(pl);
        LOG.info("FAR process completed for Plan");
        return pl;
    }

    private BigDecimal farDeductions(Plan pl, BigDecimal totalBuiltUpArea) {
        LOG.info("Making Deductions in BuiltUpArea: "+ totalBuiltUpArea);
        BigDecimal parkingAndServiceFloorArea = BigDecimal.ZERO;
        BigDecimal totalAreaToDeduct = BigDecimal.ZERO;

        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.FAR.getValue(), false);
        LOG.info("Rules in Far Assam file :: " + rules);
        Optional<FarRequirement> matchedRule = rules.stream().filter(FarRequirement.class::isInstance)
                .map(FarRequirement.class::cast).filter(ruleObj -> Boolean.TRUE.equals(ruleObj.getActive())).findFirst();

        if (!matchedRule.isPresent()) {
            LOG.error("**No matching rule found for Far Deductions**");
            return totalBuiltUpArea;
        }

        FarRequirement mdmsRule = matchedRule.get();
        BigDecimal farGuardRoomArea = mdmsRule.getFarGuardRoomArea();
        BigDecimal farCareTakerRoomArea = mdmsRule.getFarCareTakerRoomArea();
        BigDecimal farCanopyLength = mdmsRule.getFarCanopyLength();
        BigDecimal farCanopyWidth = mdmsRule.getFarCanopyWidth();
        BigDecimal farCanopyHeight = mdmsRule.getFarCanopyHeight();
        BigDecimal farBalconyWidth = mdmsRule.getFarBalconyWidth();
        BigDecimal farBalconySetback = mdmsRule.getFarBalconySetback();
        BigDecimal farBalconyLength = mdmsRule.getFarBalconyLength();
        BigDecimal farEntranceLobbyArea = mdmsRule.getFarEntranceLobbyArea();
        BigDecimal farMaxBalconyExemption = mdmsRule.getFarMaxBalconyExemption();
        BigDecimal farCorridorArea = mdmsRule.getFarCorridorArea();
        BigDecimal farProjectionWidth = mdmsRule.getFarProjectionWidth();
        BigDecimal farProjectionLength = mdmsRule.getFarProjectionLength();
        BigDecimal farPermittedRoomAreaPercentage = mdmsRule.getFarPermittedRoomAreaPercentage();

        BigDecimal floorWiseAreaToDeduct = BigDecimal.ZERO;
        for (Block blk : pl.getBlocks()) {
            if (!blk.getBuilding().getFloors().isEmpty())
                for (Floor floor : blk.getBuilding().getFloors()) {
                    floorWiseAreaToDeduct = floorWiseAreaToDeduct.add(farFloorWiseDeduction(pl, blk, floor, parkingAndServiceFloorArea,
                            farEntranceLobbyArea, farMaxBalconyExemption, farCorridorArea, farProjectionLength, farProjectionWidth, farPermittedRoomAreaPercentage, totalBuiltUpArea));
                }
        }

        BigDecimal farCommonDeduction = farCommonDeductions(pl, farGuardRoomArea, farCareTakerRoomArea, farCanopyHeight, farCanopyWidth, farCanopyLength);
        validateBlockBalconies(pl, farBalconyLength, farBalconyWidth, farBalconySetback);

        totalAreaToDeduct = totalAreaToDeduct.add(floorWiseAreaToDeduct).add(farCommonDeduction);

        // allow 30% of permissible far while excluding parking and service floor areas
        BigDecimal exemptableArea = totalAreaToDeduct.subtract(parkingAndServiceFloorArea);
        BigDecimal allowedDeduction = totalBuiltUpArea.multiply(BigDecimal.valueOf(0.30));

        if (exemptableArea.compareTo(allowedDeduction) > 0) {
            LOG.info("Allowed Deduction by law is: " + allowedDeduction + " and the parking and service floor area is: " + parkingAndServiceFloorArea);
            pl.addError(DEDUCTION_LIMIT_EXCEEDED, DEDUCTION_LIMIT_EXCEEDED_DESC);

            // If exemptable area is greater than allowed deduction,
            // then deduct 30% of total build up area + parking and service floor area
            totalAreaToDeduct = allowedDeduction.add(parkingAndServiceFloorArea);
        }
        totalBuiltUpArea = totalBuiltUpArea.subtract(totalAreaToDeduct);

        LOG.info("Made All BuiltUpArea Deductions, Remaining BuiltUpArea is: " + totalBuiltUpArea);

        return totalBuiltUpArea;
    }

    private BigDecimal farCommonDeductions(Plan pl,
                                     BigDecimal farGuardRoomArea, BigDecimal farCareTakerRoomArea,
                                     BigDecimal farCanopyHeight, BigDecimal farCanopyWidth,
                                     BigDecimal farCanopyLength){
        LOG.info("Running farCommonDeductions function ...");
        BigDecimal totalAreaToDeduct = BigDecimal.ZERO;

        // sentry box and guard room (maximum of 3.5 sq. m each)
        BigDecimal guardRoomArea = BigDecimal.ZERO;
        if(pl.getGuardRoom() != null && pl.getGuardRoom().getGuardRooms() != null) {
            for (Measurement guardRoom : pl.getGuardRoom().getGuardRooms()) {
                if (guardRoom.getArea() != null) {
                    if (guardRoom.getArea().compareTo(farGuardRoomArea) <= 0) {
                        guardRoomArea = guardRoomArea.add(guardRoom.getArea());
                    } else if (guardRoom.getArea().compareTo(farGuardRoomArea) > 0) {
                        guardRoomArea = guardRoomArea.add(farGuardRoomArea);
                    }
                }
            }
            LOG.info("Guard Room Area to Deduct: {}", guardRoomArea);
            totalAreaToDeduct = totalAreaToDeduct.add(guardRoomArea);
        }

        // care taker room (maximum 8 sq. m)
        BigDecimal careTakerRoomArea = BigDecimal.ZERO;
        if(pl.getCareTakerRoom() != null && pl.getCareTakerRoom().getCareTakerRooms() != null) {
            for (Measurement ctRoom : pl.getCareTakerRoom().getCareTakerRooms()) {
                if (ctRoom.getArea() != null) {
                    if (ctRoom.getArea().compareTo(farCareTakerRoomArea) <= 0) {
                        careTakerRoomArea = careTakerRoomArea.add(ctRoom.getArea());
                    } else if (ctRoom.getArea().compareTo(farCareTakerRoomArea) > 0) {
                        careTakerRoomArea = careTakerRoomArea.add(farCareTakerRoomArea);
                    }
                }
            }
            LOG.info("Care Taker Room Area to Deduct: {}", careTakerRoomArea);
            totalAreaToDeduct = totalAreaToDeduct.add(careTakerRoomArea);
        }

        if(pl.getBlocks()!=null){
            for(Block block: pl.getBlocks()){
                if(block.getBuilding()!=null){
                    if(block.getBuilding().getCanopy() != null){
                        for(Canopy canopy: block.getBuilding().getCanopy()){
                            if(canopy.getLength().compareTo(farCanopyLength) > 0
                                    || canopy.getWidth().compareTo(farCanopyWidth) > 0
                                    || canopy.getHeight().compareTo(farCanopyHeight) > 0
                            ){
                                pl.addError(CANOPY_DIMENSION_ERROR, CANOPY_DIMENSION_ERROR_DESC);
                            }
                        }
                    }
                }
            }
        }

        return totalAreaToDeduct;
    }

    private BigDecimal farFloorWiseDeduction(Plan pl, Block blk, Floor floor, BigDecimal parkingAndServiceFloorArea,
                                       BigDecimal farEntranceLobbyArea, BigDecimal farMaxBalconyExemption, BigDecimal farCorridorArea,
                                       BigDecimal farProjectionLength, BigDecimal farProjectionWidth, BigDecimal farPermittedRoomAreaPercentage, BigDecimal totalBuiltUpArea){

        LOG.info("Starting FloorWise Deduction of FAR builtUpArea...");

        BigDecimal totalAreaToDeduct = BigDecimal.ZERO;

        // Subtracting Basement Parking area from total build up area
        if(floor.getParking() != null && floor.getParking().getBasementCars() != null) {
            for(Measurement basementCar: floor.getParking().getBasementCars()){
                if(basementCar.getArea().compareTo(BigDecimal.valueOf(0)) > 0){
                    totalAreaToDeduct = totalAreaToDeduct.add(basementCar.getArea());
                    parkingAndServiceFloorArea = parkingAndServiceFloorArea.add(basementCar.getArea());
                    LOG.info("Subtracting Basement Parking Area from TotalBuildUpArea: " + basementCar.getArea());
                }
            }
        }

        // Subtracting Basement ServiceRooms Area from total build up area
        if(floor.getNumber() < 0){
            if (floor.getUnits() != null && !floor.getUnits().isEmpty())
                for (FloorUnit floorUnit : floor.getUnits())
                    for (Measurement serviceFloor : floorUnit.getServiceRooms()) {
                        totalAreaToDeduct = totalAreaToDeduct.add(serviceFloor.getArea());
                        parkingAndServiceFloorArea = parkingAndServiceFloorArea.add(serviceFloor.getArea());
                        LOG.info("Subtracting Basement ServiceRoom Area from totalBuildUpArea: " + serviceFloor.getArea());
                    }
        }

        // Entrance Lobby Area Deduction
        if(floor.getEntranceLobbies() != null){
            for(EntranceLobby lobby : floor.getEntranceLobbies()){
                if(lobby.getArea() != null){
                    LOG.info("Subtracting EntranceLobby Area from totalBuildUpArea: " + lobby.getArea());
                    if(lobby.getArea().compareTo(farEntranceLobbyArea) <= 0){
                        totalAreaToDeduct = totalAreaToDeduct.add(lobby.getArea());
                    }else if(lobby.getArea().compareTo(farEntranceLobbyArea) > 0){
                        totalAreaToDeduct = totalAreaToDeduct.add(farEntranceLobbyArea);
                    }
                }
            }
        }

        // Deducted balcony area
        BigDecimal balconyArea = BigDecimal.ZERO;
        if(floor.getBalconies() != null || !floor.getBalconies().isEmpty()) {
            for (Balcony balcony : floor.getBalconies()) {
                if(balcony.getBuiltUpArea() != null)
                    balconyArea = balconyArea.add(balcony.getBuiltUpArea());
            }

            if (balconyArea.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal maxBalconyExemption = totalBuiltUpArea.multiply(farMaxBalconyExemption);
                BigDecimal balconyExemption = balconyArea.compareTo(maxBalconyExemption) <= 0 ?
                        balconyArea : maxBalconyExemption;
                totalAreaToDeduct = totalAreaToDeduct.add(balconyExemption);
                LOG.info("Subtracting Balcony Area from TotalBuildUpArea: {} (max 4% exemption applied)", balconyExemption);
            }
        }

        // Corridor exemptions for specific building types (max 36 sq.m per floor)
        BigDecimal corridorExemption = BigDecimal.ZERO;
        boolean qualifiesForCorridorExemption = false;
        OccupancyTypeHelper occupancy = blk.getBuilding().getMostRestrictiveFarHelper();
        if (occupancy != null && occupancy.getType() != null) {
            String occCode = occupancy.getType().getCode();
            String subOccCode = occupancy.getSubtype() != null ? occupancy.getSubtype().getCode() : null;

            // Educational, Medical, Government/Public, Hotels
            if (B.equalsIgnoreCase(occCode) || C.equalsIgnoreCase(occCode) || J.equalsIgnoreCase(occCode)
                    || (F_H.equals(subOccCode) && pl.getPlanInformation().getFourFiveStaredHotel())) {
                qualifiesForCorridorExemption = true;
            }
        }

        if (qualifiesForCorridorExemption && floor.getCorridor() != null) {
            BigDecimal corridorArea = floor.getCorridor().getArea() != null ?
                    floor.getCorridor().getArea() : BigDecimal.ZERO;

            corridorExemption = corridorArea.compareTo(farCorridorArea) <= 0 ?
                    corridorArea : farCorridorArea;
            totalAreaToDeduct = totalAreaToDeduct.add(corridorExemption);
            LOG.info("Subtracting Corridor Area from TotalBuildUpArea: {} (max 36 sq.m per floor)", corridorExemption);
        }

        // Deducting Projection area if greater than 2% of room area
        if(floor.getNumber() > 0) {
            if(floor.getUnits() != null && !floor.getUnits().isEmpty())
                for(FloorUnit unit: floor.getUnits()) {
                    for (Room regularRoom : unit.getRegularRooms()) {
                        BigDecimal totalProjectionArea = BigDecimal.ZERO;
                        BigDecimal roomArea = regularRoom.getRooms().stream().map(Measurement::getArea).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                        BigDecimal permittedRoomArea = roomArea.multiply(farPermittedRoomAreaPercentage);

                        BigDecimal totalProjectionLength = BigDecimal.ZERO;
                        BigDecimal totalProjectionWidth = BigDecimal.ZERO;

                        for (Projections projection : regularRoom.getRoomProjections()) {
                            LOG.info("Projection's Length and width validating and deducting...");
                            if (projection.getWidth() != null && projection.getLength() != null) {
                                if (projection.getWidth().compareTo(farProjectionWidth) > 0) {
                                    pl.addError(PROJECTION_WIDTH_INCREASED,
                                            PROJECTION + projection.getNumber() + WIDTH_STRING + projection.getWidth() + PROJECTION_WIDTH_DESC);
                                } else if (projection.getWidth().compareTo(farProjectionWidth) <= 0) {
                                    totalProjectionWidth = totalProjectionWidth.add(projection.getWidth());
                                }
                                if (projection.getLength().compareTo(farProjectionLength) > 0) {
                                    pl.addError(PROJECTION_LENGTH_INCREASED,
                                            PROJECTION + projection.getNumber() + LENGTH + projection.getLength() + PROJECTION_LENGTH_DESC);
                                } else if (projection.getLength().compareTo(farProjectionLength) <= 0) {
                                    totalProjectionLength = totalProjectionLength.add(projection.getLength());
                                }
                            }
                        }

                        if (totalProjectionLength.compareTo(farProjectionLength) <= 0
                                && totalProjectionWidth.compareTo(farProjectionWidth) <= 0) {
                            totalProjectionArea = totalProjectionLength.multiply(totalProjectionWidth);
                        } else {
                            pl.addError(PROJECTION_LENGTH_WIDTH_INCREASED, PROJECTION_LENGTH_WIDTH_INCREASED_DESC);
                        }

                        if (permittedRoomArea.compareTo(totalProjectionArea) < 0) {
                            totalAreaToDeduct = totalAreaToDeduct.add(permittedRoomArea);
                            pl.addError(PROJECTION_AREA_INCREASED, PROJECTION_AREA_INCREASED_DESC);
                        } else if (permittedRoomArea.compareTo(totalProjectionArea) >= 0) {
                            totalAreaToDeduct = totalAreaToDeduct.add(totalProjectionArea);
                        }
                    }
                }
        }

        // Update occupancy deductions at floor level
        List<Occupancy> occupancies = floor.getOccupancies();
        if (!occupancies.isEmpty())
            for (Occupancy occupancyD : occupancies) {
                occupancyD.addDeduction(totalAreaToDeduct);
            }

        return totalAreaToDeduct;
    }

    /**
     * Validates balcony projections
     */

    private void validateBlockBalconies(Plan plan, BigDecimal farBalconyLength, BigDecimal farBalconyWidth, BigDecimal farBalconySetback) {
        for (Block block : plan.getBlocks()) {
            if (block.getBuilding() != null) {
                for (Floor floor : block.getBuilding().getFloors()) {
                        validateBalconyProjection(plan, block, floor, farBalconyLength, farBalconyWidth, farBalconySetback);
                }
            }
        }
    }

    private void validateBalconyProjection(Plan plan, Block block, Floor floor, BigDecimal farBalconyLength, BigDecimal farBalconyWidth, BigDecimal farBalconySetback) {

        LOG.info("Validating Balcony Projections...");
        if(floor.getNumber() > 0) {
            // Check min setback 1.5m
            if (!isBalconyWithinMinSetback(floor, farBalconySetback)) {
                plan.addError(BALCONY_SETBACK_VIOLATION,
                        BALCONY + EdcrReportConstants.AT_FLOOR + floor.getNumber() + BALCONY_SETBACK_DESC);
            }

            // Check max length 1/4 of building dimension and max width 1.5m
            if (!isBalconyWidthCompliant(block, plan, floor, farBalconyLength, farBalconyWidth)) {
                plan.addError(BALCONY_LENGTH_EXCEEDED, BALCONY_LENGTH_DESC + floor.getNumber());
            }
        }
    }

    private boolean isBalconyWithinMinSetback(Floor floor, BigDecimal farBalconySetback) {
        LOG.info("Checking if Balcony is within Minimum Setback...");
        for (BigDecimal balconyDistance: floor.getBalconyDistanceFromPlotBoundary()){
            if(balconyDistance.compareTo(farBalconySetback) <= 0)
                return false;
        }

        return true;
    }

    private boolean isBalconyWidthCompliant(Block block, Plan plan, Floor floor, BigDecimal farBalconyLength, BigDecimal farBalconyWidth) {
        LOG.info("Checking if Balcony Length is within 1/4th of Building Length...");
        BigDecimal buildingLength = block.getBuilding().getBuildingLength();
        BigDecimal quarterBuildingLength = BigDecimal.ZERO;

        if(buildingLength == null || buildingLength.compareTo(BigDecimal.ZERO) == 0){
            plan.addError(BUILDING_LENGTH_NULL, BUILDING_LENGTH_NOT_DEFINED + block.getNumber());
        }
        else{
            quarterBuildingLength = buildingLength.divide(farBalconyLength, 2, RoundingMode.HALF_UP);
        }

        if(floor.getFloorProjectedBalconies() != null || !floor.getFloorProjectedBalconies().isEmpty())
            for (Measurement projectedBalcony : floor.getFloorProjectedBalconies()) {
                if(projectedBalcony != null && projectedBalcony.getWidth().compareTo(farBalconyWidth) > 0)
                    return false;
                if (projectedBalcony != null && projectedBalcony.getWidth().compareTo(quarterBuildingLength) > 0)
                    return false;
            }

        return true;
    }

    /**
     * Checks if any new validation errors have been added since the initial error count.
     */
    private boolean validationFailed(Plan pl, int initialErrorCount) {
        int validatedErrors = pl.getErrors().size();
        LOG.info("Validating errors: before={}, after={}", initialErrorCount, validatedErrors);
        if (validatedErrors > initialErrorCount) {
            LOG.error("error" + pl.getErrors().get(PLOT_AREA));
            LOG.warn("New validation errors detected.");
            return true;
        }
        return false;
    }

    /**
     * Collects all distinct occupancy types used across all blocks in the plan.
     */
    private Set<OccupancyTypeHelper> collectDistinctOccupancyTypes(Plan pl) {
        List<OccupancyTypeHelper> plotWiseOccupancyTypes = new ArrayList<>();
        for (Block block : pl.getBlocks()) {
            for (Occupancy occupancy : block.getBuilding().getOccupancies()) {
                if (occupancy.getTypeHelper() != null) {
                    plotWiseOccupancyTypes.add(occupancy.getTypeHelper());
                }
            }
        }
        Set<OccupancyTypeHelper> distinctSet = new HashSet<>(plotWiseOccupancyTypes);
        LOG.info("Collected distinct occupancy types count: {}", distinctSet.size());
        return distinctSet;
    }

    /**
     * Populates the Plan's occupancy information based on virtual building occupancy types.
     */
    private void processOccupancyInformation(Plan pl) {
        if (pl.getVirtualBuilding() != null && !pl.getVirtualBuilding().getOccupancyTypes().isEmpty()) {
            List<String> occupancies = new ArrayList<>();
            pl.getVirtualBuilding().getOccupancyTypes().forEach(occ -> {
                if (occ.getType() != null)
                    occupancies.add(occ.getType().getName());
            });

            Set<String> distinctOccupancies = new HashSet<>(occupancies);
            pl.getPlanInformation()
                    .setOccupancy(distinctOccupancies.stream().map(String::new).collect(Collectors.joining(",")));

            LOG.info("Processed occupancy information. Distinct occupancies: {}", distinctOccupancies);
        } else {
            LOG.info("No virtual building occupancy types to process.");
        }
    }

    /**
     * Calculates the total surrender road area for the plan.
     */
    private BigDecimal calculateSurrenderRoadArea(Plan pl) {
        BigDecimal surrenderRoadArea = BigDecimal.ZERO;
        if (!pl.getSurrenderRoads().isEmpty()) {
            for (Measurement measurement : pl.getSurrenderRoads()) {
                surrenderRoadArea = surrenderRoadArea.add(measurement.getArea());
            }
        }
        LOG.info("Calculated surrender road area: {}", surrenderRoadArea);
        return surrenderRoadArea;
    }

    /**
     * Calculates the total plot area including surrender road area.
     */
    private BigDecimal calculateTotalPlotArea(Plan pl, BigDecimal surrenderRoadArea) {
        BigDecimal totalPlotArea = pl.getPlot() != null ? pl.getPlot().getArea().add(surrenderRoadArea) : BigDecimal.ZERO;
        LOG.info("Calculated total plot area: {}", totalPlotArea);
        return totalPlotArea;
    }

    /**
     * Calculates the provided Floor Area Ratio (FAR).
     */
    private BigDecimal calculateProvidedFar(Plan pl, BigDecimal plotArea) {
        if (plotArea.doubleValue() > 0) {
            BigDecimal far = pl.getVirtualBuilding().getTotalFloorArea().divide(plotArea,
                    DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS);
            LOG.info("Calculated FAR: {}", far);
            return far;
        }
        LOG.info("Plot area is zero or negative, FAR set to zero");
        return BigDecimal.ZERO;
    }

    private boolean applySpecialFarForNarrowRoad(Plan pl, BigDecimal roadWidth, BigDecimal providedFar, HashMap<String, String> errorMsgs) {
        if (roadWidth != null && roadWidth.compareTo(BigDecimal.valueOf(2.40)) == 0) {
            int allowedFloors = 2; // Ground + 1
            BigDecimal permissibleFar = BigDecimal.valueOf(125);

            int actualFloors = pl.getBlocks().stream()
                    .mapToInt(block -> block.getBuilding() != null ? block.getBuilding().getTotalFloors().intValue() : 0)
                    .max()
                    .orElse(0);

            if (actualFloors > allowedFloors) {
                String errMsg = "For 2.40m road width, only Ground + 1 floors are permitted.";
                errorMsgs.put("FAR_RULE", errMsg);
                LOG.info("Validation failed: {}", errMsg);
                return true;
            }

            if (providedFar.compareTo(permissibleFar) > 0) {
                String errMsg = "Provided FAR exceeds permissible FAR (125) for 2.40m road width.";
                errorMsgs.put("FAR_RULE", errMsg);
                LOG.info("Validation failed: {}", errMsg);
                return true;
            }

            LOG.info("Applied special FAR rule for 2.40m road width: AllowedFloors={}, PermissibleFAR={}, ActualFloors={}, ProvidedFAR={}",
                    allowedFloors, permissibleFar, actualFloors, providedFar);
            LOG.info("Applied special FAR condition for 2.40m road width: AllowedFloors={}, PermissibleFAR={}", allowedFloors, permissibleFar);
            return true;
        }
        return false;
    }


    /**
     * Computes and validates the FAR for the given plan.
     */
    private void processFarComputation(Plan pl, BigDecimal providedFar, BigDecimal plotArea, HashMap<String, String> errorMsgs) {
        OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding() != null
                ? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
                : null;

        String typeOfArea = pl.getPlanInformation().getTypeOfArea();
        BigDecimal roadWidth = pl.getPlanInformation().getRoadWidth();
        String feature = MdmsFeatureConstants.FAR;



        LOG.info("Processing FAR computation with parameters - MostRestrictiveOccupancyType: {}, TypeOfArea: {}, RoadWidth: {}",
                mostRestrictiveOccupancyType, typeOfArea, roadWidth);

        // First check special condition for 2.40m road width
        if (applySpecialFarForNarrowRoad(pl, roadWidth, providedFar, errorMsgs)) {
            return; // stop further processing if condition is applied
        }

        if (mostRestrictiveOccupancyType != null && StringUtils.isNotBlank(typeOfArea) && roadWidth != null
                && !processFarForSpecialOccupancy(pl, mostRestrictiveOccupancyType, providedFar, typeOfArea, roadWidth,
                errorMsgs)) {
            processFar(pl, mostRestrictiveOccupancyType, providedFar, typeOfArea, roadWidth, errorMsgs,
                    feature, mostRestrictiveOccupancyType.getType().getName());
            LOG.info("Processed FAR for normal occupancy");
        } else {
            processFarIndustrial(pl, mostRestrictiveOccupancyType, providedFar, typeOfArea, roadWidth, errorMsgs,
                    feature, mostRestrictiveOccupancyType.getType().getName());
            LOG.info("Processed FAR for industrial occupancy");
        }
    }

    /**
     * Iterates over all blocks and processes each block's occupancy details.
     */
    private void processAllBlockOccupancies(Plan pl) {
        LOG.info("Start processing all block occupancies");

        for (Block blk : pl.getBlocks()) {
            processBlockOccupancies(pl, blk);
        }

        LOG.info("Completed processing all block occupancies");
    }

    /**
     * Processes occupancy details for a specific block and updates Plan totals.
     */
    private void processBlockOccupancies(Plan pl, Block blk) {
        LOG.info("Inside processBlockOccupancies() for Block Number: {}", blk.getNumber());

	    BigDecimal flrArea = BigDecimal.ZERO;
	    BigDecimal bltUpArea = BigDecimal.ZERO;
	    BigDecimal existingFlrArea = BigDecimal.ZERO;
	    BigDecimal existingBltUpArea = BigDecimal.ZERO;
	    BigDecimal carpetArea = BigDecimal.ZERO;
	    BigDecimal existingCarpetArea = BigDecimal.ZERO;
	    BigDecimal totalExistingBuiltUpArea = BigDecimal.ZERO;
	    BigDecimal totalExistingFloorArea = BigDecimal.ZERO;
	    BigDecimal totalBuiltUpArea = BigDecimal.ZERO;
	    BigDecimal totalFloorArea = BigDecimal.ZERO;
	    BigDecimal totalCarpetArea = BigDecimal.ZERO;
	    BigDecimal totalExistingCarpetArea = BigDecimal.ZERO;

        Building building = blk.getBuilding();

        for (Floor flr : building.getFloors()) {
            LOG.info("Processing Floor Number: {} in Block: {}", flr.getNumber(), blk.getNumber());

            for (Occupancy occupancy : flr.getOccupancies()) {
                validate2(pl, blk, flr, occupancy);

                bltUpArea = bltUpArea.add(occupancy.getBuiltUpArea() == null ? BigDecimal.ZERO : occupancy.getBuiltUpArea());
                existingBltUpArea = existingBltUpArea.add(occupancy.getExistingBuiltUpArea() == null ? BigDecimal.ZERO : occupancy.getExistingBuiltUpArea());
                flrArea = flrArea.add(occupancy.getFloorArea());
                existingFlrArea = existingFlrArea.add(occupancy.getExistingFloorArea());
               

                LOG.info("Updated occupancy areas -> FloorArea: {}, BuiltUpArea: {}, ExistingFloorArea: {}, ExistingBuiltUpArea: {}",
                        flrArea, bltUpArea, existingFlrArea, existingBltUpArea);
            }
        }
        
        for (Floor flr : building.getFloors()) {
       	 for (FloorUnit unit : flr.getUnits()) {
           for (Occupancy occupancy : unit.getOccupancies()) {
               carpetArea = carpetArea.add(occupancy.getCarpetArea());
               existingCarpetArea = existingCarpetArea.add(occupancy.getExistingCarpetArea());
               LOG.info("Updated occupancy areas ->  CarpetArea: {}, ExistingCarpetArea: {}",
                        carpetArea, existingCarpetArea);
           }
       }
   }
        

        building.setTotalFloorArea(flrArea);
        building.setTotalBuitUpArea(bltUpArea);
        building.setTotalExistingBuiltUpArea(existingBltUpArea);
        building.setTotalExistingFloorArea(existingFlrArea);

        if (existingBltUpArea.compareTo(bltUpArea) == 0) {
            blk.setCompletelyExisting(Boolean.TRUE);
            LOG.info("Block {} marked as completely existing.", blk.getNumber());
        }

        totalFloorArea = totalFloorArea.add(flrArea);
        totalBuiltUpArea = totalBuiltUpArea.add(bltUpArea);
        totalExistingBuiltUpArea = totalExistingBuiltUpArea.add(existingBltUpArea);
        totalExistingFloorArea = totalExistingFloorArea.add(existingFlrArea);
        totalCarpetArea = totalCarpetArea.add(carpetArea);
        totalExistingCarpetArea = totalExistingCarpetArea.add(existingCarpetArea);

        LOG.info("Final totals for Block {} -> FloorArea: {}, BuiltUpArea: {}, ExistingFloorArea: {}, ExistingBuiltUpArea: {}, CarpetArea: {}, ExistingCarpetArea: {}",
                blk.getNumber(), flrArea, bltUpArea, existingFlrArea, existingBltUpArea, carpetArea, existingCarpetArea);

        processBlockOccupancyTypes(blk);
        LOG.info("Completed processBlockOccupancyTypes for Block Number: {}", blk.getNumber());
    }


    /**
     * Processes and categorizes occupancy types block-wise and computes their aggregated areas.
     */
    private void processBlockOccupancyTypes(Block blk) {
        LOG.info("Inside processBlockOccupancyTypes() for Block Number: {}", blk.getNumber());

        Set<OccupancyTypeHelper> occupancyByBlock = new HashSet<>();
        for (Floor flr : blk.getBuilding().getFloors()) {
            for (Occupancy occupancy : flr.getOccupancies()) {
                if (occupancy.getTypeHelper() != null) {
                    occupancyByBlock.add(occupancy.getTypeHelper());
                }
            }
        }
        LOG.info("Unique occupancy types identified for Block {}: {}", blk.getNumber(), occupancyByBlock.size());

        List<Map<String, Object>> listOfMapOfAllDtls = new ArrayList<>();
        List<OccupancyTypeHelper> listOfOccupancyTypes = new ArrayList<>();

        for (OccupancyTypeHelper occupancyType : occupancyByBlock) {
            LOG.info("Processing occupancyType: {}", occupancyType.getType().getCode());

            Map<String, Object> allDtlsMap = new HashMap<>();
            BigDecimal blockWiseFloorArea = BigDecimal.ZERO;
            BigDecimal blockWiseBuiltupArea = BigDecimal.ZERO;
            BigDecimal blockWiseExistingFloorArea = BigDecimal.ZERO;
            BigDecimal blockWiseExistingBuiltupArea = BigDecimal.ZERO;

            for (Floor flr : blk.getBuilding().getFloors()) {
                for (Occupancy occupancy : flr.getOccupancies()) {
                    if (occupancyType.getType() != null && occupancyType.getType().getCode() != null
                            && occupancy.getTypeHelper() != null && occupancy.getTypeHelper().getType() != null
                            && occupancy.getTypeHelper().getType().getCode() != null
                            && occupancy.getTypeHelper().getType().getCode().equals(occupancyType.getType().getCode())) {

                        blockWiseFloorArea = blockWiseFloorArea.add(occupancy.getFloorArea());
                        blockWiseBuiltupArea = blockWiseBuiltupArea
                                .add(occupancy.getBuiltUpArea() == null ? BigDecimal.ZERO : occupancy.getBuiltUpArea());
                        blockWiseExistingFloorArea = blockWiseExistingFloorArea.add(occupancy.getExistingFloorArea());
                        blockWiseExistingBuiltupArea = blockWiseExistingBuiltupArea
                                .add(occupancy.getExistingBuiltUpArea() == null ? BigDecimal.ZERO
                                        : occupancy.getExistingBuiltUpArea());
                    }
                }
            }

            LOG.info("Block {} -> Occupancy {}: FloorArea={}, BuiltUpArea={}, ExistingFloorArea={}, ExistingBuiltUpArea={}",
                    blk.getNumber(), occupancyType.getType().getCode(),
                    blockWiseFloorArea, blockWiseBuiltupArea, blockWiseExistingFloorArea, blockWiseExistingBuiltupArea);

            Occupancy occupancy = new Occupancy();
            occupancy.setBuiltUpArea(blockWiseBuiltupArea);
            occupancy.setFloorArea(blockWiseFloorArea);
            occupancy.setExistingFloorArea(blockWiseExistingFloorArea);
            occupancy.setExistingBuiltUpArea(blockWiseExistingBuiltupArea);
            occupancy.setCarpetArea(blockWiseFloorArea.multiply(BigDecimal.valueOf(.80)));
            occupancy.setTypeHelper(occupancyType);
            blk.getBuilding().getTotalArea().add(occupancy);

            allDtlsMap.put(OCCUPANCY, occupancyType);
            allDtlsMap.put(TOTAL_FLOOR_AREA, blockWiseFloorArea);
            allDtlsMap.put(TOTAL_BUILDUP_AREA, blockWiseBuiltupArea);
            allDtlsMap.put(EXISTING_FLOOR_AREA, blockWiseExistingFloorArea);
            allDtlsMap.put(EXISTING_BUILT_UP_AREA, blockWiseExistingBuiltupArea);

            listOfOccupancyTypes.add(occupancyType);
            listOfMapOfAllDtls.add(allDtlsMap);
        }

        LOG.info("Completed processing occupancies for Block {}. Total Occupancy Types: {}", blk.getNumber(), listOfOccupancyTypes.size());

        buildOccupancyListForBlock(blk, listOfOccupancyTypes, listOfMapOfAllDtls);
        LOG.info("Occupancy list built and mapped for Block {}", blk.getNumber());
    }

    /**
     * Checks if the given occupancy matches the occupancy type helper.
     */
    private boolean occupancyTypeMatches(OccupancyTypeHelper type, Occupancy occ) {
        boolean match = type.getType() != null && type.getType().getCode() != null
                && occ.getTypeHelper() != null && occ.getTypeHelper().getType() != null
                && occ.getTypeHelper().getType().getCode() != null
                && occ.getTypeHelper().getType().getCode().equals(type.getType().getCode());
        LOG.info("Matching occupancy type. OccupancyTypeHelper code: {}, Occupancy code: {}, Match: {}",
                type.getType().getCode(), occ.getTypeHelper() != null ? occ.getTypeHelper().getType().getCode() : null, match);
        return match;
    }

    /**
     * Builds a list of occupancy objects for a block, aggregates areas, and classifies the block.
     */
    private void buildOccupancyListForBlock(Block blk, List<OccupancyTypeHelper> listOfOccupancyTypes,
                                            List<Map<String, Object>> listOfMapOfAllDtls) {

        Set<OccupancyTypeHelper> setOfOccupancyTypes = new HashSet<>(listOfOccupancyTypes);
        List<Occupancy> listOfOccupanciesOfAParticularblock = new ArrayList<>();

        for (OccupancyTypeHelper occupancyType : setOfOccupancyTypes) {
            if (occupancyType != null) {
                Occupancy occupancy = new Occupancy();
                BigDecimal totalFlrArea = BigDecimal.ZERO;
                BigDecimal totalBltUpArea = BigDecimal.ZERO;
                BigDecimal totalExistingFlrArea = BigDecimal.ZERO;
                BigDecimal totalExistingBltUpArea = BigDecimal.ZERO;

                for (Map<String, Object> dtlsMap : listOfMapOfAllDtls) {
                    if (occupancyType.equals(dtlsMap.get(OCCUPANCY))) {
                        totalFlrArea = totalFlrArea.add((BigDecimal) dtlsMap.get(TOTAL_FLOOR_AREA));
                        totalBltUpArea = totalBltUpArea.add((BigDecimal) dtlsMap.get(TOTAL_BUILDUP_AREA));
                        totalExistingBltUpArea = totalExistingBltUpArea.add((BigDecimal) dtlsMap.get(EXISTING_BUILT_UP_AREA));
                        totalExistingFlrArea = totalExistingFlrArea.add((BigDecimal) dtlsMap.get(EXISTING_FLOOR_AREA));
                    }
                }

                occupancy.setTypeHelper(occupancyType);
                occupancy.setBuiltUpArea(totalBltUpArea);
                occupancy.setFloorArea(totalFlrArea);
                occupancy.setExistingBuiltUpArea(totalExistingBltUpArea);
                occupancy.setExistingFloorArea(totalExistingFlrArea);
                occupancy.setExistingCarpetArea(totalExistingFlrArea.multiply(BigDecimal.valueOf(0.80)));
                occupancy.setCarpetArea(totalFlrArea.multiply(BigDecimal.valueOf(0.80)));

                listOfOccupanciesOfAParticularblock.add(occupancy);

                LOG.info("Built occupancy for type: {} with FloorArea: {}, BuiltUpArea: {}", occupancyType, totalFlrArea, totalBltUpArea);
            }
        }

        blk.getBuilding().setOccupancies(listOfOccupanciesOfAParticularblock);
        LOG.info("Set occupancies for block Number: {} with count: {}", blk.getNumber(), listOfOccupanciesOfAParticularblock.size());
        classifyBlock(blk, listOfOccupanciesOfAParticularblock);
    }

    /**
     * Classifies the block based on occupancy types.
     */
    private void classifyBlock(Block blk, List<Occupancy> listOfOccupancies) {
        LOG.info("Inside classifyBlock() for Block Number: {} with {} occupancies", blk.getNumber(), listOfOccupancies.size());

        if (!listOfOccupancies.isEmpty()) {
            boolean singleFamilyBuildingTypeOccupancyPresent = false;
            boolean otherThanSingleFamilyOccupancyTypePresent = false;

            for (Occupancy occupancy : listOfOccupancies) {
                if (occupancy.getTypeHelper().getSubtype() != null
                        && A_R.equals(occupancy.getTypeHelper().getSubtype().getCode())) {
                    singleFamilyBuildingTypeOccupancyPresent = true;
                } else {
                    otherThanSingleFamilyOccupancyTypePresent = true;
                    break;
                }
            }

            blk.setSingleFamilyBuilding(!otherThanSingleFamilyOccupancyTypePresent && singleFamilyBuildingTypeOccupancyPresent);
            LOG.info("Block {} classified as Single Family Building: {}", blk.getNumber());

            int allResidentialOccTypes = 0;
            int allResidentialOrCommercialOccTypes = 0;

            for (Occupancy occupancy : listOfOccupancies) {
                if (occupancy.getTypeHelper() != null && occupancy.getTypeHelper().getType() != null) {
                    int residentialOccupancyType = 0;
                    if (A.equals(occupancy.getTypeHelper().getType().getCode())) {
                        residentialOccupancyType = 1;
                    }
                    if (residentialOccupancyType == 0) {
                        allResidentialOccTypes = 0;
                        break;
                    } else {
                        allResidentialOccTypes = 1;
                    }
                }
            }

            blk.setResidentialBuilding(allResidentialOccTypes == 1);
            LOG.info("Block {} classified as Residential Building: {}", blk.getNumber());

            for (Occupancy occupancy : listOfOccupancies) {
                if (occupancy.getTypeHelper() != null && occupancy.getTypeHelper().getType() != null) {
                    int residentialOrCommercialOccupancyType = 0;
                    if (A.equals(occupancy.getTypeHelper().getType().getCode())
                            || F.equals(occupancy.getTypeHelper().getType().getCode())) {
                        residentialOrCommercialOccupancyType = 1;
                    }
                    if (residentialOrCommercialOccupancyType == 0) {
                        allResidentialOrCommercialOccTypes = 0;
                        break;
                    } else {
                        allResidentialOrCommercialOccTypes = 1;
                    }
                }
            }

            blk.setResidentialOrCommercialBuilding(allResidentialOrCommercialOccTypes == 1);
            LOG.info("Block {} classified as Residential/Commercial Building: {}", blk.getNumber());
        } else {
            LOG.info("Block {} has no occupancies to classify.", blk.getNumber());
        }
    }


    /**
     * Checks whether the given occupancies only belong to specified allowed type codes.
     */
    private boolean isOnlyOfType(List<Occupancy> occupancies, String... allowedTypes) {
        Set<String> allowed = new HashSet<>(Arrays.asList(allowedTypes));
        for (Occupancy occ : occupancies) {
            if (occ.getTypeHelper() == null || occ.getTypeHelper().getType() == null
                    || !allowed.contains(occ.getTypeHelper().getType().getCode())) {
                LOG.info("Occupancy code not in allowed list: {}", occ.getTypeHelper() != null ? occ.getTypeHelper().getType().getCode() : "null");
                return false;
            }
        }
        return true;
    }

    /**
     * Processes all blocks in the given plan:
     * - Identifies most restrictive FAR for each block
     * - Validates floor areas against carpet and built-up areas
     */
    private void processBlocks(Plan pl) {
        LOG.info("Processing blocks to identify most restrictive FAR and validate areas");

        for (Block blk : pl.getBlocks()) {
            Building building = blk.getBuilding();
            Set<OccupancyTypeHelper> setOfBlockDistinctOccupancyTypes = processBlockOccupancies(building);
            OccupancyTypeHelper mostRestrictiveFar = getMostRestrictiveFar(setOfBlockDistinctOccupancyTypes);
            building.setMostRestrictiveFarHelper(mostRestrictiveFar);

            LOG.info("Block Number: {} set most restrictive FAR: {}", blk.getNumber(), mostRestrictiveFar);

            for (Floor flr : building.getFloors()) {
            	 for (FloorUnit unit : flr.getUnits()) {
                validateFloorAreas(pl, blk, flr, unit, mostRestrictiveFar);
            }
        }
    }
   }

    /**
     * Extracts set of distinct occupancy types in a building.
     */
    private Set<OccupancyTypeHelper> processBlockOccupancies(Building building) {
        List<OccupancyTypeHelper> blockWiseOccupancyTypes = new ArrayList<>();
        for (Occupancy occupancy : building.getOccupancies()) {
            if (occupancy.getTypeHelper() != null) {
                blockWiseOccupancyTypes.add(occupancy.getTypeHelper());
            }
        }
        Set<OccupancyTypeHelper> occupancySet = new HashSet<>(blockWiseOccupancyTypes);
        LOG.info("Processed block occupancies, distinct count: {}", occupancySet.size());
        return occupancySet;
    }

    /**
     * Validates the floor area, carpet area, and built-up area of a floor against the
     * most restrictive FAR subtype. Adds errors to the plan if validation fails.
     *
     * @param pl the plan to which validation errors are to be added
     * @param blk the block containing the floor
     * @param flr the floor being validated
     * @param mostRestrictiveFar the most restrictive occupancy type helper for the block
     */
    private void validateFloorAreas(Plan pl, Block blk, Floor flr, FloorUnit unit, OccupancyTypeHelper mostRestrictiveFar) {
        BigDecimal flrArea = BigDecimal.ZERO;
        BigDecimal existingFlrArea = BigDecimal.ZERO;
        BigDecimal carpetArea = BigDecimal.ZERO;
        BigDecimal existingCarpetArea = BigDecimal.ZERO;
        BigDecimal existingBltUpArea = BigDecimal.ZERO;

        for (Occupancy occupancy : flr.getOccupancies()) {
            flrArea = flrArea.add(occupancy.getFloorArea());
            existingFlrArea = existingFlrArea.add(occupancy.getExistingFloorArea());
           
        }
        
          	 for (FloorUnit units : flr.getUnits()) {
              for (Occupancy occupancy : units.getOccupancies()) {
            	  carpetArea = carpetArea.add(occupancy.getCarpetArea());
                  existingCarpetArea = existingCarpetArea.add(occupancy.getExistingCarpetArea());
                  LOG.info("Updated occupancy areas ->  CarpetArea: {}, ExistingCarpetArea: {}",
                           carpetArea, existingCarpetArea);
              }
          }
      

        for (Occupancy occupancy : flr.getOccupancies()) {
            existingBltUpArea = existingBltUpArea.add(
                    occupancy.getExistingBuiltUpArea() != null ? occupancy.getExistingBuiltUpArea() : BigDecimal.ZERO);
        }

        if (mostRestrictiveFar != null && mostRestrictiveFar.getConvertedSubtype() != null
                && !A_R.equals(mostRestrictiveFar.getSubtype().getCode())) {
            if (carpetArea.compareTo(BigDecimal.ZERO) == 0) {
                pl.addError(CARPET_AREA_BLOCK + blk.getNumber() + FLOOR_SPACED + flr.getNumber(), UNIT + unit.getUnitNumber() +  CARPET_AREA_NOT_DEFINED_BLOCK + blk.getNumber() + FLOOR_SPACED + flr.getNumber());
            }

            if (existingBltUpArea.compareTo(BigDecimal.ZERO) > 0
                    && existingCarpetArea.compareTo(BigDecimal.ZERO) == 0) {
                pl.addError(EXISTING_CARPET_AREA_BLOCK + blk.getNumber() + FLOOR_SPACED + flr.getNumber(), UNIT + unit.getUnitNumber() +
                        EXISTING_CARPET_AREA_NOT_DEFINED + blk.getNumber() + FLOOR_SPACED
                                + flr.getNumber());
            }
        }

        if (flrArea.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS)
                .compareTo(carpetArea.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                        DcrConstants.ROUNDMODE_MEASUREMENTS)) < 0) {
            pl.addError(FLOOR_AREA_BLOCK + blk.getNumber() + FLOOR_SPACED + flr.getNumber(),
                    FLOOR_AREA_LESS_THAN_CARPET_AREA + blk.getNumber() + FLOOR_SPACED
                            + flr.getNumber());
        }

        if (existingBltUpArea.compareTo(BigDecimal.ZERO) > 0 && existingFlrArea
                .setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS)
                .compareTo(existingCarpetArea.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                        DcrConstants.ROUNDMODE_MEASUREMENTS)) < 0) {
            pl.addError(EXISTING_FLOOR_AREA_BLOCK + blk.getNumber() + FLOOR_SPACED + flr.getNumber(),
                    EXISTING_FLOOR_LESS_CARPET_AREA + blk.getNumber() + FLOOR_SPACED
                            + flr.getNumber());
        }
    }

    /**
     * Collects and aggregates occupancy details for the given plan from the provided set of distinct occupancy types.
     *
     * @param setOfDistinctOccupancyTypes a set of distinct occupancy type helpers used in the plan
     * @param pl the {@link Plan} object representing the building plan
     * @return a list of {@link Occupancy} objects corresponding to the occupancy types in the plan
     */

    private List<Occupancy> collectOccupanciesForPlan(Set<OccupancyTypeHelper> setOfDistinctOccupancyTypes, Plan pl) {
        List<Occupancy> occupanciesForPlan = new ArrayList<>();
        for (OccupancyTypeHelper occupancyType : setOfDistinctOccupancyTypes) {
            if (occupancyType != null) {
                occupanciesForPlan.add(aggregateOccupancyForType(occupancyType, pl));
            }
        }
        return occupanciesForPlan;
    }

    /**
     * Populates the plan object and its virtual building with calculated occupancy and area details.
     *
     * @param pl the {@link Plan} object to populate
     * @param setOfDistinctOccupancyTypes all distinct occupancy types used in the plan
     * @param distinctOccupancyTypesHelper filtered set of occupancy types used for type-specific validations
     * @param totalFloorArea total floor area for all blocks
     * @param totalCarpetArea total carpet area for all blocks
     * @param totalExistingBuiltUpArea total existing built-up area
     * @param totalExistingFloorArea total existing floor area
     * @param totalExistingCarpetArea total existing carpet area
     * @param totalBuiltUpArea total built-up area for the proposed construction
     */
    private void populatePlanAndVirtualBuildingDetails(Plan pl, Set<OccupancyTypeHelper> setOfDistinctOccupancyTypes,
                                                       Set<OccupancyTypeHelper> distinctOccupancyTypesHelper, BigDecimal totalFloorArea,
                                                       BigDecimal totalCarpetArea, BigDecimal totalExistingBuiltUpArea, BigDecimal totalExistingFloorArea,
                                                       BigDecimal totalExistingCarpetArea, BigDecimal totalBuiltUpArea) {

        setOccupanciesAndAreas(pl, setOfDistinctOccupancyTypes, distinctOccupancyTypesHelper,
                totalFloorArea, totalCarpetArea, totalExistingBuiltUpArea,
                totalExistingFloorArea, totalExistingCarpetArea, totalBuiltUpArea);

        updateBuildingTypeFlags(pl, distinctOccupancyTypesHelper);
    }


    /**
     * Sets occupancies and area-related attributes in the {@link Plan} and its virtual building.
     *
     * @param pl the {@link Plan} object being processed
     * @param setOfDistinctOccupancyTypes set of distinct occupancy types in the plan
     * @param distinctOccupancyTypesHelper helper set of distinct occupancy types
     * @param totalFloorArea total floor area for all occupancies
     * @param totalCarpetArea total carpet area for all occupancies
     * @param totalExistingBuiltUpArea total existing built-up area
     * @param totalExistingFloorArea total existing floor area
     * @param totalExistingCarpetArea total existing carpet area
     * @param totalBuiltUpArea total proposed built-up area
     */
    private void setOccupanciesAndAreas(Plan pl, Set<OccupancyTypeHelper> setOfDistinctOccupancyTypes,
                                        Set<OccupancyTypeHelper> distinctOccupancyTypesHelper, BigDecimal totalFloorArea,
                                        BigDecimal totalCarpetArea, BigDecimal totalExistingBuiltUpArea, BigDecimal totalExistingFloorArea,
                                        BigDecimal totalExistingCarpetArea, BigDecimal totalBuiltUpArea) {

        List<Occupancy> occupanciesForPlan = collectOccupanciesForPlan(setOfDistinctOccupancyTypes, pl);
        pl.setOccupancies(occupanciesForPlan);

        pl.getVirtualBuilding().setTotalFloorArea(totalFloorArea);
        pl.getVirtualBuilding().setTotalCarpetArea(totalCarpetArea);
        pl.getVirtualBuilding().setTotalExistingBuiltUpArea(totalExistingBuiltUpArea);
        pl.getVirtualBuilding().setTotalExistingFloorArea(totalExistingFloorArea);
        pl.getVirtualBuilding().setTotalExistingCarpetArea(totalExistingCarpetArea);
        pl.getVirtualBuilding().setOccupancyTypes(distinctOccupancyTypesHelper);
        pl.getVirtualBuilding().setTotalBuitUpArea(totalBuiltUpArea);
        pl.getVirtualBuilding().setMostRestrictiveFarHelper(getMostRestrictiveFar(setOfDistinctOccupancyTypes));
    }

    /**
     * Updates flags in the virtual building of the plan indicating whether the building is residential or commercial.
     *
     * @param pl the {@link Plan} object
     * @param distinctOccupancyTypesHelper the set of distinct occupancy types used to determine the building type
     */
    private void updateBuildingTypeFlags(Plan pl, Set<OccupancyTypeHelper> distinctOccupancyTypesHelper) {
        if (!distinctOccupancyTypesHelper.isEmpty()) {
            boolean isAllResidential = areAllResidential(distinctOccupancyTypesHelper);
            pl.getVirtualBuilding().setResidentialBuilding(isAllResidential);

            boolean isAllResidentialOrCommercial = areAllResidentialOrCommercial(distinctOccupancyTypesHelper);
            pl.getVirtualBuilding().setResidentialOrCommercialBuilding(isAllResidentialOrCommercial);
        }
    }

    /**
     * Checks if all occupancy types are residential.
     *
     * @param occupancyTypes set of occupancy types to check
     * @return {@code true} if all types are residential (type code "A"), otherwise {@code false}
     */
    private boolean areAllResidential(Set<OccupancyTypeHelper> occupancyTypes) {
        for (OccupancyTypeHelper occupancy : occupancyTypes) {
            LOG.info("occupancy :" + occupancy);
            if (occupancy.getType() == null || !A.equals(occupancy.getType().getCode())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if all occupancy types are either residential (code "A") or commercial (code "F").
     *
     * @param occupancyTypes set of occupancy types to check
     * @return {@code true} if all types are residential or commercial, otherwise {@code false}
     */
    private boolean areAllResidentialOrCommercial(Set<OccupancyTypeHelper> occupancyTypes) {
        for (OccupancyTypeHelper occupancy : occupancyTypes) {
            if (occupancy.getType() == null || !(A.equals(occupancy.getType().getCode()) || F.equals(occupancy.getType().getCode()))) {
                return false;
            }
        }
        return true;
    }


    /**
     * Aggregates area details (built-up, floor, carpet) across all blocks for a specific occupancy type.
     *
     * @param occupancyType the {@link OccupancyTypeHelper} to aggregate for
     * @param pl the {@link Plan} object containing the blocks and occupancies
     * @return an {@link Occupancy} object with aggregated values for the specified type
     */
    private Occupancy aggregateOccupancyForType(OccupancyTypeHelper occupancyType, Plan pl) {
        BigDecimal totalFloorAreaForAllBlks = BigDecimal.ZERO;
        BigDecimal totalBuiltUpAreaForAllBlks = BigDecimal.ZERO;
        BigDecimal totalCarpetAreaForAllBlks = BigDecimal.ZERO;
        BigDecimal totalExistBuiltUpAreaForAllBlks = BigDecimal.ZERO;
        BigDecimal totalExistFloorAreaForAllBlks = BigDecimal.ZERO;
        BigDecimal totalExistCarpetAreaForAllBlks = BigDecimal.ZERO;

        for (Block block : pl.getBlocks()) {
            for (Occupancy buildingOccupancy : block.getBuilding().getOccupancies()) {
                if (occupancyType.equals(buildingOccupancy.getTypeHelper())) {
                    totalFloorAreaForAllBlks = totalFloorAreaForAllBlks.add(buildingOccupancy.getFloorArea());
                    totalBuiltUpAreaForAllBlks = totalBuiltUpAreaForAllBlks.add(buildingOccupancy.getBuiltUpArea());
                    totalCarpetAreaForAllBlks = totalCarpetAreaForAllBlks.add(buildingOccupancy.getCarpetArea());
                    totalExistBuiltUpAreaForAllBlks = totalExistBuiltUpAreaForAllBlks.add(buildingOccupancy.getExistingBuiltUpArea());
                    totalExistFloorAreaForAllBlks = totalExistFloorAreaForAllBlks.add(buildingOccupancy.getExistingFloorArea());
                    totalExistCarpetAreaForAllBlks = totalExistCarpetAreaForAllBlks.add(buildingOccupancy.getExistingCarpetArea());
                }
            }
        }

        Occupancy occupancy = new Occupancy();
        occupancy.setTypeHelper(occupancyType);
        occupancy.setBuiltUpArea(totalBuiltUpAreaForAllBlks);
        occupancy.setCarpetArea(totalCarpetAreaForAllBlks);
        occupancy.setFloorArea(totalFloorAreaForAllBlks);
        occupancy.setExistingBuiltUpArea(totalExistBuiltUpAreaForAllBlks);
        occupancy.setExistingFloorArea(totalExistFloorAreaForAllBlks);
        occupancy.setExistingCarpetArea(totalExistCarpetAreaForAllBlks);
        return occupancy;
    }


    /**
     * Determines if NOC (No Objection Certificate) is required for the plan based on height, coverage,
     * or proximity to a monument, and updates the plan information accordingly.
     *
     * Sets:
     * - NOC from Fire Department if building height > 5m or coverage > 500 sq.m.
     * - NOC near Monument if distance from monument > 300m.
     *
     * @param pl the {@link Plan} object to evaluate
     */
    private void decideNocIsRequired(Plan pl) {
        Boolean isHighRise = false;
        for (Block b : pl.getBlocks()) {
            if ((b.getBuilding() != null && b.getBuilding().getBuildingHeight() != null
                    && b.getBuilding().getBuildingHeight().compareTo(new BigDecimal(5)) > 0)
                    || (b.getBuilding() != null && b.getBuilding().getCoverageArea() != null
                    && b.getBuilding().getCoverageArea().compareTo(new BigDecimal(500)) > 0)) {
                isHighRise = true;

            }
        }
        if (isHighRise) {
            pl.getPlanInformation().setNocFireDept(DcrConstants.YES);
        }

        if (StringUtils.isNotBlank(pl.getPlanInformation().getBuildingNearMonument())
                && DcrConstants.YES.equalsIgnoreCase(pl.getPlanInformation().getBuildingNearMonument())) {
            BigDecimal minDistanceFromMonument = BigDecimal.ZERO;
            List<BigDecimal> distancesFromMonument = pl.getDistanceToExternalEntity().getMonuments();
            if (!distancesFromMonument.isEmpty()) {

                minDistanceFromMonument = distancesFromMonument.stream().reduce(BigDecimal::min).get();

                if (minDistanceFromMonument.compareTo(BigDecimal.valueOf(300)) > 0) {
                    pl.getPlanInformation().setNocNearMonument(DcrConstants.YES);
                }
            }

        }

    }

    /**
     * Validates occupancy data for a specific block and floor, checking for negative built-up, floor, or existing areas.
     * Adds validation errors to the plan if any area values are negative.
     *
     * @param pl the {@link Plan} to record errors in
     * @param blk the {@link Block} being validated
     * @param flr the {@link Floor} under validation
     * @param occupancy the {@link Occupancy} object being checked
     */
    private void validate2(Plan pl, Block blk, Floor flr, Occupancy occupancy) {
        String occupancyTypeHelper = StringUtils.EMPTY;
        if (occupancy.getTypeHelper() != null) {
            if (occupancy.getTypeHelper().getType() != null) {
                occupancyTypeHelper = occupancy.getTypeHelper().getType().getName();
            } else if (occupancy.getTypeHelper().getSubtype() != null) {
                occupancyTypeHelper = occupancy.getTypeHelper().getSubtype().getName();
            }
        }

        if (occupancy.getBuiltUpArea() != null && occupancy.getBuiltUpArea().compareTo(BigDecimal.valueOf(0)) < 0) {
            pl.addError(VALIDATION_NEGATIVE_BUILTUP_AREA, getLocaleMessage(VALIDATION_NEGATIVE_BUILTUP_AREA,
                    blk.getNumber(), flr.getNumber().toString(), occupancyTypeHelper));
        }
        if (occupancy.getExistingBuiltUpArea() != null
                && occupancy.getExistingBuiltUpArea().compareTo(BigDecimal.valueOf(0)) < 0) {
            pl.addError(VALIDATION_NEGATIVE_EXISTING_BUILTUP_AREA,
                    getLocaleMessage(VALIDATION_NEGATIVE_EXISTING_BUILTUP_AREA, blk.getNumber(),
                            flr.getNumber().toString(), occupancyTypeHelper));
        }
        occupancy.setFloorArea((occupancy.getBuiltUpArea() == null ? BigDecimal.ZERO : occupancy.getBuiltUpArea())
                .subtract(occupancy.getDeduction() == null ? BigDecimal.ZERO : occupancy.getDeduction()));
        if (occupancy.getFloorArea() != null && occupancy.getFloorArea().compareTo(BigDecimal.valueOf(0)) < 0) {
            pl.addError(VALIDATION_NEGATIVE_FLOOR_AREA, getLocaleMessage(VALIDATION_NEGATIVE_FLOOR_AREA,
                    blk.getNumber(), flr.getNumber().toString(), occupancyTypeHelper));
        }
        occupancy.setExistingFloorArea(
                (occupancy.getExistingBuiltUpArea() == null ? BigDecimal.ZERO : occupancy.getExistingBuiltUpArea())
                        .subtract(occupancy.getExistingDeduction() == null ? BigDecimal.ZERO
                                : occupancy.getExistingDeduction()));
        if (occupancy.getExistingFloorArea() != null
                && occupancy.getExistingFloorArea().compareTo(BigDecimal.valueOf(0)) < 0) {
            pl.addError(VALIDATION_NEGATIVE_EXISTING_FLOOR_AREA,
                    getLocaleMessage(VALIDATION_NEGATIVE_EXISTING_FLOOR_AREA, blk.getNumber(),
                            flr.getNumber().toString(), occupancyTypeHelper));
        }
    }

    /**
     * Identifies and returns the most restrictive FAR (Floor Area Ratio) type among the given occupancy types
     * based on a predefined order of precedence.
     *
     * @param distinctOccupancyTypes set of distinct occupancy types to evaluate
     * @return the {@link OccupancyTypeHelper} with the most restrictive FAR requirement
     */
    protected OccupancyTypeHelper getMostRestrictiveFar(Set<OccupancyTypeHelper> distinctOccupancyTypes) {
        Set<String> codes = new HashSet<>();
        Map<String, OccupancyTypeHelper> codesMap = new HashMap<>();
        for (OccupancyTypeHelper typeHelper : distinctOccupancyTypes) {

            if (typeHelper.getType() != null)
                codesMap.put(typeHelper.getType().getCode(), typeHelper);
            if (typeHelper.getSubtype() != null)
                codesMap.put(typeHelper.getSubtype().getCode(), typeHelper);
        }
        codes = codesMap.keySet();
        if (codes.contains(S_ECFG))
            return codesMap.get(S_ECFG);
        else if (codes.contains(A_FH))
            return codesMap.get(A_FH);
        else if (codes.contains(S_SAS))
            return codesMap.get(S_SAS);
        else if (codes.contains(D_B))
            return codesMap.get(D_B);
        else if (codes.contains(D_C))
            return codesMap.get(D_C);
        else if (codes.contains(D_A))
            return codesMap.get(D_A);
        else if (codes.contains(H_PP))
            return codesMap.get(H_PP);
        else if (codes.contains(E_NS))
            return codesMap.get(E_NS);
        else if (codes.contains(M_DFPAB))
            return codesMap.get(M_DFPAB);
        else if (codes.contains(E_PS))
            return codesMap.get(E_PS);
        else if (codes.contains(E_SFMC))
            return codesMap.get(E_SFMC);
        else if (codes.contains(E_SFDAP))
            return codesMap.get(E_SFDAP);
        else if (codes.contains(E_EARC))
            return codesMap.get(E_EARC);
        else if (codes.contains(S_MCH))
            return codesMap.get(S_MCH);
        else if (codes.contains(S_BH))
            return codesMap.get(S_BH);
        else if (codes.contains(S_CRC))
            return codesMap.get(S_CRC);
        else if (codes.contains(S_CA))
            return codesMap.get(S_CA);
        else if (codes.contains(S_SC))
            return codesMap.get(S_SC);
        else if (codes.contains(S_ICC))
            return codesMap.get(S_ICC);
        else if (codes.contains(A2))
            return codesMap.get(A2);
        else if (codes.contains(E_CLG))
            return codesMap.get(E_CLG);
        else if (codes.contains(M_OHF))
            return codesMap.get(M_OHF);
        else if (codes.contains(M_VH))
            return codesMap.get(M_VH);
        else if (codes.contains(M_NAPI))
            return codesMap.get(M_NAPI);
        else if (codes.contains(A_SA))
            return codesMap.get(A_SA);
        else if (codes.contains(M_HOTHC))
            return codesMap.get(M_HOTHC);
        else if (codes.contains(E_SACA))
            return codesMap.get(E_SACA);
        else if (codes.contains(G))
            return codesMap.get(G);
        else if (codes.contains(F))
            return codesMap.get(F);
        else if (codes.contains(A))
            return codesMap.get(A);
        else
            return null;

    }

    private Boolean processFarForSpecialOccupancy(Plan pl, OccupancyTypeHelper occupancyType, BigDecimal far,
                                                  String typeOfArea, BigDecimal roadWidth, HashMap<String, String> errors) {

        OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding() != null
                ? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
                : null;
        String expectedResult = StringUtils.EMPTY;
        boolean isAccepted = false;
        if (mostRestrictiveOccupancyType != null && mostRestrictiveOccupancyType.getSubtype() != null) {
            if (mostRestrictiveOccupancyType.getSubtype().getCode().equals(S_ECFG)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(A_FH)) {
                isAccepted = far.compareTo(POINTTWO) <= 0;
                expectedResult = LESS_THAN_EQUAL_TO_ZERO_POINT_TWO;
                return true;
            }

            if (mostRestrictiveOccupancyType.getSubtype().getCode().equals(S_SAS)) {
                isAccepted = far.compareTo(POINTFOUR) <= 0;
                expectedResult = LESS_THAN_EQUAL_TO_ZERO_POINT_FOUR;
                return true;
            }

            if (mostRestrictiveOccupancyType.getSubtype().getCode().equals(D_B)) {
                isAccepted = far.compareTo(POINTFIVE) <= 0;
                expectedResult = LESS_THAN_EQUAL_TO_ZERO_POINT_FIVE;
                return true;
            }

            if (mostRestrictiveOccupancyType.getSubtype().getCode().equals(D_C)) {
                isAccepted = far.compareTo(POINTSIX) <= 0;
                expectedResult = LESS_THAN_EQUAL_TO_ZERO_POINT_SIX;
                return true;
            }

            if (mostRestrictiveOccupancyType.getSubtype().getCode().equals(D_A)) {
                isAccepted = far.compareTo(POINTSEVEN) <= 0;
                expectedResult = LESS_THAN_EQUAL_TO_ZERO_POINT_SEVEN;
                return true;
            }

            if (mostRestrictiveOccupancyType.getSubtype().getCode().equals(H_PP)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(E_NS)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(M_DFPAB)) {
                isAccepted = far.compareTo(ONE) <= 0;
                expectedResult = LESS_THAN_EQUAL_TO_ONE;
                return true;
            }

            if (mostRestrictiveOccupancyType.getSubtype().getCode().equals(E_PS)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(E_SFMC)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(E_SFDAP)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(E_EARC)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(S_MCH)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(S_BH)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(S_CRC)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(S_CA)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(S_SC)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(S_ICC)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(A2)) {
                isAccepted = far.compareTo(ONE_POINTTWO) <= 0;
                expectedResult = LESS_THAN_EQUAL_TO_ONE_POINT_TWO;
                return true;
            }

            if (mostRestrictiveOccupancyType.getSubtype().getCode().equals(B2)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(E_CLG)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(M_OHF)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(M_VH)
                    || mostRestrictiveOccupancyType.getSubtype().getCode().equals(M_NAPI)) {
                isAccepted = far.compareTo(ONE_POINTFIVE) <= 0;
                expectedResult = LESS_THAN_EQUAL_TO_ONE_POINT_FIVE;
                return true;
            }

            if (mostRestrictiveOccupancyType.getSubtype().getCode().equals(A_SA)) {
                isAccepted = far.compareTo(TWO_POINTFIVE) <= 0;
                expectedResult = LESS_THAN_EQUAL_TO_TWO_POINT_FIVE;
                return true;
            }

            if (mostRestrictiveOccupancyType.getSubtype().getCode().equals(E_SACA)) {
                isAccepted = far.compareTo(FIFTEEN) <= 0;
                expectedResult = LESS_THAN_EQUAL_TO_FIFTEEN;
                return true;
            }

        }

        String occupancyName = occupancyType.getSubtype() != null ? occupancyType.getSubtype().getName()
                : occupancyType.getType().getName();

        if (StringUtils.isNotBlank(expectedResult)) {
            buildResult(pl, occupancyName, far, typeOfArea, roadWidth, expectedResult, isAccepted, null, null);
        }

        return false;
    }

    /**
     * Processes and validates the Floor Area Ratio (FAR) for residential occupancy
     * based on plot area, road width, and permissible FAR rules fetched from the
     * cache.
     *
     * <p>
     * This method:
     * <ul>
     * <li>Fetches applicable FAR rules for residential occupancy from the MDMS
     * feature rule cache.</li>
     * <li>Determines the permissible FAR by matching the plot area range with the
     * rules.</li>
     * <li>Compares the actual FAR with the permissible FAR to check if it's
     * compliant.</li>
     * <li>Updates the {@link Plan} with the permissible FAR and builds the result
     * if valid.</li>
     * </ul>
     *
     * @param pl            the {@link Plan} object containing details of the plot
     *                      and FAR to validate
     * @param occupancyType the {@link OccupancyTypeHelper} representing the current
     *                      occupancy type
     * @param far           the actual FAR value for the given occupancy
     * @param typeOfArea    a string indicating the type of area being evaluated
     *                      (e.g., "Built-up", "Floor", etc.)
     * @param roadWidth     the width of the road adjacent to the plot
     * @param errors        a map of validation errors, to which any issues found
     *                      during validation may be added
     * @param feature       the feature name (e.g., FAR) used to query rules from
     *                      the cache
     * @param occupancyName the name of the occupancy type being evaluated (e.g.,
     *                      "Residential")
     */

    private void processFar(Plan pl, OccupancyTypeHelper occupancyType, BigDecimal far, String typeOfArea,
                            BigDecimal roadWidth, HashMap<String, String> errors, String feature, String occupancyName) {

        final BigDecimal ONE_BIGHA_IN_SQM = ONEBIGHA;
        BigDecimal plotArea = pl.getPlot().getArea();
        BigDecimal permissibleFar = BigDecimal.ZERO;
        String TDR = pl.getPlanInformation().getTDR();
        String todZone = pl.getPlanInformation().getTodZone();
        BigDecimal additionalMixedUseFar = BigDecimal.ZERO;
        BigDecimal additionalEWSLIGFar = BigDecimal.ZERO;

        LOG.info("Starting processFar with plotArea: {}, far: {}, roadWidth: {}", plotArea, far, roadWidth);

        OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding() != null
                ? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
                : null;

        LOG.info("Most restrictive occupancy type: {}", mostRestrictiveOccupancyType);

        Optional<FarRequirement> matchedRule = findMatchedFarRule(pl, mostRestrictiveOccupancyType, plotArea,
                roadWidth);

        if (matchedRule.isPresent()) {
            FarRequirement rule = matchedRule.get();
            permissibleFar = rule.getPermissible();
            LOG.info("Permissible FAR from matched rule: {}", permissibleFar);

            // TOD FAR CALCULATION ---
            if(todZone != null) {
                permissibleFar = calculateTodFar(rule, TDR, todZone);
            }
            //  Apply TDR loading if available
            if(TDR != null && TDR.equals("YES")) {
                if (rule.getMaxTDRLoading() != null && rule.getMaxTDRLoading().compareTo(BigDecimal.ZERO) > 0) {
                    permissibleFar = permissibleFar.add(rule.getMaxTDRLoading());
                    LOG.info("TDR loading applied. Updated permissible FAR with TDR: {}", permissibleFar);
                }}

            // Apply 30% Mixed Use FAR only if it's A_AF (Apartment) and plot area < 1 bigha
            if (isResidentialApartmentEligibleForMixedUse(occupancyType, plotArea, ONE_BIGHA_IN_SQM)) {
                additionalMixedUseFar = permissibleFar.multiply(POINTTHREE);
              
                LOG.info("30% mixed-use FAR applied for residential plot < 1 bigha. New Permissible FAR = {}", permissibleFar);

//                permissibleFar = applyMixedUseFARIfApplicable(pl, occupancyType, permissibleFar);
            }

//		    // Apply 25% EWS/LIG FAR increase only for Group Housing
//		    if (isGroupHousingWithEWSLIG(pl, occupancyType, plotArea)) {
//		        permissibleFar = applyEWSLIGFarRelaxationIfApplicable(permissibleFar);
//		        LOG.info("25% additional FAR applied for Group Housing with EWS/LIG. New permissible FAR: {}", permissibleFar);
//		    }

//		    // Apply 25% EWS/LIG FAR increase only for Group Housing and validate specific carpet areas for EWS and LIG
            if (isEligibleForEWSLIGFarBonus(pl, occupancyType, plotArea)) {
                additionalEWSLIGFar = permissibleFar.multiply(POINTTWOFIVE);
               
                LOG.info("Applied 25% EWS/LIG FAR relaxation. New permissible FAR: {}", permissibleFar);
//                permissibleFar = applyEWSLIGFarRelaxationIfApplicable(permissibleFar);
            }
        }
        else {
            LOG.warn("No FAR rule matched for given parameters: plotArea={}, roadWidth={}", plotArea, roadWidth);
        }

        try {
            LOG.info("Final permissible FAR to validate against: {}", permissibleFar);
        } catch (NullPointerException e) {
            LOG.error("Permissible FAR not found or null", e);
        }

        boolean isAccepted = far.compareTo(permissibleFar) <= 0;
        pl.getFarDetails().setPermissableFar(permissibleFar.doubleValue());
        String expectedResult = "<= " + permissibleFar;

        LOG.info("FAR validation result for occupancy '{}': provided FAR = {}, accepted = {}", occupancyName, far,
                isAccepted);

        if (errors.isEmpty() && StringUtils.isNotBlank(expectedResult)) {
            buildResult(pl, occupancyName, far, typeOfArea, roadWidth, expectedResult, isAccepted, additionalMixedUseFar, additionalEWSLIGFar);
        }
    }

    private Optional<FarRequirement> findMatchedFarRule(Plan pl, OccupancyTypeHelper occupancy, BigDecimal plotArea,
                                                        BigDecimal roadWidth) {
        LOG.info("Finding matched FAR rule with plotArea: {}, roadWidth: {}", plotArea, roadWidth);

        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.FAR.getValue(), false);

        if (occupancy == null || occupancy.getType() == null) {
            LOG.warn("Occupancy or occupancy type is null, cannot find matched FAR rule.");
            return Optional.empty();
        }

        String occCode = occupancy.getType().getCode();
        LOG.info("Occupancy code for FAR matching: {}", occCode);

        if (B.equalsIgnoreCase(occCode) || H.equalsIgnoreCase(occCode) || D.equalsIgnoreCase(occCode)) {
            LOG.info("Matching FAR based on road width for industrial or similar occupancy.");
            return rules.stream().filter(FarRequirement.class::isInstance).map(FarRequirement.class::cast)
                    .filter(rule -> roadWidth.compareTo(rule.getFromRoadWidth()) >= 0
                            && roadWidth.compareTo(rule.getToRoadWidth()) < 0)
                    .findFirst();

        } else if (K.equalsIgnoreCase(occCode)) {
            LOG.info("Matching FAR based on plot area only for special occupancy.");
            return rules.stream().filter(FarRequirement.class::isInstance).map(FarRequirement.class::cast)
                    .filter(rule -> plotArea.compareTo(rule.getFromPlotArea()) >= 0
                            && plotArea.compareTo(rule.getToPlotArea()) < 0)
                    .findFirst();

        } else {
            LOG.info("Matching FAR based on plot area and road width for default occupancy.");
            return rules.stream().filter(FarRequirement.class::isInstance).map(FarRequirement.class::cast)
                    .filter(rule -> plotArea.compareTo(rule.getFromPlotArea()) >= 0
                            && plotArea.compareTo(rule.getToPlotArea()) < 0
                            && roadWidth.compareTo(rule.getFromRoadWidth()) >= 0
                            && roadWidth.compareTo(rule.getToRoadWidth()) < 0)
                    .findFirst();
        }
    }

    /**
     * Calculates the permissible FAR (Floor Area Ratio) for a plot under Transit Oriented Development (TOD)
     * based on the zone type (Intense or Transition), base FAR, premium FAR, and optional TDR loading.
     * <p>
     * Formula:
     * <ul>
     *   <li><b>Intense Zone:</b> M = (A + B + 40% of (A+B) + D)</li>
     *   <li><b>Transition Zone:</b> M = (A + B + 30% of (A+B) + D)</li>
     * </ul>
     * where:
     * <ul>
     *   <li>A = Base FAR (as per Bye Laws)</li>
     *   <li>B = Premium FAR (Permissible - Base)</li>
     *   <li>D = TDR FAR (if applicable)</li>
     * </ul>
     * The result is not capped here. Capping (e.g. FAR ≤ 400) should be applied by the caller if required.
     *
     * @param rule   the {@link FarRequirement} rule containing base FAR, permissible FAR, and TDR limits
     * @param TDR    "YES" if TDR is applicable, otherwise treated as not applicable
     * @param todZone the TOD zone type; expected values: "Intense" or "Transition"
     * @return the calculated permissible FAR including TOD adjustments
     */
    private BigDecimal calculateTodFar(FarRequirement rule, String TDR, String todZone) {
        BigDecimal baseFar = rule.getBaseFar() != null ? rule.getBaseFar() : BigDecimal.ZERO;
        BigDecimal permissible = rule.getPermissible() != null ? rule.getPermissible() : BigDecimal.ZERO;
        BigDecimal premiumFar = permissible.subtract(baseFar);
        BigDecimal tdrFar = (TDR != null && TDR.equalsIgnoreCase("YES") && rule.getMaxTDRLoading() != null)
                ? rule.getMaxTDRLoading()
                : BigDecimal.ZERO;

        BigDecimal permissibleFar = permissible; // default

        if (todZone != null && INTENSE.equalsIgnoreCase(todZone)) {
            // Intense Zone: 40% of (A+B)
            BigDecimal additionalFar = (baseFar.add(premiumFar))
                    .multiply(BigDecimal.valueOf(0.40))
                    .setScale(2, RoundingMode.HALF_UP);

            permissibleFar = baseFar.add(premiumFar).add(additionalFar).add(tdrFar);

            LOG.info("Intense Zone FAR calculation: Base={}, Premium={}, Add(40%)={}, TDR={}, Total={}",
                    baseFar, premiumFar, additionalFar, tdrFar, permissibleFar);

        } else if (todZone != null && TRANSITION.equalsIgnoreCase(todZone)) {
            // Transition Zone: 30% of (A+B)
            BigDecimal additionalFar = (baseFar.add(premiumFar))
                    .multiply(BigDecimal.valueOf(0.30))
                    .setScale(2, RoundingMode.HALF_UP);

            permissibleFar = baseFar.add(premiumFar).add(additionalFar).add(tdrFar);

            LOG.info("Transition Zone FAR calculation: Base={}, Premium={}, Add(30%)={}, TDR={}, Total={}",
                    baseFar, premiumFar, additionalFar, tdrFar, permissibleFar);
        }

        return permissibleFar;
    }


    private boolean isResidentialApartmentEligibleForMixedUse(OccupancyTypeHelper occupancyType, BigDecimal plotArea,
                                                              BigDecimal oneBigha) {
        boolean eligible = occupancyType != null && occupancyType.getSubtype() != null
                && A_AF.equalsIgnoreCase(occupancyType.getSubtype().getCode()) && plotArea.compareTo(oneBigha) < 0;
        LOG.info("Residential apartment eligible for mixed use: {}", eligible);
        return eligible;
    }

    private void processFarIndustrial(Plan pl, OccupancyTypeHelper occupancyType, BigDecimal far, String typeOfArea,
                                      BigDecimal roadWidth, HashMap<String, String> errors, String feature, String occupancyName) {

        BigDecimal permissibleFar = BigDecimal.ZERO;

        OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding() != null
                ? pl.getVirtualBuilding().getMostRestrictiveFarHelper()
                : null;

        String subtypeCode = mostRestrictiveOccupancyType != null && mostRestrictiveOccupancyType.getSubtype() != null
                ? mostRestrictiveOccupancyType.getSubtype().getCode()
                : null;

        LOG.info("Processing FAR for industrial occupancy, subtype: {}", subtypeCode);

        List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.FAR.getValue(), false);
        Optional<FarRequirement> matchedRule = rules.stream().filter(FarRequirement.class::isInstance)
                .map(FarRequirement.class::cast).filter(ruleObj -> Boolean.TRUE.equals(ruleObj.getActive())).findFirst();

        if (matchedRule.isPresent()) {
            FarRequirement mdmsRule = matchedRule.get();

            if (G_SI.equalsIgnoreCase(subtypeCode)) {
                permissibleFar = mdmsRule.getPermissibleLight();
            } else if (G_LI.equalsIgnoreCase(subtypeCode)) {
                permissibleFar = mdmsRule.getPermissibleMedium();
            } else if (G_PHI.equalsIgnoreCase(subtypeCode)) {
                permissibleFar = mdmsRule.getPermissibleFlattered();
            } else {
                permissibleFar = mdmsRule.getPermissible(); // fallback
            }
            LOG.info("Permissible FAR for industrial subtype '{}': {}", subtypeCode, permissibleFar);
        } else {
            LOG.warn("No active FAR rule found for industrial processing.");
        }

        boolean isAccepted = far.compareTo(permissibleFar) <= 0;
        pl.getFarDetails().setPermissableFar(permissibleFar.doubleValue());
        String expectedResult = LESS_THAN_EQUAL_TO + permissibleFar;

        LOG.info("Industrial FAR validation result for occupancy '{}': provided FAR = {}, accepted = {}", occupancyName,
                far, isAccepted);

        if (errors.isEmpty() && StringUtils.isNotBlank(expectedResult)) {
            buildResult(pl, occupancyName, far, typeOfArea, roadWidth, expectedResult, isAccepted, null, null);
        }
    }

    private BigDecimal applyMixedUseFARIfApplicable(Plan pl, OccupancyTypeHelper occupancyType, BigDecimal permissibleFar) {

        BigDecimal additionalMixedUseFar = permissibleFar.multiply(POINTTHREE);
        permissibleFar = permissibleFar.add(additionalMixedUseFar);

        LOG.info("30% mixed-use FAR applied for residential plot < 1 bigha. New Permissible FAR = {}", permissibleFar);
        return permissibleFar;
    }

//private boolean isGroupHousingWithEWSLIG(Plan pl, OccupancyTypeHelper occupancyType, BigDecimal plotArea) {
//	boolean result = occupancyType != null && occupancyType.getSubtype() != null
//			&& A_AF_GH.equalsIgnoreCase(occupancyType.getSubtype().getCode()) && plotArea.compareTo(TWOTHOUSAND) >= 0
//			&& pl.getPlanInformation() != null && pl.getPlanInformation().getPlotType() != null
//			&& (pl.getPlanInformation().getPlotType().equalsIgnoreCase(EWS)
//					|| pl.getPlanInformation().getPlotType().equalsIgnoreCase(LIG));
//	LOG.info("Group housing with EWS/LIG eligibility: {}", result);
//	return result;
//}

    private BigDecimal applyEWSLIGFarRelaxationIfApplicable(BigDecimal permissibleFar) {
        BigDecimal additionalFar = permissibleFar.multiply(POINTTWOFIVE);
        BigDecimal relaxedFar = permissibleFar.add(additionalFar);
        LOG.info("Applied 25% EWS/LIG FAR relaxation. New permissible FAR: {}", relaxedFar);
        return relaxedFar;
    }

    /**
     * Checks if the plan qualifies for EWS/LIG FAR bonus based on:
     * - Group Housing/Apartment building type
     * - Plot area >= 2000 sq.m
     * - EWS/LIG housing units present with specified carpet areas5
     */
    private boolean isEligibleForEWSLIGFarBonus(Plan pl, OccupancyTypeHelper occupancyType, BigDecimal plotArea) {
        // Check if it's Group Housing or Apartment
        boolean isGroupHousingOrApartment = occupancyType != null && occupancyType.getSubtype() != null
                && (A_AF_GH.equalsIgnoreCase(occupancyType.getSubtype().getCode())
                || A_AF.equalsIgnoreCase(occupancyType.getSubtype().getCode()))	&& pl.getPlanInformation() != null && pl.getPlanInformation().getPlotType() != null && (pl.getPlanInformation().getPlotType().equalsIgnoreCase(EWS));

        // Check minimum plot area requirement (2000 sq.m)
        boolean meetsPlotAreaRequirement = plotArea.compareTo(TWOTHOUSAND) >= 0;

        // Check if EWS/LIG units are present with proper carpet areas
        boolean hasEWSLIGUnits = hasValidEWSLIGUnits(pl);

        boolean eligible = isGroupHousingOrApartment && meetsPlotAreaRequirement && hasEWSLIGUnits;

        LOG.info("EWS/LIG FAR bonus eligibility check: GroupHousing/Apartment={}, PlotArea>=2000={}, HasEWSLIG={}, Result={}",
                isGroupHousingOrApartment, meetsPlotAreaRequirement, hasEWSLIGUnits, eligible);

        return eligible;
    }

    /**
     * Validates if the plan has valid EWS/LIG housing units with proper carpet areas:
     * - EWS units: 31-34 sq.m carpet area
     * - LIG units: up to 66 sq.m carpet area
     */
    private boolean hasValidEWSLIGUnits(Plan pl) {
        for (Block block : pl.getBlocks()) {
            for (Floor floor : block.getBuilding().getFloors()) {
            	 for (FloorUnit unit : floor.getUnits()) {
                for (Occupancy occupancy : unit.getOccupancies()) {
                    BigDecimal carpetArea = occupancy.getCarpetArea();
                    if (carpetArea != null) {
                        // EWS units: 31-34 sq.m carpet area
                        if (carpetArea.compareTo(BigDecimal.valueOf(31)) >= 0
                                && carpetArea.compareTo(BigDecimal.valueOf(34)) <= 0) {
                            LOG.info("Found EWS unit with carpet area: {} sq.m", carpetArea);
                            return true;
                        }
                        // LIG units: up to 66 sq.m carpet area
                        if (carpetArea.compareTo(BigDecimal.valueOf(66)) <= 0
                                && carpetArea.compareTo(BigDecimal.valueOf(34)) > 0) {
                            LOG.info("Found LIG unit with carpet area: {} sq.m", carpetArea);
                            return true;
                        }
                    }
                }
            	 } }
        }
        return false;
    }

    private void buildResult(Plan pl, String occupancyName, BigDecimal far, String typeOfArea, BigDecimal roadWidth,
                             String expectedResult, boolean isAccepted, BigDecimal mixedUseFAR, BigDecimal additionalEWSLIGFar) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, OCCUPANCY);
        scrutinyDetail.addColumnHeading(3, AREA_TYPE);
        scrutinyDetail.addColumnHeading(4, ROAD_WIDTH);
        scrutinyDetail.addColumnHeading(5, PERMISSIBLE);
        scrutinyDetail.addColumnHeading(6, PROVIDED);
        scrutinyDetail.addColumnHeading(7, STATUS);
        scrutinyDetail.setKey(COMMON_FAR);

        String actualResult = far.toString();

        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(RULE_38);
        detail.setOccupancy(occupancyName);
        detail.setAreaType(typeOfArea);
        detail.setRoadWidth(roadWidth.toString());
        detail.setPermissible(expectedResult
                + (mixedUseFAR != null && mixedUseFAR.compareTo(BigDecimal.ZERO) > 0 ? (" (Other uses FAR: " + mixedUseFAR) : "")
                + (additionalEWSLIGFar != null && additionalEWSLIGFar.compareTo(BigDecimal.ZERO) > 0 ? (" (EWS/LIG FAR: " + additionalEWSLIGFar) : "")
                + ")");
        detail.setProvided(actualResult);
        detail.setStatus(isAccepted ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());

        Map<String, String> details = mapReportDetails(detail);
        addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
    }

    private ScrutinyDetail getFarScrutinyDetail(String key) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, AREA_TYPE);
        scrutinyDetail.addColumnHeading(3, ROAD_WIDTH);
        scrutinyDetail.addColumnHeading(4, PERMISSIBLE);
        scrutinyDetail.addColumnHeading(5, PROVIDED);
        scrutinyDetail.addColumnHeading(6, STATUS);
        scrutinyDetail.setKey(key);
        return scrutinyDetail;
    }

    @Override
    public Map<String, Date> getAmendments() {
        return new LinkedHashMap<>();
    }
}
