package org.egov.bpa.calculator.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.bpa.calculator.config.BPACalculatorConfig;
import org.egov.bpa.calculator.repository.PreapprovedPlanRepository;
import org.egov.bpa.calculator.repository.ServiceRequestRepository;
import org.egov.bpa.calculator.utils.BPACalculatorConstants;
import org.egov.bpa.calculator.web.models.CalculationReq;
import org.egov.bpa.calculator.web.models.CalulationCriteria;
import org.egov.bpa.calculator.web.models.PreapprovedPlan;
import org.egov.bpa.calculator.web.models.PreapprovedPlanSearchCriteria;
import org.egov.bpa.calculator.web.models.bpa.BPA;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
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
public class MDMSService {

	@Autowired
	 private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private BPACalculatorConfig config;

	@Autowired
	private EDCRService edcrService;

	@Autowired
	private PreapprovedPlanRepository preapprovedPlanRepository;
	
    public Object mDMSCall(CalculationReq calculationReq,String tenantId){
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(calculationReq,tenantId);
        StringBuilder url = getMdmsSearchUrl();
        Object result = serviceRequestRepository.fetchResult(url , mdmsCriteriaReq);
        return result;
    }

    /**
     * Creates and returns the url for mdms search endpoint
     *
     * @return MDMS Search URL
     */
    private StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsSearchEndpoint());
    }

    /**
     * Creates MDMS request
     * @param requestInfo The RequestInfo of the calculationRequest
     * @param tenantId The tenantId of the tradeLicense
     * @return MDMSCriteria Request
     */
    private MdmsCriteriaReq getMDMSRequest(CalculationReq calculationReq, String tenantId) {
    	RequestInfo requestInfo = 	calculationReq.getRequestInfo();
        List<MasterDetail> bpaMasterDetails = new ArrayList<>();
        
        bpaMasterDetails.add(MasterDetail.builder().name(BPACalculatorConstants.MDMS_CALCULATIONTYPE)
        		.build());
      
        bpaMasterDetails.add(MasterDetail.builder().name(BPACalculatorConstants.MDMS_TAXAMOUNT)
        		.build());

        ModuleDetail bpaModuleDtls = ModuleDetail.builder().masterDetails(bpaMasterDetails)
                .moduleName(BPACalculatorConstants.MDMS_BPA).build();
        
        List<ModuleDetail> moduleDetails = new ArrayList<>();
        
        moduleDetails.add(bpaModuleDtls);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
    }


    /**
     * Gets the calculationType for the city for a particular financialYear
     * If for particular financialYear entry is not there previous year is taken
     * If MDMS data is not available default values are returned
     * @param requestInfo The RequestInfo of the calculationRequest
     * @param license The tradeLicense for which calculation is done
     * @return Map contianing the calculationType for TradeUnit and accessory
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getCalculationType(RequestInfo requestInfo,BPA bpa,Object mdmsData, String feeType){
        HashMap<String,Object> calculationType = new HashMap<>();
        String businessService = bpa.getBusinessService();
        LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
		Map<String, String> additionalDetails = new HashMap<String, String>();
		JSONArray serviceType = null ;
		JSONArray applicationType = null;
        try {
            List jsonOutput = JsonPath.read(mdmsData, BPACalculatorConstants.MDMS_CALCULATIONTYPE_PATH);
            if (BPACalculatorConstants.BUSINESSSERVICE_PREAPPROVEDPLAN.equalsIgnoreCase(businessService)) {
        		//fetch preapproved plan-
        		PreapprovedPlan preapprovedPlan = fetchPreapprovedPlanFromDrawingNo(bpa.getEdcrNumber());
        		//responseMap = (LinkedHashMap<String, Object>) preapprovedPlan.getDrawingDetail();
                Object drawingDetail = preapprovedPlan.getDrawingDetail();
                
                // Check if drawingDetail is of type LinkedHashMap<String, Object>
                if (drawingDetail instanceof LinkedHashMap) {
                    // Safely cast to LinkedHashMap<String, Object>
                    responseMap = (LinkedHashMap<String, Object>) drawingDetail;
                } else if (drawingDetail instanceof Map) {
                    // Convert to LinkedHashMap if it's some other type of Map
                    responseMap = new LinkedHashMap<>((Map<String, Object>) drawingDetail);
                }
                } 
            else {
            responseMap = edcrService.getEDCRDetails(requestInfo, bpa);
            }
            String jsonString = new JSONObject(responseMap).toString();
    		DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
            if (BPACalculatorConstants.BUSINESSSERVICE_PREAPPROVEDPLAN.equalsIgnoreCase(businessService)) {
        		String serviceType1 = context.read("serviceType");
        		String applicationType1 = context.read("applicationType");
        		additionalDetails.put("serviceType", serviceType1);
        		additionalDetails.put("applicationType", applicationType1);
            }
            else {
    		serviceType = context.read("edcrDetail.*.serviceType");
    		if (CollectionUtils.isEmpty(serviceType)) {
    			serviceType.add("NEW_CONSTRUCTION");
    		}
    		applicationType = context.read("edcrDetail.*.appliactionType");
    		if (org.springframework.util.StringUtils.isEmpty(applicationType)) {
    			applicationType.add("permit");
    		}
    		additionalDetails.put("serviceType", serviceType.get(0).toString());
    		additionalDetails.put("applicationType", applicationType.get(0).toString());
            }            
            log.debug("applicationType is " + additionalDetails.get("applicationType"));
            log.debug("serviceType is " + additionalDetails.get("serviceType"));
            String filterExp = "$.[?((@.applicationType == '"+ additionalDetails.get("applicationType")+"' || @.applicationType === 'ALL' ) &&  @.feeType == '"+feeType+"')]";
            List<Object> calTypes = JsonPath.read(jsonOutput, filterExp);
            
            filterExp = "$.[?(@.serviceType == '"+ additionalDetails.get("serviceType")+"' || @.serviceType === 'ALL' )]";
            calTypes = JsonPath.read(calTypes, filterExp);
            
            filterExp = "$.[?(@.riskType == '"+bpa.getRiskType()+"' || @.riskType === 'ALL' )]";
            calTypes = JsonPath.read(calTypes, filterExp);
            
            if(calTypes.size() > 1){
	            	filterExp = "$.[?(@.riskType == '"+bpa.getRiskType()+"' )]";
	            	calTypes  = JsonPath.read(calTypes, filterExp);
            }
            
            if(calTypes.size() == 0) {
            		return defaultMap(feeType);
            }
            
             Object obj = calTypes.get(0);
           
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            
           String financialYear = currentYear + "-" + (currentYear + 1);
           System.out.println(financialYear);
            
            calculationType = (HashMap<String, Object>) obj;
        }
        catch (Exception e){
            throw new CustomException(BPACalculatorConstants.CALCULATION_ERROR, "Failed to get calculationType");
        }

        return calculationType;
    }
   
    /**
     * Creates and return default calculationType values as map
     * @return default calculationType Map
     */	
    private Map defaultMap(String feeType){
        Map defaultMap = new HashMap();
        String feeAmount = ( feeType.equalsIgnoreCase(BPACalculatorConstants.MDMS_CALCULATIONTYPE_APL_FEETYPE) ) ? config.getApplFeeDefaultAmount() : config.getSancFeeDefaultAmount();
        defaultMap.put( BPACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT,feeAmount);
        return defaultMap;
    }

	private PreapprovedPlan fetchPreapprovedPlanFromDrawingNo(String drawingNo) {
		PreapprovedPlanSearchCriteria criteria = new PreapprovedPlanSearchCriteria();
		criteria.setDrawingNo(drawingNo);
		List<PreapprovedPlan> preapprovedPlans = preapprovedPlanRepository.getPreapprovedPlansData(criteria);
		if (CollectionUtils.isEmpty(preapprovedPlans)) {
			log.error("No preapproved plan with provided drawingNo:" + drawingNo);
			throw new CustomException("No preapproved plan with provided drawingNo",
					"No preapproved plan with provided drawingNo");
		}
		return preapprovedPlans.get(0);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map<String, Object>> getCalculationType(RequestInfo requestInfo, BPA bpa, Object mdmsData,
			CalulationCriteria calulationCriteria) {

		List<Map<String, Object>> finalList = new ArrayList<>();
		String feeType = calulationCriteria.getFeeType();
		String applicationType = calulationCriteria.getApplicationType();
		String wallType = calulationCriteria.getWallType();
		String floorLevel = calulationCriteria.getFloorLevel();
		String subOccupancy = calulationCriteria.getSubOccupancy();
		
		log.info(String.format("TenantID: %s, FeeType: %s, ApplicationType: %s, WallType: %s", bpa.getTenantId(),
				feeType, applicationType, wallType));

		try {
			// Fetch whole calculationType JSON from MDMS
			List jsonOutput = JsonPath.read(mdmsData, BPACalculatorConstants.MDMS_CALCULATIONTYPE_PATH);

			// 1. Filter by feeType
			String filterExp = "$.[?(@.feeType == '" + feeType + "')]";
			List<Object> calTypes = JsonPath.read(jsonOutput, filterExp);

			// 2. Filter by applicationType 
			filterExp = "$.[?(@.applicationType == '" + applicationType + "' || @.applicationType == 'ALL')]";
			calTypes = JsonPath.read(calTypes, filterExp);
			
			// 3. Filter by subOccupancy 
			if (StringUtils.isNotBlank(subOccupancy)) {

	            filterExp = "$.[?(@.subOccupancy == '" + subOccupancy + "')]";
	            List<Object> subOccMatched = JsonPath.read(calTypes, filterExp);

	            if (!subOccMatched.isEmpty()) {
	                calTypes = subOccMatched;
	            }
	        }

			// 4. Filter by wallType/floorLevel 
			if (StringUtils.isNotBlank(wallType)) {

				filterExp = "$.[?(@.wallType == '" + wallType + "' || @.wallType == 'ALL')]";
				calTypes = JsonPath.read(calTypes, filterExp);
			}

			if (calTypes.isEmpty()) {
				return Collections.singletonList(defaultMap(feeType));
			}

			for (Object obj : calTypes) {

				HashMap<String, Object> calculationType = (HashMap<String, Object>) obj;
				calculationType.put("unitType", calculationType.getOrDefault("unitType", "FIXED"));
				calculationType.put("rate", calculationType.getOrDefault("rate", "0"));
				calculationType.put("additionalFee", calculationType.getOrDefault("additionalFee", "0"));
				calculationType.put("multiplier", calculationType.getOrDefault("multiplier", "1"));
				finalList.add(calculationType);
			}

		} catch (Exception e) {
			throw new CustomException(BPACalculatorConstants.CALCULATION_ERROR,
					"Failed to get calculationType for feeType: " + feeType + e.getLocalizedMessage() + e.getMessage());
		}

		return finalList;
	}
	
	/**
	 * Fetches the Labour Cess rate from MDMS configuration.
	 *
	 * <p>
	 * This method reads the Labour Cess master data from MDMS and extracts
	 * the configured rate value. The rate is expected to be defined as a
	 * percentage (e.g., 1 for 1%) and is later applied on the aggregated
	 * building permit fee.
	 * </p>
	 *
	 * <p>
	 * Key Notes:
	 * <ul>
	 *   <li>The Labour Cess rate is tenant-configurable via MDMS.</li>
	 *   <li>If no active Labour Cess configuration is found, this method
	 *       safely returns {@link BigDecimal#ZERO}.</li>
	 *   <li>The actual calculation logic (percentage / flat, etc.) is
	 *       handled by the caller based on fee type.</li>
	 * </ul>
	 * </p>
	 *
	 * @param requestInfo the request information containing tenant and
	 *                    user context (currently not used, but kept for
	 *                    future extensibility and consistency with other
	 *                    MDMS fetch methods)
	 * @param mdmsData    the MDMS response object containing Labour Cess
	 *                    configuration under the BPA module
	 *
	 * @return the Labour Cess rate as {@link BigDecimal}; returns
	 *         {@link BigDecimal#ZERO} if no configuration is available
	 */
	
	public BigDecimal getLabourCessRate(RequestInfo requestInfo, Object mdmsData) {
	    List<Map<String, Object>> cessList =
	            JsonPath.read(mdmsData, BPACalculatorConstants.MDMS_TAX_AMOUNT_PATH);

	    if (cessList == null || cessList.isEmpty())
	        return BigDecimal.ZERO;

	    Map<String, Object> cess = cessList.get(0);
	    return new BigDecimal(cess.get("rate").toString());
	}


}
