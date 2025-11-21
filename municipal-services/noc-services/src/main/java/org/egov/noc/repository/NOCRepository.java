package org.egov.noc.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
	
	public List<Noc> getNewAAINocData(NocSearchCriteria criteria) {

		StringBuilder query = new StringBuilder("SELECT NOC.APPLICATIONNO FROM EG_NOC NOC WHERE 1=1");

		String nocType = criteria.getNocType();
		if (nocType != null && !nocType.trim().isEmpty()) {
			List<String> nocTypes = Arrays.stream(nocType.split(",")).map(s -> "'" + s.trim() + "'")
					.collect(Collectors.toList());
			query.append(" AND NOC.NOCTYPE IN (").append(String.join(",", nocTypes)).append(")");
		}

		String applicationStatus = criteria.getApplicationStatus();
		if (applicationStatus != null && !applicationStatus.trim().isEmpty()) {
			List<String> statuses = Arrays.stream(applicationStatus.split(",")).map(s -> "'" + s.trim() + "'")
					.collect(Collectors.toList());
			query.append(" AND NOC.APPLICATIONSTATUS IN (").append(String.join(",", statuses)).append(")");
		}

		return jdbcTemplate.query(query.toString(), (rs, rowNum) -> {
			Noc noc = new Noc();
			noc.setApplicationNo(rs.getString("applicationno"));
			return noc;
		});
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
