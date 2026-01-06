package org.egov.inbox.service;

import static org.egov.inbox.util.BpaConstants.BPA;
import static org.egov.inbox.util.BpaConstants.BPAREG;
import static org.egov.inbox.util.BpaConstants.BPA_APPLICATION_NUMBER_PARAM;
import static org.egov.inbox.util.BpaConstants.LOCALITY_PARAM;
import static org.egov.inbox.util.BpaConstants.MOBILE_NUMBER_PARAM;
import static org.egov.inbox.util.BpaConstants.OFFSET_PARAM;
import static org.egov.inbox.util.BpaConstants.STATUS_ID;
import static org.egov.inbox.util.BpaConstants.STATUS_PARAM;
import static org.egov.inbox.util.BpaConstants.COUNT;
import static org.egov.inbox.util.NocConstants.NOC;
import static org.egov.inbox.util.NocConstants.NOC_APPLICATION_NUMBER_PARAM;
import static org.egov.inbox.util.TLConstants.APPLICATION_NUMBER_PARAM;
import static org.egov.inbox.util.TLConstants.TL;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.inbox.config.InboxConfiguration;
import org.egov.inbox.repository.ServiceRequestRepository;
import org.egov.inbox.util.*;
import org.egov.inbox.web.model.Inbox;
import org.egov.inbox.web.model.InboxResponse;
import org.egov.inbox.web.model.InboxSearchCriteria;
import org.egov.inbox.web.model.RequestInfoWrapper;
import org.egov.inbox.web.model.workflow.BusinessService;
import org.egov.inbox.web.model.workflow.ProcessInstance;
import org.egov.inbox.web.model.workflow.ProcessInstanceResponse;
import org.egov.inbox.web.model.workflow.ProcessInstanceSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class InboxService {

	private InboxConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private ObjectMapper mapper;

	private WorkflowService workflowService;

	@Autowired
	private TLInboxFilterService tlInboxFilterService;

	@Autowired
	private BPAInboxFilterService bpaInboxFilterService;

	@Autowired
	private NOCInboxFilterService nocInboxFilterService;

	@Autowired
	public InboxService(InboxConfiguration config, ServiceRequestRepository serviceRequestRepository,
						ObjectMapper mapper, WorkflowService workflowService) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
		this.mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		this.workflowService = workflowService;
	}

	public InboxResponse fetchInboxData(InboxSearchCriteria criteria, RequestInfo requestInfo) {

		ProcessInstanceSearchCriteria processCriteria = criteria.getProcessSearchCriteria();
		HashMap moduleSearchCriteria = criteria.getModuleSearchCriteria();
		processCriteria.setTenantId(criteria.getTenantId());

		Integer totalCount = 0;
		log.info(processCriteria.getModuleName().toString());
		totalCount = workflowService.getProcessCount(criteria.getTenantId(), requestInfo, processCriteria);
		Integer nearingSlaProcessCount = 0;
			nearingSlaProcessCount = workflowService.getNearingSlaProcessCount(criteria.getTenantId(), requestInfo,
					processCriteria);


		List<String> inputStatuses = new ArrayList<>();
		if (!CollectionUtils.isEmpty(processCriteria.getStatus()))
			inputStatuses = new ArrayList<>(processCriteria.getStatus());
		StringBuilder assigneeUuid = new StringBuilder();
		if (!ObjectUtils.isEmpty(processCriteria.getAssignee())) {
			assigneeUuid = assigneeUuid.append(processCriteria.getAssignee());
			processCriteria.setStatus(null);
		}
		// Since we want the whole status count map regardless of the status filter and
		// assignee filter being passed
		processCriteria.setAssignee(null);
		processCriteria.setStatus(null);

		List<HashMap<String, Object>> bpaCitizenStatusCountMap = new ArrayList<HashMap<String, Object>>();
		List<String> roles = requestInfo.getUserInfo().getRoles().stream().map(Role::getCode)
				.collect(Collectors.toList());

		String moduleName = processCriteria.getModuleName();
		List<HashMap<String, Object>> statusCountMap = workflowService.getProcessStatusCount(requestInfo,
				processCriteria);
		processCriteria.setModuleName(moduleName);
		processCriteria.setStatus(inputStatuses);
		processCriteria.setAssignee(assigneeUuid.toString());
		List<String> businessServiceName = processCriteria.getBusinessService();
		List<Inbox> inboxes = new ArrayList<Inbox>();
		InboxResponse response = new InboxResponse();
		JSONArray businessObjects = null;
		Map<String, String> srvMap = fetchAppropriateServiceMap(businessServiceName, moduleName);
		if (CollectionUtils.isEmpty(businessServiceName)) {
			throw new CustomException(ErrorConstants.MODULE_SEARCH_INVLAID,
					"Bussiness Service is mandatory for module search");
		}

		Map<String, Long> businessServiceSlaMap = new HashMap<>();

		if (!CollectionUtils.isEmpty(moduleSearchCriteria)) {
			moduleSearchCriteria.put("tenantId", criteria.getTenantId());
			moduleSearchCriteria.put("offset", criteria.getOffset());
			moduleSearchCriteria.put("limit", criteria.getLimit());
			List<BusinessService> bussinessSrvs = new ArrayList<BusinessService>();
			for (String businessSrv : businessServiceName) {
				BusinessService businessService = workflowService.getBusinessService(criteria.getTenantId(),
						requestInfo, businessSrv);
				bussinessSrvs.add(businessService);
				businessServiceSlaMap.put(businessService.getBusinessService(),
						businessService.getBusinessServiceSla());
			}
			HashMap<String, String> StatusIdNameMap = workflowService.getActionableStatusesForRole(requestInfo,
					bussinessSrvs, processCriteria);
			String applicationStatusParam = srvMap.get("applsStatusParam");
			String businessIdParam = srvMap.get("businessIdProperty");
			if (StringUtils.isEmpty(applicationStatusParam)) {
				applicationStatusParam = "applicationStatus";
			}
            if (StatusIdNameMap.values().size() > 0) {
				if (!CollectionUtils.isEmpty(processCriteria.getStatus())) {
					List<String> statuses = new ArrayList<String>();
					processCriteria.getStatus().forEach(status -> {
						statuses.add(StatusIdNameMap.get(status));
					});
					moduleSearchCriteria.put(applicationStatusParam,
							StringUtils.arrayToDelimitedString(statuses.toArray(), ","));
				} else {
					moduleSearchCriteria.put(applicationStatusParam,
							StringUtils.arrayToDelimitedString(StatusIdNameMap.values().toArray(), ","));
				}

			}

			Map<String, List<String>> tenantAndApplnNumbersMap = new HashMap<>();
			if (processCriteria != null && !ObjectUtils.isEmpty(processCriteria.getModuleName())
					&& processCriteria.getModuleName().equals(BPA) && roles.contains(BpaConstants.CITIZEN)) {
				List<Map<String, String>> tenantWiseApplns = bpaInboxFilterService
						.fetchTenantWiseApplicationNumbersForCitizenInboxFromSearcher(criteria, StatusIdNameMap,
								requestInfo);
				if (moduleSearchCriteria == null || moduleSearchCriteria.isEmpty()) {
					moduleSearchCriteria = new HashMap<>();
					moduleSearchCriteria.put(MOBILE_NUMBER_PARAM, requestInfo.getUserInfo().getMobileNumber());
					criteria.setModuleSearchCriteria(moduleSearchCriteria);
				}
				for (Map<String, String> tenantAppln : tenantWiseApplns) {
					String tenant = tenantAppln.get("tenant_id");
					String applnNo = tenantAppln.get("application_no");
					if (tenantAndApplnNumbersMap.containsKey(tenant)) {
						List<String> applnNos = tenantAndApplnNumbersMap.get(tenant);
						applnNos.add(applnNo);
						tenantAndApplnNumbersMap.put(tenant, applnNos);
					} else {
						List<String> l = new ArrayList<>();
						l.add(applnNo);
						tenantAndApplnNumbersMap.put(tenant, l);
					}
				}
				String inputTenantID = processCriteria.getTenantId();
				List<String> inputBusinessIds = processCriteria.getBusinessIds();
				List<String> inputStatus = processCriteria.getStatus();
				if (!StatusIdNameMap.isEmpty())
					processCriteria.setStatus(
							StatusIdNameMap.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList()));
				for (Map.Entry<String, List<String>> t : tenantAndApplnNumbersMap.entrySet()) {
					processCriteria.setTenantId(t.getKey());
					processCriteria.setBusinessIds(t.getValue());
					List<HashMap<String, Object>> tenantWiseStatusCount = workflowService
							.getProcessStatusCount(requestInfo, processCriteria);
					if (bpaCitizenStatusCountMap.isEmpty()) {
						bpaCitizenStatusCountMap.addAll(tenantWiseStatusCount);
					} else {
						for (HashMap<String, Object> tenantStatusMap : tenantWiseStatusCount) {
							for (HashMap<String, Object> bpaStatusMap : bpaCitizenStatusCountMap) {
								if (bpaStatusMap.containsValue(tenantStatusMap.get(STATUS_ID))) {
									bpaStatusMap.put(COUNT, Integer.parseInt(String.valueOf(bpaStatusMap.get(COUNT)))
											+ Integer.parseInt(String.valueOf(tenantStatusMap.get(COUNT))));
								}
							}
						}
					}
				}
				statusCountMap = bpaCitizenStatusCountMap;
				processCriteria.setTenantId(inputTenantID);
				processCriteria.setBusinessIds(inputBusinessIds);
				processCriteria.setStatus(inputStatus);
			}

			/*
			 * In the WF statuscount API, locality based fileter is not supported. To
			 * support status wise count based on locality, with status and locality API is
			 * called and those count will be set in statuscount response.
			 */
			if (processCriteria != null && !ObjectUtils.isEmpty(processCriteria.getModuleName())
					&& processCriteria.getModuleName().equals(BPA)) {
				if (moduleSearchCriteria.get(LOCALITY_PARAM) != null) {
					for (Map<String, Object> statusWiseCount : statusCountMap) {
						List<String> statusList = new ArrayList<>();
						statusList.add(String.valueOf(statusWiseCount.get(STATUS_ID)));
						criteria.getProcessSearchCriteria().setStatus(statusList);
						Integer count = bpaInboxFilterService.fetchApplicationCountFromSearcher(criteria,
								StatusIdNameMap, requestInfo);
						if (count == 0) {
							statusWiseCount.clear();
						} else {
							statusWiseCount.put(COUNT, count);
						}
					}
					criteria.getProcessSearchCriteria().setStatus(inputStatuses);
				}
				if (!statusCountMap.isEmpty()) {
					List<HashMap<String, Object>> bpaInboxStatusCountMap = new ArrayList<>();
					for (HashMap<String, Object> bpaLoclalityStatusCount : statusCountMap) {
						if (!bpaLoclalityStatusCount.isEmpty())
							bpaInboxStatusCountMap.add(bpaLoclalityStatusCount);
					}
					statusCountMap = bpaInboxStatusCountMap;
				}
			}
			Boolean isSearchResultEmpty = false;
			List<String> businessKeys = new ArrayList<>();

			if (!ObjectUtils.isEmpty(processCriteria.getModuleName())
					&& (processCriteria.getModuleName().equals(TL) || processCriteria.getModuleName().equals(BPAREG))) {
				totalCount = tlInboxFilterService.fetchApplicationCountFromSearcher(criteria, StatusIdNameMap,
						requestInfo);
				List<String> applicationNumbers = tlInboxFilterService.fetchApplicationNumbersFromSearcher(criteria,
						StatusIdNameMap, requestInfo);
				if (!CollectionUtils.isEmpty(applicationNumbers)) {
					moduleSearchCriteria.put(APPLICATION_NUMBER_PARAM, applicationNumbers);
					businessKeys.addAll(applicationNumbers);
					moduleSearchCriteria.remove(TLConstants.STATUS_PARAM);
					moduleSearchCriteria.remove(LOCALITY_PARAM);
					moduleSearchCriteria.remove(OFFSET_PARAM);
				} else {
					isSearchResultEmpty = true;
				}
			}


			if (processCriteria != null && !ObjectUtils.isEmpty(processCriteria.getModuleName())
					&& processCriteria.getModuleName().equals(BPA)) {
				totalCount = bpaInboxFilterService.fetchApplicationCountFromSearcher(criteria, StatusIdNameMap,
						requestInfo);
				List<String> applicationNumbers = bpaInboxFilterService.fetchApplicationNumbersFromSearcher(criteria,
						StatusIdNameMap, requestInfo);
				if (!CollectionUtils.isEmpty(applicationNumbers)) {
					moduleSearchCriteria.put(BPA_APPLICATION_NUMBER_PARAM, applicationNumbers);
					businessKeys.addAll(applicationNumbers);
					moduleSearchCriteria.remove(STATUS_PARAM);
					moduleSearchCriteria.remove(MOBILE_NUMBER_PARAM);
					moduleSearchCriteria.remove(LOCALITY_PARAM);
					moduleSearchCriteria.remove(OFFSET_PARAM);
				} else {
					isSearchResultEmpty = true;
				}
			}

			if (processCriteria != null && !ObjectUtils.isEmpty(processCriteria.getModuleName())
					&& processCriteria.getModuleName().equals(NOC)) {
				totalCount = nocInboxFilterService.fetchApplicationCountFromSearcher(criteria, StatusIdNameMap,
						requestInfo);
				List<String> applicationNumbers = nocInboxFilterService.fetchApplicationNumbersFromSearcher(criteria,
						StatusIdNameMap, requestInfo);
				if (!CollectionUtils.isEmpty(applicationNumbers)) {
					moduleSearchCriteria.put(NOC_APPLICATION_NUMBER_PARAM, applicationNumbers);
					businessKeys.addAll(applicationNumbers);
					moduleSearchCriteria.remove(STATUS_PARAM);
					moduleSearchCriteria.remove(MOBILE_NUMBER_PARAM);
					moduleSearchCriteria.remove(LOCALITY_PARAM);
					moduleSearchCriteria.remove(OFFSET_PARAM);
				} else {
					isSearchResultEmpty = true;
				}
			}

			List<Map<String, Object>> result = new ArrayList<>();
			businessObjects = new JSONArray();
			// Search module specific data from respective modules. Works for all modules
			if (!isSearchResultEmpty) {
				businessObjects = fetchModuleObjects(moduleSearchCriteria, businessServiceName, criteria.getTenantId(),
						requestInfo, srvMap, processCriteria.getModuleName(), criteria);
			}
			Map<String, Object> businessMap = new HashMap<>();

				businessMap = StreamSupport.stream(businessObjects.spliterator(), false)
						.collect(Collectors.toMap(s1 -> ((JSONObject) s1).get(businessIdParam).toString(), s1 -> s1,
								(e1, e2) -> e1, LinkedHashMap::new));



			ArrayList businessIds = new ArrayList();
			businessIds.addAll(businessMap.keySet());
			processCriteria.setBusinessIds(businessIds);
			processCriteria.setIsProcessCountCall(false);

            ProcessInstanceResponse processInstanceResponse;
			/*
			 * In BPA, the stakeholder can able to submit applications for multiple cities
			 * and in the single inbox all cities submitted applications need to show
			 */
			if (processCriteria != null && !ObjectUtils.isEmpty(processCriteria.getModuleName())
					&& processCriteria.getModuleName().equals(BPA) && roles.contains(BpaConstants.CITIZEN)) {
				Map<String, List<String>> tenantAndApplnNoForProcessInstance = new HashMap<>();
				for (Object businessId : businessIds) {
					for (Map.Entry<String, List<String>> tenantAppln : tenantAndApplnNumbersMap.entrySet()) {
						String tenantId = tenantAppln.getKey();
						if (tenantAppln.getValue().contains(String.valueOf(businessId))) {
							if (tenantAndApplnNoForProcessInstance.containsKey(tenantId)) {
								tenantAndApplnNoForProcessInstance.get(tenantId).add(String.valueOf(businessId));
							} else {
								List<String> businessIdsList = new ArrayList<>();
								businessIdsList.add(String.valueOf(businessId));
								tenantAndApplnNoForProcessInstance.put(tenantId, businessIdsList);
							}
						}
					}
				}
				ProcessInstanceResponse processInstanceRes = new ProcessInstanceResponse();
				for (Map.Entry<String, List<String>> appln : tenantAndApplnNoForProcessInstance.entrySet()) {
					processCriteria.setTenantId(appln.getKey());
					processCriteria.setBusinessIds(appln.getValue());
					ProcessInstanceResponse processInstance = workflowService.getProcessInstance(processCriteria,
							requestInfo);
					processInstanceRes.setResponseInfo(processInstance.getResponseInfo());
					if (processInstanceRes.getProcessInstances() == null)
						processInstanceRes.setProcessInstances(processInstance.getProcessInstances());
					else
						processInstanceRes.getProcessInstances().addAll(processInstance.getProcessInstances());
				}
				processInstanceResponse = processInstanceRes;
			} else {
				processInstanceResponse = workflowService.getProcessInstance(processCriteria, requestInfo);
			}

			List<ProcessInstance> processInstances = processInstanceResponse.getProcessInstances();

			Map<String, ProcessInstance> processInstanceMap = new HashMap<>();
			if (!CollectionUtils.isEmpty(processInstances)) {
				for (ProcessInstance processInstance : processInstances) {
					processInstanceMap.put(processInstance.getBusinessId(), processInstance);
				}
			}


			Map<String, Object> finalBusinessMap = businessMap; // Create a final reference
			if (businessObjects.length() > 0 && processInstances.size() > 0) {
				if (CollectionUtils.isEmpty(businessKeys)) {
					businessMap.keySet().forEach(businessKey -> {
						if (null != processInstanceMap.get(businessKey)) {

								// For non- Bill Amendment Inbox search
								Inbox inbox = new Inbox();
								inbox.setProcessInstance(processInstanceMap.get(businessKey));
								inbox.setBusinessObject(toMap((JSONObject) finalBusinessMap.get(businessKey)));
								inboxes.add(inbox);

						}
					});

				} else {
					// For non- Bill Amendment Inbox search

						businessKeys.forEach(businessKey -> {
							Inbox inbox = new Inbox();
							inbox.setProcessInstance(processInstanceMap.get(businessKey));
							inbox.setBusinessObject(toMap((JSONObject) finalBusinessMap.get(businessKey)));
							if (inbox.getProcessInstance() != null)
								inboxes.add(inbox);
						});

				}
			}
			// This is to handle the case when Bpa applications are fetched without process instances for Development Authoritiy users for all tenants under a single planning area
			else if(processInstances.isEmpty() && moduleName.equals(BPA) && businessObjects.length() > 0 &&  criteria.getPlanningAreaCode() != null && !criteria.getPlanningAreaCode().isEmpty()) {
				businessMap.keySet().forEach(businessKey -> {
					Inbox inbox = new Inbox();
					inbox.setBusinessObject(toMap((JSONObject) finalBusinessMap.get(businessKey)));
					inboxes.add(inbox);
				});
			}
		} else {
			processCriteria.setOffset(criteria.getOffset());
			processCriteria.setLimit(criteria.getLimit());

			ProcessInstanceResponse processInstanceResponse = workflowService.getProcessInstance(processCriteria,
					requestInfo);
			List<ProcessInstance> processInstances = processInstanceResponse.getProcessInstances();
            Map<String, ProcessInstance> processInstanceMap = processInstances.stream()
					.collect(Collectors.toMap(ProcessInstance::getBusinessId, Function.identity()));
			moduleSearchCriteria = new HashMap<String, String>();
			if (CollectionUtils.isEmpty(srvMap)) {
				throw new CustomException(ErrorConstants.INVALID_MODULE,
						"config not found for the businessService : " + businessServiceName);
			}
			String businessIdParam = srvMap.get("businessIdProperty");
			moduleSearchCriteria.put(srvMap.get("applNosParam"),
					StringUtils.arrayToDelimitedString(processInstanceMap.keySet().toArray(), ","));
			moduleSearchCriteria.put("tenantId", criteria.getTenantId());
			// moduleSearchCriteria.put("offset", criteria.getOffset());
			moduleSearchCriteria.put("limit", -1);
			businessObjects = fetchModuleObjects(moduleSearchCriteria, businessServiceName, criteria.getTenantId(),
					requestInfo, srvMap, processCriteria.getModuleName(), criteria);
			Map<String, Object> businessMap = StreamSupport.stream(businessObjects.spliterator(), false)
					.collect(Collectors.toMap(s1 -> ((JSONObject) s1).get(businessIdParam).toString(), s1 -> s1));

			if (businessObjects.length() > 0 && processInstances.size() > 0) {
				processInstanceMap.keySet().forEach(pinstance -> {
					Inbox inbox = new Inbox();
					inbox.setProcessInstance(processInstanceMap.get(pinstance));
					inbox.setBusinessObject(toMap((JSONObject) businessMap.get(pinstance)));
					inboxes.add(inbox);
				});
			}

		}

		log.info("statusCountMap size :::: " + statusCountMap.size());

		response.setTotalCount(totalCount);
		response.setNearingSlaCount(nearingSlaProcessCount);
		response.setStatusMap(statusCountMap);
		response.setItems(inboxes);
		return response;
	}

	private Map<String, String> fetchAppropriateServiceMap(List<String> businessServiceName, String moduleName) {
		StringBuilder appropriateKey = new StringBuilder();
		for (String businessServiceKeys : config.getServiceSearchMapping().keySet()) {
			if (businessServiceKeys.contains(businessServiceName.get(0))) {
				appropriateKey.append(businessServiceKeys);
				break;
			}
		}
		if (ObjectUtils.isEmpty(appropriateKey)) {
			throw new CustomException("EG_INBOX_SEARCH_ERROR",
					"Inbox service is not configured for the provided business services");
		}

		for (String inputBusinessService : businessServiceName) {
				if (!appropriateKey.toString().contains(inputBusinessService)) {
					throw new CustomException("EG_INBOX_SEARCH_ERROR", "Cross module search is NOT allowed.");
				}
		}
		return config.getServiceSearchMapping().get(appropriateKey.toString());
	}

	private JSONArray fetchModuleObjects(HashMap moduleSearchCriteria, List<String> businessServiceName,
										 String tenantId, RequestInfo requestInfo, Map<String, String> srvMap, String moduleName, InboxSearchCriteria criteria) {
		JSONArray resutls = null;

		if (CollectionUtils.isEmpty(srvMap) || StringUtils.isEmpty(srvMap.get("searchPath"))) {
			throw new CustomException(ErrorConstants.INVALID_MODULE_SEARCH_PATH,
					"search path not configured for the businessService : " + businessServiceName);
		}
		StringBuilder url = new StringBuilder(srvMap.get("searchPath"));
		url.append("?tenantId=").append(tenantId);

		// Add isInboxSearch flag for BPA module only when planningAreaCode is present
		if (BPA.equals(moduleName) && criteria != null
				&& criteria.getPlanningAreaCode() != null && !criteria.getPlanningAreaCode().isEmpty()) {
			url.append("&isInboxSearch=true");
		}

		Set<String> searchParams = moduleSearchCriteria.keySet();
		searchParams.forEach((param) -> {

			if (!param.equalsIgnoreCase("tenantId")) {

				if (moduleSearchCriteria.get(param) instanceof Collection) {
					url.append("&").append(param).append("=");
					url.append(StringUtils
							.arrayToDelimitedString(((Collection<?>) moduleSearchCriteria.get(param)).toArray(), ","));
				} else if (param.equalsIgnoreCase("appStatus")) {
					url.append("&").append("applicationStatus").append("=")
							.append(moduleSearchCriteria.get(param).toString());
				} else if (param.equalsIgnoreCase("consumerNo")) {
					url.append("&").append("connectionNumber").append("=")
							.append(moduleSearchCriteria.get(param).toString());
				}else if (null != moduleSearchCriteria.get(param)) {
					url.append("&").append(param).append("=").append(moduleSearchCriteria.get(param).toString());
				}
			}
		});

		log.info("\nfetchModuleObjects URL :::: " + url.toString());

		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);

		LinkedHashMap responseMap;
		try {
			responseMap = mapper.convertValue(result, LinkedHashMap.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException(ErrorConstants.PARSING_ERROR,
					"Failed to parse response of ProcessInstance Count");
		}

		JSONObject jsonObject = new JSONObject(responseMap);

		try {
			resutls = (JSONArray) jsonObject.getJSONArray(srvMap.get("dataRoot"));
		} catch (Exception e) {
			throw new CustomException(ErrorConstants.INVALID_MODULE_DATA,
					" search api could not find data in dataroot " + srvMap.get("dataRoot"));
		}

		return resutls;
	}

	public static Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

		if (object == null) {
			return map;
		}
		Iterator<String> keysItr = object.keys();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	public static List<Object> toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

}
