package org.egov.bpa.service;

import java.util.Arrays;
import java.util.List;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.CalculationReq;
import org.egov.bpa.web.model.CalulationCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CalculationService {

	private ServiceRequestRepository serviceRequestRepository;

	private BPAConfiguration config;

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
	public void addCalculation(BPARequest bpaRequest) {

		BPA bpa = bpaRequest.getBPA();
		CalculationReq calulcationRequest = new CalculationReq();
		calulcationRequest.setRequestInfo(bpaRequest.getRequestInfo());
		CalulationCriteria calculationCriteria = new CalulationCriteria();
		calculationCriteria.setApplicationNo(bpa.getApplicationNo());
		calculationCriteria.setBpa(bpa);
		calculationCriteria.setFeeType(bpa.getFeeType());
		calculationCriteria.setTenantId(bpa.getTenantId());
		calculationCriteria.setBpa(bpa);
		calculationCriteria.setFloors(bpa.getFloors());
		calculationCriteria.setWallType(bpa.getWallType());
		calculationCriteria.setApplicationType(bpa.getApplicationType());
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
