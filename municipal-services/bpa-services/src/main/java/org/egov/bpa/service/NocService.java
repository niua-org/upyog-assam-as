package org.egov.bpa.service;

import static org.egov.bpa.util.BPAConstants.INPROGRESS_STATUS;
import static org.egov.bpa.util.BPAConstants.ACTION_INITIATE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAErrorConstants;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.RequestInfoWrapper;
import org.egov.bpa.web.model.NOC.Document;
import org.egov.bpa.web.model.NOC.Noc;
import org.egov.bpa.web.model.NOC.NocRequest;
import org.egov.bpa.web.model.NOC.NocResponse;
import org.egov.bpa.web.model.NOC.NocType;
import org.egov.bpa.web.model.NOC.Workflow;
import org.egov.bpa.web.model.NOC.enums.ApplicationType;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Service
@Slf4j
public class NocService {

	@Autowired
	private EDCRService edcrService;

	@Autowired
	private NOCEvaluator nocEval;

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	/**
	 * Creates the list of applicable NOC types for a BPA application based on
	 * permit type, EDCR response, site engineer suggestions and MDMS NOC mapping.
	 *
	 * @param bpaRequest The BPA request containing application details and request
	 *                   info.
	 * @param mdmsData   MDMS master data containing NOC type mapping configuration.
	 */
	@SuppressWarnings("unchecked")
	public void createNocRequest(BPARequest bpaRequest, Object mdmsData) {

		List<String> nocTypes = new ArrayList<>();

		Map<String, String> edcrResponse = edcrService.getEDCRDetails(bpaRequest.getRequestInfo(), bpaRequest.getBPA());

		BPA bpa = bpaRequest.getBPA();
		Object additionalDetails = bpa.getAdditionalDetails();
		String permitType = null;
		if (additionalDetails instanceof Map) {
			Map<String, Object> bpaAdditionalDetails = (Map<String, Object>) additionalDetails;
			Object nocDetails = bpaAdditionalDetails.get("nocDetails");
			if (nocDetails instanceof Map) {
				Map<String, Object> nocMap = (Map<String, Object>) nocDetails;
				permitType = (String) nocMap.get("permitType");
			}
		}
		
		if (StringUtils.isBlank(permitType)) {
			throw new CustomException("ERROR", "Permit type can't be null.");
		}

		String nocPath = BPAConstants.NOC_TYPE_MAPPING_PATH.replace("{1}", permitType);
		List<Object> nocMappingResponse = (List<Object>) JsonPath.read(mdmsData, nocPath);

		// fetching Site Engr NOCs
		String SiteEngrfilterExp = "$.[?(@.source=='SITE_ENGINEER')].code";
		Set<String> allowedNocsBySiteEng = new HashSet<>(JsonPath.read(nocMappingResponse, SiteEngrfilterExp));
		nocTypes.addAll(fetchNOCBySiteEngr(bpa.getNocList(), allowedNocsBySiteEng));

		// fetching other source NOCs
		String OthersfilterExp = "$.[?(@.source!='SITE_ENGINEER')]";
		List<Map<String, Object>> nocByOthers = JsonPath.read(nocMappingResponse, OthersfilterExp);
		nocTypes.addAll(fetchNOCByOthers(edcrResponse, nocByOthers));

		// CREATE NOCs
		String applType = edcrResponse.get(BPAConstants.APPLICATIONTYPE);
		createNOCList(bpaRequest, nocTypes, applType);

		//Removing NOC details from bpa additional details
		if (bpa.getAdditionalDetails() instanceof Map) {
			Map<String, Object> details =
					(Map<String, Object>) bpa.getAdditionalDetails();
			details.remove("nocDetails");
			bpa.setAdditionalDetails(details);
		}

	}

	/**
	 * Builds a list of NOC request objects for each applicable NOC type and
	 * initiates NOC creation workflow.
	 *
	 * @param bpaRequest The BPA request containing tenant and application details.
	 * @param nocTypes   List of applicable NOC types to be created.
	 * @param applType   Application type derived from EDCR, used to determine NOC
	 *                   source.
	 */
	private void createNOCList(BPARequest bpaRequest, List<String> nocTypes, String applType) {

		List<Noc> nocs = new ArrayList<>();
		String tenantId = bpaRequest.getBPA().getTenantId();
		String applicationNo = bpaRequest.getBPA().getApplicationNo();
		ApplicationType applicationType = ApplicationType.valueOf(BPAConstants.NOC_APPLICATIONTYPE);
		String source = config.getNocSourceConfig().get(applType);
		Workflow workflow = Workflow.builder().action(ACTION_INITIATE).build();
		Object additionalDetails = bpaRequest.getBPA().getAdditionalDetails();
		Map<String, Object> nocAdditionalDetails = new HashMap<>();
		Map<String, List<Document>> docMap = new HashMap<>();

		if (additionalDetails instanceof Map) {
			Map<String, Object> adMap = (Map<String, Object>) additionalDetails;
			Object nocDetails = adMap.get("nocDetails");
			if (nocDetails instanceof Map) {
				Map<String, Object> nocMap = (Map<String, Object>) nocDetails;

				Object aaiObj = nocMap.get("AAI_NOC_DETAILS");
				if (aaiObj instanceof Map) {
					Map<String, Object> aaiMap = (Map<String, Object>) aaiObj;
					for (Map.Entry<String, Object> entry : aaiMap.entrySet()) {
						if (!"documents".equals(entry.getKey())) {
							nocAdditionalDetails.put(entry.getKey(), entry.getValue());
						}
					}

					Object docsObj = aaiMap.get("documents");
					docMap.put("CIVIL_AVIATION", getDocumentList(docsObj));

				}
			}

		}
		log.info("nocAdditionalDetails : " + nocAdditionalDetails);
		log.info("Applicable NOCs are, " + nocTypes);

		for (String nocType : nocTypes) {
			List<Document> documents = docMap.get(nocType);

			// Sets additionalDetails only in case of Civil Aviation
			if(nocType.equals("CIVIL_AVIATION")) {
				Noc noc = Noc.builder().tenantId(tenantId).applicationType(applicationType).sourceRefId(applicationNo)
						.nocType(nocType).source(source).workflow(workflow).documents(documents)
						.additionalDetails(nocAdditionalDetails).build();
				nocs.add(noc);
			} else {
				Noc noc = Noc.builder().tenantId(tenantId).applicationType(applicationType).sourceRefId(applicationNo)
						.nocType(nocType).source(source).workflow(workflow).documents(documents)
						.build();
				nocs.add(noc);
			}
		}

		//TODO: Added this FIRE NOC for testing will remove once testing is done
//		Noc noc = Noc.builder().tenantId(tenantId).applicationType(applicationType).sourceRefId(applicationNo)
//				.nocType("FIRE_SAFETY").source(source).workflow(workflow).documents(new ArrayList<>())
//				.additionalDetails(nocAdditionalDetails).build();
//		nocs.add(noc);

		log.info("Final NOC List to be created : " + nocs);

		// Retrieve the existing roles from the userInfo
		List<Role> roles = bpaRequest.getRequestInfo().getUserInfo().getRoles();

		// Check if roles are null and throw an exception
		if (roles == null) {
			throw new IllegalArgumentException("Roles list is null in the RequestInfo object");
		}

		/*Hardcoding role to bpa engineer to bypass the NOC role check BPA_ENGINEER_DA.
		 We are displaying all tenants of DA to engineer in single inbox */
		Role extraRole = Role.builder()
				.name("BPA Engineer")
				.code("BPA_ENGINEER")
				.tenantId(bpaRequest.getBPA().getTenantId())
				.build();
		roles.add(extraRole);

		// Log the updated roles for debugging
		log.info("Updated Roles with extra role: " + roles);

		NocRequest nocRequest = NocRequest.builder().nocList(nocs).requestInfo(bpaRequest.getRequestInfo()).build();

		createNoc(nocRequest);
	}
	
	/**
	 * Converts raw document data into a list of Document objects. Maps document
	 * metadata and stores fileName inside additionalDetails.
	 *
	 * @param docsObj Raw documents object (expected as List of Map)
	 * @return List of populated Document objects
	 */
	private List<Document> getDocumentList(Object docsObj) {

		List<Document> documents = new ArrayList<>();

		if (docsObj instanceof List) {

			List<Map<String, Object>> docsList = (List<Map<String, Object>>) docsObj;

			for (Map<String, Object> docMap : docsList) {
				Document document = new Document();

				document.setDocumentType((String) docMap.get("documentType"));
				document.setFileStoreId((String) docMap.get("fileStoreId"));
				document.setDocumentUid((String) docMap.get("documentUid"));

				Map<String, Object> additionalDetailsMap = new HashMap<>();
				additionalDetailsMap.put("fileName", docMap.get("fileName"));
				document.setAdditionalDetails(additionalDetailsMap);

				documents.add(document);
			}
		}
		log.info("NOC document details to be sent : {}", documents);
		return documents;
	}

	/**
	 * Determines the applicable NOCs whose eligibility depends on EDCR parameters.
	 * Reads NOC conditions from MDMS and evaluates them against EDCR response.
	 *
	 * @param edcrResponse EDCR key–value details fetched for the application.
	 * @param nocByOthers  List of NOC mapping entries other than Site Engineer
	 *                     source.
	 * @return List of applicable NOC types based on EDCR evaluation.
	 */
	private List<String> fetchNOCByOthers(Map<String, String> edcrResponse, List<Map<String, Object>> nocByOthers) {

		Map<String, List<String>> nocTypeConditionsByOthers = nocByOthers.stream()
				.collect(Collectors.toMap(n -> (String) n.get("code"), n -> (List<String>) n.get("conditions")));

		List<String> applicableNocs = new ArrayList<>();

		if (!CollectionUtils.isEmpty(nocTypeConditionsByOthers)) {
			applicableNocs =  nocEval.getApplicableNOCList(nocTypeConditionsByOthers, edcrResponse);
		} else {
			log.debug("NOC Mapping is not found!!");
			return applicableNocs;
		}
		log.info("NOCs applicable as per NOC evaluator : " + applicableNocs);
		return applicableNocs;
	}

	/**
	 * Filters the NOCs suggested by Site Engineer based on the list of allowed NOCs
	 * configured in MDMS. Invalid suggestions are logged and ignored.
	 *
	 * @param siteEngrNocs         List of NOCs suggested by Site Engineer in the
	 *                             BPA request.
	 * @param allowedNocsBySiteEng Set of NOC types allowed for SITE_ENGINEER as per
	 *                             MDMS mapping.
	 * @return List of valid NOC types requested by Site Engineer.
	 */
	private List<String> fetchNOCBySiteEngr(List<NocType> siteEngrNocs, Set<String> allowedNocsBySiteEng) {

		List<String> nocTypes = new ArrayList<>();

		if (siteEngrNocs == null || siteEngrNocs.isEmpty()) {
			log.info("No NOCs suggested by Site Engineer.");
			return nocTypes;
		}

		List<String> siteEngSuggestedNocs = siteEngrNocs.stream().map(NocType::toString).collect(Collectors.toList());

		for (String nocType : siteEngSuggestedNocs) {
			if (allowedNocsBySiteEng.contains(nocType)) {
				nocTypes.add(nocType);
			} else {
				log.warn("Site Engineer suggested invalid NOC: {}", nocType);
			}
		}

		log.info("NOCs requested by Site Engineer : " + nocTypes);
		return nocTypes;
	}

	@SuppressWarnings("unchecked")
	private void createNoc(NocRequest nocRequest) {
		StringBuilder uri = new StringBuilder(config.getNocServiceHost());
		uri.append(config.getNocCreateEndpoint());

		LinkedHashMap<String, Object> responseMap = null;
		try {
			log.info("NOC request : {}", nocRequest);
			responseMap = (LinkedHashMap<String, Object>) serviceRequestRepository.fetchResult(uri, nocRequest);
			NocResponse nocResponse = mapper.convertValue(responseMap, NocResponse.class);
			log.info("NOC response : {}", nocResponse);
		} catch (Exception se) {
			throw new CustomException(BPAErrorConstants.NOC_SERVICE_EXCEPTION,
					" Failed to create NOC of Type " + nocRequest.getNoc().getNocType());
		}
	}

	@SuppressWarnings("unchecked")
	public List<Noc> fetchNocRecords(BPARequest bpaRequest) {

		StringBuilder url = getNOCWithSourceRef(bpaRequest);

		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(bpaRequest.getRequestInfo())
				.build();
		LinkedHashMap<String, Object> responseMap = null;
		try {
			responseMap = (LinkedHashMap<String, Object>) serviceRequestRepository.fetchResult(url, requestInfoWrapper);
			NocResponse nocResponse = mapper.convertValue(responseMap, NocResponse.class);
			return nocResponse.getNoc();
		} catch (Exception e) {
			throw new CustomException(BPAErrorConstants.NOC_SERVICE_EXCEPTION, " Unable to fetch the NOC records");
		}
	}

	/**
	 * fetch the noc records with sourceRefId
	 * @param bpaRequest
	 * @return
	 */
	private StringBuilder getNOCWithSourceRef(BPARequest bpaRequest) {
		StringBuilder uri = new StringBuilder(config.getNocServiceHost());
		uri.append(config.getNocSearchEndpoint());
		uri.append("?tenantId=");
		uri.append(bpaRequest.getBPA().getTenantId());
		NocRequest nocRequest = new NocRequest();
		nocRequest.setRequestInfo(bpaRequest.getRequestInfo());
		uri.append("&sourceRefId=");
		uri.append(bpaRequest.getBPA().getApplicationNo());
		return uri;
	}

}
