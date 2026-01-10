package org.egov.bpa.validator;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import org.apache.commons.lang.StringUtils;
import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.service.EDCRService;
import org.egov.bpa.service.NocService;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAErrorConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.web.model.*;
import org.egov.bpa.web.model.NOC.Noc;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BPAValidator {

	@Autowired
	private MDMSValidator mdmsValidator;
	
	@Autowired
	private BPAConfiguration config;
	
	@Autowired
	private EDCRService edcrService;

	@Autowired
	private BPAUtil bpaUtil;
	
	@Autowired
	private NocService nocService;

	/**
	 * Validates the BPA request during creation.
	 * Ensures that MDMS data, application documents, and risk type are valid.
	 *
	 * @param bpaRequest The BPA request object.
	 * @param mdmsData The MDMS data for validation.
	 * @param values Additional values for validation.
	 */
	public void validateCreate(BPARequest bpaRequest, Object tenantMdmsData, Object stateMdmsData) {
		/*Map<String, String> additionalDetails = bpaRequest.getBPA().getAdditionalDetails() != null
                ? (Map<String, String>) bpaRequest.getBPA().getAdditionalDetails()
                : new HashMap<String, String>();*/
        Map<String, Set<String>> lookup = new HashMap<>();
		mdmsValidator.validateMdmsData(bpaRequest, tenantMdmsData, lookup);
		mdmsValidator.validateStateMdmsData(bpaRequest, stateMdmsData, lookup);
		//TODO: need to check if it can be used
	//	validateApplicationDocuments(bpaRequest, mdmsData, null, values);

       /* if (bpaRequest.getBPA().getRiskType() != null) {
            additionalDetails.put(BPAConstants.RISKTYPE, bpaRequest.getBPA().getRiskType());
        }*/
		
	}


	/**
	 * Validates the application documents in the BPA request against MDMS data.
	 * Ensures that required documents are present and valid.
	 *
	 * @param request The BPA request object.
	 * @param mdmsData The MDMS data for validation.
	 * @param currentState The current state of the workflow.
	 * @param values Additional values for validation.
	 */
	private void validateApplicationDocuments(BPARequest request, Object mdmsData, String currentState, Map<String, String> values) {
		Map<String, List<String>> masterData = mdmsValidator.getAttributeValuesForState(mdmsData);
		BPA bpa = request.getBPA();

		if (!bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_REJECT)
				&& !bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_ADHOC)
				&& !bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_PAY)) {

			String applicationType = values.get(BPAConstants.APPLICATIONTYPE);
			String serviceType = values.get(BPAConstants.SERVICETYPE);
			
			String filterExp = "$.[?(@.applicationType=='" + applicationType + "' && @.ServiceType=='"
					+ serviceType + "' && @.RiskType=='" + bpa.getRiskType() + "' && @.WFState=='"
					+ currentState + "')].docTypes";
			
			List<Object> docTypeMappings = JsonPath.read(masterData.get(BPAConstants.DOCUMENT_TYPE_MAPPING), filterExp);

			List<Document> allDocuments = new ArrayList<Document>();
			if (bpa.getDocuments() != null) {
				allDocuments.addAll(bpa.getDocuments());
			}

			if (CollectionUtils.isEmpty(docTypeMappings)) {
				return;
			}

			filterExp = "$.[?(@.required==true)].code";
			List<String> requiredDocTypes = JsonPath.read(docTypeMappings.get(0), filterExp);

			List<String> validDocumentTypes = masterData.get(BPAConstants.DOCUMENT_TYPE);

			if (!CollectionUtils.isEmpty(allDocuments)) {

				allDocuments.forEach(document -> {

					if (!validDocumentTypes.contains(document.getDocumentType())) {
						throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_DOCUMENTTYPE,
								document.getDocumentType() + " is Unkown");
					}
				});

				if (requiredDocTypes.size() > 0 && allDocuments.size() < requiredDocTypes.size()) {

					throw new CustomException(BPAErrorConstants.BPA_MDNADATORY_DOCUMENTPYE_MISSING,
							BPAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
				} else if (requiredDocTypes.size() > 0) {

					List<String> addedDocTypes = new ArrayList<String>();
					allDocuments.forEach(document -> {

						String docType = document.getDocumentType();
						int lastIndex = docType.lastIndexOf(".");
						String documentNs = "";
						if (lastIndex > 1) {
							documentNs = docType.substring(0, lastIndex);
						} else if (lastIndex == 1) {
							throw new CustomException(BPAErrorConstants.BPA_INVALID_DOCUMENTTYPE,
									document.getDocumentType() + " is Invalid");
						} else {
							documentNs = docType;
						}

						addedDocTypes.add(documentNs);
					});
					requiredDocTypes.forEach(docType -> {
						String docType1 = docType.toString();
						if (!addedDocTypes.contains(docType1)) {
							throw new CustomException(BPAErrorConstants.BPA_MDNADATORY_DOCUMENTPYE_MISSING,
									"Document Type " + docType1 + " is Missing");
						}
					});
				}
			} else if (requiredDocTypes.size() > 0) {
				throw new CustomException(BPAErrorConstants.BPA_MDNADATORY_DOCUMENTPYE_MISSING,
						"Atleast " + requiredDocTypes.size() + " Documents are requied ");
			}
			bpa.setDocuments(allDocuments);
		}

	}

	/**
	 * Validates that there are no duplicate documents in the BPA request.
	 * Ensures that each document has a unique file store ID.
	 *
	 * @param request The BPA request object.
	 */
	private void validateDuplicateDocuments(BPARequest request) {
		if (request.getBPA().getDocuments() != null) {
			List<String> documentFileStoreIds = new LinkedList<String>();
			request.getBPA().getDocuments().forEach(document -> {
				if (documentFileStoreIds.contains(document.getFileStoreId()))
					throw new CustomException(BPAErrorConstants.BPA_DUPLICATE_DOCUMENT, "Same document cannot be used multiple times");
				else
					documentFileStoreIds.add(document.getFileStoreId());
			});
		}
	}

	/**
	 * Validates the search criteria for BPA search requests.
	 * Ensures that the search parameters are valid based on user type and configuration.
	 *
	 * @param requestInfo The request info containing user details.
	 * @param criteria The BPA search criteria.
	 */
   //TODO: need to make the changes in the data
	public void validateSearch(RequestInfo requestInfo, BPASearchCriteria criteria) {
		
		log.info("Validating Search Parameters ");
		log.info("User Type: " + requestInfo.getUserInfo().getType());
		log.info("criteria.isEmpty(): " + criteria.isEmpty());
		
		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.CITIZEN) && criteria.isEmpty())
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "Search without any parameters is not allowed");

		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.CITIZEN) && !criteria.tenantIdOnly()
				&& criteria.getTenantId() == null)
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "TenantId is mandatory in search");

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.CITIZEN) && !criteria.isEmpty()
				&& !criteria.tenantIdOnly() && criteria.getTenantId() == null)
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "TenantId is mandatory in search");

		String allowedParamStr = null;

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.CITIZEN))
			allowedParamStr = config.getAllowedCitizenSearchParameters();
		else if (requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.EMPLOYEE))
			allowedParamStr = config.getAllowedEmployeeSearchParameters();
		else
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH,
					"The userType: " + requestInfo.getUserInfo().getType() + " does not have any search config");

		if (StringUtils.isEmpty(allowedParamStr) && !criteria.isEmpty())
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "No search parameters are expected");
		else {
			List<String> allowedParams = Arrays.asList(allowedParamStr.split(","));
			log.info("Allowed Parameters: " + allowedParams);
			validateSearchParams(criteria, allowedParams);
		}
	}

	/**
	 * Validates individual search parameters against allowed parameters.
	 *
	 * @param criteria The BPA search criteria.
	 * @param allowedParams The list of allowed search parameters.
	 */
	private void validateSearchParams(BPASearchCriteria criteria, List<String> allowedParams) {

		if (criteria.getApplicationNo() != null && !allowedParams.contains("applicationNo"))
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "Search on applicationNo is not allowed");

		if (criteria.getEdcrNumber() != null && !allowedParams.contains("edcrNumber"))
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "Search on edcrNumber is not allowed");

		if (criteria.getStatus() != null && !allowedParams.contains("status"))
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "Search on Status is not allowed");

		if (criteria.getIds() != null && !allowedParams.contains("ids"))
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "Search on ids is not allowed");

		if (criteria.getMobileNumber() != null && !allowedParams.contains("mobileNumber"))
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "Search on mobileNumber is not allowed");

		if (criteria.getOffset() != null && !allowedParams.contains("offset"))
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "Search on offset is not allowed");

		if (criteria.getLimit() != null && !allowedParams.contains("limit"))
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "Search on limit is not allowed");
		
		if (criteria.getApprovalDate() != null && (criteria.getApprovalDate() > new Date().getTime()))
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "Permit Order Genarated date cannot be a future date");
		
		if (criteria.getName() != null && !allowedParams.contains("name"))
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "Search on name is not allowed");

		if (criteria.getDistrict() != null && !allowedParams.contains("district"))
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "Search on district is not allowed");
		
		if (criteria.getFromDate() != null && (criteria.getFromDate() > new Date().getTime()))
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "From date cannot be a future date");

		if (criteria.getToDate() != null && criteria.getFromDate() != null
				&& (criteria.getFromDate() > criteria.getToDate()))
			throw new CustomException(BPAErrorConstants.INVALID_SEARCH, "To date cannot be prior to from date");
	}

	/**
	 * valide the update BPARequest
	 * @param bpaRequest
	 * @param tenantMdmsData
	 * @param stateMdmsData
	 */
	public void validateUpdate(BPARequest bpaRequest, Object tenantMdmsData, Object stateMdmsData) {

//		BPA bpa = bpaRequest.getBPA();
//		validateApplicationDocuments(bpaRequest, mdmsData, currentState, edcrResponse);
//		validateAllIds(searchResult, bpa);
        Map<String, Set<String>> lookup = new HashMap<>();
		mdmsValidator.validateMdmsData(bpaRequest, tenantMdmsData, lookup);
        mdmsValidator.validateStateMdmsData(bpaRequest, stateMdmsData, lookup);
		validateDuplicateDocuments(bpaRequest);
//		setFieldsFromSearch(bpaRequest, searchResult, mdmsData);

	}

	/**
	 * Set the fields from search result to the bpaRequest
	 * @param bpaRequest
	 * @param searchResult
	 * @param mdmsData
	 */
	private void setFieldsFromSearch(BPARequest bpaRequest, List<BPA> searchResult, Object mdmsData) {
		Map<String, BPA> idToBPAFromSearch = new HashMap<>();

		searchResult.forEach(bpa -> {
			idToBPAFromSearch.put(bpa.getId(), bpa);
		});

		bpaRequest.getBPA().getAuditDetails()
				.setCreatedBy(idToBPAFromSearch.get(bpaRequest.getBPA().getId()).getAuditDetails().getCreatedBy());
		bpaRequest.getBPA().getAuditDetails()
				.setCreatedTime(idToBPAFromSearch.get(bpaRequest.getBPA().getId()).getAuditDetails().getCreatedTime());
		bpaRequest.getBPA().setStatus(idToBPAFromSearch.get(bpaRequest.getBPA().getId()).getStatus());
	}



	/**
	 * validate all the ids in the search result
	 * @param searchResult
	 * @param bpa
	 */
	private void validateAllIds(List<BPA> searchResult, BPA bpa) {

		Map<String, BPA> idToBPAFromSearch = new HashMap<>();
		searchResult.forEach(bpas -> {
			idToBPAFromSearch.put(bpas.getId(), bpas);
		});

		Map<String, String> errorMap = new HashMap<>();
		BPA searchedBpa = idToBPAFromSearch.get(bpa.getId());

		if (!searchedBpa.getApplicationNo().equalsIgnoreCase(bpa.getApplicationNo()))
			errorMap.put("INVALID UPDATE", "The application number from search: " + searchedBpa.getApplicationNo()
					+ " and from update: " + bpa.getApplicationNo() + " does not match");

		if (!searchedBpa.getId().equalsIgnoreCase(bpa.getId()))
			errorMap.put("INVALID UPDATE", "The id " + bpa.getId() + " does not exist");

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}




	/**
	 * validate the checklist questions and documents
	 * @param mdmsData
	 * @param bpaRequest
	 * @param wfState
	 */
	public void validateCheckList(Object mdmsData, BPARequest bpaRequest, String wfState) {
		BPA bpa = bpaRequest.getBPA();
	     
		Map<String, String> edcrResponse = new HashMap<>();
		
		edcrResponse = edcrService.getEDCRDetails(bpaRequest.getRequestInfo(), bpaRequest.getBPA());
		log.debug("applicationType is " + edcrResponse.get(BPAConstants.APPLICATIONTYPE));
        log.debug("serviceType is " + edcrResponse.get(BPAConstants.SERVICETYPE));
        
		validateQuestions(mdmsData, bpa, wfState, edcrResponse);
		validateFIDocTypes(mdmsData, bpa, wfState, edcrResponse);
	}

	
	/**
	 * Validate the checklist questions from the request with the MDMS data
	 * @param mdmsData
	 * @param bpa
	 * @param wfState
	 * @param edcrResponse
	 */
	@SuppressWarnings(value = { "unchecked", "rawtypes" })
	private void validateQuestions(Object mdmsData, BPA bpa, String wfState, Map<String, String> edcrResponse) {
		List<String> mdmsQns = null;

		log.debug("Fetching MDMS result for the state " + wfState);

		try {
			String questionsPath = BPAConstants.QUESTIONS_MAP.replace("{1}", wfState)
					.replace("{2}", bpa.getRiskType().toString()).replace("{3}", edcrResponse.get(BPAConstants.SERVICETYPE))
					.replace("{4}", edcrResponse.get(BPAConstants.APPLICATIONTYPE));

			List<Object> mdmsQuestionsArray = (List<Object>) JsonPath.read(mdmsData, questionsPath);

			if (!CollectionUtils.isEmpty(mdmsQuestionsArray))
				mdmsQns = JsonPath.read(mdmsQuestionsArray.get(0), BPAConstants.QUESTIONS_PATH);

			log.debug("MDMS questions " + mdmsQns);
			if (!CollectionUtils.isEmpty(mdmsQns)) {
				if (bpa.getAdditionalDetails() != null) {
					List checkListFromReq = (List) ((Map) bpa.getAdditionalDetails()).get(wfState.toLowerCase());
					if (!CollectionUtils.isEmpty(checkListFromReq)) {
						for (int i = 0; i < checkListFromReq.size(); i++) {
							// MultiItem framework adding isDeleted object to
							// additionDetails object whenever report is being
							// removed.
							// So skipping that object validation.
							if (((Map) checkListFromReq.get(i)).containsKey("isDeleted")) {
								checkListFromReq.remove(i);
								i--;
								continue;
							}
							List<Map> requestCheckList = new ArrayList<Map>();
							List<String> requestQns = new ArrayList<String>();
							validateDateTime((Map)checkListFromReq.get(i));
							List<Map> questions = ((Map) checkListFromReq.get(i))
									.get(BPAConstants.QUESTIONS_TYPE) != null
											? (List<Map>) ((Map) checkListFromReq.get(i))
													.get(BPAConstants.QUESTIONS_TYPE)
											: null;
							if (questions != null)
								requestCheckList.addAll(questions);
							if (!CollectionUtils.isEmpty(requestCheckList)) {
								for (Map reqQn : requestCheckList) {
									requestQns.add((String) reqQn.get(BPAConstants.QUESTION_TYPE));
								}
							}

							log.debug("Request questions " + requestQns);

							if (!CollectionUtils.isEmpty(requestQns)) {
								if (requestQns.size() < mdmsQns.size())
									throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_QUESTIONS,
											BPAErrorConstants.BPA_UNKNOWN_QUESTIONS_MSG);
								else {
									List<String> pendingQns = new ArrayList<String>();
									for (String qn : mdmsQns) {
										if (!requestQns.contains(qn)) {
											pendingQns.add(qn);
										}
									}
									if (pendingQns.size() > 0) {
										throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_QUESTIONS,
												BPAErrorConstants.BPA_UNKNOWN_QUESTIONS_MSG);
									}
								}
							} else {
								throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_QUESTIONS,
										BPAErrorConstants.BPA_UNKNOWN_QUESTIONS_MSG);
							}
						}
					} else {
						throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_QUESTIONS, BPAErrorConstants.BPA_UNKNOWN_QUESTIONS_MSG);
					}
				} else {
					throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_QUESTIONS, BPAErrorConstants.BPA_UNKNOWN_QUESTIONS_MSG);
				}
			}
		} catch (PathNotFoundException ex) {
			log.error("Exception occured while validating the Checklist Questions" + ex.getMessage());
		}
	}

	/**
	 * Validate fieldinspection documents and their documentTypes
	 * @param mdmsData
	 * @param bpa
	 * @param wfState
	 * @param edcrResponse
	 */
	@SuppressWarnings(value = { "unchecked", "rawtypes" })
	private void validateFIDocTypes(Object mdmsData, BPA bpa, String wfState, Map<String, String> edcrResponse) {
		List<String> mdmsDocs = null;

		log.debug("Fetching MDMS result for the state " + wfState);

		try {
			String docTypesPath = BPAConstants.DOCTYPES_MAP.replace("{1}", wfState)
					.replace("{2}", bpa.getRiskType().toString()).replace("{3}", edcrResponse.get(BPAConstants.SERVICETYPE))
					.replace("{4}", edcrResponse.get(BPAConstants.APPLICATIONTYPE));;

			List<Object> docTypesArray = (List<Object>) JsonPath.read(mdmsData, docTypesPath);

			if (!CollectionUtils.isEmpty(docTypesArray))
				mdmsDocs = JsonPath.read(docTypesArray.get(0), BPAConstants.DOCTYPESS_PATH);

			log.debug("MDMS DocTypes " + mdmsDocs);
			if (!CollectionUtils.isEmpty(mdmsDocs)) {
				if (bpa.getAdditionalDetails() != null) {
					List checkListFromReq = (List) ((Map) bpa.getAdditionalDetails()).get(wfState.toLowerCase());
					if (!CollectionUtils.isEmpty(checkListFromReq)) {
						for (int i = 0; i < checkListFromReq.size(); i++) {
							List<Map> requestCheckList = new ArrayList<Map>();
							List<String> requestDocs = new ArrayList<String>();
							List<Map> docs = ((Map) checkListFromReq.get(i)).get(BPAConstants.DOCS) != null
									? (List<Map>) ((Map) checkListFromReq.get(i)).get(BPAConstants.DOCS) : null;
							if (docs != null)
								requestCheckList.addAll(docs);
							
							if (!CollectionUtils.isEmpty(requestCheckList)) {
								for (Map reqDoc : requestCheckList) {
									String fileStoreId = ((String) reqDoc.get(BPAConstants.FILESTOREID));
									if (!StringUtils.isEmpty(fileStoreId)) {
										String docType = (String) reqDoc.get(BPAConstants.CODE);
										int lastIndex = docType.lastIndexOf(".");
										String documentNs = "";
										if (lastIndex > 1) {
											documentNs = docType.substring(0, lastIndex);
										} else if (lastIndex == 1) {
											throw new CustomException(BPAErrorConstants.BPA_INVALID_DOCUMENTTYPE,
													(String) reqDoc.get(BPAConstants.CODE) + " is Invalid");
										} else {
											documentNs = docType;
										}
										requestDocs.add(documentNs);
									} else {
										throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_DOCS,
												BPAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
									}
								}
							}

							log.debug("Request Docs " + requestDocs);

							if (!CollectionUtils.isEmpty(requestDocs)) {
								if (requestDocs.size() < mdmsDocs.size())
									throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_DOCS,
											BPAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
								else {
									List<String> pendingDocs = new ArrayList<String>();
									for (String doc : mdmsDocs) {
										if (!requestDocs.contains(doc)) {
											pendingDocs.add(doc);
										}
									}
									if (pendingDocs.size() > 0) {
										throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_DOCS,
												BPAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
									}
								}
							} else {
								throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_DOCS, BPAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
							}
						}
					} else {
						throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_DOCS, BPAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
					}
				} else {
					throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_DOCS, BPAErrorConstants.BPA_UNKNOWN_DOCS_MSG);
				}
			}
		} catch (PathNotFoundException ex) {
			log.error("Exception occured while validating the Checklist Documents" + ex.getMessage());
		}
	}
	
	/**
	 * Validate FieldINpsection report date and time
	 * @param checkListFromRequest
	 */
	private void validateDateTime(@SuppressWarnings("rawtypes") Map checkListFromRequest) {

		if (checkListFromRequest.get(BPAConstants.INSPECTION_DATE) == null
				|| StringUtils.isEmpty(checkListFromRequest.get(BPAConstants.INSPECTION_DATE).toString())) {
			throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_DATE, "Please mention the inspection date");
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date dt;
			try {
				dt = sdf.parse(checkListFromRequest.get(BPAConstants.INSPECTION_DATE).toString());
				long inspectionEpoch = dt.getTime();
				if (inspectionEpoch > new Date().getTime()) {
					throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_DATE, "Inspection date cannot be a future date");
				} else if (inspectionEpoch < 0) {
					throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_DATE, "Provide the date in specified format 'yyyy-MM-dd'");
				}
			} catch (ParseException e) {
				throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_DATE, "Unable to parase the inspection date");
			}
		}
		if (checkListFromRequest.get(BPAConstants.INSPECTION_TIME) == null
				|| StringUtils.isEmpty(checkListFromRequest.get(BPAConstants.INSPECTION_TIME).toString())) {
			throw new CustomException(BPAErrorConstants.BPA_UNKNOWN_TIME, "Please mention the inspection time");
		}
	}

	public void validateActionForPendingNoc(BPARequest bpaRequest){
		String action = Optional.ofNullable(bpaRequest.getBPA().getWorkflow()).map(Workflow::getAction).orElse("");
		List<String> pendingNocNotAllowedActions = Arrays.asList(config.getPendingNocNotAllowedActions().split(","));
		if(pendingNocNotAllowedActions.contains(action)) {
			List<Noc> nocs = nocService.fetchNocRecords(bpaRequest);
			if (!CollectionUtils.isEmpty(nocs)) {
				for (Noc noc : nocs) {
						List<String> statuses = Arrays.asList(config.getNocValidationCheckStatuses().split(","));
						if(!statuses.contains(noc.getApplicationStatus())) {
							log.error("Noc is not approved having applicationNo :" + noc.getApplicationNo());
							throw new CustomException(BPAErrorConstants.NOC_SERVICE_EXCEPTION,
									" Application can't be forwarded without NOC "
											+ StringUtils.join(statuses, " or "));
						}
				}
			}
		}

	}

    public void validateChecklist(BPARequest request) {
        BPA bpa = request.getBPA();
        String tenantId = bpa.getTenantId();
        String state = bpaUtil.extractState(tenantId);

        // Get MDMS data for checklist
        Object mdmsData = bpaUtil.mDMSCall(request.getRequestInfo(), state);

        // Extract checklist data from MDMS
        List<Object> checklistData = getChecklistFromMDMS(mdmsData, bpa);

        if (CollectionUtils.isEmpty(checklistData)) {
            throw new CustomException("CHECKLIST_VALIDATION_ERROR", "Checklist data fetch failed from mdms");
        }

        // Get inspection data from BPA additional details
        Object additionalDetails = bpa.getAdditionalDetails();
        if (additionalDetails == null) {
            throw new CustomException("CHECKLIST_VALIDATION_ERROR", "Additional details not found for checklist validation");
        }

        List<Map<String, Object>> inspectionData = getInspectionData(additionalDetails);
        if (CollectionUtils.isEmpty(inspectionData)) {
            throw new CustomException("CHECKLIST_VALIDATION_ERROR", "Inspection data not found for checklist validation");
        }

        validateDate(additionalDetails);

        Map<String, Object> currentInspection = inspectionData.get(0);
        validateMandatoryFields(checklistData, currentInspection);
    }

    private List<Object> getChecklistFromMDMS(Object mdmsData, BPA bpa) {
        try {
            String jsonPath = "$.MdmsRes.BPA.CheckList[?(@.applicationType=='BUILDING_PLAN_SCRUTINY' && @.WFState=='PENDING_DA_ENGINEER')].questions[*]";
            return JsonPath.read(mdmsData, jsonPath);
        } catch (Exception e) {
            log.error("Error reading checklist from MDMS: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getInspectionData(Object additionalDetails) {
        Object inspectionObj = ((Map<String, Object>) additionalDetails).get("inspectionChecklist");
        if (inspectionObj instanceof List) {
            return (List<Map<String, Object>>) inspectionObj;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private void validateDate(Object additionalDetails) {
        List<Map<String, Object>> inspectionData = (List<Map<String, Object>>) ((Map<String, Object>) additionalDetails).get("submitReportinspection_pending");

        if (!CollectionUtils.isEmpty(inspectionData)) {
            String inspectionDate = (String) inspectionData.get(0).get("inspectionDate");
            if (inspectionDate != null) {
                String today = LocalDate.now().toString();
                if (!today.equals(inspectionDate)) {
                    throw new CustomException("INVALID_INSPECTION_DATE", "Inspection date must be today's date");
                }
            }
        }
    }

    private void validateMandatoryFields(List<Object> checklistData, Map<String, Object> inspectionData) {
        Set<String> validFieldKeys = new HashSet<>();
        List<String> missingFields = new ArrayList<>();

        // Collect all valid field keys from checklist
        for (Object questionObj : checklistData) {
            Map<String, Object> question = (Map<String, Object>) questionObj;
            String fieldKey = (String) question.get("fieldKey");
            if (fieldKey != null) {
                validFieldKeys.add(fieldKey);
            }
        }

        // Validate inspection data keys
        for (String inspectionKey : inspectionData.keySet()) {
            if (!validFieldKeys.contains(inspectionKey)) {
                throw new CustomException("INVALID_FIELD_KEY", "Invalid field key in inspection data: " + inspectionKey);
            }
        }

        for (Object questionObj : checklistData) {
            Map<String, Object> question = (Map<String, Object>) questionObj;
            Boolean mandatory = (Boolean) question.get("mandatory");
            if (Boolean.TRUE.equals(mandatory)) {
                String fieldKey = (String) question.get("fieldKey");
                String questionName = (String) question.get("name");

                if (fieldKey != null) {
                    Object fieldValue = inspectionData.get(fieldKey);
                    if (fieldValue == null || (fieldValue instanceof String && ((String) fieldValue).trim().isEmpty())) {
                        missingFields.add(questionName);
                    }
                }
            }
        }

        if (!missingFields.isEmpty()) {
            String errorMessage = "Mandatory fields are missing: " + String.join(", ", missingFields);
            throw new CustomException("MANDATORY_FIELDS_MISSING", errorMessage);
        }
    }
}
