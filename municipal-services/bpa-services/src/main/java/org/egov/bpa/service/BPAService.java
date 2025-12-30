package org.egov.bpa.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.validation.Valid;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.BPARepository;
import org.egov.bpa.repository.IdGenRepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAErrorConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.util.NotificationUtil;
import org.egov.bpa.validator.BPAValidator;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.bpa.web.model.CalculationReq;
import org.egov.bpa.web.model.CalulationCriteria;
import org.egov.bpa.web.model.Floor;
import org.egov.bpa.web.model.Workflow;
import org.egov.bpa.web.model.idgen.IdResponse;
import org.egov.bpa.web.model.landInfo.LandInfo;
import org.egov.bpa.web.model.landInfo.LandSearchCriteria;
import org.egov.bpa.web.model.user.UserDetailResponse;
import org.egov.bpa.web.model.user.UserSearchRequest;
import org.egov.bpa.web.model.workflow.BusinessService;
import org.egov.bpa.workflow.ActionValidator;
import org.egov.bpa.workflow.WorkflowIntegrator;
import org.egov.bpa.workflow.WorkflowService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Service
@Slf4j
public class BPAService {


    @Autowired
    private WorkflowIntegrator wfIntegrator;

    @Autowired
    private EnrichmentService enrichmentService;

    @Autowired
    private EDCRService edcrService;

    @Autowired
    private BPARepository repository;

    @Autowired
    private ActionValidator actionValidator;

    @Autowired
    private BPAValidator bpaValidator;

    @Autowired
    private BPAUtil util;

    @Autowired
    private CalculationService calculationService;

    @Autowired
    private WorkflowService workflowService;

	@Autowired
	private IdGenRepository idGenRepository;

    @Autowired
    private NotificationUtil notificationUtil;

    @Autowired
    private BPALandService landService;

    @Autowired
    private OCService ocService;

    @Autowired
    private UserService userService;

    @Autowired
    private NocService nocService;

    @Autowired
    private BPAConfiguration config;

    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;
    
    @Autowired
    private MdmsCacheService mdmsCacheService;

    /**
     * does all the validations required to create BPA Record in the system
     *
     * @param bpaRequest
     * @return
     */
    public BPA create(BPARequest bpaRequest) {
      //  Map<String, String> values = new HashMap<>();
        RequestInfo requestInfo = bpaRequest.getRequestInfo();
        String stateTenantId = centralInstanceUtil.getStateLevelTenant(bpaRequest.getBPA().getTenantId());
        String tenantId = bpaRequest.getBPA().getAreaMapping().getConcernedAuthority();
        
        // Get MDMS Data for request validation
        Object mdmsTenantData = mdmsCacheService.getMdmsData(requestInfo, tenantId);
        
        Object mdmsStateData = mdmsCacheService.getMdmsData(requestInfo, stateTenantId);

        LinkedHashMap<String, Object> edcr = new LinkedHashMap<>();
        if (centralInstanceUtil.isTenantIdStateLevel(bpaRequest.getBPA().getTenantId())) {
            throw new CustomException(BPAErrorConstants.INVALID_TENANT, "Application cannot be create at StateLevel");
        }

        //Since approval number should be generated at approve stage
        if (!StringUtils.isEmpty(bpaRequest.getBPA().getApprovalNo())) {
            bpaRequest.getBPA().setApprovalNo(null);
        }
		bpaRequest.getBPA().setApplicationDate(util.getCurrentTimestampMillis());

      //  values = edcrService.validateEdcrPlan(bpaRequest, mdmsData);

     //   String applicationType = values.get(BPAConstants.APPLICATIONTYPE);
       // String serviceType = values.get(BPAConstants.SERVICETYPE);
       // this.validateCreateOC(applicationType, values, requestInfo, bpaRequest);

		bpaValidator.validateStateMdmsData(bpaRequest, mdmsStateData);
        bpaValidator.validateMdmsData(bpaRequest, mdmsTenantData);
       

        landService.addLandInfoToBPA(bpaRequest);
        enrichmentService.enrichBPACreateRequest(bpaRequest, null);

        wfIntegrator.callWorkFlow(bpaRequest);

     //   this.addCalculation(applicationType, bpaRequest);

        log.info("bpaRequest before create : " + String.valueOf(bpaRequest.getBPA().getApplicationNo()) + "---"
                + String.valueOf(bpaRequest.getBPA().getAdditionalDetails()));
        repository.save(bpaRequest);
        return bpaRequest.getBPA();
    }

    /**
     * applies the required vlaidation for OC on Create
     *
     * @param applicationType
     * @param values
     * @param requestInfo
     * @param bpaRequest
     */
    private void validateCreateOC(String applicationType, Map<String, String> values, RequestInfo requestInfo, BPARequest bpaRequest) {

        if (applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN_OC)) {
            String approvalNo = values.get(BPAConstants.PERMIT_NO);

            BPASearchCriteria criteria = new BPASearchCriteria();
            criteria.setTenantId(bpaRequest.getBPA().getTenantId());
            criteria.setApprovalNo(approvalNo);
            List<BPA> ocBpas = search(criteria, requestInfo);

            if (ocBpas.size() <= 0 || ocBpas.size() > 1) {
                throw new CustomException(BPAErrorConstants.CREATE_ERROR,
                        ((ocBpas.size() <= 0) ? "BPA not found with approval Number :" : "Multiple BPA applications found for approval number :") + approvalNo);
            } else if (ocBpas.get(0).getStatus().equalsIgnoreCase(BPAConstants.STATUS_REVOCATED)) {
                throw new CustomException(BPAErrorConstants.CREATE_ERROR, "This permit number is revocated you cannot use this permit number");
            } else if (!ocBpas.get(0).getStatus().equalsIgnoreCase(BPAConstants.STATUS_APPROVED)) {
                throw new CustomException(BPAErrorConstants.CREATE_ERROR, "The selected permit number still in workflow approval process, Please apply occupancy after completing approval process.");
            }

            values.put("landId", ocBpas.get(0).getLandId());
            criteria.setEdcrNumber(ocBpas.get(0).getEdcrNumber());
            ocService.validateAdditionalData(bpaRequest, criteria);
            bpaRequest.getBPA().setLandInfo(ocBpas.get(0).getLandInfo());
        }
    }

    /**
     * Searches the Bpa for the given criteria if search is on owner paramter
     * then first user service is called followed by query to db
     *
     * @param criteria    The object containing the parameters on which to search
     * @param requestInfo The search request's requestInfo
     * @return List of bpa for the given criteria
     */
    public List<BPA> search(BPASearchCriteria criteria, RequestInfo requestInfo) {
        List<BPA> bpas = new LinkedList<>();
        bpaValidator.validateSearch(requestInfo, criteria);
        LandSearchCriteria landcriteria = new LandSearchCriteria();
        landcriteria.setTenantId(criteria.getTenantId());
        landcriteria.setLocality(criteria.getLocality());
        List<String> edcrNos = null;
        if (criteria.getMobileNumber() != null) {
            bpas = this.getBPAFromMobileNumber(criteria, landcriteria, requestInfo);
        }else if (criteria.getName() != null) {
            bpas = this.getBPAFromApplicantName(criteria, landcriteria, requestInfo);
        } else {
            List<String> roles = new ArrayList<>();
            for (Role role : requestInfo.getUserInfo().getRoles()) {
                roles.add(role.getCode());
            }
            if ((criteria.tenantIdOnly() || criteria.isEmpty()) && roles.contains(BPAConstants.CITIZEN)) {
                log.debug("loading data of created and by me");
                bpas = this.getBPACreatedForByMe(criteria, requestInfo, landcriteria, edcrNos);
                log.debug("no of bpas retuning by the search query" + bpas.size());
            } else {
                bpas = getBPAFromCriteria(criteria, requestInfo, edcrNos);
                ArrayList<String> landIds = new ArrayList<>();
                if (!bpas.isEmpty()) {

                    /*
                     * Filter landids that are not null*/
                    landIds = bpas.stream().map(BPA::getLandId).
                            filter(Objects::nonNull).distinct().
                            collect(java.util.stream.Collectors.toCollection(ArrayList::new));
                    log.info("land ids for bpa application : {}", landIds);
                    if(landIds.isEmpty()){
                        return bpas;
                    }

                    landcriteria.setIds(landIds);
                    if(requestInfo != null && requestInfo.getUserInfo() != null) {
                        boolean isRTP = requestInfo.getUserInfo().getRoles().stream()
                                .anyMatch(role -> role.getCode().equalsIgnoreCase(BPAConstants.BPA_ARCHITECT_ROLE));
                        if(isRTP) {
                            landcriteria.setTenantId(requestInfo.getUserInfo().getTenantId());
                        }else {
                            landcriteria.setTenantId(bpas.get(0).getTenantId());
                        }
                    }
                    log.debug("Call with tenantId to Land::" + landcriteria.getTenantId());
                    landcriteria.setIsInboxSearch(criteria.getIsInboxSearch());
                    ArrayList<LandInfo> landInfos = landService.searchLandInfoToBPA(requestInfo, landcriteria);

                    this.populateLandToBPA(bpas, landInfos, requestInfo);
                }
            }
        }
        return bpas;
    }

    /**
     * In case of landId is not present in BPA then try to fetch from additional details
     *
     * @param bpas - list of bpas application
     */

    /**
     * search the BPA records created by and create for the logged in User
     *
     * @param criteria
     * @param requestInfo
     * @param landcriteria
     * @param edcrNos
     */
    private List<BPA> getBPACreatedForByMe(BPASearchCriteria criteria, RequestInfo requestInfo, LandSearchCriteria landcriteria, List<String> edcrNos) {
        List<BPA> bpas = null;
        UserSearchRequest userSearchRequest = new UserSearchRequest();
        if (criteria.getTenantId() != null) {
            userSearchRequest.setTenantId(criteria.getTenantId());
        }
        List<String> uuids = new ArrayList<>();
        if (requestInfo.getUserInfo() != null && !StringUtils.isEmpty(requestInfo.getUserInfo().getUuid())) {
            uuids.add(requestInfo.getUserInfo().getUuid());
            criteria.setOwnerIds(uuids);
            criteria.setCreatedBy(uuids);
        }
        log.debug("loading data of created and by me" + uuids.toString());
        UserDetailResponse userInfo = userService.getUser(criteria, requestInfo);
        if (userInfo != null) {
            landcriteria.setMobileNumber(userInfo.getUser().get(0).getMobileNumber());
        }
        log.debug("Call with multiple to Land::" + landcriteria.getTenantId() + landcriteria.getMobileNumber());
        ArrayList<LandInfo> landInfos = landService.searchLandInfoToBPA(requestInfo, landcriteria);
        ArrayList<String> landIds = new ArrayList<>();
        if (!landInfos.isEmpty()) {
            landInfos.forEach(land -> landIds.add(land.getId()));
            criteria.setLandId(landIds);
        }

        bpas = getBPAFromCriteria(criteria, requestInfo, edcrNos);
        log.debug("no of bpas queried" + bpas.size());
        this.populateLandToBPA(bpas, landInfos, requestInfo);
        return bpas;
    }

    /**
     * populate appropriate landInfo to BPA
     *
     * @param bpas
     * @param landInfos
     */
    private void populateLandToBPA(List<BPA> bpas, List<LandInfo> landInfos, RequestInfo requestInfo) {
        for (int i = 0; i < bpas.size(); i++) {
            for (int j = 0; j < landInfos.size(); j++) {
                if (landInfos.get(j).getId().equalsIgnoreCase(bpas.get(i).getLandId())) {
                    bpas.get(i).setLandInfo(landInfos.get(j));
                }
            }
            if (bpas.get(i).getLandId() != null && bpas.get(i).getLandInfo() == null) {
                LandSearchCriteria missingLandcriteria = new LandSearchCriteria();
                List<String> missingLandIds = new ArrayList<>();
                missingLandIds.add(bpas.get(i).getLandId());
                missingLandcriteria.setTenantId(bpas.get(0).getTenantId());
                missingLandcriteria.setIds(missingLandIds);
                log.debug("Call with land ids to Land::" + missingLandcriteria.getTenantId() + missingLandcriteria.getIds());
                List<LandInfo> newLandInfo = landService.searchLandInfoToBPA(requestInfo, missingLandcriteria);
                for (int j = 0; j < newLandInfo.size(); j++) {
                    if (newLandInfo.get(j).getId().equalsIgnoreCase(bpas.get(i).getLandId())) {
                        bpas.get(i).setLandInfo(newLandInfo.get(j));
                    }
                }
            }
        }
    }

    /**
     * search the land with mobile number and then BPA from the land
     *
     * @param criteria
     * @param landcriteria
     * @param requestInfo
     * @return
     */
    private List<BPA> getBPAFromMobileNumber(BPASearchCriteria criteria, LandSearchCriteria landcriteria, RequestInfo requestInfo) {
        List<BPA> bpas = new LinkedList<>();

        log.info("Call with mobile number to Land::" + criteria.getMobileNumber());
        landcriteria.setMobileNumber(criteria.getMobileNumber());
        ArrayList<LandInfo> landInfo = landService.searchLandInfoToBPA(requestInfo, landcriteria);
        ArrayList<String> landId = new ArrayList<>();
        if (!landInfo.isEmpty()) {
            landInfo.forEach(land -> landId.add(land.getId()));
            criteria.setLandId(landId);
        }

        String tenantId = criteria.getTenantId();
        if (landInfo.isEmpty() && !tenantId.isEmpty() && tenantId != null) {
            return bpas;
        }

        bpas = getBPAFromLandId(criteria, requestInfo, null);
        if (!landInfo.isEmpty()) {
            for (int i = 0; i < bpas.size(); i++) {
                for (int j = 0; j < landInfo.size(); j++) {
                    if (landInfo.get(j).getId().equalsIgnoreCase(bpas.get(i).getLandId())) {
                        bpas.get(i).setLandInfo(landInfo.get(j));
                    }
                }
            }
        }
        return bpas;
    }


    private List<BPA> getBPAFromLandId(BPASearchCriteria criteria, RequestInfo requestInfo, List<String> edcrNos) {
        List<BPA> bpa = new LinkedList<>();
        bpa = repository.getBPAData(criteria, edcrNos);
        if (bpa.size() == 0) {
            return Collections.emptyList();
        }
        return bpa;
    }


    // Search the Land from name and then from BPA after extracting teh land id
    private List<BPA> getBPAFromApplicantName(BPASearchCriteria criteria, LandSearchCriteria landcriteria, RequestInfo requestInfo) {
        List<BPA> bpas = new LinkedList<>();
        log.info("Call with name to Land::" + criteria.getName());
        landcriteria.setName(criteria.getName());
        ArrayList<LandInfo> landInfo = landService.searchLandInfoToBPA(requestInfo, landcriteria);
        ArrayList<String> landId = new ArrayList<>();
        if (!landInfo.isEmpty()) {
            landInfo.forEach(land -> landId.add(land.getId()));
            criteria.setLandId(landId);
        }

        String tenantId = criteria.getTenantId();
        if (landInfo.isEmpty() && !tenantId.isEmpty() && tenantId != null) {
            return bpas;
        }

        bpas = getBPAFromLandId(criteria, requestInfo, null);
        if (!landInfo.isEmpty()) {
            for (int i = 0; i < bpas.size(); i++) {
                for (int j = 0; j < landInfo.size(); j++) {
                    if (landInfo.get(j).getId().equalsIgnoreCase(bpas.get(i).getLandId())) {
                        bpas.get(i).setLandInfo(landInfo.get(j));
                    }
                }
            }
        }
        return bpas;
    }



    /**
     * Returns the bpa with enriched owners from user service
     *
     * @param criteria    The object containing the parameters on which to search
     * @param requestInfo The search request's requestInfo
     * @return List of bpa for the given criteria
     */
    public List<BPA> getBPAFromCriteria(BPASearchCriteria criteria, RequestInfo requestInfo, List<String> edcrNos) {
        List<BPA> bpa = repository.getBPAData(criteria, edcrNos);
        if (bpa.isEmpty())
            return Collections.emptyList();
        return bpa;
    }

    /**
     * Updates the bpa
     *
     * @param bpaRequest The update Request
     * @return Updated bpa
     */
    @SuppressWarnings("unchecked")
    public BPA update(BPARequest bpaRequest) {
        RequestInfo requestInfo = bpaRequest.getRequestInfo();
        
        String stateTenantId = centralInstanceUtil.getStateLevelTenant(bpaRequest.getBPA().getTenantId());
        String tenantId = bpaRequest.getBPA().getAreaMapping().getConcernedAuthority();

        // Get MDMS Data for request validation
        // Get MDMS Data for request validation
        Object mdmsTenantData = mdmsCacheService.getMdmsData(requestInfo, tenantId);
        
        Object mdmsStateData = mdmsCacheService.getMdmsData(requestInfo, stateTenantId);
        // Validate the update request
        bpaValidator.validateStateMdmsData(bpaRequest, mdmsStateData);
        bpaValidator.validateMdmsData(bpaRequest, mdmsTenantData);

        BPA bpa = bpaRequest.getBPA();

    //    String businessServices = bpaRequest.getBPA().getBusinessService();
     //   Map<String, String> edcrResponse = new HashMap<>();

        if (bpa.getId() == null) {
            throw new CustomException(BPAErrorConstants.UPDATE_ERROR, "Application Not found in the System to Update");
        }

     //   edcrResponse = edcrService.getEDCRDetails(bpaRequest.getRequestInfo(), bpaRequest.getBPA());

     //   String applicationType = edcrResponse.get(BPAConstants.APPLICATIONTYPE);
     //   log.debug("applicationType is " + applicationType);

        BusinessService businessService = workflowService.getBusinessService(bpa, bpaRequest.getRequestInfo(),
                bpa.getApplicationNo());

        List<BPA> searchResult = getBPAWithBPAId(bpaRequest);
        if (CollectionUtils.isEmpty(searchResult) || searchResult.size() > 1) {
            throw new CustomException(BPAErrorConstants.UPDATE_ERROR, "Failed to Update the Application, Found None or multiple applications!");
        }

        // Handle RTP details update without workflow (only for citizens)
        BPA existingBPA = searchResult.get(0);

        //Handle file store id for update of documents like Planning permit, Building permit, Occupancy certificate
        boolean updateRequired = enrichmentService.enrichFileStoreIdsForBPAUpdate(bpa, existingBPA);
        if(updateRequired){
            log.info("File store ids updated for application no: {}", bpa.getApplicationNo());
            repository.update(bpaRequest, BPAConstants.UPDATE);
            return bpaRequest.getBPA();
        }

        /* RTP details can be updated without workflow only when all the below conditions are met:
         * 1. The existing application should not have RTP details as null
         * 2. The incoming application should not have RTP details as null
         * 3. The RTP UUID of existing and incoming application should be different
         * 4. The action in workflow should be null or empty
         * 5. The role of the logged in user should be CITIZEN
         */

		bpaRequest.getBPA().setAuditDetails(searchResult.get(0).getAuditDetails());

		String action = Optional.ofNullable(bpa.getWorkflow()).map(Workflow::getAction).orElse("");
		boolean isRtpChanged = existingBPA.getRtpDetails() != null && bpa.getRtpDetails() != null
				&& !Objects.equals(existingBPA.getRtpDetails().getRtpUUID(), bpa.getRtpDetails().getRtpUUID());

		if (isRtpChanged) {
			action = "RTP_IS_CHANGED";
		}

		switch (action.toUpperCase()) {

		case "RTP_IS_CHANGED":
            actionValidator.validateActionForRTPUpdateWithoutWorkflowUpdate(bpaRequest, action);

			reassignRTP(bpaRequest);
			log.info("RTP details updated successfully without workflow for citizen application: {}",
					bpa.getApplicationNo());
			break;

		case "EDIT":
			enrichmentService.enrichBPAUpdateRequest(bpaRequest, null);
			wfIntegrator.callWorkFlow(bpaRequest);
			repository.update(bpaRequest, BPAConstants.UPDATE_ALL_BUILDING_PLAN);
			landService.updateLandInfo(bpaRequest);
			break;

		case "SUBMIT_REPORT":
//			Object mdmsData = util.mDMSCall(requestInfo, tenantId);
			bpaRequest.getBPA().setPlanningPermitNo(getPlanningPermitNo(bpaRequest));
			bpaRequest.getBPA().setPlanningPermitDate(util.getCurrentTimestampMillis());
			log.info("Planning Permit No. generated : " + bpaRequest.getBPA().getPlanningPermitNo());

			nocService.createNocRequest(bpaRequest, mdmsStateData);
			enrichmentService.enrichBPAUpdateRequest(bpaRequest, businessService);
            calculationService.addCalculation(bpaRequest, "PLANNING_PERMIT_FEE");
			wfIntegrator.callWorkFlow(bpaRequest);
			repository.update(bpaRequest, BPAConstants.UPDATE);
			break;

		case "PAY":// CITIZEN_FINAL_PAYMENT
			bpaRequest.getBPA().setBuildingPermitNo(getBuildingPermitNo(bpaRequest));
			bpaRequest.getBPA().setBuildingPermitDate(util.getCurrentTimestampMillis());
			log.info("Building Permit No. generated : " + bpaRequest.getBPA().getBuildingPermitNo());
			// TODO: TO_BE_CHANGED and add oc certificate no generation
			bpaRequest.getBPA().setOccupancyCertificateNo(getOccupancyCertificateNo(bpaRequest));
			bpaRequest.getBPA().setOccupancyCertificateDate(util.getCurrentTimestampMillis());
			log.info("Occupancy Certificate No. generated : " + bpaRequest.getBPA().getOccupancyCertificateNo());

			enrichmentService.enrichBPAUpdateRequest(bpaRequest, businessService);
			wfIntegrator.callWorkFlow(bpaRequest);
			repository.update(bpaRequest, BPAConstants.UPDATE);
			break;

		default:
			enrichmentService.enrichBPAUpdateRequest(bpaRequest, businessService);
			wfIntegrator.callWorkFlow(bpaRequest);
			repository.update(bpaRequest, BPAConstants.UPDATE);
			break;
		}

        if ("PENDING_CHAIRMAN_PRESIDENT_MB".equalsIgnoreCase(bpaRequest.getBPA().getStatus())
                || "PENDING_CHAIRMAN_PRESIDENT_GP".equalsIgnoreCase(bpaRequest.getBPA().getStatus())) {
        	calculationService.addCalculation(bpaRequest, "BUILDING_PERMIT_FEE");
        }
		return bpaRequest.getBPA();

      //  Map<String, String> additionalDetails = bpa.getAdditionalDetails() != null ? (Map<String, String>) bpa.getAdditionalDetails()
     //           : new HashMap<String, String>();

    //    if (bpa.getStatus().equalsIgnoreCase(BPAConstants.FI_STATUS)
      //          && bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_SENDBACKTOCITIZEN)) {
        //    if (additionalDetails.get(BPAConstants.FI_ADDITIONALDETAILS) != null)
          //      additionalDetails.remove(BPAConstants.FI_ADDITIONALDETAILS);
       // }
    //TODO: check if required as we dont have oc to manage in this current scope of work

    //    this.processOcUpdate(applicationType, edcrResponse.get(BPAConstants.PERMIT_NO), bpaRequest, requestInfo, additionalDetails);

//TODO: check if required as we dont have noc to manage in this current scope of work
      //  nocService.manageOfflineNocs(bpaRequest, mdmsData);
        //Validate payments and noc
       // bpaValidator.validatePreEnrichData(bpaRequest, mdmsData);

       // this.handleRejectSendBackActions(applicationType, bpaRequest, businessService, searchResult, mdmsData, edcrResponse);
    //    String state = workflowService.getCurrentState(bpa.getStatus(), businessService);
     //   String businessSrvc = businessService.getBusinessService();

        /*
         * Before approving the application we need to check sanction fee is applicable
         * or not for that purpose on PENDING_APPROVAL_STATE the demand is generating.
         */
        // Generate the sanction Demand
       /* if ((businessSrvc.equalsIgnoreCase(BPAConstants.BPA_OC_MODULE_CODE)
                || businessSrvc.equalsIgnoreCase(BPAConstants.BPA_BUSINESSSERVICE))
                && state.equalsIgnoreCase(BPAConstants.PENDING_APPROVAL_STATE)) {
            calculationService.addCalculation(bpaRequest, BPAConstants.SANCTION_FEE_KEY);
        }*/


        /*
         * For Permit medium/high and OC on approval stage, we need to check whether for a
         * application sanction fee is applicable or not. If sanction fee is not applicable
         * then we need to skip the payment on APPROVE and need to make it APPROVED instead
         * of SANCTION FEE PAYMENT PEDNING.
         */
       /* if ((businessSrvc.equalsIgnoreCase(BPAConstants.BPA_OC_MODULE_CODE)
                || businessSrvc.equalsIgnoreCase(BPAConstants.BPA_BUSINESSSERVICE))
                && state.equalsIgnoreCase(BPAConstants.PENDING_APPROVAL_STATE) &&
                bpa.getWorkflow() != null && bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_APPROVE)
                && util.getDemandAmount(bpaRequest).compareTo(BigDecimal.ZERO) <= 0) {
            Workflow workflow = Workflow.builder().action(BPAConstants.ACTION_SKIP_PAY).build();
            bpa.setWorkflow(workflow);
        }*/

        //TODO: uncomment this if it is required in future
        //Generate approval no if it is in approved state
      //  enrichmentService.postStatusEnrichment(bpaRequest);


        /*
         * if (Arrays.asList(config.getSkipPaymentStatuses().split(",")).contains(bpa.getStatus())) {
         * enrichmentService.skipPayment(bpaRequest); enrichmentService.postStatusEnrichment(bpaRequest); }
         */

    }

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

    /**
     * handle the reject and Send Back action of the update
     *
     * @param applicationType
     * @param bpaRequest
     * @param businessService
     * @param searchResult
     * @param mdmsData
     * @param edcrResponse
     */
    private void handleRejectSendBackActions(String applicationType, BPARequest bpaRequest, BusinessService businessService, List<BPA> searchResult, Object mdmsData, Map<String, String> edcrResponse) {
        BPA bpa = bpaRequest.getBPA();
        if (bpa.getWorkflow().getAction() != null && (bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_REJECT)
                || bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_REVOCATE))) {

            if (bpa.getWorkflow().getComments() == null || bpa.getWorkflow().getComments().isEmpty()) {
                throw new CustomException(BPAErrorConstants.BPA_UPDATE_ERROR_COMMENT_REQUIRED,
                        "Comment is mandatory, please provide the comments ");
            }
            nocService.handleBPARejectedStateForNoc(bpaRequest);

        } else {

            if (!bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_SENDBACKTOCITIZEN)) {
                actionValidator.validateUpdateRequest(bpaRequest, businessService);
                bpaValidator.validateUpdate(bpaRequest, searchResult, mdmsData,
                        workflowService.getCurrentState(bpa.getStatus(), businessService), edcrResponse);
                if (!applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN_OC)) {
                    landService.updateLandInfo(bpaRequest);
                }
                bpaValidator.validateCheckList(mdmsData, bpaRequest,
                        workflowService.getCurrentState(bpa.getStatus(), businessService));
            }
        }
    }

    /**
     * for OC application update logic is handled which specific to OC
     *
     * @param applicationType
     * @param approvalNo
     * @param bpaRequest
     * @param requestInfo
     * @param additionalDetails
     */
    private void processOcUpdate(String applicationType, String approvalNo, BPARequest bpaRequest, RequestInfo requestInfo, Map<String, String> additionalDetails) {
        if (applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN_OC)) {

            BPASearchCriteria criteria = new BPASearchCriteria();
            criteria.setTenantId(bpaRequest.getBPA().getTenantId());
            criteria.setApprovalNo(approvalNo);
            List<BPA> bpas = search(criteria, requestInfo);
            if (bpas.size() <= 0 || bpas.size() > 1) {
                throw new CustomException(BPAErrorConstants.UPDATE_ERROR,
                        ((bpas.size() <= 0) ? "BPA not found with approval Number :" : "Multiple BPA applications found for approval number :") + approvalNo);
            } else if (bpas.get(0).getStatus().equalsIgnoreCase(BPAConstants.STATUS_REVOCATED)) {
                throw new CustomException(BPAErrorConstants.UPDATE_ERROR, "This permit number is revocated you cannot use this permit number");
            } else if (!bpas.get(0).getStatus().equalsIgnoreCase(BPAConstants.STATUS_APPROVED)) {
                throw new CustomException(BPAErrorConstants.UPDATE_ERROR, "The selected permit number still in workflow approval process, Please apply occupancy after completing approval process.");
            }

            additionalDetails.put("landId", bpas.get(0).getLandId());
            additionalDetails.put("riskType", bpas.get(0).getRiskType());
            criteria.setEdcrNumber(bpas.get(0).getEdcrNumber());
            ocService.validateAdditionalData(bpaRequest, criteria);
            bpaRequest.getBPA().setLandInfo(bpas.get(0).getLandInfo());
        }
    }

    /**
     * Returns bpa from db for the update request
     *
     * @param request The update request
     * @return List of bpas
     */
    public List<BPA> getBPAWithBPAId(BPARequest request) {
        BPASearchCriteria criteria = new BPASearchCriteria();
        List<String> ids = new LinkedList<>();
        ids.add(request.getBPA().getId());
        criteria.setTenantId(request.getBPA().getTenantId());
        criteria.setIds(ids);
        List<BPA> bpa = repository.getBPAData(criteria, null);
        return bpa;
    }

    /**
     * downloads the EDCR Report from the edcr system and stamp the permit no and generated date on the download pdf and return
     *
     * @param bpaRequest
     */
    public void getEdcrPdf(BPARequest bpaRequest) {

        String fileName = BPAConstants.EDCR_PDF;
        PdfDocument pdfDoc = null;
        BPA bpa = bpaRequest.getBPA();

        if (StringUtils.isEmpty(bpa.getApprovalNo())) {
            throw new CustomException(BPAErrorConstants.INVALID_REQUEST, "Approval Number is required.");
        }

        try {
            pdfDoc = createTempReport(bpaRequest, fileName);
            String localizationMessages = notificationUtil.getLocalizationMessages(bpa.getTenantId(),
                    bpaRequest.getRequestInfo());
            String permitNo = notificationUtil.getMessageTemplate(BPAConstants.PERMIT_ORDER_NO, localizationMessages);
            permitNo = permitNo != null ? permitNo : BPAConstants.PERMIT_ORDER_NO;
            String generatedOn = notificationUtil.getMessageTemplate(BPAConstants.GENERATEDON, localizationMessages);
            generatedOn = generatedOn != null ? generatedOn : BPAConstants.GENERATEDON;
            if (pdfDoc != null)
                addDataToPdf(pdfDoc, bpaRequest, permitNo, generatedOn, fileName);

        } catch (IOException ex) {
            log.debug("Exception occured while downloading pdf", ex.getMessage());
            throw new CustomException(BPAErrorConstants.UNABLE_TO_DOWNLOAD, "Unable to download the file");
        } finally {
            if (pdfDoc != null && !pdfDoc.isClosed())
                pdfDoc.close();
        }
    }

    /**
     * make edcr call and get the edcr report url to download the edcr report
     *
     * @param bpaRequest
     * @return
     */
    private URL getEdcrReportDownloaUrl(BPARequest bpaRequest) {
        String pdfUrl = edcrService.getEDCRPdfUrl(bpaRequest);
        URL downloadUrl = null;
        try {
            downloadUrl = new URL(pdfUrl);
            log.debug("Connecting to redirect url" + downloadUrl.toString() + " ... ");
            URLConnection urlConnection = downloadUrl.openConnection();

            // Checking whether the URL contains a PDF
            if (!urlConnection.getContentType().equalsIgnoreCase("application/pdf")) {
                String downloadUrlString = urlConnection.getHeaderField("Location");
                if (!StringUtils.isEmpty(downloadUrlString)) {
                    downloadUrl = new URL(downloadUrlString);
                    log.debug("Connecting to download url" + downloadUrl.toString() + " ... ");
                    urlConnection = downloadUrl.openConnection();
                    if (!urlConnection.getContentType().equalsIgnoreCase("application/pdf")) {
                        log.error("Download url content type is not application/pdf.");
                        throw new CustomException(BPAErrorConstants.INVALID_EDCR_REPORT,
                                "Download url content type is not application/pdf.");
                    }
                } else {
                    log.error("Unable to fetch the location header URL");
                    throw new CustomException(BPAErrorConstants.INVALID_EDCR_REPORT, "Unable to fetch the location header URL");
                }
            }
        } catch (IOException e) {
            log.error("Invalid download URL::" + pdfUrl);
            throw new CustomException(BPAErrorConstants.INVALID_EDCR_REPORT, "Invalid download URL::" + pdfUrl);
        }

        return downloadUrl;
    }

    /**
     * download the edcr report and create in tempfile
     *
     * @param bpaRequest
     * @param fileName
     */
    private PdfDocument createTempReport(BPARequest bpaRequest, String fileName) {

        InputStream readStream = null;
        PdfDocument pdfDocument = null;
        try {
            URL downloadUrl = this.getEdcrReportDownloaUrl(bpaRequest);
            readStream = downloadUrl.openStream();
            pdfDocument = new PdfDocument(new PdfReader(readStream),
                    new PdfWriter(fileName));

        } catch (IOException e) {
            log.error("Error while creating temp report.");
        } finally {
            try {
                readStream.close();
            } catch (IOException e) {
                log.error("Error while creating temp report.");
            }
        }
        return pdfDocument;
    }

    private void addDataToPdf(PdfDocument pdfDoc, BPARequest bpaRequest, String permitNo, String generatedOn, String fileName)
            throws IOException {

        BPA bpa = bpaRequest.getBPA();
        Document doc = new Document(pdfDoc);
        Paragraph headerLeft = new Paragraph(permitNo + " : " + bpaRequest.getBPA().getApprovalNo())
                .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN))
                .setFontSize(10);
        String generatedOnMsg;
        if (bpa.getApprovalDate() != null) {
            Date date = new Date(bpa.getApprovalDate());
            DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = format.format(date);
            generatedOnMsg = generatedOn + " : " + formattedDate;
        } else {
            generatedOnMsg = generatedOn + " : " + "NA";
        }
        Paragraph headerRight = new Paragraph(generatedOnMsg)
                .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN))
                .setFontSize(10);

        for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
            Rectangle pageSize = pdfDoc.getPage(i).getPageSize();
            float margin = 32;
            float x = pageSize.getX() + margin;
            float y = pageSize.getTop() - (margin / 2);
            doc.showTextAligned(headerLeft, x, y, i, TextAlignment.LEFT, VerticalAlignment.BOTTOM, 0);
            float x1 = pageSize.getWidth() - 22;
            float y1 = pageSize.getTop() - (margin / 2);
            doc.showTextAligned(headerRight, x1, y1, i, TextAlignment.RIGHT, VerticalAlignment.BOTTOM, 0);
        }
        pdfDoc.close();
        doc.close();
    }

    public int getBPACount(BPASearchCriteria criteria, RequestInfo requestInfo) {


        LandSearchCriteria landcriteria = new LandSearchCriteria();
        landcriteria.setTenantId(criteria.getTenantId());
        landcriteria.setLocality(criteria.getLocality());
        List<String> edcrNos = null;
        if (criteria.getMobileNumber() != null) {
            landcriteria.setMobileNumber(criteria.getMobileNumber());
            ArrayList<LandInfo> landInfo = landService.searchLandInfoToBPA(requestInfo, landcriteria);
            ArrayList<String> landId = new ArrayList<>();
            if (!landInfo.isEmpty()) {
                landInfo.forEach(land -> landId.add(land.getId()));
                criteria.setLandId(landId);
            }
        } else if (criteria.getName() != null) {
            //To Update the Counter
            landcriteria.setName(criteria.getName());
            ArrayList<LandInfo> landInfo = landService.searchLandInfoToBPA(requestInfo, landcriteria);
            ArrayList<String> landId = new ArrayList<>();
            if (!landInfo.isEmpty()) {
                landInfo.forEach(land -> landId.add(land.getId()));
                criteria.setLandId(landId);
            }
        } else {
            List<String> roles = new ArrayList<>();
            for (Role role : requestInfo.getUserInfo().getRoles()) {
                roles.add(role.getCode());
            }
            if ((criteria.tenantIdOnly() || criteria.isEmpty()) && roles.contains(BPAConstants.CITIZEN)) {
                UserSearchRequest userSearchRequest = new UserSearchRequest();
                if (criteria.getTenantId() != null) {
                    userSearchRequest.setTenantId(criteria.getTenantId());
                }
                List<String> uuids = new ArrayList<>();
                if (requestInfo.getUserInfo() != null && !StringUtils.isEmpty(requestInfo.getUserInfo().getUuid())) {
                    uuids.add(requestInfo.getUserInfo().getUuid());
                    criteria.setOwnerIds(uuids);
                    criteria.setCreatedBy(uuids);
                }
                UserDetailResponse userInfo = userService.getUser(criteria, requestInfo);
                if (userInfo != null) {
                    landcriteria.setMobileNumber(userInfo.getUser().get(0).getMobileNumber());
                }
                ArrayList<LandInfo> landInfos = landService.searchLandInfoToBPA(requestInfo, landcriteria);
                ArrayList<String> landIds = new ArrayList<>();
                if (!landInfos.isEmpty()) {
                    landInfos.forEach(land -> landIds.add(land.getId()));
                    criteria.setLandId(landIds);
                }
            }
        }
        return repository.getBPACount(criteria, edcrNos);

    }

    public List<BPA> plainSearch(BPASearchCriteria criteria, RequestInfo requestInfo) {
        List<BPA> bpas = new LinkedList<>();
        bpaValidator.validateSearch(requestInfo, criteria);
        LandSearchCriteria landcriteria = new LandSearchCriteria();
        List<String> edcrNos = null;

        List<String> roles = new ArrayList<>();
        for (Role role : requestInfo.getUserInfo().getRoles()) {
            roles.add(role.getCode());
        }

        bpas = getBPAFromCriteriaForPlainSearch(criteria, requestInfo, edcrNos);
        ArrayList<String> landIds = new ArrayList<>();
        if (!bpas.isEmpty()) {
            for (int i = 0; i < bpas.size(); i++) {
                landIds.add(bpas.get(i).getLandId());
            }
            landcriteria.setIds(landIds);
            landcriteria.setTenantId(criteria.getTenantId());
            ArrayList<LandInfo> landInfos = landService.searchLandInfoToBPAForPlaneSearch(requestInfo, landcriteria);

            this.populateLandToBPAForPlainSearch(bpas, landInfos, requestInfo);
        }


        return bpas;
    }

    public List<BPA> getBPAFromCriteriaForPlainSearch(BPASearchCriteria criteria, RequestInfo requestInfo, List<String> edcrNos) {
        List<BPA> bpa = repository.getBPADataForPlainSearch(criteria, edcrNos);
        if (bpa.isEmpty())
            return Collections.emptyList();
        return bpa;
    }

    private void populateLandToBPAForPlainSearch(List<BPA> bpas, List<LandInfo> landInfos, RequestInfo requestInfo) {
        for (int i = 0; i < bpas.size(); i++) {
            for (int j = 0; j < landInfos.size(); j++) {
                if (landInfos.get(j).getId().equalsIgnoreCase(bpas.get(i).getLandId())) {
                    bpas.get(i).setLandInfo(landInfos.get(j));
                }
            }
            if (bpas.get(i).getLandId() != null && bpas.get(i).getLandInfo() == null) {
                LandSearchCriteria missingLandcriteria = new LandSearchCriteria();
                List<String> missingLandIds = new ArrayList<>();
                missingLandIds.add(bpas.get(i).getLandId());
                missingLandcriteria.setTenantId(bpas.get(i).getTenantId());
                missingLandcriteria.setIds(missingLandIds);
                log.debug("Call with land ids to Land::" + missingLandcriteria.getTenantId() + missingLandcriteria.getIds());
                List<LandInfo> newLandInfo = landService.searchLandInfoToBPAForPlaneSearch(requestInfo, missingLandcriteria);
                for (int j = 0; j < newLandInfo.size(); j++) {
                    if (newLandInfo.get(j).getId().equalsIgnoreCase(bpas.get(i).getLandId())) {
                        bpas.get(i).setLandInfo(newLandInfo.get(j));
                    }
                }
            }
        }
    }


	/**
	 * call BPA-calculator and fetch the fee estimate
	 *
	 * @param bpaRequest
	 * @return
	 */
	public Object getFeeEstimateFromBpaCalculator(Object bpaRequest) {
		return calculationService.callBpaCalculatorEstimate(bpaRequest);
	}

	public Object getFeeEstimateFromBpaCalculatorV2(CalculationReq calcRequest) {

		RequestInfo requestInfo = calcRequest.getRequestInfo();
		List<CalulationCriteria> input = calcRequest.getCalulationCriteria();

		for (CalulationCriteria obj : input) {
			Map<String, Object> edcrDetails = edcrService.getEDCRFeeCalculationDetails(requestInfo, obj.getBpa());
			List<Floor> floors = (List<Floor>) edcrDetails.get(BPAConstants.FLOOR);
			String wallType = (String) edcrDetails.get(BPAConstants.WALLTYPE);
			String occupancy = (String) edcrDetails.get(BPAConstants.APPLICATIONTYPE);
			obj.setFloors(floors);
			obj.setWallType(wallType);
			obj.setApplicationType(occupancy);
		}

		return calculationService.callBpaCalculatorEstimate(calcRequest);
	}

    /**
     * Search for RTP (Registered Technical Person) using user search API
     *
     * @param userSearchRequest The user search request containing search criteria
     * @return UserDetailResponse containing RTP details
     */
    public UserDetailResponse searchRTP(UserSearchRequest userSearchRequest) {
        userSearchRequest.setTenantId(userSearchRequest.getTenantId());
        userSearchRequest.setRoleCodes(userSearchRequest.getRoleCodes());
        StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
        return userService.userCall(userSearchRequest, uri);
    }

    /**
     * Reassigns the RTP for a BPA application and updates the application in the repository
     * New RTP details are enriched, workflow is called for reassignment, and the application is updated
     * @param bpaRequest The BPARequest containing the BPA application details and new RTP information
     * @return Updated BPA application with reassigned RTP
    * */
    public BPA reassignRTP(@Valid BPARequest bpaRequest) {
        enrichmentService.enrichBPAUpdateRequest(bpaRequest, null);
        wfIntegrator.reassignRTP(bpaRequest);
        repository.update(bpaRequest, BPAConstants.RTP_UPDATE);
        return bpaRequest.getBPA();
    }
}
