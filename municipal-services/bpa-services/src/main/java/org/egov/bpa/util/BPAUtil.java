package org.egov.bpa.util;

import static org.egov.bpa.util.BPAConstants.BILL_AMOUNT;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.web.model.AuditDetails;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.RequestInfoWrapper;
import org.egov.bpa.web.model.landInfo.LandInfo;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.gson.Gson;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BPAUtil {

	private BPAConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	public BPAUtil(BPAConfiguration config, ServiceRequestRepository serviceRequestRepository) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
	}

	/**
	 * Method to return auditDetails for create/update flows
	 *
	 * @param by
	 * @param isCreate
	 * @return AuditDetails
	 */
	public AuditDetails getAuditDetails(String by, Boolean isCreate) {
		Long time = System.currentTimeMillis();
		if (isCreate)
			return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time)
					.build();
		else
			return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
	}

	/**
	 * Returns the URL for MDMS search end point
	 *
	 * @return URL for MDMS search end point
	 */
	public StringBuilder getMdmsSearchUrl() {
		return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
	}

	/**
	 * Creates request to search ApplicationType and etc from MDMS
	 *
	 * @param tenantCode
	 *            The tenantId of the BPA
	 * @return request to search ApplicationType and etc from MDMS
	 */
	public List<ModuleDetail> getBPAModuleRequest(String tenantCode) {
		

		// master details for BPA module
		List<MasterDetail> bpaMasterDtls = new ArrayList<>();

		// filter to only get code field from master data
		final String filterCode = "$.[?(@.active==true)].code";

		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.APPLICATION_TYPE).filter(filterCode).build());
//		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.SERVICE_TYPE).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.DOCUMENT_TYPE_MAPPING).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.RISKTYPE_COMPUTATION).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.OCCUPANCY_TYPE).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.SUB_OCCUPANCY_TYPE).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.USAGES).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.CalculationType).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.CHECKLIST_NAME).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.NOC_TYPE_MAPPING).build());
//		New MDMS masters added for BPA
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.PERMISSIBLE_ZONE).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.CONSTRUCTION_TYPE).build());
		
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.RTP_CATEGORIES).build());
		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.STATES).build());
//		bpaMasterDtls.add(MasterDetail.builder().name(BPAConstants.ULB_WARD_DETAILS).build());
		

		ModuleDetail bpaModuleDtls = ModuleDetail.builder().masterDetails(bpaMasterDtls)
				.moduleName(BPAConstants.BPA_MODULE).build();

		// master details for common-masters module
		List<MasterDetail> commonMasterDetails = new ArrayList<>();
//		commonMasterDetails.add(MasterDetail.builder().name(BPAConstants.OWNERSHIP_CATEGORY).filter(filterCode).build());
//		commonMasterDetails.add(MasterDetail.builder().name(BPAConstants.OWNER_TYPE).filter(filterCode).build());
		commonMasterDetails.add(MasterDetail.builder().name(BPAConstants.DOCUMENT_TYPE).filter(filterCode).build());
		ModuleDetail commonMasterMDtl = ModuleDetail.builder().masterDetails(commonMasterDetails)
				.moduleName(BPAConstants.COMMON_MASTERS_MODULE).build();
		
		// master details for NOC module
		List<MasterDetail> nocMasterDetails = new ArrayList<>();
		nocMasterDetails.add(MasterDetail.builder().name(BPAConstants.NOC_TYPE_MAPPING).build());
		nocMasterDetails
				.add(MasterDetail.builder().name(BPAConstants.NOC_TYPE).build());
		ModuleDetail nocMDtl = ModuleDetail.builder().masterDetails(nocMasterDetails)
				.moduleName(BPAConstants.NOC_MODULE).build();
		
		List<MasterDetail> egovLocationMasterDetails = new ArrayList<>();
		egovLocationMasterDetails.add(MasterDetail.builder().name(BPAConstants.EGOV_LOCATION).build());
		
		ModuleDetail egovLocationMasterDtl = ModuleDetail.builder().masterDetails(egovLocationMasterDetails)
				.moduleName(BPAConstants.EGOV_LOCATION).build();
		
		List<MasterDetail> tenantMasterDetails = new ArrayList<>();
		tenantMasterDetails.add(MasterDetail.builder().name(BPAConstants.TENANTS).build());
		ModuleDetail tenantsMasterDtl = ModuleDetail.builder().masterDetails(tenantMasterDetails)
				.moduleName(BPAConstants.TENANT).build();
		

		return Arrays.asList(bpaModuleDtls, commonMasterMDtl, nocMDtl, egovLocationMasterDtl, tenantsMasterDtl);

	}

	/**
	 * Builds and returns the list of MDMS {@link ModuleDetail} required for
	 * fetching tenant-level BPA module data from MDMS.
	 * <p>
	 * This method prepares the MDMS request structure specifically for:
	 * <ul>
	 *     <li>{@code EGOV_LOCATION}</li>
	 *     <li>{@code TENANT_BOUNDARY}</li>
	 * </ul>
	 * These modules are used to extract ward, village, and boundary-level details
	 * needed during BPA validations.
	 * </p>
	 *
	 * @param tenantCode The tenant for which the MDMS request structure is needed.
	 *                   (Currently not used in logic but kept for future extensibility.)
	 *
	 * @return A list containing the {@link ModuleDetail} definitions required for
	 *         MDMS lookup at tenant level.
	 */
	public List<ModuleDetail> getBPAModuleRequestTenant(String tenantCode) {

		List<MasterDetail> egovLocationMasterDetails = new ArrayList<>();
		egovLocationMasterDetails.add(MasterDetail.builder().name(BPAConstants.EGOV_LOCATION).build());
		egovLocationMasterDetails.add(MasterDetail.builder().name(BPAConstants.TENANT_BOUNDARY).build());
		ModuleDetail egovLocationMasterDtl = ModuleDetail.builder().masterDetails(egovLocationMasterDetails)
				.moduleName(BPAConstants.EGOV_LOCATION).build();
		
		
		return Arrays.asList(egovLocationMasterDtl);

	}

	/**
	 * prepares the mdms request object
	 * @param requestInfo
	 * @param tenantId
	 * @return
	 */
	public MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId) {
		List<ModuleDetail> moduleRequest = null;
		if (tenantId != null && tenantId.contains(".")) {

		 moduleRequest = getBPAModuleRequestTenant(tenantId);
		} else {
			moduleRequest = getBPAModuleRequest(tenantId);
		}
		List<ModuleDetail> moduleDetails = new LinkedList<>();
		moduleDetails.addAll(moduleRequest);

		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId).build();

		MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria).requestInfo(requestInfo)
				.build();
		return mdmsCriteriaReq;
	}

	/**
	 * makes mdms call with the given criteria and reutrn mdms data
	 * @param requestInfo
	 * @param tenantId
	 * @return
	 */
	public Object mDMSCall(RequestInfo requestInfo, String tenantId) {
		MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId);
		Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
		return result;
	}
	
	/**
	 * json path's defuault cofig to read/parse the json
	 */
	public void defaultJsonPathConfig() {
		Configuration.setDefaults(new Configuration.Defaults() {

			private final JsonProvider jsonProvider = new JacksonJsonProvider();
			private final MappingProvider mappingProvider = new JacksonMappingProvider();

			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return mappingProvider;
			}

			@Override
			public Set<Option> options() {
				return EnumSet.noneOf(Option.class);
			}
		});
	}

	/**
	 * gererate bill url with the query params
	 * @param bpa
	 * @return
	 */
	public StringBuilder getBillUri(BPA bpa) {
		String code = getFeeBusinessSrvCode(bpa);

		StringBuilder builder = new StringBuilder(config.getBillingHost());
		builder.append(config.getDemandSearchEndpoint());
		builder.append("?tenantId=");
		builder.append(bpa.getTenantId());
		builder.append("&consumerCode=");
		builder.append(bpa.getApplicationNo());
		builder.append("&businessService=");
		builder.append(code);
		return builder;
	}
	
	/**
	 * return the FeeBusiness Service code based on the BPA workflowCode, BPA Status
	 * @param bpa
	 * @return
	 */
	public String getFeeBusinessSrvCode(BPA bpa) {
		Map<String, Map<String, String>> wfStBSrvMap = config.getWorkflowStatusFeeBusinessSrvMap();
		String businessSrvCode = null;
		Map<String, String> statusBusSrvMap = wfStBSrvMap.get(bpa.getBusinessService());
		if (!CollectionUtils.isEmpty(statusBusSrvMap)) {
			if (bpa.getStatus() != null) {
				businessSrvCode = statusBusSrvMap.get(bpa.getStatus());
			} 
		}
		return businessSrvCode;
		
	}

	public long getCurrentTimestampMillis() {
		return Instant.now().toEpochMilli();
	}

	/**
	 * Extracts the state part from a tenantId. If the input does not contain a dot
	 * or is blank, the method returns the input as-is.
	 * 
	 * @param tenantId
	 * @return the extracted state
	 **/
	public String extractState(String tenantId) {

		if (tenantId == null || tenantId.trim().isEmpty()) {
			return tenantId;
		}
		int dotIndex = tenantId.indexOf('.');
		return dotIndex > 0 ? tenantId.substring(0, dotIndex) : tenantId;
	}

	public static String generateUUID() {
		return UUID.randomUUID().toString();
	}

}
