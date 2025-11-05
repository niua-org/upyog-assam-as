package org.egov.bpa.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.producer.Producer;
import org.egov.bpa.repository.querybuilder.BPAQueryBuilder;
import org.egov.bpa.repository.rowmapper.BPARowMapper;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.exception.InvalidTenantIdException;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class BPARepository {

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private Producer producer;

	@Autowired
	private BPAQueryBuilder queryBuilder;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private BPARowMapper rowMapper;

	@Autowired
	private MultiStateInstanceUtil centralInstanceUtil;

	/**
	 * Pushes the request on save topic through kafka
	 *
	 * @param bpaRequest
	 *            The bpa create request
	 */
	public void save(BPARequest bpaRequest) {
		producer.push(bpaRequest.getBPA().getTenantId(),config.getSaveTopic(), bpaRequest);
	}

	/**
	 * pushes the request on update or workflow update topic through kafaka based on th isStateUpdatable 
	 * @param bpaRequest
	 */
	public void update(BPARequest bpaRequest, String type) {
		log.info("BPARepository update type : {}", type);
		switch (type) {
			case BPAConstants.RTP_UPDATE:
				producer.push(bpaRequest.getBPA().getTenantId(), config.getUpdateRTPDetailsTopic(), bpaRequest);
				break;
			case BPAConstants.UPDATE_ALL_BUILDING_PLAN:
				producer.push(bpaRequest.getBPA().getTenantId(), config.getUpdateAllBuildingPlanTopic(), bpaRequest);
				break;
			default:
				producer.push(bpaRequest.getBPA().getTenantId(), config.getUpdateTopic(), bpaRequest);
				break;
		}
	}

	/**
	 * BPA search in database
	 *
	 * @param criteria
	 *            The BPA Search criteria
	 * @return List of BPA from search
	 */
	public List<BPA> getBPAData(BPASearchCriteria criteria, List<String> edcrNos) {
		List<Object> preparedStmtList = new ArrayList<>();
		String query = queryBuilder.getBPASearchQuery(criteria, preparedStmtList, edcrNos, false);
		try {
			query = centralInstanceUtil.replaceSchemaPlaceholder(query, criteria.getTenantId());
			log.info("getBPAData query : {} and preparedStmtList : {}", query, preparedStmtList);
		} catch (InvalidTenantIdException e) {
			throw new CustomException("EG_PT_TENANTID_ERROR",
					"TenantId length is not sufficient to replace query schema in a multi state instance");
		}
		List<BPA> BPAData = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
		return BPAData;
	}
	
	/**
         * BPA search count in database
         *
         * @param criteria
         *            The BPA Search criteria
         * @return count of BPA from search
         */
        public int getBPACount(BPASearchCriteria criteria, List<String> edcrNos) {
                List<Object> preparedStmtList = new ArrayList<>();
                String query = queryBuilder.getBPASearchQuery(criteria, preparedStmtList, edcrNos, true);
				try {
					query = centralInstanceUtil.replaceSchemaPlaceholder(query, criteria.getTenantId());
				} catch (InvalidTenantIdException e) {
					throw new CustomException("EG_PT_TENANTID_ERROR",
							"TenantId length is not sufficient to replace query schema in a multi state instance");
				}
                int count = jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
                return count;
        }

        public List<BPA> getBPADataForPlainSearch(BPASearchCriteria criteria, List<String> edcrNos) {
    		List<Object> preparedStmtList = new ArrayList<>();
    		String query = queryBuilder.getBPASearchQueryForPlainSearch(criteria, preparedStmtList, edcrNos, false);
			try {
				query = centralInstanceUtil.replaceSchemaPlaceholder(query, criteria.getTenantId());
			} catch (InvalidTenantIdException e) {
				throw new CustomException("EG_PT_TENANTID_ERROR",
						"TenantId length is not sufficient to replace query schema in a multi state instance");
			}
    		List<BPA> BPAData = jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
    		return BPAData;
    	}

}
