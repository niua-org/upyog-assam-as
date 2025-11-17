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
 *         derived works should carry eGovernments Foundation LOGGERo on the top right corner.
 *
 *      For the LOGGERo, please refer http://egovernments.org/html/LOGGERo/egov_LOGGERo.png.
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

import static org.egov.edcr.constants.CommonFeatureConstants.DA_RAMP;
import static org.egov.edcr.constants.CommonFeatureConstants.DA_RAMP_DEFINED;
import static org.egov.edcr.constants.CommonFeatureConstants.DA_RAMP_LANDING;
import static org.egov.edcr.constants.CommonFeatureConstants.DA_RAMP_MAX_SLOPE;
import static org.egov.edcr.constants.CommonFeatureConstants.DA_RAMP_SLOPE;
import static org.egov.edcr.constants.CommonFeatureConstants.DA_ROOM;
import static org.egov.edcr.constants.CommonFeatureConstants.EMPTY_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.FLOOR;
import static org.egov.edcr.constants.CommonFeatureConstants.LESS_THAN_SLOPE;
import static org.egov.edcr.constants.CommonFeatureConstants.MIN_NUMBER_DA_ROOMS;
import static org.egov.edcr.constants.CommonFeatureConstants.RAMP_MAX_SLOPE;
import static org.egov.edcr.constants.CommonFeatureConstants.RAMP_MIN_WIDTH;
import static org.egov.edcr.constants.CommonFeatureConstants.RAMP_LENGTH_WIDTH;
import static org.egov.edcr.constants.CommonFeatureConstants.RAMP_POLYLINE_ERROR;
import static org.egov.edcr.constants.CommonFeatureConstants.UNDERSCORE;
import static org.egov.edcr.constants.CommonKeyConstants.BLOCK;
import static org.egov.edcr.constants.CommonKeyConstants.DA_RAMP_NUMBER;
import static org.egov.edcr.constants.CommonKeyConstants.SLOPE_STRING;
import static org.egov.edcr.constants.DxfFileConstants.A_R;
import static org.egov.edcr.constants.EdcrReportConstants.SUBRULE_50_C_4_B;
import static org.egov.edcr.constants.EdcrReportConstants.SUBRULE_50_C_4_B_SLOPE_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.SUBRULE_50_C_4_B_SLOPE_MAN_DESC;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_RAMP_LENGTH;
import static org.egov.edcr.constants.EdcrReportConstants.DESC_RAMP_WIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.DA_DESC_RAMP_WIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_RAMP_WIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.PERMISSIBLE_WIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.NOT_DEFINED;
import static org.egov.edcr.constants.EdcrReportConstants.DESC_RAMP_LENGTH;
import static org.egov.edcr.constants.EdcrReportConstants.DA_DESC_RAMP_LENGTH;
import static org.egov.edcr.constants.EdcrReportConstants.PERMISSIBLE_LENGTH;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.egov.edcr.constants.CommonFeatureConstants.DA_RAMP_LENGTH_WIDTH;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.DARamp;
import org.egov.common.entity.edcr.FeatureEnum;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.OccupancyType;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.Ramp;
import org.egov.common.entity.edcr.RampLanding;
import org.egov.common.entity.edcr.RampServiceRequirement;
import org.egov.common.entity.edcr.ReportScrutinyDetail;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import static org.egov.edcr.constants.CommonFeatureConstants.RAMP_HT;

@Service
public class RampService_Assam extends RampService {
	private static final Logger LOGGER = LogManager.getLogger(RampService_Assam.class);

    @Autowired
	MDMSCacheManager cache;
 
    /**
     * Validates the given Plan object for ramp and DA ramp requirements.
     *
     * @param pl the Plan object to validate
     * @return the validated Plan object with errors added if any
     */
    @Override
    public Plan validate(Plan pl) {
        validateRampMeasurements(pl);
        validateDARamps(pl);
        return pl;
    }

    /**
     * Validates ramp measurements for each floor of every block in the plan.
     *
     * @param pl the Plan object containing blocks and floors to validate ramps
     */
    private void validateRampMeasurements(Plan pl) {
        for (Block block : pl.getBlocks()) {
            if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty()) {
                for (Floor floor : block.getBuilding().getFloors()) {
                    List<Ramp> ramps = floor.getRamps();
                    if (ramps != null && !ramps.isEmpty()) {
                        for (Ramp ramp : ramps) {
                            List<Measurement> rampPolyLines = ramp.getRamps();
                            if (rampPolyLines != null && !rampPolyLines.isEmpty()) {
                                validateRampDimensions(pl, block, floor, scrutinyDetail);
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Validates the presence and slope of DA (Differently Abled) ramps in each block of the plan.
     *
     * @param pl the Plan object containing blocks and occupancy information
     */

    private void validateDARamps(Plan pl) {
        HashMap<String, String> errors = new HashMap<>();
        OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding().getMostRestrictiveFarHelper();

        if (pl != null && !pl.getBlocks().isEmpty()) {
            for (Block block : pl.getBlocks()) {
                if (shouldValidateDARamps(pl, block, mostRestrictiveOccupancyType)) {
                    if (!block.getDARamps().isEmpty()) {
                        validateSlopeForDARamps(pl, block, errors);
                    } else {
                        addMissingDARampError(pl, block, errors);
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Determines if the DA ramp validation should be performed for a given block based on occupancy and plot conditions.
     *
     * @param pl the Plan object
     * @param block the Block to be checked
     * @param mostRestrictiveOccupancyType the most restrictive occupancy type in the plan
     * @return true if validation should be performed; false otherwise
     */

    private boolean shouldValidateDARamps(Plan pl, Block block, OccupancyTypeHelper mostRestrictiveOccupancyType) {
        return pl.getPlot() != null
                && !Util.checkExemptionConditionForSmallPlotAtBlkLevel(pl.getPlot(), block)
                && mostRestrictiveOccupancyType != null
                && mostRestrictiveOccupancyType.getSubtype() != null
                && !A_R.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode());
    }

    /**
     * Validates the slope values for DA ramps in a block and adds errors to the plan if not defined.
     *
     * @param pl the Plan object to add errors to
     * @param block the Block containing DA ramps
     * @param errors map of errors to be added
     */
    private void validateSlopeForDARamps(Plan pl, Block block, Map<String, String> errors) {
        boolean isSlopeDefined = false;
        for (DARamp daRamp : block.getDARamps()) {
            if (daRamp != null && daRamp.getSlope() != null && daRamp.getSlope().compareTo(BigDecimal.valueOf(0)) > 0) {
                isSlopeDefined = true;
            }
	
        }
        if (!isSlopeDefined) {
            errors.put(String.format(DcrConstants.RAMP_SLOPE, EMPTY_STRING, block.getNumber()),
                    edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                            new String[]{String.format(DcrConstants.RAMP_SLOPE, EMPTY_STRING, block.getNumber())},
                            LocaleContextHolder.getLocale()));
            pl.addErrors(errors);
        }
    }

    /**
     * Adds an error to the Plan indicating the absence of a required DA ramp in the block.
     *
     * @param pl the Plan object to add errors to
     * @param block the Block missing the DA ramp
     * @param errors map of errors to be added
     */
    private void addMissingDARampError(Plan pl, Block block, Map<String, String> errors) {
        errors.put(String.format(DA_RAMP, block.getNumber()),
                edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[]{String.format(DA_RAMP, block.getNumber())},
                        LocaleContextHolder.getLocale()));
        pl.addErrors(errors);
    }

    /**
     * Checks if a given occupancy type is one for which ramps are required.
     *
     * @param occupancyType the OccupancyType to check
     * @return true if the occupancy type requires ramps; false otherwise
     */

    private boolean getOccupanciesForRamp(OccupancyType occupancyType) {
        return occupancyType.equals(OccupancyType.OCCUPANCY_A2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_A3) || occupancyType.equals(OccupancyType.OCCUPANCY_A4) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_B1) || occupancyType.equals(OccupancyType.OCCUPANCY_B2) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_B3) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_C) || occupancyType.equals(OccupancyType.OCCUPANCY_C1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_C2) || occupancyType.equals(OccupancyType.OCCUPANCY_C3) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_D) || occupancyType.equals(OccupancyType.OCCUPANCY_D1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_D2) || occupancyType.equals(OccupancyType.OCCUPANCY_E) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_F) || occupancyType.equals(OccupancyType.OCCUPANCY_F1) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_F2) || occupancyType.equals(OccupancyType.OCCUPANCY_F3) ||
                occupancyType.equals(OccupancyType.OCCUPANCY_F4);
    }

    
    /**
     * Processes the Plan object for compliance with ramp and DA ramp rules,
     * performing validations and adding scrutiny details.
     *
     * @param pl the Plan object to process
     * @return the processed Plan with scrutiny details
     */
    @Override
    public Plan process(Plan pl) {
        BigDecimal rampServiceValueOne = BigDecimal.ZERO;
        BigDecimal rampServiceExpectedSlopeOne = BigDecimal.ZERO;
        BigDecimal rampServiceDivideExpectedSlope = BigDecimal.ZERO;
        BigDecimal rampServiceSlopValue = BigDecimal.ZERO;
        BigDecimal rampServiceBuildingHeight = BigDecimal.ZERO;
        BigDecimal rampServiceTotalLength = BigDecimal.ZERO;
        BigDecimal rampServiceExpectedSlopeCompare = BigDecimal.ZERO;
        BigDecimal rampServiceExpectedSlopeTwo = BigDecimal.ZERO;
        BigDecimal rampServiceExpectedSlopeCompareTrue = BigDecimal.ZERO;
        BigDecimal rampServiceExpectedSlopeCompareFalse = BigDecimal.ZERO;
        BigDecimal rampServiceMinHeightEntrance = BigDecimal.ZERO;
        BigDecimal rampServiceDivideExpectedSlopeOne = BigDecimal.ZERO;

        validate(pl);

        if (pl != null && !pl.getBlocks().isEmpty()) {
            for (Block block : pl.getBlocks()) {
            	 for (Floor floor : block.getBuilding().getFloors()) {
                ScrutinyDetail scrutinyDetail = createScrutinyDetail(DA_RAMP_DEFINED, block.getNumber(), false);
                ScrutinyDetail scrutinyDetail1 = createScrutinyDetail(DA_RAMP_SLOPE, block.getNumber(), false);
                ScrutinyDetail scrutinyDetail2 = createScrutinyDetail(DA_RAMP_MAX_SLOPE, block.getNumber(), false);
                ScrutinyDetail scrutinyDetail8 = createScrutinyDetail(RAMP_MAX_SLOPE, block.getNumber(), false);
                ScrutinyDetail scrutinyDetail3 = createScrutinyDetail(DA_ROOM, block.getNumber(), false);
                ScrutinyDetail scrutinyDetail4 = createScrutinyDetail(RAMP_MIN_WIDTH, block.getNumber(), true);
                ScrutinyDetail scrutinyDetail5 = createScrutinyDetail(RAMP_LENGTH_WIDTH, block.getNumber(), true);
                ScrutinyDetail scrutinyDetail7 = createScrutinyDetail(DA_RAMP_LENGTH_WIDTH, block.getNumber(), false);
                ScrutinyDetail scrutinyDetail6 = createScrutinyDetail(RAMP_HT, block.getNumber(), false);


                List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.RAMP_SERVICE.getValue(), false);
                Optional<RampServiceRequirement> matchedRule = rules.stream()
                    .filter(RampServiceRequirement.class::isInstance)
                    .map(RampServiceRequirement.class::cast)
                    .findFirst();

                if (matchedRule.isPresent()) {
                	RampServiceRequirement rule = matchedRule.get();
                    rampServiceValueOne = rule.getRampServiceValueOne();
                    rampServiceExpectedSlopeOne = rule.getRampServiceExpectedSlopeOne();
                    rampServiceDivideExpectedSlope = rule.getRampServiceDivideExpectedSlope();
                    rampServiceDivideExpectedSlopeOne = rule.getRampServiceDivideExpectedSlopeOne();
                    rampServiceSlopValue = rule.getRampServiceSlopValue();
                    rampServiceBuildingHeight = rule.getRampServiceBuildingHeight();
                    rampServiceTotalLength = rule.getRampServiceTotalLength();
                    rampServiceExpectedSlopeTwo = rule.getRampServiceExpectedSlopeTwo();
                    rampServiceExpectedSlopeCompare = rule.getRampServiceExpectedSlopeCompare();
                    rampServiceExpectedSlopeCompareTrue = rule.getRampServiceExpectedSlopeCompareTrue();
                    rampServiceExpectedSlopeCompareFalse = rule.getRampServiceExpectedSlopeCompareFalse();
                    rampServiceMinHeightEntrance = rule.getRampServiceMinHeightEntrance();
                }

                processDARampSlopeValidation(pl, block, rampServiceValueOne, rampServiceExpectedSlopeOne,
                        rampServiceDivideExpectedSlope, rampServiceSlopValue, scrutinyDetail1, scrutinyDetail2);
                processRampSlopeValidation(pl, block, floor, rampServiceValueOne, rampServiceExpectedSlopeOne,
                        rampServiceDivideExpectedSlopeOne, rampServiceSlopValue, scrutinyDetail1, scrutinyDetail8);

                processDARoomValidation(pl, block, rampServiceBuildingHeight, scrutinyDetail3);
                
                validateMinHeightEntrance(pl, block, rampServiceMinHeightEntrance, scrutinyDetail6);
                
                validateDARampDimensions(pl, block, scrutinyDetail7);
                
                validateRampDimensions(pl, block, floor, scrutinyDetail8);

            }
        }
        }
        return pl;
    }
    
    /**
     * Creates a ScrutinyDetail object with given parameters.
     *
     * @param keySuffix suffix to be used in the scrutiny detail key
     * @param blockNumber the number of the block being validated
     * @param hasFloorColumn whether floor column should be added
     * @return the constructed ScrutinyDetail object
     */

    private ScrutinyDetail createScrutinyDetail(String keySuffix, String blockNumber, boolean hasFloorColumn) {
        ScrutinyDetail scrutinyDetail = new ScrutinyDetail();
        scrutinyDetail.addColumnHeading(1, RULE_NO);
        scrutinyDetail.addColumnHeading(2, DESCRIPTION);
        int columnIndex = 3;
        if (hasFloorColumn) {
            scrutinyDetail.addColumnHeading(columnIndex++, FLOOR);
        }
        scrutinyDetail.addColumnHeading(columnIndex++, REQUIRED);
        scrutinyDetail.addColumnHeading(columnIndex++, PROVIDED);
        scrutinyDetail.addColumnHeading(columnIndex, STATUS);
        scrutinyDetail.setKey(BLOCK + blockNumber + UNDERSCORE + keySuffix);
        if (keySuffix.equals(DA_ROOM)) {
            scrutinyDetail.setSubHeading(MIN_NUMBER_DA_ROOMS);
        }
        return scrutinyDetail;
    }
    
    /**
     * Performs validation of DA ramp slope for the given block using specified slope values and adds scrutiny details.
     *
     * @param pl the Plan object
     * @param block the Block containing the DA ramps
     * @param rampServiceValueOne base value to check if slope is defined
     * @param rampServiceExpectedSlopeOne numerator of expected slope calculation
     * @param rampServiceDivideExpectedSlope denominator of expected slope calculation
     * @param rampServiceSlopValue minimum allowed slope value
     * @param scrutinyDetail1 scrutiny detail for slope definition
     * @param scrutinyDetail2 scrutiny detail for slope compliance
     */

    private void processDARampSlopeValidation(Plan pl, Block block, BigDecimal rampServiceValueOne,
            BigDecimal rampServiceExpectedSlopeOne, BigDecimal rampServiceDivideExpectedSlope,
            BigDecimal rampServiceSlopValue, ScrutinyDetail scrutinyDetail1, ScrutinyDetail scrutinyDetail2) {

        OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding().getMostRestrictiveFarHelper();

        if (pl.getPlot() != null
                && !Util.checkExemptionConditionForSmallPlotAtBlkLevel(pl.getPlot(), block)
                && mostRestrictiveOccupancyType != null
                && mostRestrictiveOccupancyType.getSubtype() != null
                && !A_R.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode())) {

            if (!block.getDARamps().isEmpty()) {
                boolean isSlopeDefined = isSlopeDefined(block, rampServiceValueOne);

                setReportOutputDetails(pl, SUBRULE_50_C_4_B, SUBRULE_50_C_4_B_SLOPE_MAN_DESC, EMPTY_STRING,
                        isSlopeDefined ? DcrConstants.OBJECTDEFINED_DESC : DcrConstants.OBJECTNOTDEFINED_DESC,
                        isSlopeDefined ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal(),
                        scrutinyDetail1);

                if (isSlopeDefined) {
             
                    validateDARampSlopes(pl, block, rampServiceExpectedSlopeOne, rampServiceDivideExpectedSlope, scrutinyDetail2);

                }
            }
        }
    }
    

    /* Performs validation of DA ramp slope for the given block using specified slope values and adds scrutiny details.
    *
    * @param pl the Plan object
    * @param block the Block containing the DA ramps
    * @param rampServiceValueOne base value to check if slope is defined
    * @param rampServiceExpectedSlopeOne numerator of expected slope calculation
    * @param rampServiceDivideExpectedSlope denominator of expected slope calculation
    * @param rampServiceSlopValue minimum allowed slope value
    * @param scrutinyDetail1 scrutiny detail for slope definition
    * @param scrutinyDetail2 scrutiny detail for slope compliance
    */

   private void processRampSlopeValidation(Plan pl, Block block, Floor floor, BigDecimal rampServiceValueOne,
           BigDecimal rampServiceExpectedSlopeOne, BigDecimal rampServiceDivideExpectedSlope,
           BigDecimal rampServiceSlopValue, ScrutinyDetail scrutinyDetail1, ScrutinyDetail scrutinyDetail8) {

       OccupancyTypeHelper mostRestrictiveOccupancyType = pl.getVirtualBuilding().getMostRestrictiveFarHelper();

       if (pl.getPlot() != null
               && !Util.checkExemptionConditionForSmallPlotAtBlkLevel(pl.getPlot(), block)
               && mostRestrictiveOccupancyType != null
               && mostRestrictiveOccupancyType.getSubtype() != null
               && !A_R.equalsIgnoreCase(mostRestrictiveOccupancyType.getSubtype().getCode())) {

           if (!floor.getRamps().isEmpty()) {
               boolean isSlopeDefined = isSlopeDefinedForRamps(block, floor, rampServiceValueOne);

               setReportOutputDetails(pl, SUBRULE_50_C_4_B, SUBRULE_50_C_4_B_SLOPE_MAN_DESC, EMPTY_STRING,
                       isSlopeDefined ? DcrConstants.OBJECTDEFINED_DESC : DcrConstants.OBJECTNOTDEFINED_DESC,
                       isSlopeDefined ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal(),
                       scrutinyDetail1);

               if (isSlopeDefined) {
            
            	   validateRampSlopes(pl, block, floor, rampServiceExpectedSlopeOne, rampServiceDivideExpectedSlope, scrutinyDetail8);

               }
           }
       }
   }

    /**
     * Checks if any DA ramp in the block has a slope defined above a specified threshold.
     *
     * @param block the Block to check
     * @param rampServiceValueOne minimum slope value to consider as defined
     * @return true if a valid slope is defined; false otherwise
     */
    private boolean isSlopeDefined(Block block, BigDecimal rampServiceValueOne) {
        for (DARamp daRamp : block.getDARamps()) {
            if (daRamp != null && daRamp.getSlope() != null
                    && daRamp.getSlope().compareTo(rampServiceValueOne) > 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if any DA ramp in the block has a slope defined above a specified threshold.
     *
     * @param block the Block to check
     * @param rampServiceValueOne minimum slope value to consider as defined
     * @return true if a valid slope is defined; false otherwise
     */
    private boolean isSlopeDefinedForRamps(Block block, Floor floor, BigDecimal rampServiceValueOne) {
        for (Ramp ramp : floor.getRamps()) {
            if (ramp != null && ramp.getSlope() != null
                    && ramp.getSlope().compareTo(rampServiceValueOne) > 0) {
                return true;
            }
        }
        return false;
    }

	/**
	 * Validates ramp slopes against expected values and updates the scrutiny
	 * report.
	 *
	 * @param pl                  The plan
	 * @param block               The block containing ramps
	 * @param expectedSlopeOne    The numerator for expected slope calculation
	 * @param divideExpectedSlope The denominator for expected slope calculation
	 * @param rampServiceSlope    The minimum slope value to consider
	 * @param scrutinyDetail2     The scrutiny detail object to update
	 */
    private void validateDARampSlopes(Plan pl, Block block, BigDecimal expectedSlopeOne,
            BigDecimal divideExpectedSlope, ScrutinyDetail scrutinyDetail) {

		boolean valid = false;
		Map<String, String> mapOfRampNumberAndSlopeValues = new HashMap<>();

// expectedSlope = divideExpectedSlope / expectedSlopeOne
		BigDecimal expectedSlope = divideExpectedSlope.divide(expectedSlopeOne, 2, RoundingMode.HALF_UP);
		BigDecimal tolerance = new BigDecimal("0.01"); 

		BigDecimal lastSlope = BigDecimal.ZERO;
		String lastRampNumber = "";

		for (DARamp daRamp : block.getDARamps()) {

			BigDecimal slope = daRamp.getSlope(); // rise/run
			if (slope != null && slope.compareTo(BigDecimal.ZERO) > 0) {

				lastSlope = slope.setScale(2, RoundingMode.HALF_UP); 
				lastRampNumber = String.valueOf(daRamp.getNumber());

				BigDecimal lowerBound = expectedSlope.subtract(tolerance);
				BigDecimal upperBound = expectedSlope.add(tolerance);

				if (lastSlope.compareTo(lowerBound) >= 0 && lastSlope.compareTo(upperBound) <= 0) {
					valid = true;

					mapOfRampNumberAndSlopeValues.put(DA_RAMP_NUMBER, lastRampNumber);
					mapOfRampNumberAndSlopeValues.put(SLOPE_STRING, lastSlope.toPlainString());
					break;
				}
			}
		}

// Expected value formatted as 1:XX.xx
		String expectedSlopeFormatted = String.format("1:%s",
				divideExpectedSlope.setScale(2, RoundingMode.HALF_UP).toPlainString());

// For provided slope (lastSlope)
		String providedSlopeFormatted;
		String providedRampNumber = mapOfRampNumberAndSlopeValues.getOrDefault(DA_RAMP_NUMBER, lastRampNumber);
		String providedSlope = mapOfRampNumberAndSlopeValues.getOrDefault(SLOPE_STRING, lastSlope.toPlainString());

		try {
			if (lastSlope.compareTo(BigDecimal.ZERO) > 0) {

// If slope > 1 (already a ratio like 1:12), show integer
				if (lastSlope.compareTo(BigDecimal.ONE) > 0) {
					providedSlopeFormatted = String.format("1:%.2f", lastSlope.setScale(2, RoundingMode.HALF_UP));
				} else {
// Convert fractional slope (e.g. 0.0833 → 1:12.00)
					BigDecimal inverse = BigDecimal.ONE.divide(lastSlope, 2, RoundingMode.HALF_UP);
					providedSlopeFormatted = String.format("1:%.2f", inverse);
				}

			} else {
				providedSlopeFormatted = providedSlope;
			}

		} catch (ArithmeticException e) {
			providedSlopeFormatted = providedSlope; // fallback
		}

		setReportOutputDetails(pl, SUBRULE_50_C_4_B,
				String.format(SUBRULE_50_C_4_B_SLOPE_DESCRIPTION, providedRampNumber), expectedSlopeFormatted,
				providedSlopeFormatted, valid ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal(),
				scrutinyDetail);

		LOGGER.info("Ramp slope validation completed for Block {} -> Expected: {}, Provided: {}, Result: {}",
				block.getNumber(), expectedSlopeFormatted, providedSlopeFormatted, valid ? "Accepted" : "Not Accepted");
	}


	/**
	 * Validates ramp slopes against expected values and updates the scrutiny
	 * report.
	 *
	 * @param pl                  The plan
	 * @param block               The block containing ramps
	 * @param expectedSlopeOne    The numerator for expected slope calculation
	 * @param divideExpectedSlope The denominator for expected slope calculation
	 * @param rampServiceSlope    The minimum slope value to consider
	 * @param scrutinyDetail2     The scrutiny detail object to update
	 */
	private void validateRampSlopes(Plan pl, Block block, Floor floor, BigDecimal expectedSlopeOne,
			BigDecimal divideExpectedSlope, ScrutinyDetail scrutinyDetail) {

		boolean valid = false;
		Map<String, String> mapOfRampNumberAndSlopeValues = new HashMap<>();

		BigDecimal expectedSlope = divideExpectedSlope;

		BigDecimal lastSlope = BigDecimal.ZERO;
		String lastRampNumber = "";

		for (Ramp ramp : floor.getRamps()) {

			BigDecimal slope = ramp.getSlope(); 
			if (slope != null && slope.compareTo(BigDecimal.ZERO) > 0) {

				lastSlope = slope;
				lastRampNumber = String.valueOf(ramp.getNumber());


				if (slope.compareTo(expectedSlope) >= 0) {
					valid = true;
					mapOfRampNumberAndSlopeValues.put(DA_RAMP_NUMBER, lastRampNumber);
					mapOfRampNumberAndSlopeValues.put(SLOPE_STRING, slope.toString());
					break;
				}
			}
		}

     // ----------- FORMAT EXPECTED VALUE ------------
		String expectedSlopeFormatted = "1:" + expectedSlope.toPlainString();

     // ----------- FORMAT PROVIDED VALUE ------------
		String providedSlopeFormatted;

		if (lastSlope.compareTo(BigDecimal.ZERO) > 0) {
          // Provided slope already in X (not a fraction) like 10, 12, 14
			providedSlopeFormatted = "1:" + lastSlope.setScale(0, RoundingMode.HALF_UP).toPlainString();
		} else {
			providedSlopeFormatted = "N/A";
		}

		String providedRampNumber = mapOfRampNumberAndSlopeValues.getOrDefault(DA_RAMP_NUMBER, lastRampNumber);

     // ----------- REPORT OUTPUT ------------
		setReportOutputDetails(pl, SUBRULE_50_C_4_B,
				String.format(SUBRULE_50_C_4_B_SLOPE_DESCRIPTION, providedRampNumber), expectedSlopeFormatted,
				providedSlopeFormatted, valid ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal(),
				scrutinyDetail);

		LOGGER.info("Ramp slope validation for Block {} -> Required: {}, Provided: {}, Accepted: {}", block.getNumber(),
				expectedSlopeFormatted, providedSlopeFormatted, valid ? "YES" : "NO");
	}


    private void validateMinHeightEntrance(Plan pl, Block block, BigDecimal requiredMinHeight, ScrutinyDetail scrutinyDetail) {
        for (Floor floor : block.getBuilding().getFloors()) {
            List<Ramp> ramps = floor.getRamps();
            if (ramps != null) {
                for (Ramp ramp : ramps) {
                    BigDecimal providedHeight = ramp.getMinEntranceHeight();
                    String status = (providedHeight != null && providedHeight.compareTo(requiredMinHeight) >= 0)
                            ? Result.Accepted.getResultVal()
                            : Result.Not_Accepted.getResultVal();

                    String provided = (providedHeight != null) ? providedHeight.toString() : "Not Defined";
                    
                    setReportOutputDetails(
                            pl, "RULE_30", "Minimum Entrance Height for Ramp " + ramp.getNumber(),
                            requiredMinHeight.toString(), provided, status, scrutinyDetail);
                }
            }
        }
    }
    
  
    /**
     * Validates the dimensions of DA (Disabled Access) Ramps for a given block.
     * It checks both ramp length and width against the permissible limits defined in MDMS rules.
     *
     * @param pl              The plan object containing building details.
     * @param block           The block containing DA ramps.
     * @param scrutinyDetail  The scrutiny detail object for recording validation results.
     */
    private void validateDARampDimensions(Plan pl, Block block, ScrutinyDetail scrutinyDetail) {
        LOGGER.info("Validating DA Ramp dimensions for Block: {}", block.getNumber());

        for (DARamp daRamp : block.getDARamps()) {
            LOGGER.info("Processing DA Ramp: {}", daRamp);

            List<BigDecimal> widthList = daRamp.getDaRampWidth();
            List<BigDecimal> lengthList = daRamp.getDaRampLength();

            // Compute minimum width and total length
            BigDecimal minWidth = (widthList != null && !widthList.isEmpty())
                    ? widthList.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO)
                    : BigDecimal.ZERO;

            BigDecimal totalLength = (lengthList != null && !lengthList.isEmpty())
                    ? lengthList.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    : BigDecimal.ZERO;

            String providedLength = String.format("%.2f", totalLength);
            String providedWidth = String.format("%.2f", minWidth);

            LOGGER.info("Ramp Measurements → Total Length: {} m, Min Width: {} m", providedLength, providedWidth);

          
            BigDecimal rampServiceMinWidth = BigDecimal.valueOf(1.5);

            // Calculate required length based on height × slope
            BigDecimal requiredLength = (daRamp.getHeight() != null && daRamp.getSlope() != null)
                    ? daRamp.getHeight().multiply(daRamp.getSlope())
                    : BigDecimal.ZERO;

            String requiredLengthFormatted = String.format("%.2f", requiredLength);
            // Fetch rules from cache (if available)
            List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.RAMP_SERVICE.getValue(), false);
            Optional<RampServiceRequirement> matchedRule = rules.stream()
                    .filter(RampServiceRequirement.class::isInstance)
                    .map(RampServiceRequirement.class::cast)
                    .findFirst();

            if (matchedRule.isPresent()) {
                RampServiceRequirement rule = matchedRule.get(); 
                if (rule.getRampServiceMinWidth() != null)
                    rampServiceMinWidth = rule.getRampServiceMinWidth();

                LOGGER.info("Matched Rule →  MinWidth: {} m",
                       rampServiceMinWidth);
            }

            // ---------------------------------------
            //  LENGTH VALIDATION
            // ---------------------------------------
            if (totalLength.compareTo(requiredLength) >= 0) {
                LOGGER.info("Ramp Length validation passed. Provided: {} ≥ Required: {}", totalLength, requiredLengthFormatted);
                setReportOutputDetails(pl,
                        RULE_RAMP_LENGTH,
                        DA_DESC_RAMP_LENGTH,
                        requiredLengthFormatted.toString(),
                        providedLength,
                        Result.Accepted.getResultVal(),
                        scrutinyDetail);
            } else {
                LOGGER.info("Ramp Length validation failed. Provided: {} < Required: {}", totalLength, requiredLengthFormatted);
                setReportOutputDetails(pl,
                        RULE_RAMP_LENGTH,
                        DA_DESC_RAMP_LENGTH,
                        requiredLengthFormatted.toString(),
                        providedLength,
                        Result.Not_Accepted.getResultVal(),
                        scrutinyDetail);
            }

            // ---------------------------------------
            //  WIDTH VALIDATION
            // ---------------------------------------
            if (minWidth.compareTo(rampServiceMinWidth) >= 0) {
                LOGGER.info(" Ramp Width validation passed. Provided: {} ≥ Required: {}", minWidth, rampServiceMinWidth);
                setReportOutputDetails(pl,
                        RULE_RAMP_WIDTH,
                        DESC_RAMP_WIDTH,
                        rampServiceMinWidth.toString(),
                        providedWidth,
                        Result.Accepted.getResultVal(),
                        scrutinyDetail);
            } else {
                LOGGER.info(" Ramp Width validation failed. Provided: {} < Required: {}", minWidth, rampServiceMinWidth);
                setReportOutputDetails(pl,
                        RULE_RAMP_WIDTH,
                        DESC_RAMP_WIDTH,
                        rampServiceMinWidth.toString(),
                        providedWidth,
                        Result.Not_Accepted.getResultVal(),
                        scrutinyDetail);
            }
        }
    }

    /**
     * Validates the dimensions of DA (Disabled Access) Ramps for a given block.
     * It checks both ramp length and width against the permissible limits defined in MDMS rules.
     *
     * @param pl              The plan object containing building details.
     * @param block           The block containing DA ramps.
     * @param scrutinyDetail  The scrutiny detail object for recording validation results.
     */
    private void validateRampDimensions(Plan pl, Block block, Floor floor, ScrutinyDetail scrutinyDetail) {
        LOGGER.info("Validating DA Ramp dimensions for Block: {}", block.getNumber());

        for (Ramp ramp : floor.getRamps()) {
            LOGGER.info("Processing Ramp: {}", ramp);

            List<BigDecimal> widthList = ramp.getRampWidth();
            List<BigDecimal> lengthList = ramp.getRampLength();

            // Compute minimum width and total length
            BigDecimal minWidth = (widthList != null && !widthList.isEmpty())
                    ? widthList.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO)
                    : BigDecimal.ZERO;

            BigDecimal totalLength = (lengthList != null && !lengthList.isEmpty())
                    ? lengthList.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                    : BigDecimal.ZERO;

            String providedLength = String.format("%.2f", totalLength);
            String providedWidth = String.format("%.2f", minWidth);

            LOGGER.info("Ramp Measurements → Total Length: {} m, Min Width: {} m", providedLength, providedWidth);

          
            BigDecimal rampServiceMinWidth = BigDecimal.valueOf(1.5);

            // Calculate required length based on height × slope
            BigDecimal requiredLength = (ramp.getHeight() != null && ramp.getSlope() != null)
                    ? ramp.getHeight().multiply(BigDecimal.valueOf(10)).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);


            String requiredLengthFormatted = String.format("%.2f", requiredLength);
            // Fetch rules from cache (if available)
            List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.RAMP_SERVICE.getValue(), false);
            Optional<RampServiceRequirement> matchedRule = rules.stream()
                    .filter(RampServiceRequirement.class::isInstance)
                    .map(RampServiceRequirement.class::cast)
                    .findFirst();

            if (matchedRule.isPresent()) {
                RampServiceRequirement rule = matchedRule.get(); 
                if (rule.getRampServiceMinWidth() != null)
                    rampServiceMinWidth = rule.getRampServiceMinWidth();

                LOGGER.info("Matched Rule →  MinWidth: {} m",
                       rampServiceMinWidth);
            }

            // ---------------------------------------
            //  LENGTH VALIDATION
            // ---------------------------------------
            if (totalLength.compareTo(requiredLength) >= 0) {
                LOGGER.info("Ramp Length validation passed. Provided: {} ≥ Required: {}", totalLength, requiredLengthFormatted);
                setReportOutputDetails(pl,
                        RULE_RAMP_LENGTH,
                        DESC_RAMP_LENGTH,
                        requiredLengthFormatted.toString(),
                        providedLength,
                        Result.Accepted.getResultVal(),
                        scrutinyDetail);
            } else {
                LOGGER.info("Ramp Length validation failed. Provided: {} < Required: {}", totalLength, requiredLengthFormatted);
                setReportOutputDetails(pl,
                        RULE_RAMP_LENGTH,
                        DESC_RAMP_LENGTH,
                        requiredLengthFormatted.toString(),
                        providedLength,
                        Result.Not_Accepted.getResultVal(),
                        scrutinyDetail);
            }

            // ---------------------------------------
            //  WIDTH VALIDATION
            // ---------------------------------------
            if (minWidth.compareTo(rampServiceMinWidth) >= 0) {
                LOGGER.info(" Ramp Width validation passed. Provided: {} ≥ Required: {}", minWidth, rampServiceMinWidth);
                setReportOutputDetails(pl,
                        RULE_RAMP_WIDTH,
                        DESC_RAMP_WIDTH,
                        rampServiceMinWidth.toString(),
                        providedWidth,
                        Result.Accepted.getResultVal(),
                        scrutinyDetail);
            } else {
                LOGGER.info(" Ramp Width validation failed. Provided: {} < Required: {}", minWidth, rampServiceMinWidth);
                setReportOutputDetails(pl,
                        RULE_RAMP_WIDTH,
                        DESC_RAMP_WIDTH,
                        rampServiceMinWidth.toString(),
                        providedWidth,
                        Result.Not_Accepted.getResultVal(),
                        scrutinyDetail);
            }
        }
    }
    private void processDARoomValidation(Plan pl, Block block, BigDecimal rampServiceBuildingHeight, ScrutinyDetail scrutinyDetail3) {
        
    }

    private void setReportOutputDetails(Plan pl, String ruleNo, String ruleDesc, String expected, String actual, String status,
            ScrutinyDetail scrutinyDetail) {
        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(ruleNo);
        detail.setDescription(ruleDesc);
        detail.setRequired(expected);
        detail.setProvided(actual);
        detail.setStatus(status);

        Map<String, String> details = mapReportDetails(detail);
        addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
    }

    private void setReportOutputDetailsFloorWise(Plan pl, String ruleNo, String floor, String expected, String actual,
            String status, ScrutinyDetail scrutinyDetail) {
        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(ruleNo);
        detail.setFloorNo(floor);
        detail.setRequired(expected);
        detail.setProvided(actual);
        detail.setStatus(status);

        Map<String, String> details = mapReportDetails(detail);
        addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
    }

    private void setReportOutputDetailsFloorWiseWithDescription(Plan pl, String ruleNo, String ruleDesc, String floor,
            String expected, String actual, String status, ScrutinyDetail scrutinyDetail) {
        ReportScrutinyDetail detail = new ReportScrutinyDetail();
        detail.setRuleNo(ruleNo);
        detail.setDescription(ruleDesc);
        detail.setFloorNo(floor);
        detail.setRequired(expected);
        detail.setProvided(actual);
        detail.setStatus(status);

        Map<String, String> details = mapReportDetails(detail);
        addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
    }

  
    private void validateDimensions(Plan plan, String blockNo, int floorNo, String rampNo,
            List<Measurement> rampPolylines) {
        int count = 0;
        for (Measurement m : rampPolylines) {
            if (m.getInvalidReason() != null && m.getInvalidReason().length() > 0) {
                count++;
            }
        }
        if (count > 0) {
            plan.addError(String.format(DxfFileConstants.LAYER_RAMP_WITH_NO, blockNo, floorNo, rampNo),
                    count + RAMP_POLYLINE_ERROR
                            + String.format(DxfFileConstants.LAYER_RAMP_WITH_NO, blockNo, floorNo, rampNo));

        }
    }

    @Override
    public Map<String, Date> getAmendments() {
        return new LinkedHashMap<>();
    }
}
