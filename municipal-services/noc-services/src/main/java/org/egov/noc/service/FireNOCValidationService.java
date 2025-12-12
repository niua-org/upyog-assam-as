package org.egov.noc.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for validating Fire NOC by calling external Fire NOC verification API
 */
@Service
@Slf4j
public class FireNOCValidationService {

	@Autowired
	private NOCConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	/**
	 * Validates Fire NOC ARN number by calling external Fire NOC verification API
	 * 
	 * @param nocNumber Fire NOC ARN number to validate (e.g., "FNESBNOCI/2025/03415")
	 * @return Map containing full validation response including status, message, and noc_details
	 * @throws CustomException if NOC number is invalid, validation fails, or API call fails
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> validateFireNOC(String nocNumber) {
		if (StringUtils.isEmpty(nocNumber)) {
			throw new CustomException("INVALID_NOC_NUMBER", "NOC number is required for validation");
		}

		if (StringUtils.isEmpty(config.getFireNocApiUrl())) {
			throw new CustomException("FIRE_NOC_API_NOT_CONFIGURED", 
					"Fire NOC API URL is not configured. Please set fire.noc.api.url property.");
		}

		try {
			Map<String, String> requestBody = new HashMap<>();
			requestBody.put("noc_number", nocNumber);

			Map<String, String> headers = new HashMap<>();
			headers.put("Content-Type", "application/json");
			if (!StringUtils.isEmpty(config.getFireNocApiSessionCookie())) {
				headers.put("Cookie", config.getFireNocApiSessionCookie());
			}

			StringBuilder uri = new StringBuilder(config.getFireNocApiUrl());
			log.info("Validating Fire NOC: {}", nocNumber);
			
			Object response = serviceRequestRepository.fetchResultWithHeaders(uri, requestBody, headers);
			LinkedHashMap<String, Object> responseMap = (LinkedHashMap<String, Object>) response;
			
			Boolean status = (Boolean) responseMap.get("status");
			String message = (String) responseMap.get("message");

			if (status == null || !status) {
				log.warn("Fire NOC validation failed for {}: {}", nocNumber, message);
				throw new CustomException("FIRE_NOC_VALIDATION_FAILED",
						message != null ? message : "Fire NOC validation failed");
			}

			LinkedHashMap<String, Object> nocDetails = (LinkedHashMap<String, Object>) responseMap.get("noc_details");
			if (nocDetails == null) {
				throw new CustomException("INVALID_RESPONSE", "NOC details not found in response");
			}

			log.info("Fire NOC validation successful for: {}", nocNumber);
			return responseMap;

		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error validating Fire NOC {}: {}", nocNumber, e.getMessage(), e);
			throw new CustomException("FIRE_NOC_VALIDATION_ERROR",
					"Error validating Fire NOC: " + e.getMessage());
		}
	}
}

