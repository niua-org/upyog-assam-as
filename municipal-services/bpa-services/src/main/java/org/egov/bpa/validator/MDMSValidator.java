package org.egov.bpa.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAErrorConstants;
import org.egov.bpa.web.model.AreaMappingDetail;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.RTPAllocationDetails;
import org.egov.bpa.web.model.landInfo.Address;
import org.egov.bpa.web.model.landInfo.LandInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MDMSValidator {

	/**
	 * Method to validate the mdms data in the request
	 * master data is fetched from mdmsData object passed from service layer
	 * master lookup is built from the fetched master data for efficient validation
	 * master array contains all the master names to be validated
	 * <p>
	 * validateIfMasterPresent checks if the master data is present for all the masters in the master array
	 * validateRequestValues checks if the request values are present in the master data
	 * </p>
	 * @param bpaRequest
	 * @param mdmsData
	 */
	public void validateMdmsData(BPARequest bpaRequest, Object mdmsData) {

	    Map<String, List<String>> masterData = getAttributeValuesForTenant(mdmsData);

	    Map<String, Set<String>> masterLookup = buildMasterLookup(masterData);

	    List<String> masterList = new ArrayList<>();

	    if (masterData.containsKey(BPAConstants.REVENUE_VILLAGE)) {
	        masterList.add(BPAConstants.REVENUE_VILLAGE);
	    }

	    if (masterData.containsKey(BPAConstants.VILLAGES)) {
	        masterList.add(BPAConstants.VILLAGES);
	    }

	    String[] masterArray = masterList.toArray(new String[0]);

	    if (log.isInfoEnabled() && bpaRequest != null && bpaRequest.getBPA() != null) {
	        log.info("Validating master data from MDMS for : {}", bpaRequest.getBPA().getApplicationNo());
	    }

	    validateIfMasterPresent(masterArray, masterData);
	    validateRequestValues(bpaRequest, masterLookup);
	}

	
	public void validateStateMdmsData(BPARequest bpaRequest, Object mdmsData) {

		Map<String, List<String>> masterData = getAttributeValuesForState(mdmsData);
		
		Map<String, Set<String>> masterLookup = buildMasterLookup(masterData);
		String[] masterArray = { 
	          	BPAConstants.SERVICE_TYPE, BPAConstants.APPLICATION_TYPE,
				BPAConstants.OWNERSHIP_CATEGORY, BPAConstants.OWNER_TYPE, BPAConstants.OCCUPANCY_TYPE,
				BPAConstants.SUB_OCCUPANCY_TYPE, BPAConstants.USAGES, BPAConstants.PERMISSIBLE_ZONE,
			    BPAConstants.CONSTRUCTION_TYPE, BPAConstants.ULB_WARD_DETAILS, BPAConstants.STATES,			
				BPAConstants.RTP_CATEGORIES
				};

		if (log.isInfoEnabled() && bpaRequest != null && bpaRequest.getBPA() != null) {
			log.info("Validating master data from MDMS for : {}", bpaRequest.getBPA().getApplicationNo());
		}

		validateIfMasterPresent(masterArray, masterData);
		validateRequestValues(bpaRequest, masterLookup);
	}


	
	/**
	 * Extracts State-Level MDMS attribute values such as districts, planning areas,
	 * pp/bp authorities and concerned authorities.
	 *
	 * @param mdmsData MDMS JSON response object
	 * @return Map containing extracted state-level attributes
	 */
	public Map<String, List<String>> getAttributeValuesForState(Object mdmsData) {

	    final Map<String, List<String>> mdmsResMap = new HashMap<>();
	    
	    //️ Extract module master paths
	    extractModuleMasterData(mdmsData, mdmsResMap);

	    // Extract all tenant-level concerned authorities
	    extractConcernedAuthorities(mdmsData, mdmsResMap);

	    // ️Extract all egov-location → district → planning area → authorities
	    extractLocationHierarchy(mdmsData, mdmsResMap);

	    return mdmsResMap;
	}

	/**
	 * Extracts all tenant-level concerned authorities from MDMS (tenant module)
	 * and puts them in the result map.
	 *
	 * @param mdmsData   MDMS Master data JSON
	 * @param mdmsResMap Result map to update
	 */
	private void extractConcernedAuthorities(Object mdmsData, Map<String, List<String>> mdmsResMap) {

	    Object tenantObj = JsonPath.read(mdmsData, BPAConstants.TENANT_PATH);

	    List<Map<String, Object>> tenantList =
	            normalizeToList(((Map<String, Object>) tenantObj).get(BPAConstants.TENANTS));

	    List<String> tenantCodes = new ArrayList<>();
	    for (Map<String, Object> tenant : tenantList) {
	        tenantCodes.add(getString(tenant, BPAConstants.CODES));
	    }

	    mdmsResMap.put(BPAConstants.CONCERNED_AUTHORITIES, tenantCodes);
	}

	/**
	 * Extracts location hierarchy from egov-location module including:
	 * districts, planning area codes (PA), PP authority codes, and BP authority codes.
	 *
	 * @param mdmsData   MDMS Master data JSON
	 * @param mdmsResMap Result map to update
	 */
	private void extractLocationHierarchy(Object mdmsData, Map<String, List<String>> mdmsResMap) {

	    Object egovLocObj = JsonPath.read(mdmsData, BPAConstants.EGOV_LOCATION_PATH);
	    List<Map<String, Object>> egovLocList = normalizeToList(egovLocObj);

	    if (egovLocList.isEmpty()) {
	        throw new CustomException("MDMS_ERROR", "egov-location is empty");
	    }

	    List<Map<String, Object>> innerLocList =
	            normalizeToList(egovLocList.get(0).get(BPAConstants.EGOV_LOCATION));

	    List<Map<String, Object>> districts =
	            normalizeToList(innerLocList.get(0).get(BPAConstants.DISTRICTS));

	    List<String> districtCodes = new ArrayList<>();
	    List<String> planningAreaCodes = new ArrayList<>();
	    List<String> ppAuthorityCodes = new ArrayList<>();
	    List<String> bpAuthorityCodes = new ArrayList<>();

	    for (Map<String, Object> district : districts) {

	        districtCodes.add(getString(district, BPAConstants.DISTRICT_CODE));

	        List<Map<String, Object>> planningAreas =
	                normalizeToList(district.get(BPAConstants.PLANNING_AREA));

	        for (Map<String, Object> pa : planningAreas) {

	            planningAreaCodes.add(getString(pa, BPAConstants.PA_CODE));

	            // PP Authority
	            Map<String, Object> ppAuthority = (Map<String, Object>) pa.get(BPAConstants.PP_AUTHORIT);
	            if (ppAuthority != null) {
	                ppAuthorityCodes.add(getString(ppAuthority, BPAConstants.PP_AUTHORITY_CODE));
	            }

	            // BP Authorities
	            List<Map<String, Object>> bpAuthorities =
	                    normalizeToList(pa.get(BPAConstants.BP_AUTHORITY));

	            for (Map<String, Object> bp : bpAuthorities) {
	                bpAuthorityCodes.add(getString(bp, BPAConstants.CODES));
	            }
	        }
	    }

	    mdmsResMap.put(BPAConstants.DISTRICTS, districtCodes);
	    mdmsResMap.put(BPAConstants.PLANNING_AREA, planningAreaCodes);
	    mdmsResMap.put(BPAConstants.PP_AUTHORITY, ppAuthorityCodes);
	    mdmsResMap.put(BPAConstants.BP_AUTHORITY, bpAuthorityCodes);
	}

	/**
	 * Extracts additional module master data (BPA + Common Module)
	 * by reading predefined JSONPath expressions.
	 *
	 * @param mdmsData   MDMS master data JSON
	 * @param mdmsResMap Result map to update
	 */
	private void extractModuleMasterData(Object mdmsData, Map<String, List<String>> mdmsResMap) {

	    List<String> modulepaths = Arrays.asList(
	            BPAConstants.BPA_JSONPATH_CODE,
	            BPAConstants.COMMON_MASTER_JSONPATH_CODE
	    );

	    modulepaths.forEach(path -> {
	        try {
	            Map<String, List<String>> moduleMap = JsonPath.read(mdmsData, path);
	            mdmsResMap.putAll(moduleMap);
	        } catch (Exception e) {
	            throw new CustomException(
	                    BPAErrorConstants.INVALID_TENANT_ID_MDMS_KEY,
	                    BPAErrorConstants.INVALID_TENANT_ID_MDMS_MSG
	            );
	        }
	    });
	}

	
	
	/**
	 * Extracts ward, revenue village, and village codes from the tenant boundary section
	 * of the MDMS data. This method orchestrates the extraction by:
	 * <ul>
	 *     <li>Reading and validating the boundary root structure</li>
	 *     <li>Extracting ward, revenue village, and village codes</li>
	 *     <li>Adding non-empty results to the final response map</li>
	 * </ul>
	 *
	 * @param mdmsData The complete MDMS response object
	 * @return A map containing lists of revenue village codes and village codes
	 * @throws RuntimeException if boundary data is missing or no codes are found
	 */

	public Map<String, List<String>> getAttributeValuesForTenant(Object mdmsData) {

	    final Map<String, List<String>> mdmsResMap = new HashMap<>();

	    // Step 1: Extract boundary root from MDMS
	    Map<String, Object> boundaryRoot = extractBoundaryRoot(mdmsData);

	    // Step 2: Extract codes (ward, revenue village, village)
	    Map<String, List<String>> extractedCodes = extractCodes(boundaryRoot);

	    List<String> revenueVillageCodes = extractedCodes.get(BPAConstants.REVENUE_VILLAGE);
	    List<String> villageNameCodes = extractedCodes.get(BPAConstants.VILLAGES);

	    if (revenueVillageCodes.isEmpty() && villageNameCodes.isEmpty()) {
	        throw new RuntimeException("No revenue village or village codes found");
	    }

	    if (!revenueVillageCodes.isEmpty()) {
	        mdmsResMap.put(BPAConstants.REVENUE_VILLAGE, revenueVillageCodes);
	    }

	    if (!villageNameCodes.isEmpty()) {
	        mdmsResMap.put(BPAConstants.VILLAGES, villageNameCodes);
	    }

	    return mdmsResMap;
	}
	
	/**
	 * Extracts and validates the boundary root object from the MDMS tenantBoundary
	 * section. Ensures that:
	 * <ul>
	 *     <li>The tenant boundary section exists</li>
	 *     <li>The boundary root node is present</li>
	 * </ul>
	 *
	 * @param mdmsData The complete MDMS response object
	 * @return The boundary root map from which ward-level data can be read
	 * @throws RuntimeException if tenant boundary or boundary root is missing
	 */

	private Map<String, Object> extractBoundaryRoot(Object mdmsData) {

	    Object tenantBoundaryObj = JsonPath.read(mdmsData, BPAConstants.TENANT_BOUNDARY_JSON);

	    List<Map<String, Object>> tenantBoundaryList = normalizeToList(tenantBoundaryObj);

	    if (tenantBoundaryList.isEmpty()) {
	        throw new RuntimeException("TenantBoundary not found in MDMS");
	    }

	    Map<String, Object> boundaryRoot =
	            (Map<String, Object>) tenantBoundaryList.get(0).get(BPAConstants.BOUNDARY);

	    if (boundaryRoot == null) {
	        throw new RuntimeException("Boundary root is null");
	    }

	    return boundaryRoot;
	}
	
	/**
	 * Extracts all hierarchical boundary codes such as:
	 * <ul>
	 *     <li>Ward Codes</li>
	 *     <li>Revenue Village Codes</li>
	 *     <li>Village Codes</li>
	 * </ul>
	 * This method navigates through the three-level hierarchy:
	 * <pre>
	 * Ward → Revenue Village → Village
	 * </pre>
	 *
	 * @param boundaryRoot The root object containing boundary children (wards)
	 * @return A map containing lists for revenue villages and villages
	 */
	private Map<String, List<String>> extractCodes(Map<String, Object> boundaryRoot) {

	    List<String> revenueVillageCodes = new ArrayList<>();
	    List<String> villageNameCodes = new ArrayList<>();

	    traverseBoundary(boundaryRoot, revenueVillageCodes, villageNameCodes);

	    Map<String, List<String>> result = new HashMap<>();
	    result.put(BPAConstants.REVENUE_VILLAGE, revenueVillageCodes);
	    result.put(BPAConstants.VILLAGES, villageNameCodes);

	    return result;
	}
	
	/**
	 * Recursively traverse boundary children to collect revenue village and village codes
	 */
	@SuppressWarnings("unchecked")
	private void traverseBoundary(Map<String, Object> node, List<String> revenueVillageCodes, List<String> villageNameCodes) {

	    if (node == null) return;

	    String label = getString(node, BPAConstants.LABEL);
	    String code = getString(node, BPAConstants.CODES);

	    if (BPAConstants.REVENUE_VILLAGE_CODE.equalsIgnoreCase(label) && code != null) {
	        revenueVillageCodes.add(code);
	    } else if (BPAConstants.VILLAGE_CODE.equalsIgnoreCase(label) && code != null) {
	        villageNameCodes.add(code);
	    }

	    List<Map<String, Object>> children = normalizeToList(node.get(BPAConstants.CHILDREN));
	    for (Map<String, Object> child : children) {
	        traverseBoundary(child, revenueVillageCodes, villageNameCodes);
	    }
	}
	/**
	 * Normalizes an MDMS response node into a List of Map objects.
	 *
	 * <p>This helps handle inconsistent MDMS structures where a JSON node may be
	 * returned either as a single Map or as a List of Maps.</p>
	 *
	 * @param data Node received from MDMS JSONPath
	 * @return a List of maps; empty list if data is null
	 */
	private List<Map<String, Object>> normalizeToList(Object data) {
	    List<Map<String, Object>> list = new ArrayList<>();
	    if (data == null) return list;

	    if (data instanceof List) {
	        return (List<Map<String, Object>>) data;
	    } else if (data instanceof Map) {
	        list.add((Map<String, Object>) data);
	        return list;
	    }
	    return list;
	}

	/**
	 * Safely retrieves a string value from a map.
	 *
	 * @param map Source map
	 * @param key Key whose value is to be extracted
	 * @return string value or null if key not present
	 */
	private String getString(Map<String, Object> map, String key) {
	    Object v = map.get(key);
	    return v != null ? v.toString() : null;
	}

	/**
	 * Validates if MasterData is properly fetched for the given MasterData
	 * names
	 * 
	 * @param masterNames
	 * @param codes
	 */
	private void validateIfMasterPresent(String[] masterNames, Map<String, List<String>> codes) {
		Map<String, String> errorMap = new HashMap<>();
		for (String masterName : masterNames) {
			if (CollectionUtils.isEmpty(codes.get(masterName))) {
				errorMap.put("MDMS DATA ERROR ", "Unable to fetch " + masterName + " codes from MDMS");
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	/**
	 *  Validates the request values against the master data
	 *  @param bpaRequest
	 *  @param masterLookup
	 *  */
	@SuppressWarnings("unchecked")
	private void validateRequestValues(BPARequest bpaRequest, Map<String, Set<String>> masterLookup) {
		if (bpaRequest == null || bpaRequest.getBPA() == null) {
			return;
		}

		Map<String, String> errorMap = new HashMap<>();
		BPA bpa = bpaRequest.getBPA();

		validateFieldAgainstMaster(bpa.getApplicationType(), BPAConstants.CONSTRUCTION_TYPE, "Application type", masterLookup,
				errorMap);
		validateAreaMapping(bpa.getAreaMapping(), masterLookup, errorMap);
		validateRtpDetails(bpa.getRtpDetails(), masterLookup, errorMap);
		validateLandAddress(bpa.getLandInfo(), masterLookup, errorMap);

		if (!errorMap.isEmpty()) {
			throw new CustomException(errorMap);
		}
	}

	/**
	 * Validates the area mapping details against the master data
	 * @param areaMapping
	 * @param masterLookup
	 * @param errorMap
	 * */
	private void validateAreaMapping(AreaMappingDetail areaMapping, Map<String, Set<String>> masterLookup,
			Map<String, String> errorMap) {
		if (areaMapping == null) {
			return;
		}

		validateFieldAgainstMaster(areaMapping.getDistrict(), BPAConstants.DISTRICTS, "Area mapping district", masterLookup,
				errorMap);
		validateFieldAgainstMaster(areaMapping.getPlanningArea(), BPAConstants.PLANNING_AREA, "Planning area", masterLookup,
				errorMap);

		if (areaMapping.getPlanningPermitAuthority() != null) {
			validateFieldAgainstMaster(areaMapping.getPlanningPermitAuthority().getValue(), BPAConstants.PP_AUTHORITY,
					"Planning permit authority", masterLookup, errorMap);
		}
		if (areaMapping.getBuildingPermitAuthority() != null) {
			validateFieldAgainstMaster(areaMapping.getBuildingPermitAuthority().getValue(), BPAConstants.BP_AUTHORITY,
					"Building permit authority", masterLookup, errorMap);
		}
		if (areaMapping.getConcernedAuthority() != null) {
			validateFieldAgainstMaster(areaMapping.getConcernedAuthority(),
			        BPAConstants.CONCERNED_AUTHORITIES, 
			        "Concerned authority", masterLookup, errorMap);
		}

		if (areaMapping.getRevenueVillage() != null) {
		    validateFieldAgainstMaster(areaMapping.getRevenueVillage(),
		            BPAConstants.REVENUE_VILLAGE,
		            "Revenue village",
		            masterLookup,
		            errorMap);
		}

		if (areaMapping.getVillageName() != null) {
		    validateFieldAgainstMaster(areaMapping.getVillageName(),
		            BPAConstants.VILLAGES,
		            "Village name",
		            masterLookup,
		            errorMap);
		}

	}

	/**
	 * Validates the RTP details against the master data
	 * @param rtpDetails
	 * @param masterLookup
	 * @param errorMap
	 * */
	private void validateRtpDetails(RTPAllocationDetails rtpDetails, Map<String, Set<String>> masterLookup,
			Map<String, String> errorMap) {
		if (rtpDetails == null || rtpDetails.getRtpCategory() == null) {
			return;
		}

		validateFieldAgainstMaster(rtpDetails.getRtpCategory().getValue(), BPAConstants.RTP_CATEGORIES, "RTP category",
				masterLookup, errorMap);
	}

	/**
	 * Validates the land address against the master data
	 * @param landInfo
	 * @param masterLookup
	 * @param errorMap
	 * */
	private void validateLandAddress(LandInfo landInfo, Map<String, Set<String>> masterLookup, Map<String, String> errorMap) {
		if (landInfo == null || landInfo.getAddress() == null) {
			return;
		}
		validateFieldAgainstMaster(landInfo.getUnits().get(0).getOccupancyType(), BPAConstants.PERMISSIBLE_ZONE, "Permissible zone", masterLookup, errorMap);
		Address address = landInfo.getAddress();
		validateFieldAgainstMaster(address.getDistrict(), BPAConstants.DISTRICTS, "Land address district", masterLookup, errorMap);
		validateFieldAgainstMaster(address.getState(), BPAConstants.STATES, "Land address state", masterLookup, errorMap);
	}

	private String getStringValue(Object value) {
		return value == null ? null : String.valueOf(value);
	}

	/**
	 * Validates a field value against the master data
	 * @param fieldValue
	 * @param masterName
	 * @param fieldLabel
	 * @param masterLookup
	 * @param errorMap
	 * */
	private void validateFieldAgainstMaster(String fieldValue, String masterName, String fieldLabel,
			Map<String, Set<String>> masterLookup, Map<String, String> errorMap) {
		if (!StringUtils.hasText(fieldValue) || CollectionUtils.isEmpty(masterLookup.get(masterName))) {
			return;
		}

		String normalizedValue = fieldValue.trim().toLowerCase(Locale.ROOT);
		Set<String> validValues = masterLookup.getOrDefault(masterName, Collections.emptySet());
		if (!validValues.contains(normalizedValue)) {
			String errorKey = "INVALID_" + fieldLabel.toUpperCase().replaceAll("[^A-Z0-9]", "_");
			errorMap.put(errorKey,
					fieldLabel + " '" + fieldValue + "' is not present in MDMS master '" + masterName + "'");
		}
	}

	/**
	 * Builds a lookup map from master data for efficient validation
	 * @param masterData
	 * @return Map of master name to set of normalized values
	 * */
	private Map<String, Set<String>> buildMasterLookup(Map<String, List<String>> masterData) {
		Map<String, Set<String>> lookup = new HashMap<>();
		masterData.forEach((masterName, entries) -> lookup.put(masterName, flattenEntries(entries)));
		return lookup;
	}

	/**
	 *  Flattens and normalizes entries from master data
	 *  @param entries
	 *  @return Set of normalized string values
	 * */
	@SuppressWarnings("unchecked")
	private Set<String> flattenEntries(List<?> entries) {
		if (CollectionUtils.isEmpty(entries)) {
			return Collections.emptySet();
		}
		Set<String> values = new HashSet<>();
		entries.forEach(entry -> collectValues(entry, values));
		Set<String> normalized = new HashSet<>();
		values.stream().filter(StringUtils::hasText)
				.forEach(val -> normalized.add(val.trim().toLowerCase(Locale.ROOT)));
		return normalized;
	}

	/**
	 * Recursively collects string values from nested structures
	 * @param entry
	 * @param values
	 * */
	@SuppressWarnings("unchecked")
	private void collectValues(Object entry, Set<String> values) {
		if (entry == null) {
			return;
		}
		if (entry instanceof Map) {
			((Map<?, ?>) entry).values().forEach(value -> collectValues(value, values));
		} else if (entry instanceof Collection) {
			((Collection<?>) entry).forEach(value -> collectValues(value, values));
		} else {
			values.add(String.valueOf(entry));
		}
	}
}
