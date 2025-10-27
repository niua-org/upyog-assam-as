package org.upyog.gis.client.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import org.upyog.gis.client.GistcpClient;
import org.upyog.gis.config.GisProperties;
import org.upyog.gis.model.GistcpResponse;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;

/**
 * Implementation of GISTCP API client.
 * 
 * <p>This client queries the Assam GISTCP API to retrieve location information
 * based on coordinates and masterplan. It includes retry logic and error handling.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GistcpClientImpl implements GistcpClient {

    private final WebClient webClient;
    private final GisProperties gisProperties;
    private final ObjectMapper objectMapper;

    @Override
    public GistcpResponse queryLocation(double latitude, double longitude, String masterplan) throws Exception {
        log.info("Querying GISTCP API for location: lat={}, lon={}, masterplan={}", latitude, longitude, masterplan);
        
        String apiUrl = gisProperties.getGistcpApiUrl();
        
        // Build the request URL with query parameters
        URI uri = UriComponentsBuilder.fromHttpUrl(apiUrl)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("masterplan", masterplan.toLowerCase())
                .build()
                .toUri();
        
        log.debug("GISTCP API URL: {}", uri);
        
        try {
            // Execute the GET request - treat response as String first, then parse as JSON
            // This handles the case where GISTCP API returns 'application/octet-stream' instead of 'application/json'
            String responseBody = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .retryWhen(Retry.fixedDelay(gisProperties.getMaxRetries(), Duration.ofSeconds(2))
                            .filter(throwable -> !(throwable instanceof WebClientResponseException.BadRequest))
                            .doBeforeRetry(retrySignal -> 
                                log.warn("Retrying GISTCP API call, attempt: {}", retrySignal.totalRetries() + 1)))
                    .block();
            
            if (responseBody == null || responseBody.trim().isEmpty()) {
                log.error("GISTCP API returned empty response body");
                throw new RuntimeException("GISTCP API returned empty response");
            }
            
            log.debug("GISTCP API raw response: {}", responseBody);
            
            // Parse the string response as JSON
            JsonNode responseJson = objectMapper.readTree(responseBody);
            
            log.debug("GISTCP API parsed response: {}", responseJson);
            
            // The response is a map with a single key (e.g., "FO-1") containing the data
            // Extract the first (and only) entry from the map
            GistcpResponse gistcpResponse = extractResponseData(responseJson);
            
            log.info("Successfully retrieved GISTCP data: district={}, ward={}, landuse={}", 
                    gistcpResponse.getDistrict(), gistcpResponse.getWardNo(), gistcpResponse.getLanduse());
            
            return gistcpResponse;
            
        } catch (WebClientResponseException e) {
            log.error("GISTCP API error: {} {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("GISTCP API request failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to query GISTCP API", e);
            throw new RuntimeException("Failed to query GISTCP API: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extracts the response data from the JSON node.
     * The response structure is: { "FO-1": { ... actual data ... } }
     * We need to extract the nested object.
     */
    private GistcpResponse extractResponseData(JsonNode responseJson) {
        // Get the first (and only) field in the response
        Iterator<Map.Entry<String, JsonNode>> fields = responseJson.fields();
        
        if (!fields.hasNext()) {
            log.error("GISTCP response has no fields");
            throw new RuntimeException("GISTCP response has no data");
        }
        
        Map.Entry<String, JsonNode> entry = fields.next();
        String key = entry.getKey();
        JsonNode dataNode = entry.getValue();
        
        log.debug("Extracting GISTCP data from key: {}", key);
        
        // Convert the data node to GistcpResponse object
        return objectMapper.convertValue(dataNode, GistcpResponse.class);
    }
}

