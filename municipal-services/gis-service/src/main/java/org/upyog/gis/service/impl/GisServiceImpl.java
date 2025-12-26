package org.upyog.gis.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.upyog.gis.client.FilestoreClient;
import org.upyog.gis.client.GistcpClient;
import org.upyog.gis.config.GisProperties;
import org.upyog.gis.config.ServiceConstants;
import org.upyog.gis.model.*;
import org.egov.common.contract.response.ResponseInfo;
import org.upyog.gis.repository.GisLogRepository;
import org.upyog.gis.service.GisService;
import org.upyog.gis.util.GisUtils;
import org.upyog.gis.util.KmlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.upyog.gis.model.GisLogSearchCriteria;

import java.io.InputStream;
import java.time.Instant;
import java.util.UUID;

/**
 * Service implementation for GIS operations including KML parsing, GISTCP API integration, and logging.
 * Handles KML uploads, centroid extraction, GISTCP queries, and response formatting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GisServiceImpl implements GisService {

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L; // 10 MB

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILURE = "FAILURE";

    private final GistcpClient gistcpClient;
    private final GisLogRepository logRepository;
    private final GisProperties gisProperties;
    private final ObjectMapper objectMapper;
    private final FilestoreClient filestoreClient;

    @Autowired
    private GisUtils gisUtils;

    /**
     * Determines permission status by matching land use type with occupancy type from MDMS data.
     * Checks if the given occupancy type is allowed for the detected land use (RESIDENTIAL/COMMERCIAL).
     *
     * @param landUse the land use type detected from GISTCP API
     * @param occupancyType the occupancy type from the request
     * @param mdmsData the MDMS master data containing permissible zones
     * @return "YES" if allowed, "NO" if not allowed, "Land Type is Not Matching" if no match found
     */
    private String determinePermissionStatus(String landUse, String occupancyType, Object mdmsData) {
        try {
            log.info("Validation Status - landUse: {}, occupancyType: {}", landUse, occupancyType);
            JsonNode mdmsNode = objectMapper.valueToTree(mdmsData);
            JsonNode permissibleZoneArray = mdmsNode.at("/MdmsRes/BPA/PermissibleZone");

            for (JsonNode zone : permissibleZoneArray) {
                if (occupancyType.equals(zone.path("code").asText())) {
                    log.info("Found matching occupancy type: {}", occupancyType);
                    JsonNode typeOfLand = zone.path("typeOfLand").get(0);
                    String normalizedLandUse = landUse.toUpperCase().replace(" ", "_");
                    log.info("Normalized land use: {}", normalizedLandUse);

                    if (normalizedLandUse.contains("RESIDENTIAL") && typeOfLand.has("RESIDENTIAL")) {
                        String result = typeOfLand.path("RESIDENTIAL").asText();
                        log.info("Result for residential: {}", result);
                        return result;
                    }
                    if (normalizedLandUse.contains("COMMERCIAL") && typeOfLand.has("COMMERCIAL")) {
                        String result = typeOfLand.path("COMMERCIAL").asText();
                        log.info("Result for commercial: {}", result);
                        return result;
                    }
                }
            }
            log.info("No matching land type found, returning: Land Type is Not Matching");
            return "Land Type is Not Matching";
        } catch (Exception e) {
            log.error("Error in determinePermissionStatus: {}", e.getMessage());
            return "Land Type is Not Matching";
        }
    }



    /**
     * Finds zone information from a geometry file (KML/XML), uploads it to filestore, parses the geometry,
     * extracts centroid coordinates, queries GISTCP API, logs the operation, and returns a structured response.
     * Supports polygon, line, and point geometries.
     *
     * @param file the uploaded geometry file (KML/XML)
     * @param gisRequestWrapper the GIS request wrapper containing RequestInfo and GIS request data
     * @return structured response containing district, zone, landuse, and GISTCP data
     * @throws Exception if any processing step fails
     */
    @Override
    public GISResponse findZoneFromGeometry(MultipartFile file, GISRequestWrapper gisRequestWrapper) throws Exception {
        GISRequest gisRequest = gisRequestWrapper.getGisRequest();
        gisRequest.setTenantId(gisRequest.getTenantId());
        String stateId = gisUtils.extractState(gisRequestWrapper.getGisRequest().getTenantId()); // will extract only part before "." (stateID)
        Object mdmsData = gisUtils.mDMSCall(gisRequestWrapper.getRequestInfo(), stateId);
        log.info("MDMS data: {}", mdmsData);
        // Ensure planningAreaCode is set from tenantId if not provided
        String planningAreaCode = extractUlbName(gisRequest.getPlanningAreaCode());
        gisRequest.setPlanningAreaCode(planningAreaCode);
        
        log.info("Processing request - tenantId: {}, planningAreaCode: {}", gisRequest.getTenantId(), planningAreaCode);

        String fileStoreId = null;
        double latitude = 0.0;
        double longitude = 0.0;

        try {
            validatePolygonFile(file);

            // Upload to Filestore
            log.info("Uploading KML file to Filestore: {}", file.getOriginalFilename());
            fileStoreId = filestoreClient.uploadFile(file, gisRequest.getPlanningAreaCode(), "gis", "kml-upload");
            log.info("File uploaded successfully with ID: {}", fileStoreId);

            // Parse KML to get geometry
            log.info("Parsing KML file to extract geometry");
            Geometry geometry = parseKmlFile(file);
            log.info("Successfully parsed {} geometry with {} vertices", geometry.getGeometryType(), geometry.getCoordinates().length);

            // Extract centroid coordinates from geometry
            Point centroid = geometry.getCentroid();
             latitude = centroid.getY();
             longitude = centroid.getX();
            log.info("Extracted centroid coordinates: latitude={}, longitude={}", latitude, longitude);

            // Query GISTCP API for district/zone/landuse information
            log.info("Querying GISTCP API for location information");
            GistcpResponse gistcpResponse = gistcpClient.queryLocation(latitude, longitude, gisRequest.getPlanningAreaCode());
            log.info("GISTCP query completed successfully");

            // Extract information from GISTCP response
            String district = gistcpResponse.getDistrict();
            String ward = gistcpResponse.getWardNo();
            String landuse = gistcpResponse.getLanduse();
            String village = gistcpResponse.getVillage();
            log.info("Extracted district: {}, ward: {}, landuse: {}, village: {}", district, ward, landuse, village);
            String remarks = landuse;
            String validationResult = determinePermissionStatus(landuse, gisRequest.getOccupancyType(), mdmsData);
            String validationStatus = "YES".equals(validationResult) ? "ACCEPTED" : "NO".equals(validationResult) ? "REJECTED" : "LAND TYPE NOT MATCHED";
            log.info("ValidationResult: {}, ValidationStatus: {}", validationResult, validationStatus);



            // Create details for logging
            ObjectNode detailsJson = objectMapper.createObjectNode();
            detailsJson.put("fileName", file.getOriginalFilename());
            detailsJson.put("fileSize", file.getSize());
            detailsJson.put("district", district);
            detailsJson.put("ward", ward);
            detailsJson.put("landuse", landuse);
            detailsJson.put("village", village);
            detailsJson.put("geometryType", geometry.getGeometryType());
            detailsJson.put("geometryVertices", geometry.getCoordinates().length);
            detailsJson.put("centroidLatitude", latitude);
            detailsJson.put("centroidLongitude", longitude);

            // Send success log to Kafka via persister
            GisLog successLog = createGisLog(gisRequest.getApplicationNo(), gisRequest.getRtpiId(), fileStoreId,
                    gisRequest.getTenantId(), STATUS_SUCCESS, "SUCCESS", "Successfully processed geometry and retrieved location data from GISTCP", detailsJson,
                    gisRequestWrapper.getRequestInfo() != null && gisRequestWrapper.getRequestInfo().getUserInfo() != null
                        ? gisRequestWrapper.getRequestInfo().getUserInfo().getUuid() : "system", latitude, longitude, gisRequest.getPlanningAreaCode());
            logRepository.save(successLog);

            // Convert GISTCP response to JSON for the GIS response
            ObjectNode gistcpJson = objectMapper.valueToTree(gistcpResponse);

            // Create ResponseInfo
            ResponseInfo responseInfo = ResponseInfo.builder()
                    .apiId(gisRequestWrapper.getRequestInfo() != null ? gisRequestWrapper.getRequestInfo().getApiId() : null)
                    .ver(gisRequestWrapper.getRequestInfo() != null ? gisRequestWrapper.getRequestInfo().getVer() : null)
                    .ts(Instant.now().toEpochMilli())
                    .resMsgId("uief87324")
                    .msgId(gisRequestWrapper.getRequestInfo() != null ? gisRequestWrapper.getRequestInfo().getMsgId() : null)
                    .status("SUCCESSFUL")
                    .build();

            // Return successful response
            return GISResponse.builder()
                    .responseInfo(responseInfo)
                    .district(district)
                    .zone(ward) // Using ward as zone
                    .wfsResponse(gistcpJson) // GISTCP response replaces WFS response
                    .fileStoreId(fileStoreId)
                    .latitude(latitude)
                    .longitude(longitude)
                    .planningAreaCode(gisRequest.getPlanningAreaCode())
                    .remarks(remarks)
                    .validationStatus(validationStatus)
                    .build();

        } catch (Exception e) {
            log.error("Error finding zone from geometry file: {}", e.getMessage(), e);

            // Send failure log to Kafka via persister
            ObjectNode errorDetails = objectMapper.createObjectNode();
            errorDetails.put("fileName", file.getOriginalFilename());
            errorDetails.put("error", e.getMessage());
            if (fileStoreId != null) {
                errorDetails.put("fileStoreId", fileStoreId);
            }
            
            GisLog failureLog = createGisLog(gisRequest.getApplicationNo(), gisRequest.getRtpiId(), fileStoreId,
                    gisRequest.getTenantId(), STATUS_FAILURE, "FAILURE", e.getMessage(), errorDetails,
                    gisRequestWrapper.getRequestInfo() != null && gisRequestWrapper.getRequestInfo().getUserInfo() != null
                        ? gisRequestWrapper.getRequestInfo().getUserInfo().getUuid() : "system", latitude, longitude, gisRequest.getPlanningAreaCode());
            logRepository.save(failureLog);

            throw new RuntimeException("Failed to process geometry file: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts ULB name from tenantId by removing state prefix.
     * Example: "as.tinsukia" -> "tinsukia", "as.ghoungoorgp" -> "ghoungoorgp"
     */
    private String extractUlbName(String tenantId) {
        if (tenantId != null && tenantId.contains(".")) {
            String[] parts = tenantId.split("\\.");
            return parts[parts.length - 1];
        }
        return tenantId;
    }

    /**
     * Validates the uploaded polygon file
     */
    private void validatePolygonFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 10MB");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".kml") && !fileName.toLowerCase().endsWith(".xml"))) {
            throw new IllegalArgumentException("File must be a KML or XML file");
        }
    }

    /**
     * Parses KML file to extract geometry (polygon, line, or point)
     */
    private Geometry parseKmlFile(MultipartFile file) throws Exception {
        try (InputStream inputStream = file.getInputStream()) {
            return KmlParser.parseGeometry(inputStream);
        } catch (Exception e) {
            log.error("Failed to parse KML file: {}", e.getMessage());
            throw new Exception("Invalid KML file format: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a GIS log entry with a unique ID for Kafka publishing.
     *
     * @param applicationNo the application number
     * @param rtpiId the RTPI ID
     * @param fileStoreId the file store ID
     * @param tenantId the tenant ID
     * @param status the status
     * @param responseStatus the response status
     * @param responseJson the response JSON
     * @param details the details as JsonNode
     * @param createdBy the user who created the log
     * @return GisLog object ready for Kafka publishing
     */
    private GisLog createGisLog(String applicationNo, String rtpiId, String fileStoreId, String tenantId, 
                               String status, String responseStatus, String responseJson, JsonNode details, String createdBy, double latitude, double longitude, String planningAreaCode) {
        return GisLog.builder()
                .id(UUID.randomUUID().toString()) // Generate unique ID for Kafka
                .applicationNo(applicationNo)
                .rtpiId(rtpiId)
                .fileStoreId(fileStoreId)
                .tenantId(tenantId)
                .status(status)
                .responseStatus(responseStatus)
                .responseJson(responseJson)
                .createdby(createdBy)
                .createdtime(Instant.now().toEpochMilli())
                .details(details)
                .latitude(latitude)
                .longitude(longitude)
                .planningAreaCode(planningAreaCode)
                .build();
    }


    /**
     * Searches GIS logs based on provided search criteria.
     * @return list of GisLog objects matching the search criteria
     */
    @Override
    public List<GisLog> searchGisLog(GisLogSearchCriteria criteria) {
        log.info("Searching GIS logs with criteria: {}", criteria);
        return logRepository.search(criteria);
    }


}