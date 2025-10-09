package org.egov.land.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.land.config.LandConfiguration;
import org.egov.land.repository.ServiceRequestRepository;
import org.egov.land.util.LandConstants;
import org.egov.land.util.LandUtil;
import org.egov.land.web.models.CreateUserRequest;
import org.egov.land.web.models.LandInfo;
import org.egov.land.web.models.LandInfoRequest;
import org.egov.land.web.models.LandSearchCriteria;
import org.egov.land.web.models.OwnerInfo;
import org.egov.land.web.models.OwnerInfoV2;
import org.egov.land.web.models.Role;
import org.egov.land.web.models.UserDetailResponse;
import org.egov.land.web.models.UserSearchRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LandUserService {

	@Autowired
	private LandConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	MultiStateInstanceUtil centralInstanceUtil;

	public void manageCreateUser(LandInfoRequest landRequest) {
		LandInfo landInfo = landRequest.getLandInfo();
		 @Valid RequestInfo requestInfo = landRequest.getRequestInfo();

		landInfo.getOwners().forEach(owner -> {
			UserDetailResponse userDetailResponse = null;
			if (owner.getMobileNumber() != null) {

				if (owner.getTenantId() == null) {
					owner.setTenantId(centralInstanceUtil.getStateLevelTenant(landInfo.getTenantId()));
				}

				userDetailResponse = userExists(owner, requestInfo, true);

				if (userDetailResponse == null || CollectionUtils.isEmpty(userDetailResponse.getUser())) {
					log.info("No user found with mobileNo or details were change creating new one : {} ", owner.getMobileNumber());
					Role role = getCitizenRole();
					addUserDefaultFields(owner.getTenantId(), role, owner);
					StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserContextPath())
							.append(config.getUserCreateEndpoint());
					setUserName(owner, landRequest.getLandInfo());
					owner.setOwnerType(LandConstants.CITIZEN);
					OwnerInfo ownerInfo = convertToOwnerInfo(owner);
					log.info("ownerInfo-->" + ownerInfo);
					userDetailResponse = userCall(new CreateUserRequest(requestInfo, ownerInfo), uri);
					owner.setUuid(userDetailResponse.getUser().get(0).getUuid());
					log.debug("owner created --> " + userDetailResponse.getUser().get(0).getUuid());
				}else{
					owner.setUuid(userDetailResponse.getUser().get(0).getUuid());
					log.info("user already exists adding uuid to ownerinfo --> " + owner.getUuid());
				}

			} else {
				log.info("MobileNo is not existed in ownerInfo.");
				throw new CustomException(LandConstants.INVALID_ONWER_ERROR, "MobileNo is mandatory for ownerInfo");
			}
		});
	}


	/*
	* Fetches existing owner info from UserService.
	* Compares incoming info vs existing info (ignoring username, type, role, tenantId, active).
	*/
	public void manageUpdateUser(LandInfoRequest landRequest) {
		LandInfo landInfo = landRequest.getLandInfo();
		RequestInfo requestInfo = landRequest.getRequestInfo();

		landInfo.getOwners().forEach(ownerInfoV2 -> {

			// Step 3: Fetch existing user from UserService
			UserDetailResponse existingUserResponse = userExists(ownerInfoV2, requestInfo, false);
			OwnerInfo existingOwnerInfo = null;
			if (existingUserResponse != null && !CollectionUtils.isEmpty(existingUserResponse.getUser())) {
				existingOwnerInfo = existingUserResponse.getUser().get(0);
				log.info("User found "+existingOwnerInfo.getUserName() );
			}else{
				throw new CustomException(LandConstants.UPDATE_ERROR, "User not found in UserService");
			}

			// Step 4: Convert incoming owner v2 to OwnerInfo for comparison
			OwnerInfo ownerInfoFromUI = convertToOwnerInfo(ownerInfoV2);

			// Step 5: Check if owner info has changed
			if (existingOwnerInfo != null && !ownerInfoFromUI.compareWithExistingUser(existingOwnerInfo)) {
				// Step 5a: Add default fields for a Citizen user
				ownerInfoFromUI.setRoles(existingOwnerInfo.getRoles());

				// Step 5b: Call UserService to create or update user
				String uri = config.getUserHost() +
						config.getUserUpdateEndpoint();

				userCall(new CreateUserRequest(requestInfo, ownerInfoFromUI), new StringBuilder(uri));
			}

			// Note: No need to update id/uuid here as they are immutable in UserService
		});
	}




	private OwnerInfo convertToOwnerInfo(OwnerInfoV2 ownerInfoV2) {
		OwnerInfo ownerInfo = new OwnerInfo();
		ownerInfo.setActive(ownerInfoV2.getActive());
		ownerInfo.setEmailId(ownerInfoV2.getEmailId());
		ownerInfo.setFatherOrHusbandName(ownerInfoV2.getFatherOrHusbandName());
		ownerInfo.setGender(ownerInfoV2.getGender());
		ownerInfo.setId(ownerInfoV2.getId());
		ownerInfo.altContactNumber(ownerInfoV2.getAltContactNumber());
		ownerInfo.setAadhaarNumber(ownerInfoV2.getAadhaarNumber());
		ownerInfo.setPan(ownerInfoV2.getPanNumber());
		ownerInfo.setMobileNumber(ownerInfoV2.getMobileNumber());
		ownerInfo.setName(ownerInfoV2.getName());
		if(ownerInfoV2.getPermanentAddress() != null){
			ownerInfo.setPermanentAddress(ownerInfoV2.getPermanentAddress().getAddressLine1());
			ownerInfo.setPermanentPincode(ownerInfoV2.getPermanentAddress().getPincode());
		}
		if(ownerInfoV2.getCorrespondenceAddress() != null){
			ownerInfo.setCorrespondenceAddress(ownerInfoV2.getCorrespondenceAddress().getAddressLine1());
			ownerInfo.setCorrespondencePincode(ownerInfoV2.getCorrespondenceAddress().getPincode());
		}
		ownerInfo.setUuid(ownerInfoV2.getUuid());
		ownerInfo.setUserName(ownerInfoV2.getUserName());
		ownerInfo.setType(ownerInfoV2.getType());
		ownerInfo.setRoles(ownerInfoV2.getRoles());
		ownerInfo.setTenantId(ownerInfoV2.getTenantId());
		return ownerInfo;
	}


	/**
	 * Creates citizen role
	 * 
	 * @return Role object for citizen
	 */
	private Role getCitizenRole() {
		Role role = new Role();
		role.setCode(LandConstants.CITIZEN);
		role.setName("Citizen");
		return role;
	}

	/**
	 * Checks if the user exists in the database
	 * 
	 * @param owner
	 *            The owner from the LandInfo
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return The search response from the user service
	 */
	private UserDetailResponse userExists(OwnerInfoV2 owner, RequestInfo requestInfo ,boolean isCreate) {

		UserSearchRequest userSearchRequest = new UserSearchRequest();
		userSearchRequest.setTenantId(centralInstanceUtil.getStateLevelTenant(owner.getTenantId()));
		userSearchRequest.setRequestInfo(requestInfo);
		if(isCreate){
			userSearchRequest.setMobileNumber(owner.getMobileNumber());
			userSearchRequest.setUserType("CITIZEN");
			userSearchRequest.setPan(owner.getPanNumber());
			userSearchRequest.setAadhaarNumber(owner.getAadhaarNumber());
			userSearchRequest.setActive(true);
			userSearchRequest.setName(owner.getName());
		//	userSearchRequest.setFatherOrHusbandName(owner.getFatherOrHusbandName());
			userSearchRequest.setEmailId(owner.getEmailId());
		//	userSearchRequest.setGender(owner.getGender());
		//	userSearchRequest.setAltContactNumber(owner.getAltContactNumber());
		//	userSearchRequest.setDob(owner.getDob());

		}
		if(!isCreate && !StringUtils.isEmpty(owner.getUuid())) {
			List<String> uuids = new ArrayList<String>();
			uuids.add(owner.getUuid());
			userSearchRequest.setUuid(uuids);
		}

		StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
		return userCall(userSearchRequest, uri);
	}

	/**
	 * Sets the username as uuid
	 * As we set username to mobile no when we have to create the user that can login
	 * For general user just to store user details we set username to dummy value
	 * 
	 * @param owner
	 *
	 */
	private void setUserName(OwnerInfoV2 owner, LandInfo landInfo) {
		owner.setUserName(landInfo.getId());
	}

	/**
	 * Sets ownerfields from the userResponse
	 * 
	 * @param owner
	 *            The owner from landInfo
	 * @param userDetailResponse
	 *            The response from user search
	 * @param requestInfo
	 *            The requestInfo of the request
	 */
	private void setOwnerFields(OwnerInfoV2 owner, UserDetailResponse userDetailResponse, RequestInfo requestInfo) {
		owner.setId(userDetailResponse.getUser().get(0).getId());
		owner.setUuid(userDetailResponse.getUser().get(0).getUuid());
		owner.setUserName((userDetailResponse.getUser().get(0).getUserName()));
	}

	/**
	 * Sets the role,type,active and tenantId for a Citizen
	 * 
	 * @param tenantId
	 *            TenantId of the property
	 * @param role 
	 * @param role
	 *            The role of the user set in this case to CITIZEN
	 * @param owner
	 *            The user whose fields are to be set
	 */
	private void addUserDefaultFields(String tenantId, Role role, OwnerInfoV2 owner) {
		owner.setActive(true);
		owner.setTenantId(tenantId);
		owner.setRoles(Collections.singletonList(role));
		owner.setType(LandConstants.CITIZEN);
	}

	public UserDetailResponse getUsersForLandInfos(List<LandInfo> landInfos) {
		UserSearchRequest userSearchRequest = new UserSearchRequest();
		List<String> ids = new ArrayList<String>();
		Set<String> uuids = new HashSet<String>();
		landInfos.forEach(landInfo -> {
			landInfo.getOwners().forEach(owner -> {
				if (owner.getUuid() != null && owner.getStatus() )
					uuids.add(owner.getUuid().toString());
			});
		});

		for (String uuid : uuids) {
			ids.add(uuid);
		}
		userSearchRequest.setUuid(ids);
		StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
		return userCall(userSearchRequest, uri);
	}

	/**
	 * Returns UserDetailResponse by calling user service with given uri and
	 * object
	 * 
	 * @param userRequest
	 *            Request object for user service
	 * @param uri
	 *            The address of the end point
	 * @return Response from user service as parsed as userDetailResponse
	 */
	@SuppressWarnings("rawtypes")
	UserDetailResponse userCall(Object userRequest, StringBuilder uri) {
		String dobFormat = null;
		if (uri.toString().contains(config.getUserSearchEndpoint())
				|| uri.toString().contains(config.getUserUpdateEndpoint()))
			dobFormat = "yyyy-MM-dd";
		else if (uri.toString().contains(config.getUserCreateEndpoint()))
			dobFormat = "dd/MM/yyyy";
		try {
			LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, userRequest);
			parseResponse(responseMap, dobFormat);
			UserDetailResponse userDetailResponse = mapper.convertValue(responseMap, UserDetailResponse.class);
			return userDetailResponse;
		} catch (IllegalArgumentException e) {
			throw new CustomException(LandConstants.ILLEGAL_ARGUMENT_EXCEPTION, "ObjectMapper not able to convertValue in userCall");
		}
	}

	/**
	 * Parses date formats to long for all users in responseMap
	 * 
	 * @param responeMap
	 *            LinkedHashMap got from user api response
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void parseResponse(LinkedHashMap responeMap, String dobFormat) {
		List<LinkedHashMap> users = (List<LinkedHashMap>) responeMap.get("user");
		String format1 = "dd-MM-yyyy HH:mm:ss";
		if (users != null) {
			users.forEach(map -> {
				map.put("createdDate", dateTolong((String) map.get("createdDate"), format1));
				if ((String) map.get("lastModifiedDate") != null)
					map.put("lastModifiedDate", dateTolong((String) map.get("lastModifiedDate"), format1));
				if ((String) map.get("dob") != null)
					map.put("dob", dateTolong((String) map.get("dob"), dobFormat));
				if ((String) map.get("pwdExpiryDate") != null)
					map.put("pwdExpiryDate", dateTolong((String) map.get("pwdExpiryDate"), format1));
			});
		}
	}

	/**
	 * Converts date to long
	 * 
	 * @param date
	 *            date to be parsed
	 * @param format
	 *            Format of the date
	 * @return Long value of date
	 */
	private Long dateTolong(String date, String format) {
		SimpleDateFormat f = new SimpleDateFormat(format);
		Date d = null;
		try {
			d = f.parse(date);
		} catch (ParseException e) {
            log.error("Exception while parsing date to long : {}", String.valueOf(e));
			throw new CustomException(LandConstants.DATE_PARSE_EXCEPTION, "Exception while parsing date to long");
		}
		return d.getTime();
	}

	/**
	 * Call search in user service based on ownerids from criteria
	 * 
	 * @param criteria
	 *            The search criteria containing the ownerids
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return Search response from user service based on ownerIds
	 */
	public UserDetailResponse getUser(LandSearchCriteria criteria, RequestInfo requestInfo) {
		UserSearchRequest userSearchRequest = getUserSearchRequest(criteria, requestInfo);
		StringBuilder uri = new StringBuilder(config.getUserHost()).append(config.getUserSearchEndpoint());
		UserDetailResponse userDetailResponse = userCall(userSearchRequest, uri);
		return userDetailResponse;
	}

	/**
	 * Creates userSearchRequest from bpaSearchCriteria
	 * 
	 * @param criteria
	 *            The bpaSearch criteria
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @return The UserSearchRequest based on ownerIds
	 */
	private UserSearchRequest getUserSearchRequest(LandSearchCriteria criteria, RequestInfo requestInfo) {
		UserSearchRequest userSearchRequest = new UserSearchRequest();
		userSearchRequest.setRequestInfo(requestInfo);
		userSearchRequest.setTenantId(centralInstanceUtil.getStateLevelTenant(criteria.getTenantId()));
		userSearchRequest.setMobileNumber(criteria.getMobileNumber());
		userSearchRequest.setActive(true);
		userSearchRequest.setUserType(LandConstants.CITIZEN);
		return userSearchRequest;
	}

}
