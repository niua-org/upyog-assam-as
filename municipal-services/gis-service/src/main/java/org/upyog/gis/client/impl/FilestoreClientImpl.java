package org.upyog.gis.client.impl;

import org.upyog.gis.client.FilestoreClient;
import org.upyog.gis.config.GisProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;

/**
 * REST-based implementation of FilestoreClient
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FilestoreClientImpl implements FilestoreClient {

    private final RestTemplate restTemplate;
    private final GisProperties gisProperties;
    private final ObjectMapper objectMapper;

    /**
     * Uploads a file to the filestore service using REST API.
     *
     * @param file the file to upload
     * @param tenantId the tenant ID
     * @param module the module name
     * @param tag optional tag for the file
     * @return filestoreId of the uploaded file
     * @throws Exception if upload fails or response is invalid
     */
    @Override
    public String uploadFile(MultipartFile file, String tenantId, String module, String tag) throws Exception {
        log.info("Uploading file to filestore: {} (tenant: {}, module: {}, tag: {})", 
                file.getOriginalFilename(), tenantId, module, tag);

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(gisProperties.getFilestoreUrl());
        if (tenantId != null) {
            uriBuilder.queryParam("tenantId", tenantId);
        }
        URI uploadUri = uriBuilder.build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());
        body.add("module", module);
        if (tag != null) {
            body.add("tag", tag);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            log.info("Calling filestore upload URL: {}", uploadUri);
            
            // Get the response as String to parse it manually
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    uploadUri, HttpMethod.POST, requestEntity, String.class);
            
            log.info("Filestore response status: {}", rawResponse.getStatusCode());
            log.info("Filestore response body: {}", rawResponse.getBody());
            
            String responseBody = rawResponse.getBody();
            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new RuntimeException("Empty response from filestore");
            }
            
            // Parse the JSON response
            JsonNode responseJson = objectMapper.readTree(responseBody);
            String fileStoreId = extractFileStoreId(responseJson);
            
            if (fileStoreId == null || fileStoreId.trim().isEmpty()) {
                log.error("Could not extract fileStoreId from response: {}", responseBody);
                throw new RuntimeException("Invalid filestore response: missing fileStoreId");
            }


            log.info("File uploaded successfully. FileStoreId: {}", fileStoreId);
            return fileStoreId;

        } catch (Exception e) {
            log.error("Failed to upload file to filestore", e);
            throw new RuntimeException("Filestore upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts fileStoreId from response returned by the filestore service.
     *
     * @param responseJson the JSON response from the filestore service
     * @return fileStoreId if found, otherwise null
     */
    private String extractFileStoreId(JsonNode responseJson) {

        if (responseJson.has("files") && responseJson.get("files").isArray()) {
            JsonNode files = responseJson.get("files");
            if (!files.isEmpty()) {
                JsonNode firstFile = files.get(0);
                if (firstFile.has("fileStoreId")) {
                    return firstFile.get("fileStoreId").asText();
                }
            }
        }
        log.warn("Could not find fileStoreId in any expected location in response: {}", responseJson.toString());
        return null;
    }

}
