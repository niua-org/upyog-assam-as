package org.egov.land.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.land.repository.LandRepository;
import org.egov.land.util.LandConstants;
import org.egov.land.util.LandUtil;
import org.egov.land.validator.LandValidator;
import org.egov.land.web.models.LandInfo;
import org.egov.land.web.models.LandInfoRequest;
import org.egov.land.web.models.LandSearchCriteria;
import org.egov.land.web.models.OwnerInfoV2;
import org.egov.land.web.models.UserDetailResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LandService {

	@Autowired
	LandValidator landValidator;

	@Autowired
	private LandEnrichmentService enrichmentService;

	@Autowired
	private LandUserService userService;

	@Autowired
	private LandRepository repository;

	@Autowired
	private LandUtil util;

	public LandInfo create(@Valid LandInfoRequest landRequest) {

		Object mdmsData = null;//util.mDMSCall(landRequest.getRequestInfo(), landRequest.getLandInfo().getTenantId());
		if (landRequest.getLandInfo().getTenantId().split("\\.").length == 1) {
			throw new CustomException(LandConstants.INVALID_TENANT, " Application cannot be create at StateLevel");
		}
		
		//landValidator.validateLandInfo(landRequest,mdmsData);
		enrichmentService.enrichCreateLandInfo(landRequest);
		userService.manageCreateUser(landRequest);

		landRequest.getLandInfo().getOwners().forEach(owner -> {
			if (owner.getActive()) {
				owner.setStatus(true);
			}else
			{
				owner.setStatus(false);
			}
		});
		repository.save(landRequest.getLandInfo().getTenantId(), landRequest);
		return landRequest.getLandInfo();
	}

	public LandInfo update(@Valid LandInfoRequest landRequest) {
		LandInfo landInfo = landRequest.getLandInfo();

		Object mdmsData = util.mDMSCall(landRequest.getRequestInfo(), landRequest.getLandInfo().getTenantId());
		if (landInfo.getId() == null) {
			throw new CustomException(LandConstants.UPDATE_ERROR, "Id is mandatory to update ");
		}

		landInfo.getOwners().forEach(owner -> {
			if (owner.getOwnerType() == null) {
				owner.setOwnerType("NONE");
			}
		});
		landValidator.validateLandInfo(landRequest, mdmsData);

		// Below method will compare the owner info except mobile number and uuid, if changed then update the table else just convert the ownerInfo to ownerinfoV2
		userService.manageUpdateUser(landRequest);

		// Make sure updated owners are back in LandInfo
	//	landInfo.setOwners(landRequest.getLandInfo().getOwners());

		enrichmentService.enrichUpdateLandInfo(landRequest);

		landRequest.getLandInfo().getOwners().forEach(owner -> {
			if (owner.getActive()) {
				owner.setStatus(true);
			} else {
				owner.setStatus(false);
			}
		});

		repository.update(landRequest.getLandInfo().getTenantId(), landRequest);
		List<OwnerInfoV2> activeOwnerList = new ArrayList<>();
		if(landRequest.getLandInfo().getOwners().size()>1) {
			landRequest.getLandInfo().getOwners().forEach(owner -> {
			if (owner.getStatus()) {
				activeOwnerList.add(owner);
			}
		});
		landRequest.getLandInfo().setOwners(activeOwnerList);
		}
		return landRequest.getLandInfo();
	}
	
	public List<LandInfo> search(LandSearchCriteria criteria, RequestInfo requestInfo) {
		List<LandInfo> landInfos;
		landValidator.validateSearch(requestInfo, criteria);
		if (criteria.getMobileNumber() != null) {
			landInfos = getLandFromMobileNumber(criteria, requestInfo);
			// With given mobile number if no record exists then return empty response
			if (landInfos.isEmpty())
	                    return Collections.emptyList();
			List<String> landIds = new ArrayList<String>();
			for (LandInfo li : landInfos) {
				landIds.add(li.getId());
			}
			criteria.setMobileNumber(null);
			criteria.setIds(landIds);
		}
		// Handle name search - get users by name and convert to land IDs (similar to mobile number search)
		if (criteria.getName() != null) {
			landInfos = getLandFromName(criteria, requestInfo);
			if (landInfos.isEmpty())
				return Collections.emptyList();
			List<String> landIds = new ArrayList<String>();
			for (LandInfo li : landInfos) {
				landIds.add(li.getId());
			}
			criteria.setName(null);
			criteria.setIds(landIds);
		}

		landInfos = fetchLandInfoData(criteria, requestInfo);

		if (!CollectionUtils.isEmpty(landInfos)) {
			log.debug("Received final landInfo response in service call..");
		}
		return landInfos;
	}
	
	private List<LandInfo> getLandFromMobileNumber(LandSearchCriteria criteria, RequestInfo requestInfo) {

		List<LandInfo> landInfo = new LinkedList<>();
		UserDetailResponse userDetailResponse = userService.getUser(criteria, requestInfo);
		// If user not found with given user fields return empty list
		if (userDetailResponse.getUser().size() == 0) {
			return Collections.emptyList();
		}else{
			List<String> ids = new ArrayList<String>();
			for(int i=0; i<userDetailResponse.getUser().size();i++){
				ids.add(userDetailResponse.getUser().get(i).getUuid());
			}
			log.info("User IDs fetched from user service: {}", ids);
			criteria.setUserIds(ids);
		}

		landInfo = repository.getLandInfoData(criteria);

		if (landInfo.size() == 0) {
			return Collections.emptyList();
		}
		enrichmentService.enrichLandInfoSearch(landInfo, criteria, requestInfo);
		return landInfo;
	}

	/**
	 * Fetches land records based on owner name by first getting user UUIDs from user service
	 * then querying land records with those UUIDs (similar to mobile number search flow)
	 *
	 * @param criteria The search criteria containing the name
	 * @param requestInfo The request info
	 * @return List of LandInfo matching the owner name
	 */
	private List<LandInfo> getLandFromName(LandSearchCriteria criteria, RequestInfo requestInfo) {
		List<LandInfo> landInfo = new LinkedList<>();
		UserDetailResponse userDetailResponse = userService.getUserByName(criteria, requestInfo);

		// If no users found with given name, return empty list
		if (userDetailResponse.getUser().size() == 0) {
			return Collections.emptyList();
		} else {
			// Extract user UUIDs to search land records
			List<String> ids = new ArrayList<String>();
			for(int i=0; i<userDetailResponse.getUser().size();i++){
				ids.add(userDetailResponse.getUser().get(i).getUuid());
			}
			criteria.setUserIds(ids);
		}
       // Get land records for the user UUIDs
		landInfo = repository.getLandInfoData(criteria);

		if (landInfo.size() == 0) {
			return Collections.emptyList();
		}
		enrichmentService.enrichLandInfoSearch(landInfo, criteria, requestInfo);
		return landInfo;
	}
	

	/**
	 * Returns the landInfo with enriched owners from user service
	 * 
	 * @param criteria
	 *            The object containing the parameters on which to search
	 * @param requestInfo
	 *            The search request's requestInfo
	 * @return List of landInfo for the given criteria
	 */
	public List<LandInfo> fetchLandInfoData(LandSearchCriteria criteria, RequestInfo requestInfo) {
		List<LandInfo> landInfos = repository.getLandInfoData(criteria);
		if (landInfos.isEmpty())
			return Collections.emptyList();
		
		if(!CollectionUtils.isEmpty(landInfos)){
			log.debug("Received final landInfo response..");
		}
		
		landInfos = enrichmentService.enrichLandInfoSearch(landInfos, criteria, requestInfo);
		if(!CollectionUtils.isEmpty(landInfos)){
			log.debug("Received final landInfo response after enrichment..");
		}
		return landInfos;
	}
}
