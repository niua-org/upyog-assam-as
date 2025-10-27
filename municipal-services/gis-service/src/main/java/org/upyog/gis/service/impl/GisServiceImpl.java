package org.upyog.gis.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.upyog.gis.client.FilestoreClient;
import org.upyog.gis.client.GistcpClient;
import org.upyog.gis.config.GisProperties;
import org.upyog.gis.model.GisLog;
import org.upyog.gis.model.GISResponse;
import org.upyog.gis.model.GISRequestWrapper;
import org.upyog.gis.model.GISRequest;
import org.upyog.gis.model.GistcpResponse;
import org.egov.common.contract.response.ResponseInfo;
import org.upyog.gis.repository.GisLogRepository;
import org.upyog.gis.service.GisService;
import org.upyog.gis.util.KmlParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        String fileStoreId = null;

        try {
            validatePolygonFile(file);

            // Upload to Filestore
            log.info("Uploading KML file to Filestore: {}", file.getOriginalFilename());
            fileStoreId = filestoreClient.uploadFile(file, gisRequest.getTenantId(), "gis", "kml-upload");
            log.info("File uploaded successfully with ID: {}", fileStoreId);

            // Parse KML to get geometry
            log.info("Parsing KML file to extract geometry");
            Geometry geometry = parseKmlFile(file);
            log.info("Successfully parsed {} geometry with {} vertices", geometry.getGeometryType(), geometry.getCoordinates().length);

            // Extract centroid coordinates from geometry
            Point centroid = geometry.getCentroid();
            double latitude = centroid.getY();
            double longitude = centroid.getX();
            log.info("Extracted centroid coordinates: latitude={}, longitude={}", latitude, longitude);

            // Query GISTCP API for district/zone/landuse information
            log.info("Querying GISTCP API for location information");
            GistcpResponse gistcpResponse = gistcpClient.queryLocation(latitude, longitude, gisRequest.getTenantId());
            log.info("GISTCP query completed successfully");

            // Extract information from GISTCP response
            String district = gistcpResponse.getDistrict();
            String ward = gistcpResponse.getWardNo();
            String landuse = gistcpResponse.getLanduse();
            String village = gistcpResponse.getVillage();
            log.info("Extracted district: {}, ward: {}, landuse: {}, village: {}", district, ward, landuse, village);

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
                        ? gisRequestWrapper.getRequestInfo().getUserInfo().getUuid() : "system");
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
                        ? gisRequestWrapper.getRequestInfo().getUserInfo().getUuid() : "system");
            logRepository.save(failureLog);

            throw new RuntimeException("Failed to process geometry file: " + e.getMessage(), e);
        }
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
                               String status, String responseStatus, String responseJson, JsonNode details, String createdBy) {
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
                .build();
    }
}