package org.egov.inbox.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.inbox.web.model.V2.InboxQueryConfiguration;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.egov.inbox.util.InboxConstants.*;


@Component
public class MDMSUtil {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MultiStateInstanceUtil multiStateInstanceUtil;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsUrl;

    @Autowired
    private ObjectMapper objectMapper;

    @Cacheable(value="inboxConfiguration")
    public InboxQueryConfiguration getConfigFromMDMS(String tenantId, String moduleName) {

        StringBuilder uri = new StringBuilder();
        uri.append(mdmsHost).append(mdmsUrl);
        MdmsCriteriaReq mdmsCriteriaReq = getMdmsRequestForInboxQueryConfiguration(tenantId);
        Object response = new HashMap<>();
        List<Map> configs;
        try {
            response = restTemplate.postForObject(uri.toString(), mdmsCriteriaReq, Map.class);
            String jsonpath = MDMS_RESPONSE_JSONPATH.replace(MODULE_PLACEHOLDER, moduleName);
            configs = JsonPath.read(response, jsonpath);
        }catch(Exception e) {
            throw new CustomException("CONFIG_ERROR","Error in fetching inbox query configuration from MDMS for: " + moduleName);
        }

        if (CollectionUtils.isEmpty(configs))
            throw new CustomException("CONFIG_ERROR","Inbox Query Configuration not found in MDMS response for: " + moduleName);

        InboxQueryConfiguration configuration = objectMapper.convertValue(configs.get(0), InboxQueryConfiguration.class);

        return configuration;
    }

    private MdmsCriteriaReq getMdmsRequestForInboxQueryConfiguration(String tenantId) {
        MasterDetail masterDetail = new MasterDetail();
        masterDetail.setName(INBOX_QUERY_CONFIG_NAME);
        List<MasterDetail> masterDetailList = new ArrayList<>();
        masterDetailList.add(masterDetail);

        ModuleDetail moduleDetail = new ModuleDetail();
        moduleDetail.setMasterDetails(masterDetailList);
        moduleDetail.setModuleName(INBOX_MODULE_CODE);
        List<ModuleDetail> moduleDetailList = new ArrayList<>();
        moduleDetailList.add(moduleDetail);

        MdmsCriteria mdmsCriteria = new MdmsCriteria();
        mdmsCriteria.setTenantId(multiStateInstanceUtil.getStateLevelTenant(tenantId));
        mdmsCriteria.setModuleDetails(moduleDetailList);

        MdmsCriteriaReq mdmsCriteriaReq = new MdmsCriteriaReq();
        mdmsCriteriaReq.setMdmsCriteria(mdmsCriteria);
        mdmsCriteriaReq.setRequestInfo(new RequestInfo());

        return mdmsCriteriaReq;
    }

    /**
     * Fetches list of tenant IDs from MDMS based on planning area code filter
     * 
     * @param requestInfo RequestInfo object
     * @param tenantId State level tenant ID
     * @param planningAreaCode Planning area code to filter tenants
     * @return List of tenant IDs matching the planning area code
     */
    public List<String> getTenantIdsByPlanningAreaCode(RequestInfo requestInfo, String tenantId, String planningAreaCode) {
        StringBuilder uri = new StringBuilder();
        uri.append(mdmsHost).append(mdmsUrl);
        
        MdmsCriteriaReq mdmsCriteriaReq = getMdmsRequestForTenantsByPlanningAreaCode(
                multiStateInstanceUtil.getStateLevelTenant(tenantId), planningAreaCode, requestInfo);
        
        List<String> tenantIds = new ArrayList<>();
        
        try {
            Object response = restTemplate.postForObject(uri.toString(), mdmsCriteriaReq, Map.class);
            if (response != null) {
                // Extract tenant codes from response: $.MdmsRes.tenant.tenants[*].code
                tenantIds = JsonPath.read(response, "$.MdmsRes.tenant.tenants[*].code");
            }
        } catch (Exception e) {
            throw new CustomException("MDMS_TENANT_FETCH_ERROR", 
                    "Error in fetching tenant IDs from MDMS for planning area code: " + planningAreaCode + " - " + e.getMessage());
        }
        
        return tenantIds;
    }

    /**
     * Prepares MDMS request to fetch tenants filtered by planning area code
     * 
     * @param tenantId State level tenant ID
     * @param planningAreaCode Planning area code to filter
     * @param requestInfo RequestInfo object
     * @return MdmsCriteriaReq object
     */
    private MdmsCriteriaReq getMdmsRequestForTenantsByPlanningAreaCode(String tenantId, String planningAreaCode, RequestInfo requestInfo) {
        // Build filter expression: [?(@.city.planningAreaCode == 'PLANNING_AREA_CODE')]
        String filter = "[?(@.city.planningAreaCode == '" + planningAreaCode + "')]";
        
        MasterDetail masterDetail = MasterDetail.builder()
                .name("tenants")
                .filter(filter)
                .build();
        
        List<MasterDetail> masterDetailList = new ArrayList<>();
        masterDetailList.add(masterDetail);
        
        ModuleDetail moduleDetail = ModuleDetail.builder()
                .moduleName("tenant")
                .masterDetails(masterDetailList)
                .build();
        
        List<ModuleDetail> moduleDetailList = new ArrayList<>();
        moduleDetailList.add(moduleDetail);
        
        MdmsCriteria mdmsCriteria = MdmsCriteria.builder()
                .tenantId(tenantId)
                .moduleDetails(moduleDetailList)
                .build();
        
        MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder()
                .requestInfo(requestInfo)
                .mdmsCriteria(mdmsCriteria)
                .build();
        
        return mdmsCriteriaReq;
    }

}

