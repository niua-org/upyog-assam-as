package org.egov.bpa.validator;

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

		Map<String, List<String>> masterData = getAttributeValues(mdmsData);
		Map<String, Set<String>> masterLookup = buildMasterLookup(masterData);
		String[] masterArray = { BPAConstants.SERVICE_TYPE, BPAConstants.APPLICATION_TYPE,
				BPAConstants.OWNERSHIP_CATEGORY, BPAConstants.OWNER_TYPE, BPAConstants.OCCUPANCY_TYPE,
				BPAConstants.SUB_OCCUPANCY_TYPE, BPAConstants.USAGES, BPAConstants.PERMISSIBLE_ZONE,
				BPAConstants.BP_AUTHORITY, BPAConstants.CONCERNED_AUTHORITIES, BPAConstants.CONSTRUCTION_TYPE,
				BPAConstants.DISTRICTS, BPAConstants.PLANNING_AREA, BPAConstants.PP_AUTHORITY,
				BPAConstants.REVENUE_VILLAGE, BPAConstants.RTP_CATEGORIES, BPAConstants.STATES,
				BPAConstants.ULB_WARD_DETAILS, BPAConstants.VILLAGES };

		if (log.isInfoEnabled() && bpaRequest != null && bpaRequest.getBPA() != null) {
			log.info("Validating master data from MDMS for : {}", bpaRequest.getBPA().getApplicationNo());
		}

		validateIfMasterPresent(masterArray, masterData);
		validateRequestValues(bpaRequest, masterLookup);
	}


	/**
	 * Fetches all the values of particular attribute as map of field name to
	 * list
	 *
	 * takes all the masters from each module and adds them in to a single map
	 *
	 * note : if two masters from different modules have the same name then it
	 *
	 * will lead to overriding of the earlier one by the latest one added to the
	 * map
	 *
	 * @return Map of MasterData name to the list of code in the MasterData
	 *
	 */
	public Map<String, List<String>> getAttributeValues(Object mdmsData) {

		List<String> modulepaths = Arrays.asList(BPAConstants.BPA_JSONPATH_CODE,
				BPAConstants.COMMON_MASTER_JSONPATH_CODE);
		final Map<String, List<String>> mdmsResMap = new HashMap<>();
		modulepaths.forEach(modulepath -> {
			try {
				mdmsResMap.putAll(JsonPath.read(mdmsData, modulepath));
			} catch (Exception e) {
				throw new CustomException(BPAErrorConstants.INVALID_TENANT_ID_MDMS_KEY,
						BPAErrorConstants.INVALID_TENANT_ID_MDMS_MSG);
			}
		});
		return mdmsResMap;
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
//  In mdms concerned authority and bp authority are interchanged
			validateFieldAgainstMaster(areaMapping.getBuildingPermitAuthority().getValue(), BPAConstants.CONCERNED_AUTHORITIES,
					"Building permit authority", masterLookup, errorMap);
		}
//  In mdms concerned authority and bp authority are interchanged
		validateFieldAgainstMaster(areaMapping.getConcernedAuthority(), BPAConstants.BP_AUTHORITY,
				"Concerned authority", masterLookup, errorMap);
		validateFieldAgainstMaster(areaMapping.getWard(), BPAConstants.ULB_WARD_DETAILS, "Ward", masterLookup, errorMap);
		validateFieldAgainstMaster(areaMapping.getRevenueVillage(), BPAConstants.REVENUE_VILLAGE, "Revenue village",
				masterLookup, errorMap);
		validateFieldAgainstMaster(areaMapping.getVillageName(), BPAConstants.VILLAGES, "Village name", masterLookup,
				errorMap);
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
