package org.upyog.gis.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * GIS request data model containing GIS-specific fields for zone finding operations.
 * 
 * <p>This class represents the core GIS request data that accompanies
 * polygon file uploads for zone detection. It contains identifiers
 * for tracking and tenant isolation.</p>
 * 
 * @author GIS Service Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GISRequest {

    @JsonProperty("tenantId")
    @NotNull
    private String tenantId;

    @JsonProperty("applicationNo")
    private String applicationNo;

    @JsonProperty("rtpiId")
    private String rtpiId;

    @JsonProperty("planningAreaCode")
    private String planningAreaCode;

    @JsonProperty("occupancyType")
    private String occupancyType;
}
