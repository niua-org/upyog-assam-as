package org.egov.bpa.web.controller;

import org.egov.bpa.service.BPAService;
import org.egov.bpa.web.model.CalculationReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2/bpa")
public class BPAControllerV2 {

	@Autowired
	private BPAService bpaService;

	/**
	 * Wrapper API to bpa-calculator /_estimate API as cannot access bpa-calculator
	 * APIs from UI directly
	 * 
	 * @param bpaRequest The calculation Request
	 * @return Calculation Response
	 */
	@PostMapping(value = { "/_estimate" })
	public ResponseEntity<Object> getFeeEstimatev2(@RequestBody CalculationReq bpaRequest) {
		Object response = bpaService.getFeeEstimateFromBpaCalculatorV2(bpaRequest);
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
