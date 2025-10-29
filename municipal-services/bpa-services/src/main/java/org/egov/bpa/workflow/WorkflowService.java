package org.egov.bpa.workflow;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAErrorConstants;
import org.egov.bpa.web.model.*;
import org.egov.bpa.web.model.workflow.BusinessService;
import org.egov.bpa.web.model.workflow.BusinessServiceResponse;
import org.egov.bpa.web.model.workflow.State;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WorkflowService {

	private BPAConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private ObjectMapper mapper;


	// Custom key class for (Planning, Building) combination
	@AllArgsConstructor
	@EqualsAndHashCode
	private static class AuthorityKey {
		private final PlanningPermitAuthorityEnum planning;
		private final BuildingPermitAuthorityEnum building;
	}


	// Map from AuthorityKey -> Business Service String
	private static final Map<AuthorityKey, String> BUSINESS_SERVICE_MAP = new HashMap<>();
	static {
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DEVELOPMENT_AUTHORITY, BuildingPermitAuthorityEnum.MUNICIPAL_BOARD), "BPA_DA_MB");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DEVELOPMENT_AUTHORITY, BuildingPermitAuthorityEnum.GRAM_PANCHAYAT), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TACP, BuildingPermitAuthorityEnum.GRAM_PANCHAYAT), "BPA_TACP_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.GMDA, BuildingPermitAuthorityEnum.GMC), "BPA_GMDA_GMC");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.GMDA, BuildingPermitAuthorityEnum.NGMB), "BPA_GMDA_NGMB");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.GMDA, BuildingPermitAuthorityEnum.GRAM_PANCHAYAT), "BPA_GMDA_GP");
	}

	@Autowired
	public WorkflowService(BPAConfiguration config, ServiceRequestRepository serviceRequestRepository,
			ObjectMapper mapper) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
	}

	/**
	 * Get the workflow config for the given tenant
	 * 
	 * @param tenantId
	 *            The tenantId for which businessService is requested
	 * @param requestInfo
	 *            The RequestInfo object of the request
	 * @return BusinessService for the the given tenantId
	 */
	public BusinessService getBusinessService(BPA bpa, RequestInfo requestInfo, String applicationNo) {
		StringBuilder url = getSearchURLWithParams(bpa, true, null);
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		BusinessServiceResponse response = null;
		try {
			response = mapper.convertValue(result, BusinessServiceResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException(BPAErrorConstants.PARSING_ERROR, "Failed to parse response of calculate");
		}
		return response.getBusinessServices().get(0);
	}

	/**
	 * Creates url for search based on given tenantId
	 *
	 * @param tenantId
	 *            The tenantId for which url is generated
	 * @return The search url
	 */
	private StringBuilder getSearchURLWithParams(BPA bpa, boolean businessService, String applicationNo) {
		StringBuilder url = new StringBuilder(config.getWfHost());
		if (businessService) {
			url.append(config.getWfBusinessServiceSearchPath());
		} else {
			url.append(config.getWfProcessPath());
		}
		url.append("?tenantId=");
		url.append(bpa.getTenantId());
		if (businessService) {
				url.append("&businessServices=");
				url.append(bpa.getBusinessService());
		} else {
			url.append("&businessIds=");
			url.append(applicationNo);
		}
		return url;
	}

	/**
	 * Returns boolean value to specifying if the state is updatable
	 * 
	 * @param statusEnum
	 *            The stateCode of the bpa
	 * @param businessService
	 *            The BusinessService of the application flow
	 * @return State object to be fetched
	 */
	public Boolean isStateUpdatable(String status, BusinessService businessService) {
		for (org.egov.bpa.web.model.workflow.State state : businessService.getStates()) {
			if (state.getApplicationStatus() != null
					&& state.getApplicationStatus().equalsIgnoreCase(status.toString()))
				return state.getIsStateUpdatable();
		}
		return Boolean.FALSE;
	}

	/**
	 * Returns State name fo the current state of the document
	 * 
	 * @param statusEnum
	 *            The stateCode of the bpa
	 * @param businessService
	 *            The BusinessService of the application flow
	 * @return State String to be fetched
	 */
	public String getCurrentState(String status, BusinessService businessService) {
		for (State state : businessService.getStates()) {
			if (state.getApplicationStatus() != null
					&& state.getApplicationStatus().equalsIgnoreCase(status.toString()))
				return state.getState();
		}
		return null;
	}

	/**
	 * Returns State Obj fo the current state of the document
	 * 
	 * @param statusEnum
	 *            The stateCode of the bpa
	 * @param businessService
	 *            The BusinessService of the application flow
	 * @return State object to be fetched
	 */
	public State getCurrentStateObj(String status, BusinessService businessService) {
		for (State state : businessService.getStates()) {
			if (state.getApplicationStatus() != null
					&& state.getApplicationStatus().equalsIgnoreCase(status.toString()))
				return state;
		}
		return null;
	}


	/**
	 * Determining the business service based on the planning and building permit authorities.
	 * This method uses a predefined mapping to find the correct business service.
	 *
	 * @param areaMappingDetail The AreaMappingDetail containing the permit authorities.
	 * @return The determined business service or null if no valid combination is found.
	 */
	public String determineBusinessService(AreaMappingDetail areaMappingDetail) {
		PlanningPermitAuthorityEnum planning = areaMappingDetail.getPlanningPermitAuthority();
		BuildingPermitAuthorityEnum building = areaMappingDetail.getBuildingPermitAuthority();

		/* TODO: Temporary condition added as modules is developed for GMDA and GMC condition only
		if (!PlanningPermitAuthorityEnum.GMDA.equals(planning) || !BuildingPermitAuthorityEnum.GMC.equals(building)) {
			log.info("Workflow not configured for the PlanningAuthority: {} and BuildingAuthority: {}", planning, building);
			throw new CustomException(BPAErrorConstants.WORKFLOW_NOT_CONFIGURED,
					"Workflow not configured for the PlanningAuthority: " + planning +
							" and BuildingAuthority: " + building);
		} */

		log.debug("Evaluating business service with PlanningAuthority: {} and BuildingAuthority: {}", planning, building);

		String result = BUSINESS_SERVICE_MAP.get(new AuthorityKey(planning, building));

		if (result != null) {
			log.info("Matched business service: {}", result);
			return result;
		} else {
			log.warn("No valid combination found for PlanningAuthority: {} and BuildingAuthority: {}", planning, building);
			throw new CustomException(BPAErrorConstants.WORKFLOW_NOT_CONFIGURED,
					"Workflow not configured for the PlanningAuthority: " + planning +
							" and BuildingAuthority: " + building);
		}
	}
}
