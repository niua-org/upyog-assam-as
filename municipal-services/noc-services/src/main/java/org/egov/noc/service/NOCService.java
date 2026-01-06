package org.egov.noc.service;

import java.util.*;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.repository.NOCRepository;
import org.egov.noc.repository.ServiceRequestRepository;
import org.egov.noc.util.NOCConstants;
import org.egov.noc.util.NOCUtil;
import org.egov.noc.validator.NOCValidator;
import org.egov.noc.web.model.Noc;
import org.egov.noc.web.model.NocRequest;
import org.egov.noc.web.model.NocSearchCriteria;
import org.egov.noc.web.model.RequestInfoWrapper;
import org.egov.noc.web.model.Workflow;
import org.egov.noc.web.model.bpa.BPA;
import org.egov.noc.web.model.bpa.BPAResponse;
import org.egov.noc.web.model.workflow.BusinessService;
import org.egov.noc.web.model.workflow.ProcessInstance;
import org.egov.noc.web.model.workflow.ProcessInstanceResponse;
import org.egov.noc.workflow.WorkflowIntegrator;
import org.egov.noc.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;

@Service
@Slf4j
public class NOCService {
	
	@Autowired
	private NOCValidator nocValidator;
	
	@Autowired
	private WorkflowIntegrator wfIntegrator;
	
	@Autowired
	private NOCUtil nocUtil;
	
	@Autowired
	private NOCRepository nocRepository;
	
	@Autowired
	private EnrichmentService enrichmentService;
	
	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private NOCConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private MultiStateInstanceUtil centralInstanceUtil;

	@Autowired
	private FireNOCValidationService fireNOCValidationService;
	
		// Define the list of fields to copy from the external API response to additionalDetails for fire NOC
	private static final List<String> FIRE_NOC_ALLOWED_FIELDS = Arrays.asList(
			"district_fire_office",
			"fire_and_emergency_service_station_where_you_want_to_apply",
			"mobile_number_of_the_applicant",
			"type_of_occupancy",
			"select_category_of_height_of_the_building",
			"built_up_area_cum_other_parameters",
			"pan_no",
			"application_type",
			"issued_date",
			"status",
			"issued_by",
			"compliance_letter_no",
			"letter_date"
	);


	public List<Noc> createNocs(NocRequest nocRequest) {

		List<Noc> inputNoc = nocRequest.getNocList();

		if (inputNoc != null && !inputNoc.isEmpty()) {

			List<Noc> result = new ArrayList<>();

			for (Noc noc : inputNoc) {
				nocRequest.setNoc(noc);
				result.addAll(create(nocRequest));
			}

			return result;

		} else if (nocRequest.getNoc() != null) {
			// this condition is added for backward compatibility
			return create(nocRequest);
		}
		return null;
	}
	
	/**
	 * entry point from controller, takes care of next level logic from controller to create NOC application
	 * @param nocRequest
	 * @return
	 */
	public List<Noc> create(NocRequest nocRequest) {
		String tenantId = centralInstanceUtil.getStateLevelTenant(nocRequest.getNoc().getTenantId());
		Object mdmsData = nocUtil.mDMSCall(nocRequest.getRequestInfo(), tenantId);
		Map<String, String> additionalDetails = nocValidator.getOrValidateBussinessService(nocRequest.getNoc(), mdmsData);
		nocValidator.validateCreate(nocRequest,  mdmsData);
		enrichmentService.enrichCreateRequest(nocRequest, mdmsData);
		if(!ObjectUtils.isEmpty(nocRequest.getNoc().getWorkflow()) && !StringUtils.isEmpty(nocRequest.getNoc().getWorkflow().getAction())) {
		  wfIntegrator.callWorkFlow(nocRequest, additionalDetails.get(NOCConstants.WORKFLOWCODE));
		}else{
		  nocRequest.getNoc().setApplicationStatus(NOCConstants.CREATED_STATUS);
		}
		nocRepository.save(nocRequest);
		return Arrays.asList(nocRequest.getNoc());
	}
	/**
	 * entry point from controller, takes care of next level logic from controller to update NOC application
	 * @param nocRequest
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Noc> update(NocRequest nocRequest) {
		String tenantId = centralInstanceUtil.getStateLevelTenant(nocRequest.getNoc().getTenantId());
		Object mdmsData = nocUtil.mDMSCall(nocRequest.getRequestInfo(), tenantId);
		Map<String, String> additionalDetails  ;
		if(!ObjectUtils.isEmpty(nocRequest.getNoc().getAdditionalDetails()))  {
			additionalDetails = (Map) nocRequest.getNoc().getAdditionalDetails();
		} else {
			additionalDetails = nocValidator.getOrValidateBussinessService(nocRequest.getNoc(), mdmsData);
		}
		Noc searchResult = getNocForUpdate(nocRequest);
		if(searchResult.getApplicationStatus().equalsIgnoreCase("AUTO_APPROVED")
				&& nocRequest.getNoc().getApplicationStatus().equalsIgnoreCase("INPROGRESS"))
		{
			log.info("NOC_UPDATE_ERROR_AUTO_APPROVED_TO_INPROGRESS_NOTALLOWED");
			throw new CustomException("AutoApproveException","NOC_UPDATE_ERROR_AUTO_APPROVED_TO_INPROGRESS_NOTALLOWED");
		}
		nocValidator.validateUpdate(nocRequest, searchResult, additionalDetails.get(NOCConstants.MODE), mdmsData);
		enrichmentService.enrichNocUpdateRequest(nocRequest, searchResult);
		
		if(!ObjectUtils.isEmpty(nocRequest.getNoc().getWorkflow())
				&& !StringUtils.isEmpty(nocRequest.getNoc().getWorkflow().getAction())) {
		   wfIntegrator.callWorkFlow(nocRequest, additionalDetails.get(NOCConstants.WORKFLOWCODE));
		   enrichmentService.postStatusEnrichment(nocRequest, additionalDetails.get(NOCConstants.WORKFLOWCODE));
		   BusinessService businessService = workflowService.getBusinessService(nocRequest.getNoc(),
				   nocRequest.getRequestInfo(), additionalDetails.get(NOCConstants.WORKFLOWCODE));
		   if(businessService == null)
			   nocRepository.update(nocRequest, true);
		   else
			   nocRepository.update(nocRequest, workflowService.isStateUpdatable(nocRequest.getNoc().getApplicationStatus(), businessService));
		}else {
           nocRepository.update(nocRequest, Boolean.FALSE);
		}
		
		return Arrays.asList(nocRequest.getNoc());
	}
	/**
	 * entry point from controller,applies the quired fileters and encrich search criteria and
	 * return the noc application matching the search criteria
	 * @param criteria
	 * @return
	 */
//	public List<Noc> search(NocSearchCriteria criteria, RequestInfo requestInfo) {
//		/*
//		 * List<String> uuids = new ArrayList<String>();
//		 * uuids.add(requestInfo.getUserInfo().getUuid()); criteria.setAccountId(uuids);
//		 */
//		BPASearchCriteria bpaCriteria = new BPASearchCriteria();
//		ArrayList<String> sourceRef = new ArrayList<String>();
//		List<Noc> nocs = new ArrayList<Noc>();
//
//		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
//		StringBuilder uri = new StringBuilder(config.getBpaHost()).append(config.getBpaContextPath())
//				.append(config.getBpaSearchEndpoint());
//		if (criteria.getMobileNumber() != null) {
//
//			uri.append("?tenantId=").append(criteria.getTenantId());
//
//			if (criteria.getSourceRefId() != null)
//			{
//				uri.append("&applicationNo=").append(criteria.getSourceRefId());
//				uri.append("&mobileNumber=").append(criteria.getMobileNumber());
//			}else
//			{
//				uri.append("&mobileNumber=").append(criteria.getMobileNumber());
//			}
//			log.info("BPA CALL STARTED");
//			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, requestInfoWrapper);
//			BPAResponse bpaResponse = mapper.convertValue(responseMap, BPAResponse.class);
//			List<BPA> bpas = bpaResponse.getBPA();
//			Map<String, String> bpaDetails = new HashMap<String, String>();
//			bpas.forEach(bpa -> {
//				bpaDetails.put("applicantName", bpa.getLandInfo().getOwners().get(0).getName());
//				bpaDetails.put("sourceRef", bpa.getApplicationNo());
//				sourceRef.add(bpa.getApplicationNo());
//			});
//			if (!sourceRef.isEmpty()) {
//				criteria.setSourceRefId(sourceRef.toString());
//			}
//			if(criteria.getMobileNumber() != null && CollectionUtils.isEmpty(bpas)){
//				return nocs;
//			}
//			log.info("NOC CALL STARTED" + criteria.getSourceRefId());
//			nocs = nocRepository.getLimitedNocData(criteria);
//			nocs.forEach(noc -> {
//				Map<String, String> additionalDetails = noc.getAdditionalDetails() != null
//						? (Map<String, String>) noc.getAdditionalDetails()
//						: new HashMap<String, String>();
//				for (BPA bpa : bpas) {
//					if (bpa.getApplicationNo().equals(noc.getSourceRefId())) {
//						additionalDetails.put("applicantName", bpa.getLandInfo().getOwners().get(0).getName());
//					}
//				}
///*				StringBuilder url = new StringBuilder(config.getWfHost());
//				url.append(config.getWfProcessPath());
//				url.append("?businessIds=");
//				url.append(noc.getApplicationNo());
//				url.append("&tenantId=");
//				url.append(noc.getTenantId());
//
//				log.info("Process CALL STARTED" + url);
//				Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
//				ProcessInstanceResponse response = null;
//				try {
//					response = mapper.convertValue(result, ProcessInstanceResponse.class);
//				} catch (IllegalArgumentException e) {
//					throw new CustomException(NOCConstants.PARSING_ERROR, "Failed to parse response of Workflow");
//				}
//				if(response.getProcessInstances()!=null && !response.getProcessInstances().isEmpty()) {
//					ProcessInstance nocProcess = response.getProcessInstances().get(0);
//					if (nocProcess.getAssignee() != null) {
//						additionalDetails.put("currentOwner", nocProcess.getAssignee().getName());
//					} else {
//						additionalDetails.put("currentOwner", null);
//					}
//				} else {
//					additionalDetails.put("currentOwner", null);
//				}*/
//			});
//
//		}else if(criteria.getApplicationNo()!=null){
////			All details should come when criteria contains application no
//			log.info("IN 2 NOC CALL STARTED" + criteria.getSourceRefId());
//			nocs = nocRepository.getNocData(criteria);
//			nocs.forEach(noc -> {
//				Map<String, String> additionalDetails = noc.getAdditionalDetails() != null
//						? (Map<String, String>) noc.getAdditionalDetails()
//						: new HashMap<String, String>();
//
//				// BPA CALL
//				uri.append("?tenantId=").append(noc.getTenantId());
//				uri.append("&applicationNo=").append(noc.getSourceRefId());
//				System.out.println("BPA CALL STARTED");
//				LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
//						requestInfoWrapper);
//				BPAResponse bpaResponse = mapper.convertValue(responseMap, BPAResponse.class);
//				List<BPA> bpaList = new ArrayList<BPA>();
//				bpaList = bpaResponse.getBPA();
//				bpaList.forEach(bpa -> {
//					additionalDetails.put("applicantName", bpa.getLandInfo().getOwners().get(0).getName());
//				});
//				log.info("ADDITIONAL DETAILS :: " + additionalDetails.get("applicantName"));
//				// PROCESS CALL
//				StringBuilder url = new StringBuilder(config.getWfHost());
//				url.append(config.getWfProcessPath());
//				url.append("?businessIds=");
//				url.append(noc.getApplicationNo());
//				url.append("&tenantId=");
//				url.append(noc.getTenantId());
//
//				log.info("Process 2 CALL STARTED" + url);
//				Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
//				ProcessInstanceResponse response = null;
//				try {
//					response = mapper.convertValue(result, ProcessInstanceResponse.class);
//				} catch (IllegalArgumentException e) {
//					throw new CustomException(NOCConstants.PARSING_ERROR, "Failed to parse response of Workflow");
//				}
//				log.info("ProcessInstance :: " + response.getProcessInstances());
//				if(response.getProcessInstances()!=null && !response.getProcessInstances().isEmpty()) {
//					ProcessInstance nocProcess = response.getProcessInstances().get(0);
//					if (nocProcess.getAssignee() != null) {
//						additionalDetails.put("currentOwner", nocProcess.getAssignee().getName());
//					} else {
//						additionalDetails.put("currentOwner", null);
//					}
//				}else {
//					additionalDetails.put("currentOwner", null);
//				}
//				log.info("ADDITIONAL DETAILS :: " + additionalDetails.get("currentOwner"));
//			});
//		}
//		else {
////			Limited details should come when criteria does not contain application no
//			log.info("IN 3 NOC CALL STARTED" + criteria.getSourceRefId());
//			nocs = nocRepository.getLimitedNocData(criteria);
////			nocs.forEach(noc -> {
////				Map<String, String> additionalDetails = noc.getAdditionalDetails() != null
////						? (Map<String, String>) noc.getAdditionalDetails()
////						: new HashMap<String, String>();
////
////				// BPA CALL
////				uri.append("?tenantId=").append(noc.getTenantId());
////				uri.append("&applicationNo=").append(noc.getSourceRefId());
////				System.out.println("BPA CALL STARTED");
////				LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
////						requestInfoWrapper);
////				BPAResponse bpaResponse = mapper.convertValue(responseMap, BPAResponse.class);
////				List<BPA> bpaList = new ArrayList<BPA>();
////				bpaList = bpaResponse.getBPA();
////				bpaList.forEach(bpa -> {
////					additionalDetails.put("applicantName", bpa.getLandInfo().getOwners().get(0).getName());
////				});
////				log.info("ADDITIONAL DETAILS :: " + additionalDetails.get("applicantName"));
////
////			});
//		}
//		return nocs.isEmpty() ? Collections.emptyList() : nocs;
//	}
	
	/**
	 * Fetch the noc based on the id to update the NOC record
	 * @param nocRequest
	 * @return
	 */
	public Noc getNocForUpdate(NocRequest nocRequest) {		
		List<String> ids = Arrays.asList(nocRequest.getNoc().getId());
		NocSearchCriteria criteria = new NocSearchCriteria();
		criteria.setTenantId(nocRequest.getNoc().getTenantId());
		criteria.setIds(ids);
		List<Noc> nocList = search(criteria, nocRequest.getRequestInfo());
		if (CollectionUtils.isEmpty(nocList) ) {
			StringBuilder builder = new StringBuilder();
			builder.append("Noc Application not found for: ").append(nocRequest.getNoc().getId()).append(" :ID");
			throw new CustomException("INVALID_NOC_SEARCH", builder.toString());
		}else if( nocList.size() > 1) {
			StringBuilder builder = new StringBuilder();
			builder.append("Multiple Noc Application(s) not found for: ").append(nocRequest.getNoc().getId()).append(" :ID");
			throw new CustomException("INVALID_NOC_SEARCH", builder.toString());
		}
		return nocList.get(0);
	}
	
	/**
         * entry point from controller,applies the quired fileters and encrich search criteria and
         * return the noc application count the search criteria
	 * @param criteria
	 * @param requestInfo
	 * @return Integer count of NOC applications
         */
        public Integer getNocCount(NocSearchCriteria criteria, RequestInfo requestInfo) {
                /*List<String> uuids = new ArrayList<String>();
                uuids.add(requestInfo.getUserInfo().getUuid());
                criteria.setAccountId(uuids);*/
                return nocRepository.getNocCount(criteria);
        }
        
		/**
		 * Fetches newly created AAI NOC applications for given tenant
		 * If tenantId is null, fetches from default tenant 'as'
		 *
		 * @param tenantId Tenant ID or null for default
		 * @return List of NOC records in CREATED status
		 */
		public List<Noc> fetchNewAAINOCs(String tenantId) {
			NocSearchCriteria criteria = new NocSearchCriteria();
			criteria.setApplicationStatus(NOCConstants.APPLICATION_STATUS_INPROGRESS);
			criteria.setNocType(NOCConstants.CIVIL_AVIATION_NOC_TYPE);
			criteria.setTenantId(tenantId);
			criteria.setLimit(config.getMaxSearchLimit());
			criteria.setOffset(0);
			return nocRepository.getNocDatav2(criteria);
		}

		/**
		 * Retrieves BPA details for each NOC by calling the BPA service.
		 *
		 * @param nocList            list of NOC applications
		 * @param requestInfoWrapper request info for service call
		 * @return list of BPA objects mapped from service responses
		 */
		public List<BPA> getBPADetails(List<Noc> nocList, RequestInfoWrapper requestInfoWrapper) {

			List<BPA> bpaList = new ArrayList<>();

			for (Noc noc : nocList) {

				StringBuilder uri = new StringBuilder(config.getBpaHost());
				uri.append(config.getBpaContextPath());
				uri.append(config.getBpaSearchEndpoint());
				uri.append("?tenantId=").append(noc.getTenantId());
				uri.append("&applicationNo=").append(noc.getSourceRefId());

				Object result = serviceRequestRepository.fetchResult(uri, requestInfoWrapper);
				try {
					BPAResponse bpaResponse = mapper.convertValue(result, BPAResponse.class);
					List<BPA> bpa = bpaResponse.getBPA();
					bpaList.addAll(bpa);
				} catch (IllegalArgumentException e) {
					throw new CustomException(NOCConstants.PARSING_ERROR, "Failed to parse response of BPA");
				}
			}
			return bpaList;
		}



	/**
	 * Validates a Fire NOC by verifying the NOC number (ARN) against an external Fire NOC API.
	 * <p>
	 * The process includes:
	 * 1. Validating input constraints.
	 * 2. Searching for the existing NOC application in the local DB.
	 * 3. Calling the external Fire NOC service for validation.
	 * 4. Merging external details into the local application's additional details.
	 * 5. Triggering the Workflow to move the application to 'APPROVED'.
	 * </p>
	 *
	 * @param nocRequest The request object containing the Source Ref ID (BPA Application No) and Fire NOC ARN.
	 * @return A list containing the validated and updated NOC application.
	 * @throws CustomException If validation fails, NOC is not found, or multiple NOCs exist.
	 */
	@SuppressWarnings("unchecked")
	public List<Noc> validateNocs(@Valid NocRequest nocRequest) {
		// 1. Input Validation
		Noc inputNoc = nocRequest.getNoc();
		if (inputNoc == null) {
			throw new CustomException("INVALID_REQUEST", "NOC object is required for validation");
		}

		String sourceRefId = inputNoc.getSourceRefId();
		String fireNocNumber = inputNoc.getNocNo();

		if (StringUtils.isEmpty(sourceRefId)) {
			throw new CustomException("INVALID_SOURCE_REF_ID", "Source reference ID (BPA application number) is required");
		}
		if (StringUtils.isEmpty(fireNocNumber)) {
			throw new CustomException("INVALID_NOC_NUMBER", "Fire NOC number (nocNo) is required for validation");
		}

		// 2. Fetch Existing NOC
		NocSearchCriteria criteria = getFireNocSearchCriteria(inputNoc.getTenantId(), sourceRefId);
		List<Noc> existingNocs = nocRepository.getNocData(criteria);

		if (CollectionUtils.isEmpty(existingNocs)) {
			throw new CustomException("NOC_NOT_FOUND", "NOC not found for sourceRefId: " + sourceRefId);
		}
		if (existingNocs.size() > 1) {
			throw new CustomException("MULTIPLE_NOCS_FOUND", "Multiple NOCs found for sourceRefId: " + sourceRefId);
		}

		Noc existingNoc = existingNocs.get(0);

		// Check if already approved to ensure idempotency
		if (NOCConstants.APPROVED_STATE.equalsIgnoreCase(existingNoc.getApplicationStatus())) {
			log.info("NOC_ALREADY_APPROVED - sourceRefId: {}, fireNocNumber: {}", sourceRefId, fireNocNumber);
			return existingNocs;
		}

		log.info("Validating Fire NOC - applicationNo: {}, sourceRefId: {}, fireNocNumber: {}",
				existingNoc.getApplicationNo(), sourceRefId, fireNocNumber);

		// 3. External API Validation
		Map<String, Object> validationResponse = fireNOCValidationService.validateFireNOC(fireNocNumber);

		boolean validationStatus = Boolean.TRUE.equals(validationResponse.get("status"));
		String validationMessage = (String) validationResponse.getOrDefault("message", "Fire NOC validation failed");

		if (!validationStatus) {
			throw new CustomException("FIRE_NOC_VALIDATION_FAILED", validationMessage);
		}

		// 4. Update Additional Details
		Map<String, Object> additionalDetails = Optional.ofNullable((Map<String, Object>) existingNoc.getAdditionalDetails())
				.orElse(new HashMap<>());

		Map<String, Object> externalNocDetails = (Map<String, Object>) validationResponse.get("noc_details");

		// Helper method to map specific fields
		enrichFireNocDetails(additionalDetails, externalNocDetails);

		additionalDetails.put("validation_status", validationStatus);
		additionalDetails.put("validation_message", validationMessage);

		existingNoc.setNocNo(fireNocNumber);
		existingNoc.setAdditionalDetails(additionalDetails);

		// 5. Workflow Integration
		Workflow workflow = Workflow.builder()
				.action(NOCConstants.ACTION_APPROVE)
				.build();
		existingNoc.setWorkflow(workflow);

		NocRequest updateRequest = NocRequest.builder()
				.noc(existingNoc)
				.requestInfo(nocRequest.getRequestInfo())
				.build();

		// Call Workflow to transitions state
		wfIntegrator.callWorkFlow(updateRequest, NOCConstants.FIRE_NOC_WORKFLOW_CODE);
		existingNoc.setApplicationStatus(NOCConstants.APPROVED_STATE);

		// Update Database
		BusinessService businessService = workflowService.getBusinessService(existingNoc,
				nocRequest.getRequestInfo(), NOCConstants.FIRE_NOC_WORKFLOW_CODE);

		boolean isStateUpdatable = businessService == null ||
				workflowService.isStateUpdatable(existingNoc.getApplicationStatus(), businessService);

		nocRepository.update(updateRequest, isStateUpdatable);

		log.info("NOC validated and approved successfully - applicationNo: {}, nocNo: {}",
				existingNoc.getApplicationNo(), existingNoc.getNocNo());

		return Collections.singletonList(existingNoc);
	}

	/**
	 * Helper method to map allowed fields from the external API response to the local additional details map.
	 * This avoids repetitive null checks and manual put operations.
	 *
	 * @param targetDetails The map where data needs to be saved (existing NOC additionalDetails).
	 * @param sourceDetails The map received from the external API (noc_details).
	 */
	private void enrichFireNocDetails(Map<String, Object> targetDetails, Map<String, Object> sourceDetails) {
		if (CollectionUtils.isEmpty(sourceDetails)) {
			return;
		}

		for (String fieldKey : FIRE_NOC_ALLOWED_FIELDS) {
			if (sourceDetails.containsKey(fieldKey)) {
				targetDetails.put(fieldKey, sourceDetails.get(fieldKey));
			}
		}
	}

	/**
	 * Constructs the search criteria for Fire NOCs.
	 */
	private NocSearchCriteria getFireNocSearchCriteria(String tenantId, String sourceRefId) {
		if (StringUtils.isEmpty(tenantId)) {
			throw new CustomException("INVALID_TENANT_ID", "Tenant ID is required");
		}

		// NocNumber and SourceRefId are already validated in the main method
		NocSearchCriteria criteria = new NocSearchCriteria();
		criteria.setSourceRefId(sourceRefId);
		criteria.setTenantId(tenantId);
		criteria.setNocType(NOCConstants.FIRE_SAFETY_NOC_TYPE);
		return criteria;
	}

	/**
	 * Search NOC applications based on criteria, with enrichment from BPA and Workflow services.
	 * @param criteria   The search criteria containing filters like applicationNo, mobileNumber, tenantId, etc.
	 * @param requestInfo The request info containing user and auth details.
	 * @return List of NOC applications matching the criteria, enriched with BPA owner names and workflow assignees.
	 * */
	public List<Noc> search(NocSearchCriteria criteria, RequestInfo requestInfo) {
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		List<Noc> nocs;

		// 1.  If mobile number provided, find linked BPA application numbers first
		if (criteria.getMobileNumber() != null) {
			List<BPA> bpas = fetchBpasByMobile(criteria, requestInfoWrapper);
			if (CollectionUtils.isEmpty(bpas)) {
				log.info("No BPAs found for mobile number: {}", criteria.getMobileNumber());
				return Collections.emptyList();
			}
			// Extract Application Numbers to search in NOC Repository
			List<String> sourceRefIds = bpas.stream().map(BPA::getApplicationNo).collect(Collectors.toList());
			// Join list into comma-separated string as NocSearchCriteria expects String, not List
			criteria.setSourceRefId(String.join(",", sourceRefIds));
		}
		final boolean isFullSearch = criteria.getApplicationNo() != null;
		// 2. Db Search: Full or Limited based on presence of applicationNo
		if (isFullSearch) {
			log.info("Full Details NOC Search for: {}", criteria.getApplicationNo());
			nocs = nocRepository.getNocData(criteria);
		} else {
			log.info("Limited Details NOC Search (Basic Fields)");
			nocs = nocRepository.getLimitedNocData(criteria);
		}

		if (CollectionUtils.isEmpty(nocs)) return Collections.emptyList();

		// 3. BATCH ENRICHMENT: Collect IDs for external service calls
		List<String> bpaAppNos = nocs.stream().map(Noc::getSourceRefId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
		List<String> nocAppNos = nocs.stream().map(Noc::getApplicationNo).distinct().collect(Collectors.toList());

		// 4. Fetch Bpa Owner Names
		// Fetch Owner Names from BPA (Required in all cases)
		Map<String, String> bpaOwnerMap = fetchBpaOwnerMap(bpaAppNos, criteria.getTenantId(), requestInfoWrapper);

		// Fetch Workflow details (logic only calls if applicationNo was provided)
		final Map<String, String> wfAssigneeMap = criteria.getApplicationNo() != null
				? fetchWorkflowAssigneeMap(nocAppNos, criteria.getTenantId(), requestInfoWrapper)
				: Collections.emptyMap();

		// 5. MAPPING: Populate additionalDetails
		
		nocs.forEach(noc -> {
			@SuppressWarnings("unchecked")
			Map<String, Object> additionalDetails = noc.getAdditionalDetails() != null
					? (Map<String, Object>) noc.getAdditionalDetails() : new HashMap<>();

			// Set Owner Name (Satisfies "owner name in every case")
			additionalDetails.put("applicantName", bpaOwnerMap.getOrDefault(noc.getSourceRefId(), "NA"));

			// Set Workflow Owner (Only if full search)
			if (isFullSearch) {
				additionalDetails.put("currentOwner", wfAssigneeMap.get(noc.getApplicationNo()));
			}

			noc.setAdditionalDetails(additionalDetails);
		});

		return nocs;
	}

	/**
	 *
	 * Batch Fetch BPAs by mobile number
	 * @param criteria
	 * @param wrapper
	 * @return List of BPA
	 */
	@SuppressWarnings("unchecked")
	private List<BPA> fetchBpasByMobile(NocSearchCriteria criteria, RequestInfoWrapper wrapper) {
		String uri = config.getBpaHost() + config.getBpaContextPath() + config.getBpaSearchEndpoint()
				+ "?tenantId=" + criteria.getTenantId()
				+ "&mobileNumber=" + criteria.getMobileNumber();

		LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) serviceRequestRepository.fetchResult(new StringBuilder(uri), wrapper);
		BPAResponse response = mapper.convertValue(responseMap, BPAResponse.class);
		return response.getBPA();
	}

	/**
	 * Batch fetch Owner Names from BPA (Fixes URI corruption)
	 * @param bpaNos
	 * @param tenantId
	 * @param wrapper
	 * @return Map of BPA No to Owner Name
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String> fetchBpaOwnerMap(List<String> bpaNos, String tenantId, RequestInfoWrapper wrapper) {
		if (CollectionUtils.isEmpty(bpaNos)) return Collections.emptyMap();

		StringBuilder uri = new StringBuilder(config.getBpaHost())
				.append(config.getBpaContextPath())
				.append(config.getBpaSearchEndpoint())
				.append("?tenantId=").append(tenantId)
				.append("&applicationNos=").append(String.join(",", bpaNos));

		try {
			LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) serviceRequestRepository.fetchResult(uri, wrapper);
			BPAResponse response = mapper.convertValue(responseMap, BPAResponse.class);

			return response.getBPA().stream().collect(Collectors.toMap(BPA::getApplicationNo,
					bpa -> (bpa.getLandInfo() != null && !bpa.getLandInfo().getOwners().isEmpty())
							? bpa.getLandInfo().getOwners().get(0).getName() : "NA",
					(existing, replacement) -> existing));

		} catch (Exception e) {
			log.error("Error fetching BPA owner names", e);
			return Collections.emptyMap();
		}
	}

	/**
	 * Batch fetch Workflow Assignees
	 * @param nocNos
	 * @param tenantId
	 * @param wrapper
	 * @return Map of NOC No to Assignee Name
	 */
	private Map<String, String> fetchWorkflowAssigneeMap(List<String> nocNos, String tenantId, RequestInfoWrapper wrapper) {
		if (CollectionUtils.isEmpty(nocNos)) return Collections.emptyMap();

		StringBuilder url = new StringBuilder(config.getWfHost())
				.append(config.getWfProcessPath())
				.append("?businessIds=").append(String.join(",", nocNos))
				.append("&tenantId=").append(tenantId);

		Object result = serviceRequestRepository.fetchResult(url, wrapper);
		ProcessInstanceResponse response = mapper.convertValue(result, ProcessInstanceResponse.class);

		return response.getProcessInstances().stream().collect(Collectors.toMap(
				ProcessInstance::getBusinessId,
				pi -> pi.getAssignee() != null ? pi.getAssignee().getName() : "NA",
				(a, b) -> a
		));
	}
}
