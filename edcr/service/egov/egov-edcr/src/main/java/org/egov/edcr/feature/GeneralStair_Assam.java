package org.egov.edcr.feature;

import static org.egov.edcr.constants.CommonFeatureConstants.EMPTY_STRING;
import static org.egov.edcr.constants.CommonFeatureConstants.FLOOR;
import static org.egov.edcr.constants.CommonFeatureConstants.GENERAL_STAIR;
import static org.egov.edcr.constants.CommonFeatureConstants.GENERAL_STAIR_LANDING_NOT_DEFINED;
import static org.egov.edcr.constants.CommonFeatureConstants.GENERAL_STAIR_LANDING_WIDTH_NOT_DEFINED;
import static org.egov.edcr.constants.CommonFeatureConstants.GENERAL_STAIR_MANDATORY;
import static org.egov.edcr.constants.CommonFeatureConstants.GENERAL_STAIR_MANDATORY_SUFFIX;
import static org.egov.edcr.constants.CommonFeatureConstants.GENERAL_STAIR_NOT_DEFINED;
import static org.egov.edcr.constants.CommonFeatureConstants.NO_OF_RISES_COUNT_ERROR;
import static org.egov.edcr.constants.CommonFeatureConstants.RISER_HEIGHT_DESC;
import static org.egov.edcr.constants.CommonFeatureConstants.STAIR_PREFIX;
import static org.egov.edcr.constants.CommonKeyConstants.BLOCK;
import static org.egov.edcr.constants.CommonKeyConstants.BLOCK_PREFIX;
import static org.egov.edcr.constants.CommonKeyConstants.FLIGHT_POLYLINE;
import static org.egov.edcr.constants.CommonKeyConstants.FLIGHT_POLYLINE_LENGTH;
import static org.egov.edcr.constants.CommonKeyConstants.FLIGHT_POLYLINE_WIDTH;
import static org.egov.edcr.constants.CommonKeyConstants.FLOOR_SPACED;
import static org.egov.edcr.constants.CommonKeyConstants.GENERAL_STAIR_MID_LANDING;
import static org.egov.edcr.constants.CommonKeyConstants.GENERAL_STAIR_NUMBER_OF_RISERS;
import static org.egov.edcr.constants.CommonKeyConstants.GENERAL_STAIR_RISER_HEIGHT;
import static org.egov.edcr.constants.CommonKeyConstants.GENERAL_STAIR_TREAD_WIDTH;
import static org.egov.edcr.constants.CommonKeyConstants.GENERAL_STAIR_WIDTH;
import static org.egov.edcr.constants.CommonKeyConstants.IS_TYPICAL_REP_FLOOR;
import static org.egov.edcr.constants.CommonKeyConstants.MINIMUM_TWO_STAIRCASES_REQUIRED;
import static org.egov.edcr.constants.CommonKeyConstants.MINIMUM_TWO_STAIRCASES_REQUIRED_MSG;
import static org.egov.edcr.constants.CommonKeyConstants.NO_OF_RISE;
import static org.egov.edcr.constants.CommonKeyConstants.NO_OF_RISES_COUNT;
import static org.egov.edcr.constants.CommonKeyConstants.TYPICAL_FLOOR;
import static org.egov.edcr.constants.DxfFileConstants.B;
import static org.egov.edcr.constants.DxfFileConstants.C;
import static org.egov.edcr.constants.DxfFileConstants.D;
import static org.egov.edcr.constants.DxfFileConstants.G;
import static org.egov.edcr.constants.DxfFileConstants.H;
import static org.egov.edcr.constants.DxfFileConstants.I;
import static org.egov.edcr.constants.EdcrReportConstants.FLIGHT_LENGTH_DEFINED_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.FLIGHT_NOT_DEFINED_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.FLIGHT_POLYLINE_NOT_DEFINED_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.FLIGHT_WIDTH_DEFINED_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.NO_OF_RISERS;
import static org.egov.edcr.constants.EdcrReportConstants.NO_OF_RISER_DESCRIPTION_GENERAL_STAIR;
import static org.egov.edcr.constants.EdcrReportConstants.RULERISER;
import static org.egov.edcr.constants.EdcrReportConstants.RULETREAD;
import static org.egov.edcr.constants.EdcrReportConstants.RULEWIDTH;
import static org.egov.edcr.constants.EdcrReportConstants.RULE_4_4_4;
import static org.egov.edcr.constants.EdcrReportConstants.TREAD_DESCRIPTION_GEN_STAIR;
import static org.egov.edcr.constants.EdcrReportConstants.WIDTH_DESCRIPTION_GEN_STAIR;
import static org.egov.edcr.constants.EdcrReportConstants.WIDTH_LANDING_DESCRIPTION;
import static org.egov.edcr.constants.EdcrReportConstants.STAIRCASE_ERROR;
import static org.egov.edcr.constants.EdcrReportConstants.TOTAL_STEPS_ZERO_MSG;
import static org.egov.edcr.constants.EdcrReportConstants.ARITHMETIC_ERROR_MSG;
import static org.egov.edcr.constants.EdcrReportConstants.NA;
import static org.egov.edcr.constants.EdcrReportConstants.VERIFY_HEIGHT_STEPS_MSG;
import static org.egov.edcr.constants.EdcrReportConstants.VERIFY_STAIR_DETAILS_MSG;
import static org.egov.edcr.constants.EdcrReportConstants.AT_FLOOR;
import static org.egov.edcr.service.FeatureUtil.addScrutinyDetailtoPlan;
import static org.egov.edcr.service.FeatureUtil.mapReportDetails;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.common.entity.edcr.FeatureEnum;
import org.egov.common.entity.edcr.Flight;
import org.egov.common.entity.edcr.Floor;
import org.egov.common.entity.edcr.MdmsFeatureRule;
import org.egov.common.entity.edcr.Measurement;
import org.egov.common.entity.edcr.NoOfRiserRequirement;
import org.egov.common.entity.edcr.OccupancyTypeHelper;
import org.egov.common.entity.edcr.Plan;
import org.egov.common.entity.edcr.ReportScrutinyDetail;
import org.egov.common.entity.edcr.RequiredTreadRequirement;
import org.egov.common.entity.edcr.RequiredWidthRequirement;
import org.egov.common.entity.edcr.Result;
import org.egov.common.entity.edcr.RiserHeightRequirement;
import org.egov.common.entity.edcr.ScrutinyDetail;
import org.egov.common.entity.edcr.StairLanding;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.service.MDMSCacheManager;
import org.egov.edcr.service.FeatureUtil;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class GeneralStair_Assam extends FeatureProcess {
	private static final Logger LOG = LogManager.getLogger(GeneralStair_Assam.class);

	@Autowired
	MDMSCacheManager cache;
	
	@Override
	public Plan validate(Plan plan) {
		return plan;
	}

	@Override
	public Plan process(Plan plan) {
	    LOG.info("Starting process for Plan");
	    HashMap<String, String> errors = new HashMap<>();
	    for (Block block : plan.getBlocks()) {
	        LOG.info("Processing Block: {}", block.getNumber());
	        processBlock(plan, block, errors);
	    }
	    LOG.info("Completed process for Plan");
	    return plan;
	}

	/**
	 * Processes general stair information for a specific block in the plan.
	 * It validates stair attributes like width, tread width, number of risers,
	 * landing width, riser height, and accumulates errors if any.
	 *
	 * @param plan   The overall building plan.
	 * @param block  The block for which general stair information needs to be validated.
	 * @param errors The map to collect validation errors.
	 */
	private void processBlock(Plan plan, Block block, HashMap<String, String> errors) {
		
		
	    LOG.info("Started processing Block: {}", block.getNumber());
	    
	    

	    int generalStairCount = 0;
	    BigDecimal flrHt = BigDecimal.ZERO;
	    BigDecimal totalLandingWidth = BigDecimal.ZERO;
	    BigDecimal totalRisers = BigDecimal.ZERO;
	    BigDecimal totalSteps = BigDecimal.ZERO;

	    if (block.getBuilding() == null) {
	        LOG.info("Block {} has no building. Skipping...", block.getNumber());
	        return;
	    }

	    LOG.info("Creating scrutiny details for Block {}", block.getNumber());
	    ScrutinyDetail scrutinyDetail2 = createScrutinyDetail(block, GENERAL_STAIR_WIDTH);
	    ScrutinyDetail scrutinyDetail3 = createScrutinyDetail(block, GENERAL_STAIR_TREAD_WIDTH);
	    ScrutinyDetail scrutinyDetailRise = createScrutinyDetail(block, GENERAL_STAIR_NUMBER_OF_RISERS);
	    ScrutinyDetail scrutinyDetailLanding = createScrutinyDetail(block, GENERAL_STAIR_MID_LANDING);
	    ScrutinyDetail scrutinyDetail4 = createScrutinyDetail(block, GENERAL_STAIR_RISER_HEIGHT);

	    OccupancyTypeHelper mostRestrictiveOccupancyType = plan.getVirtualBuilding().getMostRestrictiveFarHelper();
	    LOG.info("Most restrictive occupancy type for Block {}: {}", block.getNumber(), mostRestrictiveOccupancyType);
	    
	    if (requiresTwoStaircases(plan, block, mostRestrictiveOccupancyType) && generalStairCount < 2) {
	        String errKey = MINIMUM_TWO_STAIRCASES_REQUIRED + "_BLOCK_" + block.getNumber();
	        String errMsg = "Block " + block.getNumber() + " " + MINIMUM_TWO_STAIRCASES_REQUIRED_MSG;

	        errors.put(errKey, errMsg);
	        plan.addErrors(errors);
	        LOG.error("Error added: {}", errMsg);
	    }



	    List<Floor> floors = block.getBuilding().getFloors();
	    LOG.info("Total floors in Block {}: {}", block.getNumber(), floors.size());

	    List<String> stairAbsent = new ArrayList<>();

	    for (Floor floor : floors) {
	        LOG.info("Processing Floor {} of Block {}", floor.getNumber(), block.getNumber());

	        if (!floor.getTerrace()) {
	            Map<String, Object> typicalFloorValues = Util.getTypicalFloorValues(block, floor, false);
	            List<org.egov.common.entity.edcr.GeneralStair> generalStairs = floor.getGeneralStairs();
	            LOG.info("Found {} general stairs on Floor {} of Block {}", generalStairs.size(), floor.getNumber(), block.getNumber());

	            generalStairCount += generalStairs.size();

	            if (!generalStairs.isEmpty()) {
	                for (org.egov.common.entity.edcr.GeneralStair generalStair : generalStairs) {
	                    LOG.info("Processing General Stair {} on Floor {} of Block {}", generalStair.getNumber(), floor.getNumber(), block.getNumber());

	                    flrHt = generalStair.getFloorHeight();
	                    LOG.info("Floor height set to {} for Stair {}", flrHt, generalStair.getNumber());

	                    totalRisers = updateTotalRisers(generalStair);
	                    LOG.info("Updated total risers = {}", totalRisers);

	                    totalLandingWidth = updateLandingWidths(generalStair, totalLandingWidth);
	                    LOG.info("Updated total landing width = {}", totalLandingWidth);

	                    totalSteps = totalRisers;
	                    LOG.info("Total steps (risers + landings) = {}", totalSteps);

	                    validateFlight(plan, errors, block, scrutinyDetail2, scrutinyDetail3, scrutinyDetailRise,
	                            mostRestrictiveOccupancyType, floor, typicalFloorValues, generalStair, generalStairCount);
	                    LOG.info("Completed validateFlight for Stair {} on Floor {}", generalStair.getNumber(), floor.getNumber());

	                    List<StairLanding> landings = generalStair.getLandings();
	                    LOG.info("Stair {} has {} landings", generalStair.getNumber(), landings.size());

	                    if (!landings.isEmpty()) {
	                        validateLanding(plan, block, scrutinyDetailLanding, mostRestrictiveOccupancyType, floor,
	                                typicalFloorValues, generalStair, landings, errors);
	                        LOG.info("Completed validateLanding for Stair {} on Floor {}", generalStair.getNumber(), floor.getNumber());
	                    } else if (floor.getNumber() != generalStairCount - 1) {
	                        String errKey = GENERAL_STAIR_LANDING_NOT_DEFINED + block.getNumber() + FLOOR_SPACED + floor.getNumber() + STAIR_PREFIX + generalStair.getNumber();
	                        errors.put(errKey, errKey);
	                        plan.addErrors(errors);
	                        LOG.info("Error added: Landing not defined for Stair {} on Floor {} of Block {}", generalStair.getNumber(), floor.getNumber(), block.getNumber());
	                    }
	                }
	                validateRiserHeight(plan, block, floor, flrHt, totalSteps, scrutinyDetail4);
	            } else {
	                if (floor.getNumber() != generalStairCount) {
	                    String absentMsg = BLOCK_PREFIX + block.getNumber() + FLOOR_SPACED + floor.getNumber();
	                    stairAbsent.add(absentMsg);
	                    LOG.info("No general stair found on Floor {} of Block {}. Added to absent list.", floor.getNumber(), block.getNumber());
	                }
	            }
	        } else {
	            LOG.info("Skipping terrace floor {} in Block {}", floor.getNumber(), block.getNumber());
	        }
	       
		    LOG.info("Completed validateRiserHeight for Block {}", block.getNumber());
	    }

	    

	    handleStairErrors(plan, block, stairAbsent, generalStairCount, errors);
	    LOG.info("Completed handleStairErrors for Block {}", block.getNumber());

	    LOG.info("Finished processing Block {}", block.getNumber());
	}

	/**
	 * Creates a ScrutinyDetail object with appropriate column headings and a unique key for a given block and title.
	 *
	 * @param block The block to which this scrutiny detail belongs.
	 * @param title The title for the scrutiny detail.
	 * @return A configured ScrutinyDetail instance.
	 */
	private ScrutinyDetail createScrutinyDetail(Block block, String title) {
		ScrutinyDetail detail = new ScrutinyDetail();
		detail.addColumnHeading(1, RULE_NO);
		detail.addColumnHeading(2, FLOOR);
		detail.addColumnHeading(3, DESCRIPTION);
		detail.addColumnHeading(4, PERMISSIBLE);
		detail.addColumnHeading(5, PROVIDED);
		detail.addColumnHeading(6, STATUS);
		detail.setKey(BLOCK + block.getNumber() + "_" + title);
		return detail;
	}

	/**
	 * Updates and returns the total number of risers for the given general stair.
	 *
	 * @param stair        The GeneralStair entity to evaluate.
	 * @param totalRisers  The running total of risers.
	 * @return Updated total number of risers including the given stair's flights.
	 */
	private BigDecimal updateTotalRisers(org.egov.common.entity.edcr.GeneralStair stair) {
	    LOG.info("Entering updateTotalRisers for stair: {}", stair != null ? stair.getNumber() : "null");
	     BigDecimal totalRisers = BigDecimal.ZERO;
	    for (Flight flight : stair.getFlights()) {
	        LOG.info("Processing flight: {}, current rises: {}", flight, flight.getNoOfRises());
	        totalRisers = totalRisers.add(flight.getNoOfRises());
	        LOG.info("Updated total risers: {}", totalRisers);
	    }

	    LOG.info("Exiting updateTotalRisers with total risers: {}", totalRisers);
	    return totalRisers;
	}


	/**
	 * Calculates and returns the total landing width for the given general stair.
	 *
	 * @param stair              The GeneralStair entity to evaluate.
	 * @param totalLandingWidth  The running total of landing widths.
	 * @return Updated total landing width.
	 */
	private BigDecimal updateLandingWidths(org.egov.common.entity.edcr.GeneralStair stair, BigDecimal totalLandingWidth) {
	    LOG.info("Entering updateLandingWidths for stair: {}", stair != null ? stair.getNumber() : "null");

	    for (StairLanding landing : stair.getLandings()) {
	        List<BigDecimal> widths = landing.getWidths();
	        LOG.info("Processing landing: {}, widths: {}", landing, widths);

	        if (!widths.isEmpty()) {
	            BigDecimal landingWidth = widths.stream().reduce(BigDecimal::min).get();
	            LOG.info("Minimum landing width found: {}", landingWidth);

	            totalLandingWidth = totalLandingWidth.add(landingWidth);
	            LOG.info("Updated total landing width: {}", totalLandingWidth);
	        } else {
	            LOG.info("Landing has no widths defined, skipping...");
	        }
	    }

	    LOG.info("Exiting updateLandingWidths with total landing width: {}", totalLandingWidth);
	    return totalLandingWidth;
	}

	/**
	 * Validates the riser height for a stair based on floor height and total steps.
	 * Adds the result to the scrutiny report.
	 *
	 * @param plan             The building plan.
	 * @param block            The block being evaluated.
	 * @param flrHt            The floor height.
	 * @param totalSteps       The total number of steps including landings.
	 * @param scrutinyDetail4  ScrutinyDetail object for riser height validation.
	 */
	private void validateRiserHeight(Plan plan, Block block, Floor floor, BigDecimal flrHt, BigDecimal totalSteps, ScrutinyDetail scrutinyDetail4) {
		BigDecimal value = getPermissibleRiserHeight(plan);
		try {
		    if (totalSteps.equals(BigDecimal.ZERO)) {
		        LOG.error("Division by zero avoided for block '{}', floor '{}'. Total steps is zero.",
		                block != null ? block.getNumber() : "N/A",
		                floor != null ? floor.getNumber() : "N/A");

		        FeatureUtil.handleError(plan, STAIRCASE_ERROR,
		                TOTAL_STEPS_ZERO_MSG 
		                + (block != null ? block.getNumber() : NA) 
		                + AT_FLOOR 
		                + (floor != null ? floor.getNumber() : NA) 
		                + VERIFY_STAIR_DETAILS_MSG);
		        return;
		    }

		    if (flrHt != null) {
				BigDecimal riserHeight = flrHt.divide(totalSteps, 2, RoundingMode.HALF_UP);
				String result = (riserHeight.compareTo(value) <= 0) ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal();
				setReportOutputDetailsFloorStairWise(plan, RULERISER, floor.getNumber().toString(), RISER_HEIGHT_DESC, EMPTY_STRING + value, EMPTY_STRING + riserHeight, result, scrutinyDetail4);
			}
		} catch (ArithmeticException e) {
		    LOG.error("Arithmetic error while calculating average step height for block '{}', floor '{}': {}",
		            block != null ? block.getNumber() : "N/A",
		            floor != null ? floor.getNumber() : "N/A",
		            e.getMessage());

		    FeatureUtil.handleError(plan, STAIRCASE_ERROR,
		    		ARITHMETIC_ERROR_MSG
		            + (block != null ? block.getNumber() : NA)
		            + ", Floor "
		            + (floor != null ? floor.getNumber() : NA)
		            + VERIFY_STAIR_DETAILS_MSG);
		}

		
	}


	/**
	 * Retrieves the permissible riser height from the rule cache.
	 *
	 * @param plan The building plan.
	 * @return The permissible riser height value.
	 */
	private BigDecimal getPermissibleRiserHeight(Plan plan) {
	    LOG.info("Fetching permissible riser height from cache for Plan ID");

	    List<Object> rules = cache.getFeatureRules(plan, FeatureEnum.RISER_HEIGHT.getValue(), false);
	    LOG.info("Number of riser height rules fetched: {}", rules.size());

	    Optional<RiserHeightRequirement> matchedRule = rules.stream()
	        .filter(RiserHeightRequirement.class::isInstance)
	        .map(RiserHeightRequirement.class::cast)
	        .findFirst();

	    if (matchedRule.isPresent()) {
	    	LOG.info("Matched riser height rule found with permissible value: {}", matchedRule.get().getPermissible());
	    } else {
	    	LOG.info("No riser height rule matched. Defaulting to 0.");
	    }

	    return matchedRule.map(MdmsFeatureRule::getPermissible).orElse(BigDecimal.ZERO);
	}

	/**
	 * Handles stair-related errors and appends them to the plan.
	 *
	 * @param plan               The building plan.
	 * @param block              The block being evaluated.
	 * @param stairAbsent        List of floors missing stair definitions.
	 * @param generalStairCount  The total number of general stairs found.
	 * @param errors             Map to collect error messages.
	 */
	private void handleStairErrors(Plan plan, Block block, List<String> stairAbsent, int generalStairCount, HashMap<String, String> errors) {
	    LOG.info("Handling stair errors for Block [{}]. GeneralStair count: {}, Absent stairs: {}", 
	              block.getNumber(), generalStairCount, stairAbsent);

	    for (String error : stairAbsent) {
	        String key = GENERAL_STAIR + error;
	        String value = GENERAL_STAIR_NOT_DEFINED + error;
	        errors.put(key, value);
	        LOG.warn("Stair absent error added: key='{}', value='{}'", key, value);
	        plan.addErrors(errors);
	    }

	    if (generalStairCount == 0) {
	        String key = GENERAL_STAIR_MANDATORY + "blk " + block.getNumber();
	        String value = GENERAL_STAIR_MANDATORY + block.getNumber() + GENERAL_STAIR_MANDATORY_SUFFIX;
	        errors.put(key, value);
	        LOG.error("No GeneralStair found in Block [{}]. Error added: key='{}', value='{}'", block.getNumber(), key, value);
	        plan.addErrors(errors);
	    }

	    LOG.info("Completed handling stair errors for Block [{}]. Current errors: {}", block.getNumber(), errors);
	}

	/**
	 * Validates the width of each landing for the general stair and adds the result to scrutiny report.
	 *
	 * @param plan                      The building plan.
	 * @param block                     The block containing the stair.
	 * @param scrutinyDetailLanding     ScrutinyDetail for landing width.
	 * @param mostRestrictiveOccupancyType Most restrictive occupancy type of the block.
	 * @param floor                     The floor under validation.
	 * @param typicalFloorValues        Map of values used for typical floor validation.
	 * @param generalStair              The stair to be validated.
	 * @param landings                  List of landings in the stair.
	 * @param errors                    Map to collect validation errors.
	 */

	private void validateLanding(Plan plan, Block block, ScrutinyDetail scrutinyDetailLanding,
	        OccupancyTypeHelper mostRestrictiveOccupancyType, Floor floor, Map<String, Object> typicalFloorValues,
	        org.egov.common.entity.edcr.GeneralStair generalStair, List<StairLanding> landings,
	        HashMap<String, String> errors) {

	    LOG.info("Validating landings for Block [{}], Floor [{}], Stair [{}], Total landings: {}",
	            block.getNumber(), floor.getNumber(), generalStair.getNumber(), landings.size());

	    for (StairLanding landing : landings) {
	        LOG.info("Processing Landing [{}] for Stair [{}]. Widths: {}", landing.getNumber(), generalStair.getNumber(), landing.getWidths());

	        List<BigDecimal> widths = landing.getWidths();
	        if (!widths.isEmpty()) {
	            BigDecimal landingWidth = widths.stream().reduce(BigDecimal::min).get();
	            LOG.info("Minimum width found for Landing [{}]: {}", landing.getNumber(), landingWidth);

	            BigDecimal minWidth = BigDecimal.ZERO;
	            boolean valid = false;

	            if (!(Boolean) typicalFloorValues.get(IS_TYPICAL_REP_FLOOR)) {
	                minWidth = Util.roundOffTwoDecimal(landingWidth);
	                BigDecimal minimumWidth = getRequiredWidth(plan, block, mostRestrictiveOccupancyType);

	                LOG.info("Comparing Landing [{}] width: {} (required min: {})", landing.getNumber(), minWidth, minimumWidth);

	                if (minWidth.compareTo(minimumWidth) >= 0) {
	                    valid = true;
	                    LOG.info("Landing [{}] for Stair [{}] PASSED width validation.", landing.getNumber(), generalStair.getNumber());
	                } else {
	                    LOG.warn("Landing [{}] for Stair [{}] FAILED width validation.", landing.getNumber(), generalStair.getNumber());
	                }

	                String value = typicalFloorValues.get(TYPICAL_FLOOR) != null
	                        ? (String) typicalFloorValues.get(TYPICAL_FLOOR)
	                        : FLOOR_SPACED + floor.getNumber();

	                setReportOutputDetailsFloorStairWise(plan, RULE_4_4_4, value,
	                        String.format(WIDTH_LANDING_DESCRIPTION, generalStair.getNumber(), landing.getNumber()),
	                        minimumWidth.toString(), String.valueOf(minWidth),
	                        valid ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal(),
	                        scrutinyDetailLanding);

	                LOG.info("Report output details set for Landing [{}] in Stair [{}].", landing.getNumber(), generalStair.getNumber());
	            }
	        } else {
	            String key = GENERAL_STAIR_LANDING_WIDTH_NOT_DEFINED + block.getNumber() + FLOOR_SPACED
	                    + floor.getNumber() + STAIR_PREFIX + generalStair.getNumber();
	            String value = GENERAL_STAIR_LANDING_WIDTH_NOT_DEFINED + block.getNumber() + FLOOR_SPACED
	                    + floor.getNumber() + STAIR_PREFIX + generalStair.getNumber();

	            errors.put(key, value);
	            plan.addErrors(errors);
	            LOG.error("Landing [{}] in Stair [{}] has NO widths defined. Error added: key='{}', value='{}'",
	                    landing.getNumber(), generalStair.getNumber(), key, value);
	        }
	    }

	    LOG.info("Completed landing validation for Block [{}], Floor [{}], Stair [{}]. Errors so far: {}",
	            block.getNumber(), floor.getNumber(), generalStair.getNumber(), errors);
	}

	/**
	 * Validates the flights associated with a general stair in a specific floor and block of the plan.
	 * <p>
	 * It ensures each flight has proper dimensions, including width, tread, and number of rises,
	 * as per the rules defined for the most restrictive occupancy type.
	 * <p>
	 * If flights are missing, an appropriate error is added to the plan.
	 *
	 * @param plan                        the building plan being validated
	 * @param errors                      map containing validation errors
	 * @param block                       the block in which the flight is located
	 * @param scrutinyDetail2            scrutiny details for width validation
	 * @param scrutinyDetail3            scrutiny details for tread validation
	 * @param scrutinyDetailRise         scrutiny details for rise validation
	 * @param mostRestrictiveOccupancyType the most restrictive occupancy type for rule evaluation
	 * @param floor                       the floor where the general stair is present
	 * @param typicalFloorValues         map containing typical floor information
	 * @param generalStair               the general stair object containing flights
	 * @param generalStairCount          the index or count of general stairs being validated
	 */
	
	private void validateFlight(Plan plan, HashMap<String, String> errors, Block block, ScrutinyDetail scrutinyDetail2,
	        ScrutinyDetail scrutinyDetail3, ScrutinyDetail scrutinyDetailRise, OccupancyTypeHelper mostRestrictiveOccupancyType,
	        Floor floor, Map<String, Object> typicalFloorValues, org.egov.common.entity.edcr.GeneralStair generalStair,
	        int generalStairCount) {

	    LOG.info("Validating flights for Block [{}], Floor [{}], GeneralStair count [{}]", 
	            block.getNumber(), floor.getNumber(), generalStairCount);

	    if (!generalStair.getFlights().isEmpty()) {
	        LOG.info("Found [{}] flights in GeneralStair [{}] for Floor [{}]", 
	                generalStair.getFlights().size(), generalStair.getNumber(), floor.getNumber());

	        for (Flight flight : generalStair.getFlights()) {
	            LOG.info("Validating single Flight [{}] in GeneralStair [{}], Floor [{}], Block [{}]", 
	                    flight.getNumber(), generalStair.getNumber(), floor.getNumber(), block.getNumber());

	            validateSingleFlight(plan, errors, block, scrutinyDetail2, scrutinyDetail3, scrutinyDetailRise,
	                    mostRestrictiveOccupancyType, floor, typicalFloorValues, generalStair, flight);
	        }

	        LOG.info("Completed validation of [{}] flights for GeneralStair [{}], Floor [{}], Block [{}]", 
	                generalStair.getFlights().size(), generalStair.getNumber(), floor.getNumber(), block.getNumber());
	    } else {
	        LOG.info("No flights found in GeneralStair [{}] for Floor [{}], Block [{}]. Handling missing flights.", 
	                generalStair.getNumber(), floor.getNumber(), block.getNumber());

	        handleMissingFlights(plan, errors, block, floor);
	    }
	    
	    
	}

	/**
	 * Determines whether a block requires two staircases based on building height 
	 * and occupancy type conditions.
	 * <p>
	 * The conditions for requiring two staircases are:
	 * <ul>
	 *   <li><b>Condition 1:</b> If the building height is greater than 15.8 meters.</li>
	 *   <li><b>Condition 2:</b> If the most restrictive occupancy type belongs to 
	 *       certain categories (Educational, Assembly, Medical/Institutional, 
	 *       Industrial, Storage, Hazardous) AND the plot area is greater than 500 sqm.</li>
	 * </ul>
	 * </p>
	 *
	 * @param pl The {@link Plan} object containing plot details.
	 * @param block The {@link Block} under consideration.
	 * @param mostRestrictiveOccupancyType The most restrictive {@link OccupancyTypeHelper}
	 *                                     for the block, used to determine occupancy category.
	 * @return {@code true} if the block requires two staircases, 
	 *         {@code false} otherwise.
	 */
	
	private boolean requiresTwoStaircases(Plan pl, Block block,
	        OccupancyTypeHelper mostRestrictiveOccupancyType) {

	    BigDecimal buildingHeight = block.getBuilding().getBuildingHeight();
	    BigDecimal plotArea = pl.getPlot() != null ? pl.getPlot().getArea() : null;

	    LOG.info("Building height for Block {}: {}", block.getNumber(), buildingHeight);
	    LOG.info("Plot area for Block {}: {}", block.getNumber(), plotArea);

	    // ----- Condition 1: Height > 15.8m -----
	    boolean isHeightConditionSatisfied =
	            buildingHeight != null
	            && buildingHeight.compareTo(BigDecimal.valueOf(15.8)) > 0;

	    // ----- Condition 2: Occupancy + Area -----
	    boolean isOccupancyConditionSatisfied = false;

	    if (mostRestrictiveOccupancyType != null
	            && mostRestrictiveOccupancyType.getType() != null
	            && mostRestrictiveOccupancyType.getType().getCode() != null
	            && plotArea != null
	            && plotArea.compareTo(BigDecimal.valueOf(500)) > 0) {

	        String occCode = mostRestrictiveOccupancyType.getType().getCode();

	        if (B.equalsIgnoreCase(occCode)   // Educational
	                || C.equalsIgnoreCase(occCode)   // Medical / Institutional
	                || D.equalsIgnoreCase(occCode)   // Assembly
	                || G.equalsIgnoreCase(occCode)   // Industrial
	                || H.equalsIgnoreCase(occCode)   // Storage
	                || I.equalsIgnoreCase(occCode))  // Hazardous
	        {
	            isOccupancyConditionSatisfied = true;
	        }
	    }

	    // ----- FINAL DECISION (AND condition) -----
	    if (isHeightConditionSatisfied && isOccupancyConditionSatisfied) {
	        LOG.info(
	            "Block {} requires two staircases (Height > 15.8m AND Occupancy condition satisfied)",
	            block.getNumber()
	        );
	        return true;
	    }

	    return false;
	}


	
	/**
	 * Validates a single flight object by checking its width, tread, and number of rises.
	 *
	 * @param plan                        the building plan
	 * @param errors                      map of error messages
	 * @param block                       the block in the plan
	 * @param scrutinyDetail2            scrutiny details for flight width
	 * @param scrutinyDetail3            scrutiny details for flight tread
	 * @param scrutinyDetailRise         scrutiny details for flight rises
	 * @param mostRestrictiveOccupancyType the most restrictive occupancy type
	 * @param floor                       the floor containing the flight
	 * @param typicalFloorValues         map containing values related to typical floor repetition
	 * @param generalStair               the stair object to which this flight belongs
	 * @param flight                      the flight object to be validated
	 */

	private void validateSingleFlight(Plan plan, HashMap<String, String> errors, Block block,
	        ScrutinyDetail scrutinyDetail2, ScrutinyDetail scrutinyDetail3, ScrutinyDetail scrutinyDetailRise,
	        OccupancyTypeHelper mostRestrictiveOccupancyType, Floor floor, Map<String, Object> typicalFloorValues,
	        org.egov.common.entity.edcr.GeneralStair generalStair, Flight flight) {

	    String flightLayerName = String.format(DxfFileConstants.LAYER_STAIR_FLIGHT, block.getNumber(),
	            floor.getNumber(), generalStair.getNumber(), flight.getNumber());

	    LOG.info("Starting validation for Flight [{}] in GeneralStair [{}], Floor [{}], Block [{}], Layer [{}]",
	            flight.getNumber(), generalStair.getNumber(), floor.getNumber(), block.getNumber(), flightLayerName);

	    if (flight == null) {
	        LOG.error("Flight object is NULL in GeneralStair [{}], Floor [{}], Block [{}]. Skipping validation.",
	                generalStair.getNumber(), floor.getNumber(), block.getNumber());
	        return;
	    }

	    List<Measurement> flightPolyLines = flight.getFlights();
	    List<BigDecimal> flightLengths = flight.getLengthOfFlights();
	    List<BigDecimal> flightWidths = flight.getWidthOfFlights();
	    BigDecimal noOfRises = flight.getNoOfRises();
	    Boolean flightPolyLineClosed = flight.getFlightClosed();

	    LOG.info("Flight [{}] details: PolyLines [{}], Lengths [{}], Widths [{}], No. of Rises [{}], Closed [{}]",
	            flight.getNumber(), (flightPolyLines != null ? flightPolyLines.size() : 0),
	            (flightLengths != null ? flightLengths.size() : 0),
	            (flightWidths != null ? flightWidths.size() : 0),
	            noOfRises, flightPolyLineClosed);

	    if (flightPolyLines != null && !flightPolyLines.isEmpty()) {
	        LOG.info("Flight [{}] has [{}] polylines. Proceeding with validation checks.",
	                flight.getNumber(), flightPolyLines.size());

	        if (Boolean.TRUE.equals(flightPolyLineClosed)) {
	            LOG.info("Flight [{}] polyline is CLOSED. Running width, tread, and riser validations.",
	                    flight.getNumber());

	            validateFlightWidth(plan, errors, scrutinyDetail2, floor, block, typicalFloorValues,
	                    generalStair, flight, flightWidths, mostRestrictiveOccupancyType, flightLayerName);

	            validateFlightTread(plan, errors, block, scrutinyDetail3, floor, typicalFloorValues,
	                    generalStair, flight, flightLengths, mostRestrictiveOccupancyType, flightLayerName);

	            validateFlightRises(plan, errors, block, scrutinyDetailRise, floor, typicalFloorValues,
	                    generalStair, flight, noOfRises, flightLayerName);

	            LOG.info("Completed validation checks for Flight [{}], GeneralStair [{}], Floor [{}], Block [{}]",
	                    flight.getNumber(), generalStair.getNumber(), floor.getNumber(), block.getNumber());
	        } else {
	            LOG.warn("Flight [{}] polyline is NOT CLOSED for GeneralStair [{}], Floor [{}], Block [{}]. Skipping validations.",
	                    flight.getNumber(), generalStair.getNumber(), floor.getNumber(), block.getNumber());
	        }
	    } else {
	        LOG.error("No polylines defined for Flight [{}] in Layer [{}]. Adding error entry.",
	                flight.getNumber(), flightLayerName);

	        errors.put(FLIGHT_POLYLINE + flightLayerName,
	                FLIGHT_POLYLINE_NOT_DEFINED_DESCRIPTION + flightLayerName);
	        plan.addErrors(errors);
	    }

	    LOG.info("Finished validation for Flight [{}] in GeneralStair [{}], Floor [{}], Block [{}]",
	            flight.getNumber(), generalStair.getNumber(), floor.getNumber(), block.getNumber());
	}

	
	/**
	 * Validates the width of the flight against required rules.
	 *
	 * @param plan             the plan object
	 * @param errors           error map
	 * @param scrutinyDetail2  scrutiny details for reporting
	 * @param floor            current floor
	 * @param block            current block
	 * @param typicalFloorValues map with typical floor repetition details
	 * @param generalStair     the general stair object
	 * @param flight           the flight being validated
	 * @param flightWidths     list of measured flight widths
	 * @param mostRestrictiveOccupancyType the most restrictive occupancy type
	 * @param flightLayerName  layer name of the flight
	 */

	private void validateFlightWidth(Plan plan, HashMap<String, String> errors, ScrutinyDetail scrutinyDetail2,
	        Floor floor, Block block, Map<String, Object> typicalFloorValues, 
	        org.egov.common.entity.edcr.GeneralStair generalStair,
	        Flight flight, List<BigDecimal> flightWidths, 
	        OccupancyTypeHelper mostRestrictiveOccupancyType, String flightLayerName) {

	    if (flightWidths != null && !flightWidths.isEmpty()) {
	        LOG.info("Validating flight width for block {}, floor {}, stair {}, flightLayerName={}",
	                block.getNumber(), floor.getNumber(), generalStair.getName(), flightLayerName);

	        validateWidth(plan, scrutinyDetail2, floor, block, typicalFloorValues,
	                generalStair, flight, flightWidths, BigDecimal.ZERO, mostRestrictiveOccupancyType);

	        LOG.info("Flight width validation completed successfully for flightLayerName={}", flightLayerName);
	    } else {
	    	LOG.info("No flight width found for flightLayerName={} in block {}, floor {}", 
	                flightLayerName, block.getNumber(), floor.getNumber());

	        errors.put(FLIGHT_POLYLINE_WIDTH + flightLayerName,
	                FLIGHT_WIDTH_DEFINED_DESCRIPTION + flightLayerName);
	        plan.addErrors(errors);

	        LOG.info("Error added: {} -> {}", FLIGHT_POLYLINE_WIDTH + flightLayerName,
	                FLIGHT_WIDTH_DEFINED_DESCRIPTION + flightLayerName);
	    }
	}

	
	/**
	 * Validates the tread (length of step) of the flight.
	 *
	 * @param plan             the plan object
	 * @param errors           map containing error messages
	 * @param block            the block in which validation is occurring
	 * @param scrutinyDetail3  scrutiny details for treads
	 * @param floor            current floor
	 * @param typicalFloorValues map with typical floor repetition info
	 * @param generalStair     stair object that includes the flight
	 * @param flight           flight being validated
	 * @param flightLengths    list of tread lengths
	 * @param mostRestrictiveOccupancyType the occupancy type used for rule checks
	 * @param flightLayerName  layer name of the flight
	 */

	private void validateFlightTread(Plan plan, HashMap<String, String> errors, Block block, ScrutinyDetail scrutinyDetail3,
	        Floor floor, Map<String, Object> typicalFloorValues, org.egov.common.entity.edcr.GeneralStair generalStair,
	        Flight flight, List<BigDecimal> flightLengths, OccupancyTypeHelper mostRestrictiveOccupancyType, String flightLayerName) {

	    if (flightLengths != null && !flightLengths.isEmpty()) {
	        try {
	            validateTread(plan, errors, block, scrutinyDetail3, floor, typicalFloorValues,
	                    generalStair, flight, flightLengths, BigDecimal.ZERO, mostRestrictiveOccupancyType);
	        } catch (ArithmeticException e) {
	            LOG.info("Denominator is zero");
	        }
	    } else {
	        errors.put(FLIGHT_POLYLINE_LENGTH + flightLayerName,
	                FLIGHT_LENGTH_DEFINED_DESCRIPTION + flightLayerName);
	        plan.addErrors(errors);
	    }
	}
	
	/**
	 * Validates the number of rises in the flight.
	 *
	 * @param plan             the plan object
	 * @param errors           error map
	 * @param block            block in which validation occurs
	 * @param scrutinyDetailRise scrutiny detail for number of rises
	 * @param floor            current floor
	 * @param typicalFloorValues map with floor repetition info
	 * @param generalStair     stair object containing the flight
	 * @param flight           the flight to be validated
	 * @param noOfRises        number of rises in the flight
	 * @param flightLayerName  name of the flight layer
	 */

	private void validateFlightRises(Plan plan, HashMap<String, String> errors, Block block, ScrutinyDetail scrutinyDetailRise,
	        Floor floor, Map<String, Object> typicalFloorValues, org.egov.common.entity.edcr.GeneralStair generalStair,
	        Flight flight, BigDecimal noOfRises, String flightLayerName) {

	    if (noOfRises != null && noOfRises.compareTo(BigDecimal.ZERO) > 0) {
	        try {
	            validateNoOfRises(plan, errors, block, scrutinyDetailRise, floor, typicalFloorValues,
	                    generalStair, flight, noOfRises);
	        } catch (ArithmeticException e) {
	            LOG.info("Denominator is zero");
	        }
	    } else {
	        errors.put(NO_OF_RISE + flightLayerName,
	                edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
	                        new String[]{NO_OF_RISERS + flightLayerName},
	                        LocaleContextHolder.getLocale()));
	        plan.addErrors(errors);
	    }
	}
	
	/**
	 * Adds error to the plan if no flight is defined for a given stair in a floor and block.
	 *
	 * @param plan   the plan object
	 * @param errors map of error messages
	 * @param block  current block
	 * @param floor  current floor
	 */

	private void handleMissingFlights(Plan plan, HashMap<String, String> errors, Block block, Floor floor) {
	    String error = String.format(FLIGHT_NOT_DEFINED_DESCRIPTION, block.getNumber(), floor.getNumber());
	    errors.put(error, error);
	    plan.addErrors(errors);
	}


	/**
	 * Compares measured flight width with required width and adds result to the scrutiny report.
	 *
	 * @param plan                        the plan object
	 * @param scrutinyDetail2            scrutiny detail for width
	 * @param floor                       current floor
	 * @param block                       current block
	 * @param typicalFloorValues         typical floor repetition info
	 * @param generalStair               the stair object
	 * @param flight                     the flight object
	 * @param flightWidths               measured widths of the flight
	 * @param minFlightWidth             minimum measured width (may be recalculated)
	 * @param mostRestrictiveOccupancyType the occupancy type for determining rule applicability
	 * @return the minimum flight width used in validation
	 */

	private BigDecimal validateWidth(Plan plan, ScrutinyDetail scrutinyDetail2, Floor floor, Block block,
	        Map<String, Object> typicalFloorValues, org.egov.common.entity.edcr.GeneralStair generalStair,
	        Flight flight, List<BigDecimal> flightWidths, BigDecimal minFlightWidth,
	        OccupancyTypeHelper mostRestrictiveOccupancyType) {

		LOG.info("Validating flight width for Block [{}], Floor [{}], Stair [{}], Flight [{}]. Width candidates: {}",
	            block.getNumber(), floor.getNumber(), generalStair.getNumber(), flight.getNumber(), flightWidths);

	    BigDecimal flightPolyLine = flightWidths.stream().reduce(BigDecimal::min).get();
	    LOG.info("Computed minimum polyline width for flight [{}]: {}", flight.getNumber(), flightPolyLine);

	    boolean valid = false;

	    if (!(Boolean) typicalFloorValues.get(IS_TYPICAL_REP_FLOOR)) {
	        minFlightWidth = Util.roundOffTwoDecimal(flightPolyLine);
	        BigDecimal minimumWidth = getRequiredWidth(plan, block, mostRestrictiveOccupancyType);

	        LOG.info("Rounded flight width: {}, Required minimum width: {}", minFlightWidth, minimumWidth);

	        if (minFlightWidth.compareTo(minimumWidth) >= 0) {
	            valid = true;
	            LOG.info("Flight width [{}] is valid (>= required minimum).", minFlightWidth);
	        } else {
	        	LOG.info("Flight width [{}] is NOT valid (< required minimum {}).", minFlightWidth, minimumWidth);
	        }

	        String value = typicalFloorValues.get(TYPICAL_FLOOR) != null
	                ? (String) typicalFloorValues.get(TYPICAL_FLOOR)
	                : FLOOR_SPACED + floor.getNumber();

	        setReportOutputDetailsFloorStairWise(plan, RULEWIDTH, value,
	                String.format(WIDTH_DESCRIPTION_GEN_STAIR, generalStair.getNumber(), flight.getNumber()),
	                minimumWidth.toString(), String.valueOf(minFlightWidth),
	                valid ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal(),
	                scrutinyDetail2);

	        LOG.info("Report details set for Flight [{}] in Stair [{}] with result: {}", 
	                flight.getNumber(), generalStair.getNumber(),
	                valid ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal());
	    } else {
	    	LOG.info("Skipping width validation for typical representative floor (Floor [{}])", floor.getNumber());
	    }

	    LOG.info("Validation completed for Flight [{}], returning minFlightWidth={}", 
	            flight.getNumber(), minFlightWidth);

	    return minFlightWidth;
	}



	/**
	 * Fetches the required minimum flight width from cached feature rules based on plan and occupancy.
	 *
	 * @param pl                         the plan object
	 * @param block                      current block
	 * @param mostRestrictiveOccupancyType the most restrictive occupancy type
	 * @return permissible flight width as per rules or BigDecimal.ZERO if not found
	 */

	private BigDecimal getRequiredWidth(Plan pl, Block block, OccupancyTypeHelper mostRestrictiveOccupancyType) {

	    BigDecimal value = BigDecimal.ZERO;
	    LOG.info("Fetching required width for Block: {}, OccupancyType: {}", block.getNumber(), mostRestrictiveOccupancyType);

	    // Fetch all rules for the given plan from the cache.
	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.REQUIRED_WIDTH.getValue(), false);
	    LOG.info("Fetched {} rules for REQUIRED_WIDTH feature", rules != null ? rules.size() : 0);

	    Optional<RequiredWidthRequirement> matchedRule = rules.stream()
	        .filter(RequiredWidthRequirement.class::isInstance)
	        .map(RequiredWidthRequirement.class::cast)
	        .findFirst();

	    if (matchedRule.isPresent()) {
	        RequiredWidthRequirement rule = matchedRule.get();
	        value = rule.getPermissible();
	        LOG.info("Matched rule found. Permissible required width = {}", value);
	    } else {
	        value = BigDecimal.ZERO;
	        LOG.info("No matching REQUIRED_WIDTH rule found. Defaulting value to {}", value);
	    }

	    LOG.info("Returning required width: {}", value);
	    return value;
	}


	/**
	 * Returns the required landing width based on occupancy type.
	 * Residential (code "A") requires 0.76m; others require 1.5m.
	 *
	 * @param block                      the block object (not used here but kept for consistency)
	 * @param mostRestrictiveOccupancyType occupancy type to determine landing width
	 * @return required landing width in meters
	 */

	private BigDecimal getRequiredLandingWidth(Block block, OccupancyTypeHelper mostRestrictiveOccupancyType) {

		if (mostRestrictiveOccupancyType != null && mostRestrictiveOccupancyType.getType() != null
				&& DxfFileConstants.A.equalsIgnoreCase(mostRestrictiveOccupancyType.getType().getCode())) {
			return BigDecimal.valueOf(0.76);
		} else {
			return BigDecimal.valueOf(1.5);
		}
	}

	/**
	 * Validates the tread of a stair flight by calculating the minimum tread based on 
	 * the total length of all flight segments and the number of risers. If the number 
	 * of risers is greater than the number of flights, the tread is calculated. 
	 * Additionally, it validates the calculated tread against the required tread 
	 * value based on occupancy type.
	 *
	 * @param plan                  The building plan object containing all plan-level details.
	 * @param errors                Map to collect validation errors.
	 * @param block                 The block to which the stair flight belongs.
	 * @param scrutinyDetail3       Object to capture scrutiny details for reporting.
	 * @param floor                 The floor on which the flight is located.
	 * @param typicalFloorValues    Map containing typical floor configuration details.
	 * @param generalStair          Staircase entity containing stair number and other info.
	 * @param flight                The flight being validated.
	 * @param flightLengths         List of lengths of all segments of the flight.
	 * @param minTread              The minimum tread value, to be calculated or reused.
	 * @param mostRestrictiveOccupancyType Occupancy type used to determine required tread rule.
	 *
	 * @return The calculated minimum tread value for the flight.
	 */
	private BigDecimal validateTread(Plan plan, HashMap<String, String> errors, Block block,
	        ScrutinyDetail scrutinyDetail3, Floor floor, Map<String, Object> typicalFloorValues,
	        org.egov.common.entity.edcr.GeneralStair generalStair, Flight flight, List<BigDecimal> flightLengths,
	        BigDecimal minTread, OccupancyTypeHelper mostRestrictiveOccupancyType) {

	    LOG.info("Validating tread for Block [{}], Floor [{}], Stair [{}], Flight [{}]", 
	            block.getNumber(), floor.getNumber(), generalStair.getNumber(), flight.getNumber());

	    BigDecimal totalLength = calculateTotalFlightLength(flightLengths);
	    LOG.info("Total flight length calculated: {}", totalLength);

	    BigDecimal requiredTread = getRequiredTread(plan, mostRestrictiveOccupancyType);
	    LOG.info("Required tread for occupancy type [{}]: {}", mostRestrictiveOccupancyType, requiredTread);

	    if (flight.getNoOfRises() != null) {
	    	LOG.info("Number of rises for flight [{}]: {}", flight.getNumber(), flight.getNoOfRises());

	        BigDecimal noOfFlights = BigDecimal.valueOf(flightLengths.size());
	        LOG.info("Number of flights in list: {}", noOfFlights);

	        if (flight.getNoOfRises().compareTo(noOfFlights) > 0) {
	        	LOG.info("Rises [{}] greater than flights [{}] - calculating denominator", 
	                    flight.getNoOfRises(), noOfFlights);

	            BigDecimal denominator = flight.getNoOfRises().subtract(noOfFlights);
	            LOG.info("Denominator calculated: {}", denominator);

	            minTread = totalLength.divide(denominator, DcrConstants.DECIMALDIGITS_MEASUREMENTS,
	                    DcrConstants.ROUNDMODE_MEASUREMENTS);
	            LOG.info("Minimum tread calculated: {}", minTread);

	            if (!(Boolean) typicalFloorValues.get(IS_TYPICAL_REP_FLOOR)) {
	            	LOG.info("Non-typical representative floor - validating tread against required");
	                validateTreadAgainstRequired(plan, scrutinyDetail3, floor, typicalFloorValues,
	                        generalStair, flight, requiredTread, minTread);
	            } else {
	            	LOG.info("Typical representative floor - skipping tread validation against required");
	            }

	        } else if (flight.getNoOfRises().compareTo(BigDecimal.ZERO) > 0) {
	        	LOG.info("Number of rises [{}] is valid but less than/equal to flights [{}] - adding error", 
	                    flight.getNoOfRises(), noOfFlights);
	            addNoOfRisesError(plan, errors, block, floor, generalStair, flight);
	        } else {
	        	LOG.info("Number of rises is zero or negative - no validation performed");
	        }
	    } else {
	    	LOG.info("Flight [{}] has null number of rises - skipping tread validation", flight.getNumber());
	    }

	    LOG.info("Returning minTread: {}", minTread);
	    return minTread;
	}

	/**
	 * Calculates the total length of a stair flight by summing up all its segment lengths
	 * and rounding the result to two decimal places.
	 *
	 * @param flightLengths List of individual flight segment lengths.
	 * @return The total flight length, rounded to two decimal places.
	 */
	private BigDecimal calculateTotalFlightLength(List<BigDecimal> flightLengths) {
	    BigDecimal totalLength = flightLengths.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
	    return Util.roundOffTwoDecimal(totalLength);
	}
	
	/**
	 * Compares the calculated minimum tread value with the required tread value and
	 * records the result in the scrutiny report. Only runs validation for non-typical floors.
	 *
	 * @param plan               The building plan being processed.
	 * @param scrutinyDetail3    Scrutiny detail for recording results.
	 * @param floor              The floor on which the tread is being validated.
	 * @param typicalFloorValues Map containing information about typical floor configuration.
	 * @param generalStair       Stair entity associated with the tread.
	 * @param flight             The flight being validated.
	 * @param requiredTread      The tread value as per applicable rules.
	 * @param minTread           The calculated minimum tread value.
	 */
	private void validateTreadAgainstRequired(Plan plan, ScrutinyDetail scrutinyDetail3, Floor floor,
	        Map<String, Object> typicalFloorValues, org.egov.common.entity.edcr.GeneralStair generalStair,
	        Flight flight, BigDecimal requiredTread, BigDecimal minTread) {

	    LOG.info("Validating tread for GeneralStair: {}, Flight: {}, Floor: {}",
	            generalStair != null ? generalStair.getNumber() : "N/A",
	            flight != null ? flight.getNumber() : "N/A",
	            floor != null ? floor.getNumber() : "N/A");

	    boolean isValid = Util.roundOffTwoDecimal(minTread)
	            .compareTo(Util.roundOffTwoDecimal(requiredTread)) >= 0;

	    LOG.info("Required tread: {}, Minimum tread provided: {}, Validation result: {}",
	            requiredTread, minTread, isValid ? "Accepted" : "Not Accepted");

	    String value = typicalFloorValues.get(TYPICAL_FLOOR) != null
	            ? (String) typicalFloorValues.get(TYPICAL_FLOOR)
	            : FLOOR_SPACED + floor.getNumber();

	    LOG.info("Derived value for floor validation: {}", value);

	    String description = String.format(TREAD_DESCRIPTION_GEN_STAIR,
	            generalStair.getNumber(), flight.getNumber());

	    LOG.info("Description generated: {}", description);

	    setReportOutputDetailsFloorStairWise(plan, RULETREAD, value, description,
	            requiredTread.toString(), minTread.toString(),
	            isValid ? Result.Accepted.getResultVal() : Result.Not_Accepted.getResultVal(), scrutinyDetail3);

	    LOG.info("Tread validation completed for GeneralStair: {}, Flight: {} - Status: {}",
	            generalStair.getNumber(), flight.getNumber(),
	            isValid ? "Accepted" : "Not Accepted");
	}

	
	/**
	 * Adds an error to the plan if the number of risers in the flight is less than or 
	 * equal to the number of defined flight segments.
	 *
	 * @param plan         The plan where the error is to be recorded.
	 * @param errors       Map to collect errors.
	 * @param block        The block in which the error occurred.
	 * @param floor        The floor containing the problematic flight.
	 * @param generalStair The stair associated with the flight.
	 * @param flight       The flight which has incorrect number of risers.
	 */
	private void addNoOfRisesError(Plan plan, HashMap<String, String> errors, Block block, Floor floor,
	        org.egov.common.entity.edcr.GeneralStair generalStair, Flight flight) {

	    String flightLayerName = String.format(DxfFileConstants.LAYER_STAIR_FLIGHT, block.getNumber(),
	            floor.getNumber(), generalStair.getNumber(), flight.getNumber());

	    errors.put(NO_OF_RISES_COUNT + flightLayerName,
	            NO_OF_RISES_COUNT_ERROR + flightLayerName);
	    plan.addErrors(errors);
	}


	/**
	 * Retrieves the required tread value from the feature rules cache based on the 
	 * most restrictive occupancy type.
	 *
	 * @param pl                           The plan from which rules are derived.
	 * @param mostRestrictiveOccupancyType The most restrictive occupancy type used to find applicable rules.
	 * @return The permissible tread value as per rule, or zero if no rule matches.
	 */
	private BigDecimal getRequiredTread(Plan pl, OccupancyTypeHelper mostRestrictiveOccupancyType) {

	    BigDecimal value = BigDecimal.ZERO;

	    LOG.info("Fetching rules for REQUIRED_TREAD for plan [{}] and occupancy [{}]",
	             mostRestrictiveOccupancyType != null ? mostRestrictiveOccupancyType.toString() : "N/A");

	    List<Object> rules = cache.getFeatureRules(pl, FeatureEnum.REQUIRED_TREAD.getValue(), false);
	    LOG.info("Total rules fetched for REQUIRED_TREAD: {}", rules != null ? rules.size() : 0);

	    Optional<RequiredTreadRequirement> matchedRule = rules.stream()
	        .filter(RequiredTreadRequirement.class::isInstance)
	        .map(RequiredTreadRequirement.class::cast)
	        .findFirst();

	    if (matchedRule.isPresent()) {
	        RequiredTreadRequirement rule = matchedRule.get();
	        value = rule.getPermissible();
	        LOG.info("Matched REQUIRED_TREAD rule found with permissible value: {}", value);
	    } else {
	        value = BigDecimal.ZERO;
	        LOG.info("No REQUIRED_TREAD rule matched. Defaulting value to ZERO.");
	    }

	    LOG.info("Returning REQUIRED_TREAD value: {}", value);
	    return value;
	}


	/**
	 * Validates the number of risers in a flight against the permissible value 
	 * from the rules. Adds the result to the scrutiny report for non-typical floors.
	 *
	 * @param plan               The plan being validated.
	 * @param errors             Map to collect any validation errors.
	 * @param block              The block containing the flight.
	 * @param scrutinyDetail3    Object for collecting scrutiny output.
	 * @param floor              The floor containing the flight.
	 * @param typicalFloorValues Map with details of typical floors.
	 * @param generalStair       Stair entity containing stair number info.
	 * @param flight             The flight for which riser count is validated.
	 * @param noOfRises          The actual number of risers in the flight.
	 */
	private void validateNoOfRises(Plan plan, HashMap<String, String> errors, Block block,
	        ScrutinyDetail scrutinyDetail3, Floor floor, Map<String, Object> typicalFloorValues,
	        org.egov.common.entity.edcr.GeneralStair generalStair, Flight flight, BigDecimal noOfRises) {

	    boolean valid = false;

	    LOG.info("Validating number of rises for Block [{}], Floor [{}], GeneralStair [{}], Flight [{}], Provided rises [{}]", 
	             block.getNumber(), floor.getNumber(), generalStair.getNumber(), flight.getNumber(), noOfRises);

	    if (!(Boolean) typicalFloorValues.get(IS_TYPICAL_REP_FLOOR)) {
	        
	        BigDecimal noOfRisersValue = BigDecimal.ZERO;

	        LOG.info("Fetching rules for NO_OF_RISER for Plan [{}]"
	                );

	        List<Object> rules = cache.getFeatureRules(plan, FeatureEnum.NO_OF_RISER.getValue(), false);
	        LOG.info("Total rules fetched for NO_OF_RISER: {}", rules != null ? rules.size() : 0);

	        Optional<NoOfRiserRequirement> matchedRule = rules.stream()
	            .filter(NoOfRiserRequirement.class::isInstance)
	            .map(NoOfRiserRequirement.class::cast)
	            .findFirst();

	        if (matchedRule.isPresent()) {
	            NoOfRiserRequirement rule = matchedRule.get();
	            noOfRisersValue = rule.getPermissible();
	            LOG.info("Matched NO_OF_RISER rule found with permissible value: {}", noOfRisersValue);
	        } else {
	            noOfRisersValue = BigDecimal.ZERO;
	            LOG.warn("No NO_OF_RISER rule matched. Defaulting permissible value to ZERO.");
	        }

	        if (Util.roundOffTwoDecimal(noOfRises).compareTo(Util.roundOffTwoDecimal(noOfRisersValue)) <= 0) {
	            valid = true;
	            LOG.info("Provided noOfRises [{}] is within permissible value [{}]. Marking as VALID.", noOfRises, noOfRisersValue);
	        } else {
	        	LOG.info("Provided noOfRises [{}] exceeds permissible value [{}]. Marking as INVALID.", noOfRises, noOfRisersValue);
	        }

	        String value = typicalFloorValues.get(TYPICAL_FLOOR) != null
	                ? (String) typicalFloorValues.get(TYPICAL_FLOOR)
	                : FLOOR_SPACED + floor.getNumber();

	        if (valid) {
	        	LOG.info("Recording result as ACCEPTED for Flight [{}], GeneralStair [{}]", flight.getNumber(), generalStair.getNumber());
	            setReportOutputDetailsFloorStairWise(plan, RULERISER, value,
	                    String.format(NO_OF_RISER_DESCRIPTION_GENERAL_STAIR, generalStair.getNumber(), flight.getNumber()),
	                    EMPTY_STRING + noOfRisersValue, String.valueOf(noOfRises), Result.Accepted.getResultVal(),
	                    scrutinyDetail3);
	        } else {
	        	LOG.info("Recording result as NOT_ACCEPTED for Flight [{}], GeneralStair [{}]", flight.getNumber(), generalStair.getNumber());
	            setReportOutputDetailsFloorStairWise(plan, RULERISER, value,
	                    String.format(NO_OF_RISER_DESCRIPTION_GENERAL_STAIR, generalStair.getNumber(), flight.getNumber()),
	                    EMPTY_STRING + noOfRisersValue, String.valueOf(noOfRises), Result.Not_Accepted.getResultVal(),
	                    scrutinyDetail3);
	        }
	    }
	}


	/*
	 * private void setReportOutputDetails(Plan pl, String ruleNo, String ruleDesc,
	 * String expected, String actual, String status, ScrutinyDetail scrutinyDetail)
	 * { Map<String, String> details = new HashMap<>(); details.put(RULE_NO,
	 * ruleNo); details.put(DESCRIPTION, ruleDesc); details.put(REQUIRED, expected);
	 * details.put(PROVIDED, actual); details.put(STATUS, status);
	 * scrutinyDetail.getDetail().add(details);
	 * pl.getReportOutput().getScrutinyDetails().add(scrutinyDetail); }
	 */

	private void setReportOutputDetailsFloorStairWise(Plan pl, String ruleNo, String floor, String description,
			String expected, String actual, String status, ScrutinyDetail scrutinyDetail) {
		ReportScrutinyDetail detail = new ReportScrutinyDetail();
		detail.setRuleNo(ruleNo);
		detail.setDescription(description);
		detail.setFloorNo(floor);
		detail.setPermissible(expected);
		detail.setProvided(actual);
		detail.setStatus(status);

		Map<String, String> details = mapReportDetails(detail);
		addScrutinyDetailtoPlan(scrutinyDetail, pl, details);
	}

	/*
	 * private void validateDimensions(Plan plan, String blockNo, int floorNo,
	 * String stairNo, List<Measurement> flightPolyLines) { int count = 0; for
	 * (Measurement m : flightPolyLines) { if (m.getInvalidReason() != null &&
	 * m.getInvalidReason().length() > 0) { count++; } } if (count > 0) {
	 * plan.addError(String.format(DxfFileConstants. LAYER_FIRESTAIR_FLIGHT_FLOOR,
	 * blockNo, floorNo, stairNo), count +
	 * " number of flight polyline not having only 4 points in layer " +
	 * String.format(DxfFileConstants.LAYER_FIRESTAIR_FLIGHT_FLOOR, blockNo,
	 * floorNo, stairNo)); } }
	 */

	@Override
	public Map<String, Date> getAmendments() {
		return new LinkedHashMap<>();
	}

}