package org.egov.bpa.service.property;
import org.egov.bpa.web.model.property.SumatoPropertyRequest;
import org.egov.bpa.web.model.property.PropertyResponse;
import org.egov.bpa.web.model.property.PropertyValidationResponse;
import org.egov.bpa.web.model.property.PropertyDetails;
import org.egov.bpa.exception.PropertyNotFoundException;
import org.egov.bpa.exception.PropertyServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PropertyValidationService {

    private final PropertyServiceClient propertyServiceClient;

    /**
     * Validates property and returns complete validation response with tax status
     */
    public PropertyValidationResponse validatePropertyWithTaxStatus(String propertyNumber) {
        try {
            log.info("Validating property with number: {}", propertyNumber);

            Instant start = Instant.now();

            SumatoPropertyRequest request = SumatoPropertyRequest.builder()
                    .property(propertyNumber)
                    .build();

            PropertyResponse response = propertyServiceClient.fetchPropertyDetails(request);

            Duration duration = Duration.between(start, Instant.now());
            double seconds = duration.toMillis() / 1000.0;

            log.info("API call to sumato property service took {} seconds", seconds);

            log.info("Received response from property service for property: {}", propertyNumber);
            log.info("Response Status: {}, Property Data: {}",
                    response.getStatus(),
                    response.getData());

            // Check if property is valid
            boolean isValid = isPropertyValid(response);

            // Get tax paid status
            boolean taxPaid = isTaxPaid(response);

            // Build validation response
            PropertyValidationResponse validationResponse = PropertyValidationResponse.builder()
                    .property(propertyNumber)
                    .isValid(isValid)
                    .taxPaid(taxPaid)
                    .message(isValid ? "Property validation successful" : "Property validation failed")
                    .details(mapToPropertyDetails(response))
                    .build();

            log.info("Property validation completed for: {} - Valid: {}, Tax Paid: {}",
                    propertyNumber, isValid, taxPaid);

            return validationResponse;

        } catch (PropertyNotFoundException e) {
            log.error("Property not found: {}", propertyNumber);
            return PropertyValidationResponse.builder()
                    .property(propertyNumber)
                    .isValid(false)
                    .taxPaid(false)
                    .message("Property not found")
                    .build();
        } catch (PropertyServiceException e) {
            log.error("Service error during validation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during property validation", e);
            throw new PropertyServiceException("Failed to validate property: " + e.getMessage());
        }
    }

    /**
     * Checks if property is valid based on response
     */
    private boolean isPropertyValid(PropertyResponse response) {
        return response != null
                && response.getStatus() == 200
                && response.getData() != null
                && response.getData().getProperty() != null;
    }

    /**
     * Checks if tax is paid for the property
     */
    private boolean isTaxPaid(PropertyResponse response) {
        if (response == null || response.getData() == null) {
            return false;
        }
        return Boolean.TRUE.equals(response.getData().getTaxPaid());
    }

    /**
     * Maps PropertyResponse to PropertyDetails
     */
    private PropertyDetails mapToPropertyDetails(PropertyResponse response) {
        if (response == null || response.getData() == null) {
            return null;
        }

        return PropertyDetails.builder()
                .ownerName(response.getData().getOwnerName())
                .guardianName(response.getData().getGuardianName())
                .address(response.getData().getAddress())
                .phone(response.getData().getPhone())
                .ulb(response.getData().getUlb())
                .ward(response.getData().getWard())
                .buildingUse(response.getData().getBuildingUse())
                .build();
    }
}

