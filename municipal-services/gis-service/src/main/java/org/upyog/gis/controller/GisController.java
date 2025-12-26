package org.upyog.gis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.upyog.gis.model.*;
import org.upyog.gis.service.GisService;
import org.upyog.gis.util.ResponseInfoFactory;

import javax.validation.Valid;


/**
 * REST controller for GIS operations
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(value = "GIS Controller", description = "Operations for GIS processing")
public class GisController {

    private final GisService gisService;
    private final ResponseInfoFactory responseInfoFactory;

    /**
     * Find zone information from polygon file
     *
     * @param file the polygon file to process
     * @param gisRequestWrapper the GIS request containing tenant id, application number, RTPI ID and RequestInfo
     * @return response containing district, zone, and WFS response
     */
    @ApiOperation(value = "Find zone from polygon file", notes = "Uploads and processes a polygon KML file to find zone information")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    @PostMapping(value = "/find-zone", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GISResponse> findZone(
            @ApiParam(value = "Polygon KML file", required = true)
            @RequestPart("file") MultipartFile file,
            @ApiParam(value = "GIS Request with RequestInfo", required = true)
            @RequestPart(value = "gisRequestWrapper") GISRequestWrapper gisRequestWrapper
    ) {

        try {
            log.info("Finding zone from polygon file: {} (tenant: {}, applicationNo: {}, rtpiId: {}, planningAreaCode: {}, occupancyType:{})",
                    file.getOriginalFilename(),
                    gisRequestWrapper.getGisRequest().getTenantId(),
                    gisRequestWrapper.getGisRequest().getApplicationNo(),
                    gisRequestWrapper.getGisRequest().getRtpiId(),
                    gisRequestWrapper.getGisRequest().getPlanningAreaCode(),
                    gisRequestWrapper.getGisRequest().getOccupancyType());

            GISResponse response = gisService.findZoneFromGeometry(file, gisRequestWrapper);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            GISResponse errorResponse = GISResponse.builder()
                    .error(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("Error finding zone from polygon file", e);
            GISResponse errorResponse = GISResponse.builder()
                    .error("Internal server error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Search GIS logs based on criteria.
     * 
     * <p>Searches GIS processing logs with filters like tenantId, applicationNo, rtpId, and status.
     *
     * @param searchRequest the search request containing RequestInfo and GisSearchCriteria
     * @return response containing list of matching GIS log records
     */
    @ApiOperation(value = "Search GIS logs", notes = "Search GIS processing logs based on criteria")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    @PostMapping("/zone/_search")
    public ResponseEntity<GisLogSearchResponse> searchGisLogs(
            @ApiParam(value = "Search criteria", required = true)
            @RequestBody @Valid GisLogSearchRequest searchRequest
    ) {
        try {
            log.info("Searching GIS logs with criteria: {}", searchRequest.getCriteria());

            List<GisLog> gisLogs = gisService.searchGisLog(searchRequest.getCriteria());

            ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(
                    searchRequest.getRequestInfo(), true);

            GisLogSearchResponse response = GisLogSearchResponse.builder()
                    .responseInfo(responseInfo)
                    .gis(gisLogs)
                    .build();

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid search request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error searching GIS logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
