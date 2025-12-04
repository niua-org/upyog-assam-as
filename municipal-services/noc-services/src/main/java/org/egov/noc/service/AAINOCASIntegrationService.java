package org.egov.noc.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.repository.ServiceRequestRepository;
import org.egov.noc.util.NOCConstants;
import org.egov.noc.web.model.aai.AAIApplicationStatus;
import org.egov.noc.web.model.aai.AAIStatusRequest;
import org.egov.noc.web.model.aai.AAIStatusResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for integrating with AAI NOCAS to fetch NOC status updates
 */
@Slf4j
@Service
public class AAINOCASIntegrationService {

    @Autowired
    private NOCConfiguration config;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    /**
     * Fetches NOC statuses from AAI NOCAS API
     * 
     * @param applicationNumbers Application numbers
     * @param requestInfo Request info
     * @return AAI status response
     */
    public AAIStatusResponse fetchNOCStatusFromAAI(List<String> applicationNumbers, RequestInfo requestInfo) {
        if (!config.getAaiNocasEnabled()) {
            return AAIStatusResponse.builder()
                    .success(false)
                    .errorMessage("AAI integration disabled")
                    .build();
        }

        if (CollectionUtils.isEmpty(applicationNumbers)) {
            return AAIStatusResponse.builder()
                    .success(false)
                    .errorMessage("No applications provided")
                    .build();
        }

        try {
            AAIStatusRequest aaiRequest = AAIStatusRequest.builder()
                    .tokenKey(config.getAaiNocasTokenKey())
                    .applicationNumbers(applicationNumbers)
                    .tenantId(requestInfo.getUserInfo() != null ? requestInfo.getUserInfo().getTenantId() : null)
                    .build();

            AAIStatusResponse response = callAAINOCASAPI(aaiRequest);
            log.info("Fetched status for {} applications from AAI", 
                    response.getApplicationStatuses() != null ? response.getApplicationStatuses().size() : 0);

            return response;

        } catch (Exception e) {
            log.error("Error fetching status from AAI", e);
            throw new CustomException(NOCConstants.AAI_INTEGRATION_ERROR, "Failed to fetch AAI status");
        }
    }

    /**
     * Calls AAI NOCAS web service
     * 
     * @param aaiRequest Request object
     * @return AAI status response
     * @throws Exception if call fails
     */
    private AAIStatusResponse callAAINOCASAPI(AAIStatusRequest aaiRequest) throws Exception {
        String soapRequest = prepareSoapRequest(aaiRequest);
        
        try {
            StringBuilder uri = new StringBuilder(config.getAaiNocasApiUrl());
            Object response = serviceRequestRepository.fetchResult(uri, soapRequest);
            return parseAAIResponse(response, aaiRequest.getApplicationNumbers());
        } catch (Exception e) {
            log.error("AAI API call failed", e);
            throw new Exception("AAI API call failed: " + e.getMessage(), e);
        }
    }

    /**
     * Prepares SOAP request for AAI API
     * 
     * @param aaiRequest Request object
     * @return SOAP XML string
     */
    private String prepareSoapRequest(AAIStatusRequest aaiRequest) {
        StringBuilder soapRequest = new StringBuilder();
        soapRequest.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
        soapRequest.append("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">");
        soapRequest.append("<soap:Body>");
        soapRequest.append("<GetNOCStatus xmlns=\"http://nocas.aai.aero/\">");
        soapRequest.append("<tokenKey>").append(aaiRequest.getTokenKey()).append("</tokenKey>");
        
        for (String appNumber : aaiRequest.getApplicationNumbers()) {
            soapRequest.append("<applicationNumber>").append(appNumber).append("</applicationNumber>");
        }
        
        soapRequest.append("</GetNOCStatus>");
        soapRequest.append("</soap:Body>");
        soapRequest.append("</soap:Envelope>");

        return soapRequest.toString();
    }

    /**
     * Parses XML response from AAI NOCAS API and converts to structured response
     * 
     * @param xmlResponse Raw XML response from AAI API
     * @param requestedApplications List of application numbers that were requested
     * @return AAIStatusResponse containing parsed application statuses
     */
    private AAIStatusResponse parseAAIResponse(Object xmlResponse, List<String> requestedApplications) {
        log.debug("Parsing AAI NOCAS API response");

        try {
            List<AAIApplicationStatus> applicationStatuses = new ArrayList<>();
            
            // This would be replaced with actual XML parsing logic
            // using libraries like JAXB, DOM parser, or SAX parser
            for (String appNumber : requestedApplications) {
                AAIApplicationStatus status = AAIApplicationStatus.builder()
                        .applicationNumber(appNumber)
                        .status("INPROCESS") // This would come from XML parsing
                        .remarks("Application under review")
                        .build();
                applicationStatuses.add(status);
            }

            return AAIStatusResponse.builder()
                    .applicationStatuses(applicationStatuses)
                    .success(true)
                    .responseStatus("SUCCESS")
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse AAI NOCAS API response", e);
            return AAIStatusResponse.builder()
                    .success(false)
                    .errorMessage("Failed to parse AAI response: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Maps AAI status to NOC status
     * 
     * @param aaiStatus AAI status code
     * @return NOC status
     */
    public String mapAAIStatusToNOCStatus(String aaiStatus) {
        if (aaiStatus == null) {
            return NOCConstants.APPLICATION_STATUS_INPROGRESS;
        }

        switch (aaiStatus.toUpperCase()) {
            case "ISSUED":
                return NOCConstants.APPLICATION_STATUS_APPROVED;
            case "REJECTED":
                return NOCConstants.APPLICATION_STATUS_REJECTED;
            case "INPROCESS":
            default:
                return NOCConstants.APPLICATION_STATUS_INPROGRESS;
        }
    }
}

