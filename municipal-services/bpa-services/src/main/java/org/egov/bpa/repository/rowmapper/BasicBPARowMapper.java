package org.egov.bpa.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.egov.bpa.web.model.AreaMappingDetail;
import org.egov.bpa.web.model.AuditDetails;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BuildingPermitAuthorityEnum;
import org.egov.bpa.web.model.PlanningPermitAuthorityEnum;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;


import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BasicBPARowMapper implements ResultSetExtractor<List<BPA>> {

	@Override
	public List<BPA> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, BPA> buildingMap = new LinkedHashMap<>();

		while (rs.next()) {
			String id = rs.getString("bpa_id");
			BPA currentbpa = buildingMap.get(id);

			if (currentbpa == null) {
				currentbpa = buildBpa(rs);
				buildingMap.put(id, currentbpa);
			}

			// Only attach area mapping detail (no documents, no RTP for basic search)
			addAreaMappingDetail(rs, currentbpa);
		}

		return new ArrayList<>(buildingMap.values());
	}

	/** ---------------------- BPA CORE MAPPING (Basic Fields Only) ---------------------- */
	private BPA buildBpa(ResultSet rs) throws SQLException {
		Long lastModifiedTime = rs.getLong("bpa_last_modified_time");
		if (rs.wasNull()) lastModifiedTime = null;

		AuditDetails auditdetails = AuditDetails.builder()
				.createdBy(rs.getString("bpa_created_by"))
				.createdTime(rs.getLong("bpa_created_time"))
				.lastModifiedBy(rs.getString("bpa_last_modified_by"))
				.lastModifiedTime(lastModifiedTime)
				.build();

		return BPA.builder()
				.id(rs.getString("bpa_id"))
				.applicationNo(rs.getString("application_no"))
				.approvalNo(rs.getString("approval_no"))
				.applicationType(rs.getString("application_type"))
				.status(rs.getString("status"))
				.tenantId(rs.getString("bpa_tenant_id"))
				.edcrNumber(rs.getString("edcr_number"))
				.approvalDate(rs.getLong("approval_date"))
				.accountId(rs.getString("account_id"))
				.landId(rs.getString("land_id"))
				.applicationDate(rs.getLong("application_date"))
				.auditDetails(auditdetails)
				.additionalDetails(null) // Not included in basic search
				.businessService(rs.getString("business_service"))
				.riskType(rs.getString("risk_type"))
				.build();
	}

	/** ---------------------- AREA MAPPING DETAIL ---------------------- */
	private void addAreaMappingDetail(ResultSet rs, BPA bpa) throws SQLException {
		String areaId = rs.getString("area_id");
		if (areaId == null) return;

		AreaMappingDetail areaMappingDetail = new AreaMappingDetail();
		areaMappingDetail.setId(areaId);
		areaMappingDetail.setDistrict(rs.getString("area_district"));
		areaMappingDetail.setPlanningArea(rs.getString("area_planning_area"));

		String planningAuthority = rs.getString("area_planning_permit_authority");
		if (planningAuthority != null) {
			areaMappingDetail.setPlanningPermitAuthority(
					PlanningPermitAuthorityEnum.valueOf(planningAuthority));
		}

		String buildingAuthority = rs.getString("area_building_permit_authority");
		if (buildingAuthority != null) {
			areaMappingDetail.setBuildingPermitAuthority(
					BuildingPermitAuthorityEnum.valueOf(buildingAuthority));
		}

		areaMappingDetail.setRevenueVillage(rs.getString("area_revenue_village"));
		areaMappingDetail.setVillageName(rs.getString("area_village_name"));
		areaMappingDetail.setConcernedAuthority(rs.getString("area_concerned_authority"));
		areaMappingDetail.setMouza(rs.getString("area_mouza"));
		areaMappingDetail.setWard(rs.getString("area_ward"));

		bpa.setAreaMapping(areaMappingDetail);
	}
}
