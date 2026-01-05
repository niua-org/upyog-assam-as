package org.egov.bpa.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.IdGenRepository;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAErrorConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.validator.MDMSValidator;
import org.egov.bpa.web.model.AuditDetails;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.Workflow;
import org.egov.bpa.web.model.edcr.RequestInfoWrapper;
import org.egov.bpa.web.model.idgen.IdResponse;
import org.egov.bpa.web.model.workflow.BusinessService;
import org.egov.bpa.workflow.WorkflowIntegrator;
import org.egov.bpa.workflow.WorkflowService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.egov.tracer.model.ServiceCallException;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Service
@Slf4j
public class EnrichmentService {

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private BPAUtil bpaUtil;

	@Autowired
	private IdGenRepository idGenRepository;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private EDCRService edcrService;

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private NocService nocService;

	@Autowired
	private BPAUtil util;

	@Autowired
	private UserService userService;

	@Autowired
	private MDMSValidator mdmsValidator;
	
	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private MultiStateInstanceUtil centralInstanceUtil;
	/**
	 * encrich create BPA Reqeust by adding audidetails and uuids
	 *
	 * @param bpaRequest
	 * @param edcrValues
	 */
	public void enrichBPACreateRequest(BPARequest bpaRequest, Map<String, String> edcrValues) {
		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		AuditDetails auditDetails = bpaUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		bpaRequest.getBPA().setAuditDetails(auditDetails);
		bpaRequest.getBPA().setId(UUID.randomUUID().toString());
		bpaRequest.getBPA().getAreaMapping().setId(UUID.randomUUID().toString());
		if(bpaRequest.getBPA().getRtpDetails() != null){
			//TODO:move this uuid generation to util method
			bpaRequest.getBPA().getRtpDetails().setId(UUID.randomUUID().toString());
		}
        Map<String, String> additionalDetails = bpaRequest.getBPA().getAdditionalDetails() != null
                ? (Map<String, String>) bpaRequest.getBPA().getAdditionalDetails()
                : new HashMap<String, String>();

		bpaRequest.getBPA().setAccountId(bpaRequest.getBPA().getAuditDetails().getCreatedBy());
	//	String applicationType = values.get(BPAConstants.APPLICATIONTYPE);

	/*if (applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN)) {
		
			if (!bpaRequest.getBPA().getRiskType().equalsIgnoreCase(BPAConstants.LOW_RISKTYPE)) {
				bpaRequest.getBPA().setBusinessService(BPAConstants.BPA_MODULE_CODE);
			} else {
				bpaRequest.getBPA().setBusinessService(BPAConstants.BPA_LOW_MODULE_CODE);
			}
		} else {
			bpaRequest.getBPA().setBusinessService(BPAConstants.BPA_OC_MODULE_CODE);
			bpaRequest.getBPA().setLandId(values.get("landId"));
		}*/
		String businessService = workflowService.determineBusinessService(bpaRequest.getBPA().getAreaMapping());
		bpaRequest.getBPA().setBusinessService(businessService);

        if (bpaRequest.getBPA().getRiskType() != null) {
            additionalDetails.put(BPAConstants.RISKTYPE, bpaRequest.getBPA().getRiskType());
        }

//		 BPA Documents
		if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getDocuments()))
			bpaRequest.getBPA().getDocuments().forEach(document -> {
				//TODO uncomment this if this check required in future
			//	if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
			//	}
			});
		setIdgenIds(bpaRequest);
	}

	/**
	 * Sets the ApplicationNumber for given bpaRequest
	 *
	 * @param request bpaRequest which is to be created
	 */
	private void setIdgenIds(BPARequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = util.extractState(request.getBPA().getTenantId());
		BPA bpa = request.getBPA();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNoIdgenName(),
				null, 1);
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		bpa.setApplicationNo(itr.next());
	}

	/**
	 * Returns a list of numbers generated from idgen
	 *
	 * @param requestInfo RequestInfo from the request
	 * @param tenantId    tenantId of the city
	 * @param idKey       code of the field defined in application properties for
	 *                    which ids are generated for
	 * @param idformat    format in which ids are to be generated
	 * @param count       Number of ids to be generated
	 * @return List of ids generated using idGen service
	 */
	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException(BPAErrorConstants.IDGEN_ERROR, "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}

	/**
	 * enchrich the updateRequest
	 *
	 * @param bpaRequest
	 * @param businessService
	 */
	public void enrichBPAUpdateRequest(BPARequest bpaRequest, BusinessService businessService) {

		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		AuditDetails auditDetails = bpaUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
		auditDetails.setCreatedBy(bpaRequest.getBPA().getAuditDetails().getCreatedBy());
		auditDetails.setCreatedTime(bpaRequest.getBPA().getAuditDetails().getCreatedTime());
		bpaRequest.getBPA().getAuditDetails().setLastModifiedTime(auditDetails.getLastModifiedTime());
		enrichAssignes(bpaRequest.getBPA());
		// BPA Documents
		if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getDocuments()))
			bpaRequest.getBPA().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		// BPA WfDocuments
		if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getWorkflow().getVarificationDocuments())) {
			bpaRequest.getBPA().getWorkflow().getVarificationDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		}

	}

	/**
	 * postStatus encrichment to update the status of the workflow to the
	 * application and generating permit and oc number when applicable
	 *
	 * @param bpaRequest
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void postStatusEnrichment(BPARequest bpaRequest) {
		BPA bpa = bpaRequest.getBPA();
		String tenantId =  centralInstanceUtil.getStateLevelTenant(bpaRequest.getBPA().getTenantId());
		Object mdmsData = util.mDMSCall(bpaRequest.getRequestInfo(), tenantId);

		BusinessService businessService = workflowService.getBusinessService(bpa, bpaRequest.getRequestInfo(),
				bpa.getApplicationNo());
		log.info("Application status is : " + bpa.getStatus());
		String state = workflowService.getCurrentState(bpa.getStatus(), businessService);

		if (state.equalsIgnoreCase(BPAConstants.DOCVERIFICATION_STATE)) {
			bpa.setApplicationDate(util.getCurrentTimestampMillis());
		}

		if (StringUtils.isEmpty(bpa.getRiskType())) {
			if (bpa.getBusinessService().equals(BPAConstants.BPA_LOW_MODULE_CODE)) {
				bpa.setRiskType(BPAConstants.LOW_RISKTYPE);
			} else {
				Map<String, List<String>> masterData = mdmsValidator.getAttributeValuesForState(mdmsData);
				StringBuilder uri = new StringBuilder(config.getEdcrHost());
				uri.append(config.getGetPlanEndPoint());
				uri.append("?").append("tenantId=").append(BPAConstants.BPA_ASSAM);
				uri.append("&").append("edcrNumber=").append(bpa.getEdcrNumber());
				org.egov.bpa.web.model.edcr.RequestInfo edcrRequestInfo = new org.egov.bpa.web.model.edcr.RequestInfo();
				
				BeanUtils.copyProperties(bpaRequest.getRequestInfo(), edcrRequestInfo);
				
				LinkedHashMap responseMap = null;
				
				try {
					responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri,
							new RequestInfoWrapper(edcrRequestInfo));
				} catch (ServiceCallException se) {
					throw new CustomException(BPAErrorConstants.EDCR_ERROR, " EDCR Number is Invalid");
				}

				if (CollectionUtils.isEmpty(responseMap))
					throw new CustomException(BPAErrorConstants.EDCR_ERROR, "The response from EDCR service is empty or null");
				String jsonString = new JSONObject(responseMap).toString();
			
				DocumentContext context = JsonPath.using(Configuration.defaultConfiguration()).parse(jsonString);
				Object plot = context.read("edcrDetail[0].planDetail.planInformation.plotArea");
				Double	plotArea = Double.valueOf(String.valueOf(plot));
				Object bldgHgt = context.read("edcrDetail[0].planDetail.blocks[0].building.buildingHeight");
				Double	buildingHeight = Double.valueOf(String.valueOf(bldgHgt));

			
				List jsonOutput = JsonPath.read(masterData, BPAConstants.RISKTYPE_COMPUTATION);
				String filterExp = "$.[?((@.fromPlotArea < " + plotArea + " && @.toPlotArea >= " + plotArea
						+ ") || ( @.fromBuildingHeight < " + buildingHeight + "  &&  @.toBuildingHeight >= "
						+ buildingHeight + "  ))].riskType";

				List<String> riskTypes = JsonPath.read(jsonOutput, filterExp);
				                                                                                     
				if (!CollectionUtils.isEmpty(riskTypes)) {
					String	expectedRiskType  = riskTypes.get(0);
					bpa.setRiskType(expectedRiskType);
				}else
				{
					throw new CustomException(BPAErrorConstants.INVALID_RISK_TYPE, "The Risk Type is not valid " );
				}
				
				
				
				
			}
		}

		log.info("Application state is : " + state);
		this.generateApprovalNo(bpaRequest, state);
		nocService.initiateNocWorkflow(bpaRequest, mdmsData);

	}

	/**
	 * generate the permit and oc number on approval status of the BPA and BPAOC
	 * respectively
	 *
	 * @param bpaRequest
	 * @param state
	 */
	private void generateApprovalNo(BPARequest bpaRequest, String state) {
		BPA bpa = bpaRequest.getBPA();
		if ((bpa.getBusinessService().equalsIgnoreCase(BPAConstants.BPA_OC_MODULE_CODE)
				&& bpa.getStatus().equalsIgnoreCase(BPAConstants.APPROVED_STATE))
				|| (bpa.getStatus().equalsIgnoreCase(BPAConstants.APPROVED_STATE))
				|| (!bpa.getBusinessService().equalsIgnoreCase(BPAConstants.BPA_OC_MODULE_CODE)
				&& ((!bpa.getRiskType().toString().equalsIgnoreCase(BPAConstants.LOW_RISKTYPE)
				&& state.equalsIgnoreCase(BPAConstants.APPROVED_STATE))
				|| (state.equalsIgnoreCase(BPAConstants.DOCVERIFICATION_STATE) && bpa.getRiskType()
				.toString().equalsIgnoreCase(BPAConstants.LOW_RISKTYPE))))) {
			int vailidityInMonths = config.getValidityInMonths();
			Calendar calendar = Calendar.getInstance();
			bpa.setApprovalDate(util.getCurrentTimestampMillis());

			// Adding 3years (36 months) to Current Date
			calendar.add(Calendar.MONTH, vailidityInMonths);
			Map<String, Object> additionalDetail = null;
			if (bpa.getAdditionalDetails() != null) {
				additionalDetail = (Map) bpa.getAdditionalDetails();
			} else {
				additionalDetail = new HashMap<String, Object>();
				bpa.setAdditionalDetails(additionalDetail);
			}

			additionalDetail.put("validityDate", calendar.getTimeInMillis());

			String tenantId = util.extractState(bpa.getTenantId());
			List<IdResponse> idResponses = idGenRepository.getId(bpaRequest.getRequestInfo(), tenantId,
					config.getPermitNoIdgenName(), null, 1).getIdResponses();
			bpa.setApprovalNo(idResponses.get(0).getId());
			if (state.equalsIgnoreCase(BPAConstants.DOCVERIFICATION_STATE)
					&& bpa.getRiskType().toString().equalsIgnoreCase(BPAConstants.LOW_RISKTYPE)) {

				Object mdmsData = bpaUtil.mDMSCall(bpaRequest.getRequestInfo(), bpaRequest.getBPA().getTenantId());
				Map<String, String> edcrResponse = new HashMap<>();
				
			    edcrResponse = edcrService.getEDCRDetails(bpaRequest.getRequestInfo(),
							bpaRequest.getBPA());
			     
				log.debug("applicationType is " + edcrResponse.get(BPAConstants.APPLICATIONTYPE));
				log.debug("serviceType is " + edcrResponse.get(BPAConstants.SERVICETYPE));

				String condeitionsPath = BPAConstants.CONDITIONS_MAP.replace("{1}", BPAConstants.PENDING_APPROVAL_STATE)
						.replace("{2}", bpa.getRiskType().toString())
						.replace("{3}", edcrResponse.get(BPAConstants.SERVICETYPE))
						.replace("{4}", edcrResponse.get(BPAConstants.APPLICATIONTYPE));
				log.debug(condeitionsPath);

				try {
					List<String> conditions = (List<String>) JsonPath.read(mdmsData, condeitionsPath);
					log.debug(conditions.toString());
					if (bpa.getAdditionalDetails() == null) {
						bpa.setAdditionalDetails(new HashMap());
					}
					Map additionalDetails = (Map) bpa.getAdditionalDetails();
					additionalDetails.put(BPAConstants.PENDING_APPROVAL_STATE.toLowerCase(), conditions.get(0));

				} catch (Exception e) {
					log.warn("No approval conditions found for the application " + bpa.getApplicationNo());
				}
			}
		}
	}

	/**
	 * handles the skippayment of the BPA when demand is zero
	 *
	 * @param bpaRequest
	 */
	public void skipPayment(BPARequest bpaRequest) {
		BPA bpa = bpaRequest.getBPA();
		BigDecimal demandAmount = bpaUtil.getDemandAmount(bpaRequest);
		if (!(demandAmount.compareTo(BigDecimal.ZERO) > 0)) {
			Workflow workflow = Workflow.builder().action(BPAConstants.ACTION_SKIP_PAY).build();
			bpa.setWorkflow(workflow);
			wfIntegrator.callWorkFlow(bpaRequest);
		}
	}

	/**
	 * In case of SENDBACKTOCITIZEN enrich the assignee with the owners and creator
	 * of BPA
	 *
	 * @param bpa BPA to be enriched
	 */
	public void enrichAssignes(BPA bpa) {
		Workflow wf = bpa.getWorkflow();
		Map<String,String> mobilenumberToUUIDs = new HashMap<>();
		Set<String> assignes = new HashSet<>();
		if (wf != null && wf.getAssignes() != null)
			assignes.addAll(wf.getAssignes());
		if (wf != null && (wf.getAction().equalsIgnoreCase(BPAConstants.ACTION_SENDBACKTOCITIZEN)
				|| wf.getAction().equalsIgnoreCase(BPAConstants.ACTION_SEND_TO_CITIZEN))) {

			// Adding owners to assignes list
			bpa.getLandInfo().getOwners().forEach(ownerInfo -> {
			        if(ownerInfo.getUuid() != null && ownerInfo.getActive()) {
							mobilenumberToUUIDs.put(ownerInfo.getMobileNumber(),ownerInfo.getUuid());
					}
			});

			Set<String> registeredUUIDS = userService.getUUidFromUserName(bpa,mobilenumberToUUIDs);

			if (!CollectionUtils.isEmpty(registeredUUIDS))
				assignes.addAll(registeredUUIDS);

		} else if (wf != null && (wf.getAction().equalsIgnoreCase(BPAConstants.ACTION_SEND_TO_ARCHITECT)
				|| (bpa.getStatus().equalsIgnoreCase(BPAConstants.STATUS_CITIZEN_APPROVAL_INPROCESS)
				&& wf.getAction().equalsIgnoreCase(BPAConstants.ACTION_APPROVE)))) {
			// Adding creator of BPA(Licensee)
			if (bpa.getAccountId() != null)
				assignes.add(bpa.getAccountId());
		}
		if (bpa.getWorkflow() == null) {
			Workflow wfNew = new Workflow();
			wfNew.setAssignes(new LinkedList<>(assignes));
			bpa.setWorkflow(wfNew);
		} else {
			bpa.getWorkflow().setAssignes(new LinkedList<>(assignes));
		}
	}

	public boolean enrichFileStoreIdsForBPAUpdate(BPA bpa, BPA existingBPA) {

		Map<String, Object> additionalDetails = bpa.getAdditionalDetails() instanceof Map
				? (Map<String, Object>) bpa.getAdditionalDetails()
				: null;

		if (additionalDetails == null || !additionalDetails.containsKey(BPAConstants.UPDATE_FILESTORE_ID)) {
			return false;
		}

		// Mark update initiated
		additionalDetails.remove(BPAConstants.UPDATE_FILESTORE_ID);
		log.info("FileStoreId update initiated from workflow for BPA: {}", bpa.getApplicationNo());
		log.info("additionalDetails after key removal: {}", additionalDetails);

		// Update filestore IDs only if new value is present and existing value is empty
		updateFileStoreId(existingBPA::setPpFileStoreId, existingBPA.getPpFileStoreId(), bpa.getPpFileStoreId());
		updateFileStoreId(existingBPA::setBpFileStoreId, existingBPA.getBpFileStoreId(), bpa.getBpFileStoreId());
		updateFileStoreId(existingBPA::setOcFileStoreId, existingBPA.getOcFileStoreId(), bpa.getOcFileStoreId());
		updateFileStoreId(existingBPA::setPpFeeReceiptFileStoreId, existingBPA.getPpFeeReceiptFileStoreId(),bpa.getPpFeeReceiptFileStoreId());
		updateFileStoreId(existingBPA::setBpFeeReceiptFileStoreId, existingBPA.getBpFeeReceiptFileStoreId(),bpa.getBpFeeReceiptFileStoreId());
		
		//update signed file store ids
		updateFileStoreId(existingBPA::setSignedOcFileStoreId, existingBPA.getSignedOcFileStoreId(), bpa.getSignedOcFileStoreId());
		updateFileStoreId(existingBPA::setSignedPpFileStoreId, existingBPA.getSignedPpFileStoreId(),bpa.getSignedPpFileStoreId());
		updateFileStoreId(existingBPA::setSignedBpFileStoreId, existingBPA.getSignedBpFileStoreId(),bpa.getSignedBpFileStoreId());

		return true;
	}

	private void updateFileStoreId(Consumer<String> setter, String existingValue, String newValue) {
		if (StringUtils.isNotEmpty(newValue) && StringUtils.isEmpty(existingValue)) {
			setter.accept(newValue);
		}
	}

	public void updatePlanningPermitNo(BPARequest bpaRequest) {
		bpaRequest.getBPA().setPlanningPermitNo(getPlanningPermitNo(bpaRequest));
		bpaRequest.getBPA().setPlanningPermitDate(util.getCurrentTimestampMillis());
		log.info("Planning Permit No. generated : " + bpaRequest.getBPA().getPlanningPermitNo());
	}

	public void updateBuildingPermitNo(BPARequest bpaRequest) {
		bpaRequest.getBPA().setBuildingPermitNo(getBuildingPermitNo(bpaRequest));
		bpaRequest.getBPA().setBuildingPermitDate(util.getCurrentTimestampMillis());
		log.info("Building Permit No. generated : " + bpaRequest.getBPA().getBuildingPermitNo());
	}

	public void updateOccupancyCertificateNo(BPARequest bpaRequest) {
		bpaRequest.getBPA().setOccupancyCertificateNo(getOccupancyCertificateNo(bpaRequest));
		bpaRequest.getBPA().setOccupancyCertificateDate(util.getCurrentTimestampMillis());
		log.info("Occupancy Certificate No. generated : " + bpaRequest.getBPA().getOccupancyCertificateNo());
	}

	// Generate Occupancy Certificate Number
	private String getOccupancyCertificateNo(BPARequest bpaRequest) {

		String tenantId = util.extractState(bpaRequest.getBPA().getTenantId());

		List<IdResponse> idResponses = idGenRepository
				.getId(bpaRequest.getRequestInfo(), tenantId, config.getOccupancyCertificateIdgenName(), null, 1)
				.getIdResponses();

		if (idResponses == null || idResponses.isEmpty()) {
			throw new CustomException("IDGEN_ERROR", "Occupancy Certificate Number could not be generated.");
		}

		return idResponses.get(0).getId();
	}

	// Generate Occupancy Certificate Number
	private String getPlanningPermitNo(BPARequest bpaRequest) {

		String tenantId = util.extractState(bpaRequest.getBPA().getTenantId());

		List<IdResponse> idResponses = idGenRepository
				.getId(bpaRequest.getRequestInfo(), tenantId, config.getPlanningPermitIdgenName(), null, 1)
				.getIdResponses();

		if (idResponses == null || idResponses.isEmpty()) {
			throw new CustomException("IDGEN_ERROR", "Planning Permit Number could not be generated.");
		}

		return idResponses.get(0).getId();
	}
	// Generate Building Permit Number
	private String getBuildingPermitNo(BPARequest bpaRequest) {

		String tenantId = util.extractState(bpaRequest.getBPA().getTenantId());

		List<IdResponse> idResponses = idGenRepository
				.getId(bpaRequest.getRequestInfo(), tenantId, config.getBuildingPermitIdgenName(), null, 1)
				.getIdResponses();

		if (idResponses == null || idResponses.isEmpty()) {
			throw new CustomException("IDGEN_ERROR", "Building Permit Number could not be generated.");
		}

		return idResponses.get(0).getId();
	}

}
