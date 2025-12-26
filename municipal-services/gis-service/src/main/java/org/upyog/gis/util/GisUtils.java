package org.upyog.gis.util;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.upyog.gis.config.GisProperties;
import org.upyog.gis.config.ServiceConstants;
import org.upyog.gis.repository.ServiceRequestRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;

@Service
public class GisUtils {
    private GisProperties config;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    public GisUtils(GisProperties config, ServiceRequestRepository serviceRequestRepository) {
        this.config = config;
        this.serviceRequestRepository = serviceRequestRepository;
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
    public List<ModuleDetail> getBPAModuleRequest(String tenantCode) {

        List<MasterDetail> permissibleZone = new ArrayList<>();
        permissibleZone.add(MasterDetail.builder().name(ServiceConstants.PERMISSIBLE_ZONE).build());
        ModuleDetail permissiblezone = ModuleDetail.builder().masterDetails(permissibleZone)
                .moduleName(ServiceConstants.BPA_MODULE).build();


        return Arrays.asList(permissiblezone);

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

            moduleRequest = getBPAModuleRequest(tenantId);
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
}
