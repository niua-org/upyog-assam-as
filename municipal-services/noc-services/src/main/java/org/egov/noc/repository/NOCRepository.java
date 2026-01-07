package org.egov.noc.repository;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.exception.InvalidTenantIdException;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.producer.Producer;
import org.egov.noc.repository.builder.NocQueryBuilder;
import org.egov.noc.repository.rowmapper.NocRowMapper;
import org.egov.noc.repository.rowmapper.NocDetailRowMapper;
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
	private NocDetailRowMapper detailRowMapper;

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
	 * this method will be used to fetch the complete noc data with documents
	 * @param criteria
	 * @return
	 */
	public List<Noc> getNocDataDetails(NocSearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getNocDetailSearchQuery(criteria, preparedStmtList, false);
		try {
			query = centralInstanceUtil.replaceSchemaPlaceholder(query, criteria.getTenantId());
		} catch (InvalidTenantIdException e) {
			throw new CustomException("EG_NOC_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");
		}
		log.info("preparedStmtList.toArray(:"+preparedStmtList.toArray().toString());
		List<Noc> nocList = jdbcTemplate.query(query, preparedStmtList.toArray(), detailRowMapper);
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
	public List<Noc> getNocDatav2(NocSearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getNocDetailSearchQuery(criteria, preparedStmtList, false);
		
		try {
			query = query.replaceAll("\\{schema\\}\\.", "");
			List<Noc> nocList = jdbcTemplate.query(query, preparedStmtList.toArray(), detailRowMapper);
			return nocList;
		} catch (Exception e) {
			log.error("Error fetching NOC data", e);
			throw new CustomException("EG_NOC_QUERY_ERROR",
					"Error executing query to fetch NOC applications: " + e.getMessage());
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
                String query = queryBuilder.getNocDetailSearchQuery(criteria, preparedStmtList, true);
				try {
					query = centralInstanceUtil.replaceSchemaPlaceholder(query, criteria.getTenantId());
				} catch (InvalidTenantIdException e) {
					throw new CustomException("EG_NOC_TENANTID_ERROR",
							"TenantId length is not sufficient to replace query schema in a multi state instance");
				}
                int count = jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
                return count;
        }

		/**
		 * Get Noc data based on search criteria for normal search results
		 * this method will be used to fetch only summary noc data without documents and lesser details
		 * @param criteria
		 * @return List<Noc>
		 * */
	public List<Noc> getNocData(NocSearchCriteria criteria) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getNocDataSearchQuery(criteria, preparedStmtList, false);
		try {
			query = centralInstanceUtil.replaceSchemaPlaceholder(query, criteria.getTenantId());
		} catch (InvalidTenantIdException e) {
			throw new CustomException("EG_NOC_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");
		}
		log.info("preparedStmtList.toArray(:"+preparedStmtList.toArray().toString());
		log.info("NOC basic data Query : "+query);
		List<Noc> nocList = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
		return nocList;
	}

}
