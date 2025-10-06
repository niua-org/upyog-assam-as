package org.egov.land.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.land.config.LandConfiguration;
import org.egov.land.util.LandConstants;
import org.egov.land.util.LandUtil;
import org.egov.land.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LandEnrichmentService {

	@Autowired
	private LandUtil landUtil;

	@Autowired
	private LandBoundaryService boundaryService;

	@Autowired
	private LandConfiguration config;

	@Autowired
	private LandUserService userService;

	public void enrichCreateLandInfo(LandInfoRequest landRequest) {
		RequestInfo requestInfo = landRequest.getRequestInfo();
		log.info("Enriching create landInfo of landId: {}", landRequest.getLandInfo().getId());
		AuditDetails auditDetails = landUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		landRequest.getLandInfo().setAuditDetails(auditDetails);
		landRequest.getLandInfo().setId(LandUtil.getRandonUUID());
		//TODO: remove false condition after testing
		if (false) {
			boundaryService.getAreaType(landRequest, config.getHierarchyTypeCode());
		}

		if (landRequest.getLandInfo().getInstitution() != null) {
				landRequest.getLandInfo().getInstitution().setId(LandUtil.getRandonUUID());
				landRequest.getLandInfo().getInstitution().setTenantId(landRequest.getLandInfo().getTenantId());
		}
		
		if (org.springframework.util.StringUtils.isEmpty(landRequest.getLandInfo().getChannel())) {
			landRequest.getLandInfo().setChannel(Channel.SYSTEM);
		}

		if (org.springframework.util.StringUtils.isEmpty(landRequest.getLandInfo().getSource())) {
			landRequest.getLandInfo().setSource(Source.MUNICIPAL_RECORDS);
		}

		// address
		if (landRequest.getLandInfo().getAddress() != null) {
			landRequest.getLandInfo().getAddress().setId(LandUtil.getRandonUUID());
			landRequest.getLandInfo().getAddress().setTenantId(landRequest.getLandInfo().getTenantId());
			landRequest.getLandInfo().getAddress().setAuditDetails(auditDetails);
			if(landRequest.getLandInfo().getAddress().getLocality() != null){
				landRequest.getLandInfo().getAddress().setLocalityCode(landRequest.getLandInfo().getAddress().getLocality().getCode());
			}
			if (landRequest.getLandInfo().getAddress().getGeoLocation() != null)
				landRequest.getLandInfo().getAddress().getGeoLocation().setId(LandUtil.getRandonUUID());
		}
		// units
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getUnits())) {
			landRequest.getLandInfo().getUnits().forEach(unit -> {
				unit.setId(LandUtil.getRandonUUID());
				unit.setTenantId(landRequest.getLandInfo().getTenantId());
				unit.setAuditDetails(auditDetails);
			});
		}

		// Documents
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getDocuments())) {
			landRequest.getLandInfo().getDocuments().forEach(document -> {
				document.setId(LandUtil.getRandonUUID());
				document.setAuditDetails(auditDetails);
			});
		}

		// Owners
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getOwners())) {
			landRequest.getLandInfo().getOwners().forEach(owner -> {
				owner.setOwnerId(LandUtil.getRandonUUID());
				owner.setAuditDetails(auditDetails);
			});
			landRequest.getLandInfo().getOwners().forEach(owner ->{
				if(owner.getCorrespondenceAddress() !=null){
					owner.getCorrespondenceAddress().setId(LandUtil.getRandonUUID());
					owner.getCorrespondenceAddress().setOwnerInfoId(owner.getOwnerId());
					owner.getCorrespondenceAddress().setAuditDetails(auditDetails);
					if(owner.getCorrespondenceAddress().getLocality() != null){
						owner.getCorrespondenceAddress().setLocalityCode(owner.
								getCorrespondenceAddress().getLocality().getCode());
					}
					landRequest.getLandInfo().getOwnerAddresses().add(owner.getCorrespondenceAddress());
				}
				if(owner.getPermanentAddress() !=null){

					owner.getPermanentAddress().setId(LandUtil.getRandonUUID());
					owner.getPermanentAddress().setOwnerInfoId(owner.getOwnerId());
					owner.getPermanentAddress().setAuditDetails(auditDetails);
					if(owner.getPermanentAddress().getLocality() != null){
						owner.getPermanentAddress().setLocalityCode(owner.
								getPermanentAddress().getLocality().getCode());
					}
					landRequest.getLandInfo().getOwnerAddresses().add(owner.getPermanentAddress());
				}
			});
		}
		log.info("enriched land info : {}", landRequest.getLandInfo());
	}

	public void enrichUpdateLandInfo(LandInfoRequest landRequest) {
		log.info("Enriching update landInfo of landId: {}", landRequest.getLandInfo().getId());
		RequestInfo requestInfo = landRequest.getRequestInfo();
		AuditDetails auditDetails = landUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
		landRequest.getLandInfo().setAuditDetails(auditDetails);
		//TODO: remove false condition after testing
		if (false) {
			boundaryService.getAreaType(landRequest, config.getHierarchyTypeCode());
		}

		// Setting institution id and tenant id it is null/empty and institution object is not null
		if (landRequest.getLandInfo().getInstitution() != null) {
			if (StringUtils.isBlank(landRequest.getLandInfo().getInstitution().getId()))
				landRequest.getLandInfo().getInstitution().setId(LandUtil.getRandonUUID());
			if (StringUtils.isBlank(landRequest.getLandInfo().getInstitution().getTenantId()))
				landRequest.getLandInfo().getInstitution().setTenantId(landRequest.getLandInfo().getTenantId());
		}

		// address
		if (landRequest.getLandInfo().getAddress() != null) {
			if (StringUtils.isBlank(landRequest.getLandInfo().getAddress().getId())) {
				landRequest.getLandInfo().getAddress().setId(LandUtil.getRandonUUID());
			}
			landRequest.getLandInfo().getAddress().setTenantId(landRequest.getLandInfo().getTenantId());
			landRequest.getLandInfo().getAddress().setAuditDetails(auditDetails);
			if (landRequest.getLandInfo().getAddress().getLocality() != null) {
				landRequest.getLandInfo().getAddress().setLocalityCode(landRequest.getLandInfo().getAddress().getLocality().getCode());
			}
			if (landRequest.getLandInfo().getAddress().getGeoLocation() != null
					&& StringUtils.isBlank(landRequest.getLandInfo().getAddress().getGeoLocation().getId())) {
				landRequest.getLandInfo().getAddress().getGeoLocation().setId(LandUtil.getRandonUUID());
			}
		}
		// units
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getUnits())) {
			landRequest.getLandInfo().getUnits().forEach(unit -> {
				if (StringUtils.isBlank(unit.getId())) {
					unit.setId(LandUtil.getRandonUUID());
				}
				unit.setTenantId(landRequest.getLandInfo().getTenantId());
				unit.setAuditDetails(auditDetails);
			});
		}

		// Documents
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getDocuments())) {
			landRequest.getLandInfo().getDocuments().forEach(document -> {
				if (StringUtils.isBlank(document.getId())) {
					document.setId(LandUtil.getRandonUUID());
				}
				document.setAuditDetails(auditDetails);
			});
		}

		// Owners
		if (!CollectionUtils.isEmpty(landRequest.getLandInfo().getOwners())) {
			landRequest.getLandInfo().getOwners().forEach(owner -> {
				if (StringUtils.isBlank(owner.getOwnerId()))
					owner.setOwnerId(LandUtil.getRandonUUID());
				owner.setAuditDetails(auditDetails);
			});
			landRequest.getLandInfo().getOwners().forEach(owner ->{
				if(owner.getCorrespondenceAddress() !=null){
					if (StringUtils.isBlank(owner.getCorrespondenceAddress().getId())){
						log.info("correspondence address id is null hence creating new id : {}", owner.getOwnerId());
						owner.getCorrespondenceAddress().setId(LandUtil.getRandonUUID());
						owner.getCorrespondenceAddress().setId(LandUtil.getRandonUUID());
						owner.getCorrespondenceAddress().setOwnerInfoId(owner.getOwnerId());
						owner.getCorrespondenceAddress().setAuditDetails(auditDetails);
						if(owner.getCorrespondenceAddress().getLocality() != null){
							owner.getCorrespondenceAddress().setLocalityCode(owner.
									getCorrespondenceAddress().getLocality().getCode());
						}
						landRequest.getLandInfo().getOwnerAddresses().add(owner.getCorrespondenceAddress());
					} else {
						log.info("correspondence address id is not null hence updating existing one : {}", owner.getOwnerId());
						owner.getCorrespondenceAddress().setAuditDetails(auditDetails);
						landRequest.getLandInfo().getOwnerAddresses().add(owner.getCorrespondenceAddress());
					}

				}
				if(owner.getPermanentAddress() !=null){
					if( StringUtils.isBlank(owner.getPermanentAddress().getId())){
						log.info("permanent address id is null hence creating new id : {}", owner.getOwnerId());
						owner.getPermanentAddress().setId(LandUtil.getRandonUUID());
						owner.getPermanentAddress().setOwnerInfoId(owner.getOwnerId());
						owner.getPermanentAddress().setAuditDetails(auditDetails);
						if(owner.getPermanentAddress().getLocality() != null){
							owner.getPermanentAddress().setLocalityCode(owner.
									getPermanentAddress().getLocality().getCode());
						}
						landRequest.getLandInfo().getOwnerAddresses().add(owner.getPermanentAddress());
					} else {
						log.info("permanent address id is not null hence updating existing one : {}", owner.getOwnerId());
						owner.getPermanentAddress().setAuditDetails(auditDetails);
						landRequest.getLandInfo().getOwnerAddresses().add(owner.getPermanentAddress());
					}
				}
			});
		}
		log.info("updated enriched land info : {}", landRequest.getLandInfo());
	}

	/**
	 * Creates search criteria from list of landInfo's
	 * 
	 * @param landInfo
	 *            's list The landInfo whose id's are added to search
	 * @return landSearch criteria on basis of landInfo id
	 */
	public LandSearchCriteria getLandCriteriaFromIds(List<LandInfo> landInfo, Integer limit) {
		LandSearchCriteria criteria = new LandSearchCriteria();
		Set<String> landIds = new HashSet<>();
		landInfo.forEach(data -> landIds.add(data.getId()));
		criteria.setIds(new LinkedList<>(landIds));
		criteria.setTenantId(landInfo.get(0).getTenantId());
		criteria.setLimit(limit);
		return criteria;
	}

	public List<LandInfo> enrichLandInfoSearch(List<LandInfo> landInfos, LandSearchCriteria criteria,
			RequestInfo requestInfo) {

		List<LandInfoRequest> landInfors = new ArrayList<LandInfoRequest>();
		landInfos.forEach(bpa -> {
			landInfors.add(new LandInfoRequest(requestInfo, bpa));
		});
		//TODO will resolve after testing
		if (criteria.getLimit() == null || !criteria.getLimit().equals(-1)) {
		//	enrichBoundary(landInfors);
		}

		UserDetailResponse userDetailResponse = userService.getUsersForLandInfos(landInfos);
		enrichOwner(userDetailResponse, landInfos);
		if(!CollectionUtils.isEmpty(landInfos) && !CollectionUtils.isEmpty(landInfos.get(0).getOwners())){
			log.debug("In enrich service...... ");
		}
		return landInfos;
	}

	private void enrichBoundary(List<LandInfoRequest> landRequests) {
		landRequests.forEach(landRequest -> {
			boundaryService.getAreaType(landRequest, config.getHierarchyTypeCode());
		});
	}

	private void enrichOwner(UserDetailResponse userDetailResponse, List<LandInfo> landInfos) {

		List<OwnerInfo> users = userDetailResponse.getUser();
		Map<String, OwnerInfo> userIdToOwnerMap = new HashMap<>();
		users.forEach(user -> userIdToOwnerMap.put(user.getUuid(), user));
		landInfos.forEach(landInfo -> {
			landInfo.getOwners().forEach(owner -> {
				if (userIdToOwnerMap.get(owner.getUuid()) == null)
					throw new CustomException(LandConstants.OWNER_SEARCH_ERROR,
							"The owner of the landInfo " + landInfo.getId() + " is not coming in user search");
				else
					owner.addUserWithoutAuditDetail(userIdToOwnerMap.get(owner.getUuid()));
			});
		});

//		/// TODO: Need to Remove it when userService corrected
//		landInfos.forEach(landInfo -> {
//			Set<String> seenUuids = new HashSet<>();
//			List<OwnerInfoV2> uniqueOwners = landInfo.getOwners().stream()
//					.filter(owner -> seenUuids.add(owner.getUuid()))
//					.collect(Collectors.toList());
//
//			landInfo.setOwners(uniqueOwners);
//		});

	}
}
