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
import java.util.concurrent.ConcurrentHashMap;

import static org.egov.inbox.util.InboxConstants.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    /**
     * Cache for tenant IDs mapped by planning area code.
     * Key: planningAreaCode (String)
     * Value: List of tenant IDs (List<String>)
     * Uses ConcurrentHashMap for thread-safe operations in concurrent web requests.
     */
    private final ConcurrentHashMap<String, List<String>> tenantIdsCache = new ConcurrentHashMap<>();

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
     * Fetches list of tenant IDs from MDMS based on planning area code filter.
     * Results are cached to avoid repeated MDMS calls for the same planning area code.
     * 
     * @param requestInfo RequestInfo object
     * @param tenantId State level tenant ID
     * @param planningAreaCode Planning area code to filter tenants
     * @return List of tenant IDs matching the planning area code
     */
    public List<String> getTenantIdsByPlanningAreaCode(RequestInfo requestInfo, String tenantId, String planningAreaCode) {
        String stateLevelTenantId = multiStateInstanceUtil.getStateLevelTenant(tenantId);
        
        // Return from cache if available (key is just planningAreaCode)
        List<String> cachedTenantIds = tenantIdsCache.get(planningAreaCode);
        if (cachedTenantIds != null) {
            log.debug("Cache hit for planning area code: {}", planningAreaCode);
            return new ArrayList<>(cachedTenantIds); // Return a copy to prevent external modification
        }
        
        // Cache miss - fetch from MDMS
        log.info("Cache miss for planning area code: {}. Fetching from MDMS...", planningAreaCode);
        List<String> tenantIds = fetchTenantIdsFromMDMS(requestInfo, stateLevelTenantId, planningAreaCode);
        
        // Store in cache (only if not empty to avoid caching empty results)
        if (tenantIds != null && !tenantIds.isEmpty()) {
            tenantIdsCache.put(planningAreaCode, new ArrayList<>(tenantIds)); // Store a copy
            log.info("Cached {} tenant IDs for planning area code: {}", 
                    tenantIds.size(), planningAreaCode);
        }
        
        return tenantIds;
    }

    /**
     * Fetches tenant IDs from MDMS service
     * 
     * @param requestInfo RequestInfo object
     * @param stateLevelTenantId State level tenant ID
     * @param planningAreaCode Planning area code to filter tenants
     * @return List of tenant IDs matching the planning area code
     */
    private List<String> fetchTenantIdsFromMDMS(RequestInfo requestInfo, String stateLevelTenantId, String planningAreaCode) {
        StringBuilder uri = new StringBuilder();
        uri.append(mdmsHost).append(mdmsUrl);
        
        MdmsCriteriaReq mdmsCriteriaReq = getMdmsRequestForTenantsByPlanningAreaCode(
                stateLevelTenantId, planningAreaCode, requestInfo);
        
        List<String> tenantIds = new ArrayList<>();
        
        try {
            Object response = restTemplate.postForObject(uri.toString(), mdmsCriteriaReq, Map.class);
            if (response != null) {
                // Extract tenant codes from response: $.MdmsRes.tenant.tenants[*].code
                tenantIds = JsonPath.read(response, "$.MdmsRes.tenant.tenants[*].code");
            }
        } catch (Exception e) {
            log.error("Error fetching tenant IDs from MDMS for planning area code: {} with tenantId: {}", 
                    planningAreaCode, stateLevelTenantId, e);
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

