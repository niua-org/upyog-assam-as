package org.egov.bpa.service.property;


import org.egov.bpa.config.FeignConfig;
import org.egov.bpa.web.model.property.SumatoPropertyRequest;
import org.egov.bpa.web.model.property.PropertyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "property-service",
        url = "${sumato.property.service.url}",
        configuration = FeignConfig.class
)
public interface PropertyServiceClient {

    @PostMapping("/obps/holding/fetch")
    PropertyResponse fetchPropertyDetails(@RequestBody SumatoPropertyRequest request);
}

