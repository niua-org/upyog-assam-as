package org.egov.noc.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.User;
import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.util.NOCConstants;
import org.egov.noc.web.model.Noc;
import org.egov.noc.web.model.NocSearchCriteria;
import org.egov.noc.web.model.aai.AAIStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * Scheduler for syncing NOC statuses with AAI NOCAS
 */
@Slf4j
@Service
public class NOCSchedulerService {

    @Autowired
    private NOCConfiguration config;

    @Autowired
    private NOCService nocService;

    @Autowired
    private AAINOCASIntegrationService aaiIntegrationService;

    @Autowired
    private NOCStatusUpdateService nocStatusUpdateService;

    /**
     * Scheduled job to sync NOC statuses with AAI NOCAS
     */
    @Scheduled(cron = "${scheduler.aai.noc.status.sync.cron}")
    @SchedulerLock(
        name = "NOCSchedulerService_syncAAINOCStatus", 
        lockAtLeastFor = "${scheduler.aai.noc.status.sync.lock.at.least.for}",
        lockAtMostFor = "${scheduler.aai.noc.status.sync.lock.at.most.for}"
    )
    public void syncAAINOCStatus() {
        if (!config.getSchedulerEnabled()) {
            log.info("Scheduler disabled");
            return;
        }

        if (!config.getAaiNocasEnabled()) {
            log.info("AAI integration disabled");
            return;
        }

        try {
            List<Noc> pendingNocs = fetchPendingAAINOCApplications();
            
            if (CollectionUtils.isEmpty(pendingNocs)) {
                log.info("No pending AIRPORT_NOC applications found");
                return;
            }

            log.info("Found {} pending applications", pendingNocs.size());

            List<String> applicationNumbers = pendingNocs.stream()
                    .map(Noc::getApplicationNo)
                    .collect(Collectors.toList());

            RequestInfo requestInfo = createSystemRequestInfo();
            AAIStatusResponse aaiResponse = aaiIntegrationService.fetchNOCStatusFromAAI(applicationNumbers, requestInfo);

            if (aaiResponse == null || !aaiResponse.getSuccess()) {
                log.error("Failed to fetch status from AAI: {}", 
                        aaiResponse != null ? aaiResponse.getErrorMessage() : "Unknown error");
                return;
            }

            List<Noc> updatedNocs = nocStatusUpdateService.updateNOCStatusFromAAI(aaiResponse, requestInfo);
            log.info("Status sync completed. Updated {} applications", updatedNocs.size());

        } catch (Exception e) {
            log.error("Error during status synchronization", e);
        }
    }

    /**
     * Fetches pending AIRPORT_NOC applications
     * 
     * @return List of NOC applications
     */
    private List<Noc> fetchPendingAAINOCApplications() {
        try {
            NocSearchCriteria searchCriteria = NocSearchCriteria.builder()
                    .nocType(NOCConstants.AIRPORT_NOC_TYPE)
                    .build();

            RequestInfo requestInfo = createSystemRequestInfo();
            List<Noc> allAirportNocs = nocService.search(searchCriteria, requestInfo);

            if (CollectionUtils.isEmpty(allAirportNocs)) {
                return new ArrayList<>();
            }

            return allAirportNocs.stream()
                    .filter(this::isApplicationPendingStatusSync)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching pending applications", e);
            return new ArrayList<>();
        }
    }

    /**
     * Checks if NOC application needs status sync
     * 
     * @param noc NOC application
     * @return true if pending sync
     */
    private boolean isApplicationPendingStatusSync(Noc noc) {
        if (noc == null || noc.getApplicationStatus() == null) {
            return false;
        }

        List<String> finalStates = Arrays.asList(
                NOCConstants.APPROVED_STATE,
                NOCConstants.AUTOAPPROVED_STATE,
                NOCConstants.APPLICATION_STATUS_REJECTED,
                NOCConstants.VOIDED_STATUS
        );

        return !finalStates.contains(noc.getApplicationStatus());
    }

    /**
     * Creates system RequestInfo for scheduler operations
     * 
     * @return RequestInfo object
     */
    private RequestInfo createSystemRequestInfo() {
        User systemUser = User.builder()
                .id(1L)
                .userName("SYSTEM")
                .name("System")
                .type("SYSTEM")
                .mobileNumber("9999999999")
                .emailId("system@egovernments.org")
                .roles(new ArrayList<>())
                .tenantId("as")
                .build();

        long currentTime = Instant.now().toEpochMilli();
        return RequestInfo.builder()
                .apiId("noc-scheduler")
                .ver("1.0")
                .ts(currentTime)
                .action("sync")
                .did("internal")
                .key("internal")
                .msgId("noc-scheduler-" + currentTime)
                .authToken("internal")
                .userInfo(systemUser)
                .build();
    }

    /**
     * Manually triggers status sync
     */
    public void triggerManualSync() {
        syncAAINOCStatus();
    }

    /**
     * Gets count of pending applications
     * 
     * @return Application count
     */
    public int getPendingApplicationsCount() {
        try {
            return fetchPendingAAINOCApplications().size();
        } catch (Exception e) {
            log.error("Error getting count", e);
            return -1;
        }
    }
}

