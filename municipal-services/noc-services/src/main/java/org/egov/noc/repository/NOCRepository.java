package org.egov.noc.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.exception.InvalidTenantIdException;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.producer.Producer;
import org.egov.noc.repository.builder.NocQueryBuilder;
import org.egov.noc.repository.rowmapper.NocRowMapper;
import org.egov.noc.web.model.Noc;
import org.egov.noc.web.model.NocRequest;
import org.egov.noc.web.model.NocSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class NOCRepository {
	
	@Autowired
	private Producer producer;
	
	@Autowired
	private NOCConfiguration config;	

	@Autowired
	private NocQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private NocRowMapper rowMapper;

	@Autowired
	private MultiStateInstanceUtil centralInstanceUtil;

	/**
	 * push the nocRequest object to the producer on the save topic
	 * @param nocRequest
	 */
	public void save(NocRequest nocRequest) {
		producer.push(nocRequest.getNoc().getTenantId(),config.getSaveTopic(), nocRequest);
	}
	
	/**
	 * pushes the nocRequest object to updateTopic if stateupdatable else to update workflow topic
	 * @param nocRequest
	 * @param isStateUpdatable
	 */
	public void update(NocRequest nocRequest, boolean isStateUpdatable) {
		log.info("Pushing NOC record with application status - "+nocRequest.getNoc().getApplicationStatus());
		if (isStateUpdatable) {
			producer.push(nocRequest.getNoc().getTenantId(),config.getUpdateTopic(), nocRequest);
		} else {
		    producer.push(nocRequest.getNoc().getTenantId(),config.getUpdateWorkflowTopic(), nocRequest);
		}
	}
	/**
	 * using the queryBulider query the data on applying the search criteria and return the data 
	 * parsing throw row mapper
	 * @param criteria
	 * @return
	 */
	public List<Noc> getNocData(NocSearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getNocSearchQuery(criteria, preparedStmtList, false);
		try {
			query = centralInstanceUtil.replaceSchemaPlaceholder(query, criteria.getTenantId());
		} catch (InvalidTenantIdException e) {
			throw new CustomException("EG_NOC_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");
		}
		log.info("preparedStmtList.toArray(:"+preparedStmtList.toArray().toString());
		List<Noc> nocList = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
		return nocList;
	}
	
	/**
	 * Retrieves Source reference ID, Tenant ID and documents of NOC records from the database
	 * based on the given search criteria. Builds a dynamic SQL query using NOC type
	 * and application status filters. This method handles multi-tenant scenarios by
	 * using state-level tenant for schema replacement.
	 *
	 * @param criteria search filters for NOC type and status
	 * @return list of matching NOC records with documents
	 */
	public List<Noc> getNewAAINocData(NocSearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		StringBuilder query = new StringBuilder("SELECT NOC.SOURCEREFID, NOC.TENANTID, NOC.ID as NOC_ID, ")
				.append("NOCDOC.ID as NOC_DOC_ID, NOCDOC.DOCUMENTTYPE, NOCDOC.FILESTOREID, ")
				.append("NOCDOC.DOCUMENTUID ")
				.append("FROM EG_NOC NOC ")
				.append("LEFT OUTER JOIN EG_NOC_DOCUMENT NOCDOC ON NOCDOC.NOCID = NOC.ID ")
				.append("WHERE 1=1");

		String nocType = criteria.getNocType();
		if (nocType != null && !nocType.trim().isEmpty()) {
			List<String> nocTypes = Arrays.stream(nocType.split(","))
					.map(String::trim)
					.collect(Collectors.toList());
			query.append(" AND NOC.NOCTYPE IN (")
					.append(String.join(",", Collections.nCopies(nocTypes.size(), "?")))
					.append(")");
			preparedStmtList.addAll(nocTypes);
		}

		String applicationStatus = criteria.getApplicationStatus();
		if (applicationStatus != null && !applicationStatus.trim().isEmpty()) {
			List<String> statuses = Arrays.stream(applicationStatus.split(","))
					.map(String::trim)
					.collect(Collectors.toList());
			query.append(" AND NOC.APPLICATIONSTATUS IN (")
					.append(String.join(",", Collections.nCopies(statuses.size(), "?")))
					.append(")");
			preparedStmtList.addAll(statuses);
		}

		try {
			String finalQuery = centralInstanceUtil.replaceSchemaPlaceholder(query.toString(), criteria.getTenantId());
			
			Map<String, Noc> nocMap = new HashMap<>();
			jdbcTemplate.query(finalQuery, preparedStmtList.toArray(), (rs, rowNum) -> {
				String nocId = rs.getString("NOC_ID");
				Noc noc = nocMap.get(nocId);
				
				if (noc == null) {
					noc = new Noc();
					noc.setId(nocId);
					noc.setSourceRefId(rs.getString("SOURCEREFID"));
					noc.setTenantId(rs.getString("TENANTID"));
					noc.setDocuments(new ArrayList<>());
					nocMap.put(nocId, noc);
				}
				
				String documentId = rs.getString("NOC_DOC_ID");
				if (documentId != null && !documentId.trim().isEmpty()) {
					org.egov.noc.web.model.Document document = new org.egov.noc.web.model.Document();
					document.setId(documentId);
					document.setDocumentType(rs.getString("DOCUMENTTYPE"));
					document.setFileStoreId(rs.getString("FILESTOREID"));
					document.setDocumentUid(rs.getString("DOCUMENTUID"));
					
					noc.addDocumentsItem(document);
				}
				
				return noc;
			});
			
			return new ArrayList<>(nocMap.values());
		} catch (InvalidTenantIdException e) {
			throw new CustomException("EG_NOC_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");
		} catch (Exception e) {
			log.error("Error fetching new AAI NOC data", e);
			throw new CustomException("EG_NOC_QUERY_ERROR",
					"Error executing query to fetch new AAI NOC applications: " + e.getMessage());
		}
	}
	
	/**
         * using the queryBulider query the data on applying the search criteria and return the count 
         * parsing throw row mapper
         * @param criteria
         * @return
         */
        public Integer getNocCount(NocSearchCriteria criteria) {
                List<Object> preparedStmtList = new ArrayList<>();
                String query = queryBuilder.getNocSearchQuery(criteria, preparedStmtList, true);
				try {
					query = centralInstanceUtil.replaceSchemaPlaceholder(query, criteria.getTenantId());
				} catch (InvalidTenantIdException e) {
					throw new CustomException("EG_NOC_TENANTID_ERROR",
							"TenantId length is not sufficient to replace query schema in a multi state instance");
				}
                int count = jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
                return count;
        }

}
