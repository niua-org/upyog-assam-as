package org.egov.bpa.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.bpa.exception.PropertyServiceException;
import org.egov.bpa.service.property.PropertyValidationService;
import org.egov.bpa.web.model.property.PropertyRequest;
import org.egov.bpa.web.model.property.PropertyValidationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/property")
@RequiredArgsConstructor
@Slf4j
public class PropertyController {

    private final PropertyValidationService propertyValidationService;

    /**
     * Single API endpoint to validate property and check tax paid status
     *
     * @param propertyRequest - PropertyRequest containing property number and request info
     * @return PropertyValidationResponse with validation status and tax paid status
     */
    @PostMapping("/validate")
    public ResponseEntity<PropertyValidationResponse> validateProperty(@Valid @RequestBody PropertyRequest propertyRequest) {
        String propertyNumber = propertyRequest.getPropertyNumber();

        log.info("Received request to validate property: {}", propertyNumber);

        PropertyValidationResponse response = propertyValidationService
                .validatePropertyWithTaxStatus(propertyNumber);

        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(PropertyServiceException.class)
    public ResponseEntity<Map<String, String>> handleServiceException(
            PropertyServiceException e) {
        log.error("Property service exception: {}", e.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("error", "Service Error");
        error.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        Map<String, String> error = new HashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

