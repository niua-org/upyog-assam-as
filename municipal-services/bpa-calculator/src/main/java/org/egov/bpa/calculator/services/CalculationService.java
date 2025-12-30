package org.egov.bpa.calculator.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.bpa.calculator.config.BPACalculatorConfig;
import org.egov.bpa.calculator.kafka.broker.BPACalculatorProducer;
import org.egov.bpa.calculator.repository.PreapprovedPlanRepository;
import org.egov.bpa.calculator.utils.BPACalculatorConstants;
import org.egov.bpa.calculator.utils.CalculationUtils;
import org.egov.bpa.calculator.web.models.BillingSlabSearchCriteria;
import org.egov.bpa.calculator.web.models.Calculation;
import org.egov.bpa.calculator.web.models.CalculationReq;
import org.egov.bpa.calculator.web.models.CalculationRes;
import org.egov.bpa.calculator.web.models.CalulationCriteria;
import org.egov.bpa.calculator.web.models.bpa.BPA;
import org.egov.bpa.calculator.web.models.bpa.EstimatesAndSlabs;
import org.egov.bpa.calculator.web.models.bpa.Floor;
import org.egov.bpa.calculator.web.models.demand.Category;
import org.egov.bpa.calculator.web.models.demand.TaxHeadEstimate;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class CalculationService {

	

	@Autowired
	private MDMSService mdmsService;

	@Autowired
	private DemandService demandService;

	@Autowired
	private EDCRService edcrService;
	
	@Autowired
	private BPACalculatorConfig config;

	@Autowired
	private CalculationUtils utils;

	@Autowired
	private BPACalculatorProducer producer;


	@Autowired
	private BPAService bpaService;
	
	@Autowired
	private PreapprovedPlanRepository preapprovedPlanRepository;

	/**
	 * Calculates tax estimates and creates demand
	 * 
	 * @param calculationReq
	 *            The calculationCriteria request
	 * @return List of calculations for all applicationNumbers or tradeLicenses
	 *         in calculationReq
	 */
	public List<Calculation> calculate(CalculationReq calculationReq) {
		String tenantId = calculationReq.getCalulationCriteria().get(0)
				.getTenantId();
		Object mdmsData = mdmsService.mDMSCall(calculationReq, tenantId);
		List<Calculation> calculations = calculateFee(calculationReq.getRequestInfo(),calculationReq.getCalulationCriteria(), mdmsData);
		demandService.generateDemand(calculationReq.getRequestInfo(),calculations, mdmsData);
		CalculationRes calculationRes = CalculationRes.builder().calculations(calculations).build();
		producer.push(config.getSaveTopic(), calculationRes);
		return calculations;
	}
	
	public List<Calculation> estimate(CalculationReq calculationReq) {
		String tenantId = calculationReq.getCalulationCriteria().get(0)
				.getTenantId();
		Object mdmsData = mdmsService.mDMSCall(calculationReq, tenantId);
		List<Calculation> calculations = calculateFee(calculationReq.getRequestInfo(),calculationReq.getCalulationCriteria(), mdmsData);
		return calculations;
	}

	/***
	 * Calculates tax estimates
	 * 
	 * @param requestInfo
	 *            The requestInfo of the calculation request
	 * @param criterias
	 *            list of CalculationCriteria containing the tradeLicense or
	 *            applicationNumber
	 * @return List of calculations for all applicationNumbers or tradeLicenses
	 *         in criterias
	 */
	public List<Calculation> getCalculation(RequestInfo requestInfo,
			List<CalulationCriteria> criterias, Object mdmsData) {
		List<Calculation> calculations = new LinkedList<>();
		for (CalulationCriteria criteria : criterias) {
			BPA bpa;
			if (criteria.getBpa() == null
					&& criteria.getApplicationNo() != null) {
				bpa = bpaService.getBuildingPlan(requestInfo, criteria.getTenantId(),
						criteria.getApplicationNo(), null);
				criteria.setBpa(bpa);
			}

			EstimatesAndSlabs estimatesAndSlabs = getTaxHeadEstimates(criteria,
					requestInfo, mdmsData);
			List<TaxHeadEstimate> taxHeadEstimates = estimatesAndSlabs
					.getEstimates();

			Calculation calculation = new Calculation();
			calculation.setBpa(criteria.getBpa());
			calculation.setTenantId(criteria.getTenantId());
			calculation.setTaxHeadEstimates(taxHeadEstimates);
			calculation.setFeeType( criteria.getFeeType());
			calculations.add(calculation);

		}
		return calculations;
	}

	/**
	 * Creates TacHeadEstimates
	 * 
	 * @param calulationCriteria
	 *            CalculationCriteria containing the tradeLicense or
	 *            applicationNumber
	 * @param requestInfo
	 *            The requestInfo of the calculation request
	 * @return TaxHeadEstimates and the billingSlabs used to calculate it
	 */
	private EstimatesAndSlabs getTaxHeadEstimates(
			CalulationCriteria calulationCriteria, RequestInfo requestInfo,
			Object mdmsData) {
		List<TaxHeadEstimate> estimates = new LinkedList<>();
		EstimatesAndSlabs estimatesAndSlabs;
		if (calulationCriteria.getFeeType().equalsIgnoreCase(BPACalculatorConstants.LOW_RISK_PERMIT_FEE_TYPE)) {

//			 stopping Application fee for lowrisk applicaiton according to BBI-391
			calulationCriteria.setFeeType(BPACalculatorConstants.MDMS_CALCULATIONTYPE_LOW_APL_FEETYPE);
			estimatesAndSlabs = getBaseTax(calulationCriteria, requestInfo, mdmsData);

			estimates.addAll(estimatesAndSlabs.getEstimates());

			calulationCriteria.setFeeType(BPACalculatorConstants.MDMS_CALCULATIONTYPE_LOW_SANC_FEETYPE);
			estimatesAndSlabs = getBaseTax(calulationCriteria, requestInfo, mdmsData);

			estimates.addAll(estimatesAndSlabs.getEstimates());

			calulationCriteria.setFeeType(BPACalculatorConstants.LOW_RISK_PERMIT_FEE_TYPE);

		} else {
			estimatesAndSlabs = getBaseTax(calulationCriteria, requestInfo, mdmsData);
			estimates.addAll(estimatesAndSlabs.getEstimates());
		}

		estimatesAndSlabs.setEstimates(estimates);

		return estimatesAndSlabs;
	}

	/**
	 * Calculates base tax and cretaes its taxHeadEstimate
	 * 
	 * @param calulationCriteria
	 *            CalculationCriteria containing the tradeLicense or
	 *            applicationNumber
	 * @param requestInfo
	 *            The requestInfo of the calculation request
	 * @return BaseTax taxHeadEstimate and billingSlabs used to calculate it
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private EstimatesAndSlabs getBaseTax(CalulationCriteria calulationCriteria, RequestInfo requestInfo,
			Object mdmsData) {
		BPA bpa = calulationCriteria.getBpa();
		EstimatesAndSlabs estimatesAndSlabs = new EstimatesAndSlabs();
		BillingSlabSearchCriteria searchCriteria = new BillingSlabSearchCriteria();
		searchCriteria.setTenantId(bpa.getTenantId());

		Map calculationTypeMap = mdmsService.getCalculationType(requestInfo, bpa, mdmsData,
				calulationCriteria.getFeeType());
		int calculatedAmout = 0;
		ArrayList<TaxHeadEstimate> estimates = new ArrayList<TaxHeadEstimate>();
		if (calculationTypeMap.containsKey("calsiLogic")) {
			LinkedHashMap ocEdcr = edcrService.getEDCRDetails(requestInfo, bpa);
			String jsonString = new JSONObject(ocEdcr).toString();
			DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
			JSONArray permitNumber = context.read("edcrDetail.*.permitNumber");
			String jsonData = new JSONObject(calculationTypeMap).toString();
			DocumentContext calcContext = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonData);
			JSONArray parameterPaths = calcContext.read("calsiLogic.*.paramPath");
			JSONArray tLimit = calcContext.read("calsiLogic.*.tolerancelimit");
			System.out.println("tolerance limit in: " + tLimit.get(0));
			DocumentContext edcrContext = null;
			if (!CollectionUtils.isEmpty(permitNumber)) {
				BPA permitBpa = bpaService.getBuildingPlan(requestInfo, bpa.getTenantId(), null,
						permitNumber.get(0).toString());
				if (permitBpa.getEdcrNumber() != null) {
					LinkedHashMap edcr = edcrService.getEDCRDetails(requestInfo, permitBpa);
					String edcrData = new JSONObject(edcr).toString();
					edcrContext = JsonPath.using(Configuration.defaultConfiguration()).parse(edcrData);
				}
			}
			
			for (int i = 0; i < parameterPaths.size(); i++) {
				Double ocTotalBuitUpArea = context.read(parameterPaths.get(i).toString());
				Double bpaTotalBuitUpArea = edcrContext.read(parameterPaths.get(i).toString());
				Double diffInBuildArea = ocTotalBuitUpArea - bpaTotalBuitUpArea;
				System.out.println("difference in area: " + diffInBuildArea);
				Double limit = Double.valueOf(tLimit.get(i).toString());
				if (diffInBuildArea > limit) {
					JSONArray data = calcContext.read("calsiLogic.*.deviation");
					System.out.println(data.get(0));
					JSONArray data1 = (JSONArray) data.get(0);
					for (int j = 0; j < data1.size(); j++) {
						LinkedHashMap diff = (LinkedHashMap) data1.get(j);
						Integer from = (Integer) diff.get("from");
						Integer to = (Integer) diff.get("to");
						Integer uom = (Integer) diff.get("uom");
						Integer mf = (Integer) diff.get("MF");
						if (diffInBuildArea >= from && diffInBuildArea <= to) {
							calculatedAmout = (int) (diffInBuildArea * mf * uom);
							break;
						}
					}
				} else {
					calculatedAmout = 0;
				}
				TaxHeadEstimate estimate = new TaxHeadEstimate();
				BigDecimal totalTax = BigDecimal.valueOf(calculatedAmout);
				if (totalTax.compareTo(BigDecimal.ZERO) == -1)
					throw new CustomException(BPACalculatorConstants.INVALID_AMOUNT, "Tax amount is negative");

				estimate.setEstimateAmount(totalTax);
				estimate.setCategory(Category.FEE);

				String taxHeadCode = utils.getTaxHeadCode(bpa.getBusinessService(), calulationCriteria.getFeeType());
				estimate.setTaxHeadCode(taxHeadCode);
				estimates.add(estimate);
			}
		} else {
			TaxHeadEstimate estimate = new TaxHeadEstimate();
			calculatedAmout = Integer
					.parseInt(calculationTypeMap.get(BPACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT).toString());

			BigDecimal totalTax = BigDecimal.valueOf(calculatedAmout);
			if (totalTax.compareTo(BigDecimal.ZERO) == -1)
				throw new CustomException(BPACalculatorConstants.INVALID_AMOUNT, "Tax amount is negative");

			estimate.setEstimateAmount(totalTax);
			estimate.setCategory(Category.FEE);

			String taxHeadCode = utils.getTaxHeadCode(bpa.getBusinessService(), calulationCriteria.getFeeType());
			estimate.setTaxHeadCode(taxHeadCode);
			estimates.add(estimate);
		}
		estimatesAndSlabs.setEstimates(estimates);
		return estimatesAndSlabs;
	}
	
	/**
	 * Calculates fee estimates for a BPA application based on floor-wise built-up area,
	 * construction cost, and MDMS configuration.
	 *
	 * <p>
	 * This method computes permit / planning fees by iterating over each floor and
	 * applying the appropriate rate slabs fetched from MDMS. It also handles special
	 * fee components such as:
	 * </p>
	 *
	 * <ul>
	 *   <li><b>Premium FAR Fee</b> – Applied only once for Planning Permit Fee, based on
	 *       total premium built-up area, irrespective of number of floors.</li>
	 *   <li><b>Labour Cess</b> – Calculated as 1% of the total Building Permit Fee and
	 *       added as a separate tax head.</li>
	 * </ul>
	 *
	 * <p>
	 * Key rules implemented:
	 * </p>
	 * <ul>
	 *   <li>Ground floor and upper floors can have different calculation types.</li>
	 *   <li>Premium FAR fee is applied only once using a guard flag.</li>
	 *   <li>Negative tax amounts are not allowed and will throw an exception.</li>
	 *   <li>All monetary values are rounded to the nearest whole number using
	 *       {@link RoundingMode#HALF_UP}.</li>
	 * </ul>
	 *
	 * @param calulationCriteria Contains BPA details, floor information, fee type,
	 *                           premium built-up area, and other inputs required for
	 *                           calculation.
	 * @param requestInfo        Request metadata including user and tenant context.
	 * @param mdmsData           MDMS data object used to derive rate slabs and
	 *                           calculation types.
	 *
	 * @return {@link EstimatesAndSlabs} containing a list of {@link TaxHeadEstimate}
	 *         objects representing calculated fees.
	 *
	 * @throws CustomException if any calculated tax amount is negative or if required
	 *                         calculation inputs are invalid.
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private EstimatesAndSlabs fetchBaseRates(CalulationCriteria calulationCriteria, RequestInfo requestInfo,
			Object mdmsData) {

		boolean premiumApplied = false;

		BPA bpa = calulationCriteria.getBpa();
		BigDecimal constructionCost = bpa.getConstructionCost();
		List<Floor> floors = calulationCriteria.getFloors();
		EstimatesAndSlabs estimatesAndSlabs = new EstimatesAndSlabs();
		ArrayList<TaxHeadEstimate> estimates = new ArrayList<>();

		List<Map<String, Object>> calculationTypeMap = mdmsService.getCalculationType(requestInfo, bpa, mdmsData, calulationCriteria);
		log.info("Calculation Type: "+calculationTypeMap);
		Map<String, Object> calcTypeGround = calculationTypeMap.get(0);
		Map<String, Object> calcTypeUpper = calculationTypeMap.get(1);
		BigDecimal totalPermitFee = BigDecimal.ZERO;

		
//		Calculating fee for each floor
		for (Floor floor : floors) {

			BigDecimal totalTax = BigDecimal.ZERO;
			BigDecimal totalBuiltupArea = floor.getBuiltUpArea();
			log.info("Total Built Up Area : "+totalBuiltupArea+", Floor Level : "+floor.getLevel());

			if (floor.getLevel() == 0) {
				totalTax = totalTax.add(calculateEstimate(totalBuiltupArea, constructionCost, calcTypeGround, BigDecimal.ZERO));
			} else {
				totalTax = totalTax.add(calculateEstimate(totalBuiltupArea, constructionCost, calcTypeUpper, calulationCriteria.getPremiumBuiltUpArea()));
			}

			
			/**
			 * Premium FAR fee calculation
			 *
			 * Premium FAR charges are applicable only for PLANNING_PERMIT_FEE.
			 * The premium amount is calculated once on the total premium built-up area,
			 * irrespective of the number of floors.
			 *
			 * Conditions:
			 * - Should be applied only once (controlled using premiumApplied flag)
			 * - Applicable only when premium built-up area is greater than zero
			 * - Rate and multiplier are picked from upper floor calculation type
			 */
			
		    if (!premiumApplied
		            && BPACalculatorConstants.PLANNING_PERMIT_FEE.equals(calulationCriteria.getFeeType())
		            && calulationCriteria.getPremiumBuiltUpArea() != null
		            && calulationCriteria.getPremiumBuiltUpArea().compareTo(BigDecimal.ZERO) > 0) {

		        BigDecimal rate = new BigDecimal(calcTypeUpper.get("rate").toString());
		        BigDecimal multiplier = new BigDecimal(calcTypeUpper.get("multiplier").toString());

		        BigDecimal premiumAmount =
		                calulationCriteria.getPremiumBuiltUpArea()
		                        .multiply(rate)
		                        .multiply(multiplier)
		                        .setScale(0, RoundingMode.HALF_UP);

		        totalTax = totalTax.add(premiumAmount);
		        premiumApplied = true;

		        log.info("Premium FAR Fee applied once: {}", premiumAmount);
		    }

			TaxHeadEstimate estimate = new TaxHeadEstimate();
			estimate.setEstimateAmount(totalTax);
			estimate.setCategory(Category.FEE);
			String taxHeadCode = utils.getTaxHeadCode(bpa.getBusinessService(), calulationCriteria.getFeeType());
			estimate.setTaxHeadCode(taxHeadCode);
			
			Map<String, Object> additional = new HashMap<>();
			String level = utils.toOrdinalFloorName(floor.getLevel());
			additional.put("floor", level);
			estimate.setAdditionalDetails(additional);

			if (totalTax.compareTo(BigDecimal.ZERO) < 0)
				throw new CustomException(BPACalculatorConstants.INVALID_AMOUNT, "Tax amount is negative");

			estimates.add(estimate);
			totalPermitFee = totalPermitFee.add(totalTax);

		}
		
		/**
		 * Labour Cess calculation
		 *
		 * As per building permit rules, Labour Cess is applicable only for
		 * BUILDING_PERMIT_FEE and is calculated as 1% of the total permit fee.
		 *
		 * - It is NOT applied for planning permit fee.
		 * - It is calculated after aggregating permit fee of all floors.
		 * - The amount is rounded to the nearest whole number (HALF_UP).
		 */
		
		if (BPACalculatorConstants.BUILDING_PERMIT_FEE.equals(calulationCriteria.getFeeType())
		        && totalPermitFee.compareTo(BigDecimal.ZERO) > 0) {

		    BigDecimal labourCessAmount = totalPermitFee
		            .multiply(new BigDecimal("0.01"))
		            .setScale(0, RoundingMode.HALF_UP);

		    TaxHeadEstimate labourCessEstimate = new TaxHeadEstimate();
		    labourCessEstimate.setEstimateAmount(labourCessAmount);
		    labourCessEstimate.setCategory(Category.FEE);
		    labourCessEstimate.setTaxHeadCode("LABOUR_CESS");

		    estimates.add(labourCessEstimate);

		    log.info("Labour Cess (1%) calculated on total permit fee {} : {}",
		            totalPermitFee, labourCessAmount);
		}
		estimatesAndSlabs.setEstimates(estimates);

		return estimatesAndSlabs;
	}

	private BigDecimal calculateEstimate(BigDecimal totalBuiltUpArea, BigDecimal constructionCost,
			Map<String, Object> calcType,  BigDecimal premiumFarBuiltUpArea) {

		String unitType = (String) calcType.get("unitType");
		BigDecimal rate = new BigDecimal(calcType.get("rate").toString());
		BigDecimal additionalFee = new BigDecimal(StringUtils.isEmpty((String) calcType.get("additionalFee")) ? "0"
				: (String) calcType.get("additionalFee"));

		BigDecimal amount = BigDecimal.ZERO;

		switch (unitType.toLowerCase()) {
		case "per sq. m":
			amount = totalBuiltUpArea.multiply(rate);
			break;

		case "percentage":
			amount = constructionCost.multiply(rate).divide(BigDecimal.valueOf(100));
			break;

		case "fixed":
		default:
			amount = rate;
			break;
		}

		amount = amount.add(additionalFee);
		amount = amount.setScale(0, RoundingMode.HALF_UP);

		return amount;
	}

	public List<Calculation> calculateFee(RequestInfo requestInfo, List<CalulationCriteria> criterias,
			Object mdmsData) {

		List<Calculation> calculations = new LinkedList<>();

		for (CalulationCriteria criteria : criterias) {

			EstimatesAndSlabs estimatesAndSlabs = fetchRates(criteria, requestInfo, mdmsData);
			List<TaxHeadEstimate> taxHeadEstimates = estimatesAndSlabs.getEstimates();

			Calculation calculation = new Calculation();
			calculation.setBpa(criteria.getBpa());
			calculation.setTenantId(criteria.getTenantId());
			calculation.setTaxHeadEstimates(taxHeadEstimates);
			calculation.setFeeType(criteria.getFeeType());
			calculations.add(calculation);
		}
		
		return calculations;
	}

	private EstimatesAndSlabs fetchRates(CalulationCriteria calulationCriteria, RequestInfo requestInfo,
			Object mdmsData) {
		
		List<TaxHeadEstimate> estimates = new LinkedList<>();
		EstimatesAndSlabs estimatesAndSlabs = fetchBaseRates(calulationCriteria, requestInfo, mdmsData);
		estimates.addAll(estimatesAndSlabs.getEstimates());
		estimatesAndSlabs.setEstimates(estimates);

		return estimatesAndSlabs;
	}
	
	

}
