package org.egov.bpa.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.CalculationReq;
import org.egov.bpa.web.model.CalulationCriteria;
import org.egov.bpa.web.model.Floor;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CalculationService {

	private ServiceRequestRepository serviceRequestRepository;

	private BPAConfiguration config;

    @Autowired
    private EDCRService edcrService;

	@Autowired
	public CalculationService(ServiceRequestRepository serviceRequestRepository, BPAConfiguration config) {
		this.serviceRequestRepository = serviceRequestRepository;
		this.config = config;
	}

	/**
	 * add calculation for the bpa object based on the FeeType
	 * @param bpaRequest
	 * @param feeType
	 */
	public void addCalculation(BPARequest bpaRequest, String feeType) {

		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		BPA bpa = bpaRequest.getBPA();

		Map<String, Object> edcrDetails = edcrService.getEDCRFeeCalculationDetails(requestInfo, bpa);
		List<Floor> floors = (List<Floor>) edcrDetails.get(BPAConstants.FLOOR);
		String wallType = (String) edcrDetails.get(BPAConstants.WALLTYPE);
		String occupancy = (String) edcrDetails.get(BPAConstants.APPLICATIONTYPE);
		String subOccupancy = (String) edcrDetails.get(BPAConstants.SUB_OCCUPANCY);
		BigDecimal premiumBuiltupFarArea = (BigDecimal) edcrDetails.get(BPAConstants.PREMIUMFARAREA);

		CalculationReq calulcationRequest = new CalculationReq();
		calulcationRequest.setRequestInfo(bpaRequest.getRequestInfo());

		CalulationCriteria calculationCriteria = new CalulationCriteria();
		calculationCriteria.setApplicationNo(bpa.getApplicationNo());
		calculationCriteria.setBpa(bpa);
		calculationCriteria.setFeeType(feeType);
		calculationCriteria.setTenantId(bpa.getTenantId());
		calculationCriteria.setFloors(floors);
		calculationCriteria.setWallType(wallType);
		calculationCriteria.setApplicationType(occupancy);
		calculationCriteria.setSubOccupancy(subOccupancy);
		calculationCriteria.setPremiumBuiltUpArea(premiumBuiltupFarArea);

		List<CalulationCriteria> criterias = Arrays.asList(calculationCriteria);
		calulcationRequest.setCalulationCriteria(criterias);

		StringBuilder url = new StringBuilder();
		url.append(this.config.getCalculatorHost());
		url.append(this.config.getCalulatorEndPoint());

		this.serviceRequestRepository.fetchResult(url, calulcationRequest);
	}

	/**
	 * call bpa-calculator /_estimate API
	 * @param bpaRequest
	 */
	public Object callBpaCalculatorEstimate(Object bpaRequest) { 
		StringBuilder url = new StringBuilder();
		url.append(this.config.getCalculatorHost());
		url.append(this.config.getBpaCalculationEstimateEndpoint());
		return this.serviceRequestRepository.fetchResult(url, bpaRequest);
	}
	

}
