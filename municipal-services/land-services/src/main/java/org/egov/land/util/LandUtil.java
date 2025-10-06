package org.egov.land.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.request.RequestInfo;
import org.egov.land.config.LandConfiguration;
import org.egov.land.repository.ServiceRequestRepository;
import org.egov.land.web.models.AuditDetails;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;

@Component
public class LandUtil {

	private LandConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	public final static String DATE_FORMAT = "dd-MM-yyyy";

	@Autowired
	public LandUtil(LandConfiguration config, ServiceRequestRepository serviceRequestRepository) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
	}

	/**
	 * Method to return auditDetails for create/update flows
	 *
	 * @param by
	 * @param isCreate
	 * @return AuditDetails
	 */
	public AuditDetails getAuditDetails(String by, Boolean isCreate) {
		Long time = System.currentTimeMillis();
		if (isCreate)
			return AuditDetails.builder().createdBy(by).lastModifiedBy(by).createdTime(time).lastModifiedTime(time)
					.build();
		else
			return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
	}

	/**
	 * Returns the URL for MDMS search end point
	 *
	 * @return URL for MDMS search end point
	 */
	public StringBuilder getMdmsSearchUrl() {
		return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());
	}

	/**
	 * Creates request to search ApplicationType and etc from MDMS
	 * 
	 * @param requestInfo
	 *            The requestInfo of the request
	 * @param tenantId
	 *            The tenantId of the BPA
	 * @return request to search ApplicationType and etc from MDMS
	 */
	public List<ModuleDetail> getBPAModuleRequest() {

		// master details for BPA module
		List<MasterDetail> bpaMasterDtls = new ArrayList<>();

		// filter to only get code field from master data
		final String filterCode = "$.[?(@.active==true)].code";

		bpaMasterDtls.add(MasterDetail.builder().name(LandConstants.APPLICATION_TYPE).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(LandConstants.SERVICE_TYPE).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(LandConstants.DOCUMENT_TYPE_MAPPING).build());
		bpaMasterDtls.add(MasterDetail.builder().name(LandConstants.RISKTYPE_COMPUTATION).build());
		bpaMasterDtls.add(MasterDetail.builder().name(LandConstants.OCCUPANCY_TYPE).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(LandConstants.SUB_OCCUPANCY_TYPE).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(LandConstants.USAGES).filter(filterCode).build());
		bpaMasterDtls.add(MasterDetail.builder().name(LandConstants.CalculationType).build());
		bpaMasterDtls.add(MasterDetail.builder().name(LandConstants.CHECKLIST_NAME).build());
		ModuleDetail bpaModuleDtls = ModuleDetail.builder().masterDetails(bpaMasterDtls)
				.moduleName(LandConstants.BPA_MODULE).build();

		// master details for common-masters module
		List<MasterDetail> commonMasterDetails = new ArrayList<>();
		commonMasterDetails
				.add(MasterDetail.builder().name(LandConstants.OWNERSHIP_CATEGORY).filter(filterCode).build());
		commonMasterDetails.add(MasterDetail.builder().name(LandConstants.OWNER_TYPE).filter(filterCode).build());
		commonMasterDetails.add(MasterDetail.builder().name(LandConstants.DOCUMENT_TYPE).filter(filterCode).build());
		ModuleDetail commonMasterMDtl = ModuleDetail.builder().masterDetails(commonMasterDetails)
				.moduleName(LandConstants.COMMON_MASTERS_MODULE).build();

		return Arrays.asList(bpaModuleDtls, commonMasterMDtl);

	}

	private MdmsCriteriaReq getMDMSRequest(RequestInfo requestInfo, String tenantId) {
		List<ModuleDetail> moduleRequest = getBPAModuleRequest();

		List<ModuleDetail> moduleDetails = new LinkedList<>();
		moduleDetails.addAll(moduleRequest);

		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId).build();

		MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria).requestInfo(requestInfo)
				.build();
		return mdmsCriteriaReq;
	}

	public Object mDMSCall(RequestInfo requestInfo, String tenantId) {
		MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(requestInfo, tenantId);
		Object result = serviceRequestRepository.fetchResult(getMdmsSearchUrl(), mdmsCriteriaReq);
		return result;
	}
	
	
	public void defaultJsonPathConfig() {
		Configuration.setDefaults(new Configuration.Defaults() {

			private final JsonProvider jsonProvider = new JacksonJsonProvider();
			private final MappingProvider mappingProvider = new JacksonMappingProvider();

			@Override
			public JsonProvider jsonProvider() {
				return jsonProvider;
			}

			@Override
			public MappingProvider mappingProvider() {
				return mappingProvider;
			}

			@Override
			public Set<Option> options() {
				return EnumSet.noneOf(Option.class);
			}
		});
	}


	public static Long getCurrentTimestamp() {
		return Instant.now().toEpochMilli();
	}

	public static LocalDate getCurrentDate() {
		return LocalDate.now();
	}

	public static String getRandonUUID() {
		return UUID.randomUUID().toString();
	}

	public static LocalDate parseStringToLocalDate(String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
		LocalDate localDate = LocalDate.parse(date, formatter);
		return localDate;
	}

	public static String parseLocalDateToString(LocalDate date, String dateFormat) {
		if (dateFormat == null) {
			dateFormat = DATE_FORMAT;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
		// Format the LocalDate
		String formattedDate = date.format(formatter);
		return formattedDate;
	}

	public static String convertDateFormat(String date, String dateFormat) {
		if (dateFormat == null) {
			dateFormat = DATE_FORMAT;
		}
		LocalDate localDate = parseStringToLocalDate(date);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
		// Format the LocalDate
		String formattedDate = localDate.format(formatter);
		return formattedDate;
	}

	public static AuditDetails getAuditDetails(ResultSet rs) throws SQLException {
		AuditDetails auditdetails = AuditDetails.builder().createdBy(rs.getString("createdBy"))
				.createdTime(rs.getLong("createdTime")).lastModifiedBy(rs.getString("lastModifiedBy"))
				.lastModifiedTime(rs.getLong("lastModifiedTime")).build();
		return auditdetails;
	}

	public static String beuatifyJson(Object result) {
		ObjectMapper mapper = new ObjectMapper();
		String data = null;
		try {
			data = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}



}
