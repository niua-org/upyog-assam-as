package org.egov.noc.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.common.contract.request.RequestInfo;
import org.egov.noc.util.NOCConstants;
import org.egov.noc.web.model.Noc;
import org.egov.noc.web.model.NocRequest;
import org.egov.noc.web.model.Workflow;
import org.egov.noc.web.model.aai.AAIApplicationStatus;
import org.egov.noc.web.model.aai.AAIStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for updating NOC statuses based on AAI responses
 */
@Slf4j
@Service
public class NOCStatusUpdateService {

    @Autowired
    private NOCService nocService;


    @Autowired
    private AAINOCASIntegrationService aaiIntegrationService;

    /**
     * Updates NOC statuses based on AAI response
     * 
     * @param aaiResponse AAI response
     * @param requestInfo Request info
     * @return Updated NOC list
     */
    public List<Noc> updateNOCStatusFromAAI(AAIStatusResponse aaiResponse, RequestInfo requestInfo) {
        if (aaiResponse == null || !aaiResponse.getSuccess() || CollectionUtils.isEmpty(aaiResponse.getApplicationStatuses())) {
            return new ArrayList<>();
        }

        List<Noc> updatedNocs = new ArrayList<>();

        for (AAIApplicationStatus aaiStatus : aaiResponse.getApplicationStatuses()) {
            try {
                Noc updatedNoc = updateSingleNOCStatus(aaiStatus, requestInfo);
                if (updatedNoc != null) {
                    updatedNocs.add(updatedNoc);
                }
            } catch (Exception e) {
                log.error("Failed to update NOC {}", aaiStatus.getApplicationNumber(), e);
            }
        }

        log.info("Updated {} applications", updatedNocs.size());
        return updatedNocs;
    }

    /**
     * Updates single NOC status
     * 
     * @param aaiStatus AAI status
     * @param requestInfo Request info
     * @return Updated NOC
     * @throws Exception if update fails
     */
    private Noc updateSingleNOCStatus(AAIApplicationStatus aaiStatus, RequestInfo requestInfo) throws Exception {
        Noc existingNoc = fetchNOCByApplicationNumber(aaiStatus.getApplicationNumber(), requestInfo);
        if (existingNoc == null) {
            return null;
        }

        String newNocStatus = aaiIntegrationService.mapAAIStatusToNOCStatus(aaiStatus.getStatus());
        
        if (newNocStatus.equals(existingNoc.getApplicationStatus())) {
            return existingNoc;
        }

        if (isStatusFinal(existingNoc.getApplicationStatus())) {
            return existingNoc;
        }

        String workflowAction = determineWorkflowAction(existingNoc.getApplicationStatus(), newNocStatus);
        if (workflowAction == null) {
            return existingNoc;
        }

        return executeNOCStatusUpdate(existingNoc, aaiStatus, workflowAction, requestInfo);
    }

    /**
     * Fetches NOC by application number
     * 
     * @param applicationNumber Application number
     * @param requestInfo Request info
     * @return NOC application
     */
    private Noc fetchNOCByApplicationNumber(String applicationNumber, RequestInfo requestInfo) {
        try {
            org.egov.noc.web.model.NocSearchCriteria searchCriteria = 
                    org.egov.noc.web.model.NocSearchCriteria.builder()
                    .applicationNo(applicationNumber)
                    .build();

            List<Noc> nocs = nocService.search(searchCriteria, requestInfo);
            return !CollectionUtils.isEmpty(nocs) ? nocs.get(0) : null;
        } catch (Exception e) {
            log.error("Error fetching NOC {}", applicationNumber, e);
            return null;
        }
    }

    /**
     * Executes NOC status update with workflow
     * 
     * @param existingNoc NOC application
     * @param aaiStatus AAI status
     * @param workflowAction Workflow action
     * @param requestInfo Request info
     * @return Updated NOC
     * @throws Exception if update fails
     */
    private Noc executeNOCStatusUpdate(Noc existingNoc, AAIApplicationStatus aaiStatus, 
                                     String workflowAction, RequestInfo requestInfo) throws Exception {
        Workflow workflow = Workflow.builder().action(workflowAction).build();
        workflow.setComment("Status updated from AAI: " + aaiStatus.getRemarks());

        existingNoc.setWorkflow(workflow);
        updateAdditionalDetailsFromAAI(existingNoc, aaiStatus);

        NocRequest nocRequest = new NocRequest();
        nocRequest.setNoc(existingNoc);
        nocRequest.setRequestInfo(requestInfo);

        List<Noc> updatedNocs = nocService.update(nocRequest);
        return !CollectionUtils.isEmpty(updatedNocs) ? updatedNocs.get(0) : existingNoc;
    }

    /**
     * Updates NOC additionalDetails with AAI response data
     * 
     * @param noc NOC application
     * @param aaiStatus AAI status
     */
    private void updateAdditionalDetailsFromAAI(Noc noc, AAIApplicationStatus aaiStatus) {
        @SuppressWarnings("unchecked")
        Map<String, Object> additionalDetails = noc.getAdditionalDetails() != null ? 
                (Map<String, Object>) noc.getAdditionalDetails() : new HashMap<>();

        additionalDetails.put("aaiStatus", aaiStatus.getStatus());
        additionalDetails.put("aaiRemarks", aaiStatus.getRemarks());
        additionalDetails.put("aaiLastUpdated", Instant.now().toEpochMilli());
        
        if (aaiStatus.getNocCertificateNumber() != null) {
            additionalDetails.put("aaiCertificateNumber", aaiStatus.getNocCertificateNumber());
        }
        
        if (aaiStatus.getIssueDate() != null) {
            additionalDetails.put("aaiIssueDate", aaiStatus.getIssueDate());
        }
        
        if (aaiStatus.getValidityDate() != null) {
            additionalDetails.put("aaiValidityDate", aaiStatus.getValidityDate());
        }

        noc.setAdditionalDetails(additionalDetails);
    }

    /**
     * Determines workflow action based on status
     * 
     * @param currentStatus Current status
     * @param newStatus New status
     * @return Workflow action
     */
    private String determineWorkflowAction(String currentStatus, String newStatus) {
        if (NOCConstants.APPLICATION_STATUS_APPROVED.equals(newStatus)) {
            return NOCConstants.ACTION_APPROVE;
        }
        
        if (NOCConstants.APPLICATION_STATUS_REJECTED.equals(newStatus)) {
            return NOCConstants.ACTION_REJECT;
        }
        
        return null;
    }

    /**
     * Checks if status is final
     * 
     * @param status NOC status
     * @return true if final
     */
    private boolean isStatusFinal(String status) {
        return NOCConstants.APPROVED_STATE.equals(status) || 
               NOCConstants.AUTOAPPROVED_STATE.equals(status) ||
               NOCConstants.APPLICATION_STATUS_REJECTED.equals(status) ||
               NOCConstants.VOIDED_STATUS.equals(status);
    }
}

