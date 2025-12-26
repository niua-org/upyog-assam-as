package org.upyog.gis.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

/**
 * Response model for GIS zone finding operations following eGov standards.
 * 
 * <p>This response contains the results of polygon processing including
 * extracted district and zone information, cleaned WFS response data,
 * and file storage references. It follows eGov municipal services
 * response structure with ResponseInfo metadata.</p>
 * 
 * <p>Response structure includes:</p>
 * <ul>
 *   <li>Standard eGov ResponseInfo with API metadata</li>
 *   <li>Extracted district and zone information</li>
 *   <li>Cleaned WFS response (essential fields only)</li>
 *   <li>File storage reference for uploaded KML</li>
 *   <li>Error information if processing fails</li>
 * </ul>
 * 
 * @author GIS Service Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GISResponse {

    @JsonProperty("ResponseInfo")
    private ResponseInfo responseInfo;

    @JsonProperty("district")
    private String district;

    @JsonProperty("zone")
    private String zone;

    @JsonProperty("wfsResponse")
    private Object wfsResponse;

    @JsonProperty("fileStoreId")
    private String fileStoreId;

    @JsonProperty("error")
    private String error;

    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("planningAreaCode")
    private String planningAreaCode;

    @JsonProperty("remarks")
    private String remarks;

    @JsonProperty("validationStatus")
    private String validationStatus;
}
