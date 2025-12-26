package org.upyog.gis.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.tracer.model.ServiceCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.upyog.gis.config.GisProperties;
import java.util.Map;

@Service
public class ServiceRequestRepository {
    private ObjectMapper mapper;

    private RestTemplate restTemplate;

    private static final Logger log = LogManager.getLogger(ServiceRequestRepository.class);
    @Autowired
    private GisProperties config;

    @Autowired
    public ServiceRequestRepository(ObjectMapper mapper, RestTemplate restTemplate) {
        this.mapper = mapper;
        this.restTemplate = restTemplate;
    }

    /**
     * Fetches result from an external service with retry mechanism for transient failures.
     *
     * @param uriStringBuilder The URI to call.
     * @param request          The request payload.
     * @return The response from the external service.
     * @throws ServiceCallException if a non-retryable error occurs or all retries are exhausted.
     */
    public Object fetchResult(StringBuilder uriStringBuilder, Object request) {
        String uri = uriStringBuilder.toString();
            log.info("URI: {}", uri);
            Object response = null;
            try {
                response = restTemplate.postForObject(uri, request, Map.class);
            }
            catch (HttpClientErrorException e) {
                // Do NOT retry — 4xx
                log.error("External Service threw a 4xx error: ", e);
                throw new ServiceCallException(e.getResponseBodyAsString());
            }
            catch (HttpServerErrorException e) {
                // 5xx → retry allowed
                log.error("External Service threw a 5xx error, retrying: ", e);
                throw e;
            }
            catch (ResourceAccessException e) {
                // ConnectException / SocketTimeoutException wrapped
                log.error("Connection related exception, retrying: ", e);
                throw e;
            }
            catch (Exception e) {
                log.error("Unknown exception: ", e);
                throw new ServiceCallException(e.getMessage());
            }

            return response;

    }

}
