package org.egov.noc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.contract.request.User;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.repository.ServiceRequestRepository;
import org.egov.noc.web.model.AuditDetails;
import org.egov.noc.web.model.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NOCUtil {

	private NOCConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	public NOCUtil(NOCConfiguration config, ServiceRequestRepository serviceRequestRepository) {
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
	 * prepares the MDMSCriteria to make MDMS Request
	 * @param requestInfo
	 * @param tenantId
	 * @return
	 */
	public MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId) {
		List<ModuleDetail> moduleRequest = getNOCModuleRequest();

		List<ModuleDetail> moduleDetails = new LinkedList<>();
		moduleDetails.addAll(moduleRequest);

		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId).build();

		MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria).requestInfo(requestInfo)
				.build();
		return mdmsCriteriaReq;
	}
	/**
	 * fetches the noc documentTypes and nocTypes mdms data
	 * @return
	 */
	public List<ModuleDetail> getNOCModuleRequest() {
		List<MasterDetail> nocMasterDtls = new ArrayList<>();

//		final String nocFilterCode = "$.[*].nocs[?(@.isActive==true)]";

		nocMasterDtls.add(MasterDetail.builder().name(NOCConstants.NOC_TYPE).build());
		nocMasterDtls.add(MasterDetail.builder().name(NOCConstants.NOC_DOC_TYPE_MAPPING).build());
		ModuleDetail nocModuleDtls = ModuleDetail.builder().masterDetails(nocMasterDtls)
				.moduleName(NOCConstants.NOC_MODULE).build();
		
		final String filterCode = "$.[?(@.active==true)]";

//		List<MasterDetail> commonMasterDetails = new ArrayList<>();
//			commonMasterDetails.add(MasterDetail.builder().name(NOCConstants.DOCUMENT_TYPE).filter(filterCode).build());
//		ModuleDetail commonMasterMDtl = ModuleDetail.builder().masterDetails(commonMasterDetails)
//				.moduleName(NOCConstants.COMMON_MASTERS_MODULE).build();

		return Arrays.asList(nocModuleDtls);
	}	

	/**
	 * prepares MDMS call 
	 * @param requestInfo
	 * @param tenantId
	 * @return
	 */
	public Object mDMSCall(RequestInfo requestInfo, String tenantId) {
		MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId);
		Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
		return result;
	}

	public RequestInfoWrapper createDefaultRequestInfo() {

		// Build RequestInfo
		RequestInfo requestInfo = new RequestInfo();
		requestInfo.setApiId("Rainmaker");
		requestInfo.setAuthToken("0f78601f-835d-4b0b-8321-58cccb5bd3cf");
		requestInfo.setMsgId("1762511663946|en_IN");
//		requestInfo.setPlainAccessRequest(new HashMap<>()); // {}

		// Build UserInfo
		User user = new User();
		user.setId(95L);
		user.setUuid("427a327d-a70a-490e-8447-66827cba169a");
		user.setUserName("7000000002");
		user.setName("Nikhil");
		user.setMobileNumber("7000000002");
		user.setEmailId("nikhil@gmail.com");
//	    user.setLocale(null);
		user.setType("CITIZEN");
//	    user.setActive(true);
		user.setTenantId("pg");
//	    user.setPermanentCity(null);

		// Build roles
		List<Role> roles = new ArrayList<>();

		Role citizenRole = new Role();
		citizenRole.setName("Citizen");
		citizenRole.setCode("CITIZEN");
		citizenRole.setTenantId("pg");
		roles.add(citizenRole);

		Role architectRole = new Role();
		architectRole.setName("BPA Architect");
		architectRole.setCode("BPA_ARCHITECT");
		architectRole.setTenantId("pg");
		roles.add(architectRole);

		user.setRoles(roles);

		// Attach userInfo
		requestInfo.setUserInfo(user);

		// Wrap into RequestInfoWrapper
		RequestInfoWrapper wrapper = new RequestInfoWrapper();
		wrapper.setRequestInfo(requestInfo);

		return wrapper;
	}
}