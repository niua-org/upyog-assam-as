package org.egov.land.repository.querybuilder;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.egov.land.config.LandConfiguration;
import org.egov.land.web.models.LandSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Slf4j
@Component
public class LandQueryBuilder {

	@Autowired
	private LandConfiguration config;

	private static final String INNER_JOIN_STRING = " INNER JOIN ";
	private static final String LEFT_OUTER_JOIN_STRING = " LEFT OUTER JOIN ";

	private static final String QUERY = "SELECT "
			+ "landInfo.id AS land_id, landInfo.land_uid AS land_uid, landInfo.land_unique_reg_no AS land_regno, "
			+ "landInfo.tenant_id AS land_tenant_id, landInfo.status AS land_status, landInfo.ownership_category AS ownership_category, "
			+ "landInfo.source AS source, landInfo.channel AS channel, landInfo.old_dag_no AS old_dag_no, landInfo.new_dag_no AS new_dag_no, "
			+ "landInfo.old_patta_no AS old_patta_no, landInfo.new_patta_no AS new_patta_no, landInfo.total_plot_area AS total_plot_area, "
			+ "landInfo.created_by AS land_created_by, landInfo.last_modified_by AS land_last_modified_by, "
			+ "landInfo.created_time AS land_created_time, landInfo.last_modified_time AS land_last_modified_time, "
			+ "landInfo.additional_details AS land_additional_details, "

			+ "landAddress.id AS land_address_id, landAddress.house_no AS house_no, landAddress.address_line_1 AS address_line_1, "
			+ "landAddress.address_line_2 AS address_line_2, landAddress.landmark AS landmark, landAddress.locality AS locality, "
			+ "landAddress.district AS district, landAddress.region AS region, landAddress.state AS state, "
			+ "landAddress.country AS country, landAddress.pincode AS pincode, "

			+ "landGeo.id AS geo_id, landGeo.latitude AS latitude, landGeo.longitude AS longitude, "

			+ "landOwner.id AS owner_id, landOwner.uuid AS owner_uuid, landOwner.is_primary_owner AS is_primary_owner, "
			+ "landOwner.ownership_percentage AS ownership_percentage, landOwner.institution_id AS owner_institution_id, "
			+ "landOwner.mother_name AS owner_mother_name, landOwner.status AS owner_status, "

			+ "ownerAddress.id AS owner_address_id, ownerAddress.owner_info_id AS owner_info_id, ownerAddress.house_no AS owner_house_no, "
			+ "ownerAddress.address_line_1 AS owner_address_line1, ownerAddress.address_line_2 AS owner_address_line2, "
			+ "ownerAddress.landmark AS owner_landmark, ownerAddress.locality AS owner_locality, ownerAddress.district AS owner_district, "
			+ "ownerAddress.region AS owner_region, ownerAddress.state AS owner_state, ownerAddress.country AS owner_country, "
			+ "ownerAddress.pincode AS owner_pincode, ownerAddress.address_type AS owner_address_type, "

			+ "landInst.id AS inst_id, landInst.type AS inst_type, landInst.designation AS inst_designation, "
			+ "landInst.name_of_authorized_person AS inst_authorized_person, "

			+ "landDoc.id AS doc_id, landDoc.document_type AS doc_type, landDoc.file_store_id AS doc_filestore, landDoc.document_uid AS doc_uid, "

			+ "landUnit.id AS unit_id, landUnit.floor_no AS floor_no, landUnit.unit_type AS unit_type, "
			+ "landUnit.usage_category AS usage_category, landUnit.occupancy_type AS occupancy_type, landUnit.occupancy_date AS occupancy_date "

			+ "FROM {schema}.ug_land_info landInfo " + INNER_JOIN_STRING
			+ "{schema}.ug_land_address landAddress ON landAddress.land_info_id = landInfo.id " + LEFT_OUTER_JOIN_STRING
			+ "{schema}.ug_land_geolocation landGeo ON landGeo.address_id = landAddress.id " + INNER_JOIN_STRING
			+ "{schema}.ug_land_owner_info landOwner ON landOwner.land_info_id = landInfo.id AND landOwner.status = true " + LEFT_OUTER_JOIN_STRING
			+ "{schema}.ug_land_owner_address ownerAddress ON ownerAddress.owner_info_id = landOwner.id " + LEFT_OUTER_JOIN_STRING
			+ "{schema}.ug_land_institution landInst ON landInst.land_info_id = landInfo.id " + LEFT_OUTER_JOIN_STRING
			+ "{schema}.ug_land_document landDoc ON landDoc.land_info_id = landInfo.id " + LEFT_OUTER_JOIN_STRING
			+ "{schema}.ug_land_unit landUnit ON landUnit.land_info_id = landInfo.id ";

	private final String paginationWrapper =
			"SELECT * FROM "
					+ "(SELECT *, DENSE_RANK() OVER (ORDER BY land_last_modified_time DESC) offset_ FROM ({}) result) result_offset "
					+ "WHERE offset_ > ? AND offset_ <= ?";


	/**
	 * To give the Search query based on the requirements.
	 * 
	 * @param criteria
	 *            landInfo search criteria
	 * @param preparedStmtList
	 *            values to be replaced on the query
	 * @return Final Search Query
	 */
	public String getLandInfoSearchQuery(LandSearchCriteria criteria, List<Object> preparedStmtList) {

		StringBuilder builder = new StringBuilder(QUERY);

		// Inbox search does not require tenant filter for Development Authority User
		// Tenant filter
		if (criteria.getTenantId() != null && !Boolean.TRUE.equals(criteria.getIsInboxSearch())) {
			if (criteria.getTenantId().split("\\.").length == 1) {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" landInfo.tenant_id LIKE ? ");
				preparedStmtList.add('%' + criteria.getTenantId() + '%');
			} else {
				addClauseIfRequired(preparedStmtList, builder);
				builder.append(" landInfo.tenant_id = ? ");
				preparedStmtList.add(criteria.getTenantId());
			}
		}
		else {
			log.info("Skipping tenant filter for inbox search"+ criteria.getIsInboxSearch() + " : "+ criteria.getTenantId());
		}
		// IDs filter
		List<String> ids = criteria.getIds();
		if (!CollectionUtils.isEmpty(ids)) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" landInfo.id IN (").append(createQuery(ids)).append(") ");
			addToPreparedStatement(preparedStmtList, ids);
		}

		// User IDs (Owner UUIDs)
		if (criteria.getUserIds() != null && !criteria.getUserIds().isEmpty()) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" landOwner.uuid IN (").append(createQuery(criteria.getUserIds())).append(") ");
			addToPreparedStatement(preparedStmtList, criteria.getUserIds());
		}

		// Land UID
		if (criteria.getLandUId() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" landInfo.land_uid = ? ");
			preparedStmtList.add(criteria.getLandUId());
		}

		// Locality filter
		if (criteria.getLocality() != null) {
			addClauseIfRequired(preparedStmtList, builder);
			builder.append(" landAddress.locality = ? ");
			preparedStmtList.add(criteria.getLocality());
		}

		return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
	}



	/**
	 * 
	 * @param query
	 *            prepared Query
	 * @param preparedStmtList
	 *            values to be replaced on the query
	 * @param criteria
	 *            landInfo search criteria
	 * @return the query by replacing the placeholders with preparedStmtList
	 */
	private String addPaginationWrapper(String query, List<Object> preparedStmtList, LandSearchCriteria criteria) {

		int limit = config.getDefaultLimit();
		int offset = config.getDefaultOffset();
		String finalQuery = paginationWrapper.replace("{}", query);

		if(criteria.getLimit() == null && criteria.getOffset() == null) {
        	limit = config.getMaxSearchLimit();
        } 
		
		if (criteria.getLimit() != null && criteria.getLimit() <= config.getMaxSearchLimit())
			limit = criteria.getLimit();

		if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit()) {
			limit = config.getMaxSearchLimit();
		}

		if (criteria.getOffset() != null)
			offset = criteria.getOffset();

		if (limit == -1) {
			finalQuery = finalQuery.replace("WHERE offset_ > ? AND offset_ <= ?", "");
		} else {
			preparedStmtList.add(offset);
			preparedStmtList.add(limit + offset);
		}

		return finalQuery;

	}

	private void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
		if (values.isEmpty())
			queryString.append(" WHERE ");
		else {
			queryString.append(" AND");
		}
	}

	private void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
		ids.forEach(preparedStmtList::add);
	}

	private Object createQuery(List<String> ids) {
		StringBuilder builder = new StringBuilder();
		int length = ids.size();
		for (int i = 0; i < length; i++) {
			builder.append(" ?");
			if (i != length - 1)
				builder.append(",");
		}
		return builder.toString();
	}
}
