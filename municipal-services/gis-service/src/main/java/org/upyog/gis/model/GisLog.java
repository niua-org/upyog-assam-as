package org.upyog.gis.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Model for GIS processing operations logging via Kafka.
 * 
 * <p>This class represents the structure of GIS log data that will be sent to Kafka
 * for processing by other services. It contains all the necessary information about
 * GIS operations including file processing, WFS queries, and results.</p>
 * 
 * @author GIS Service Team
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GisLog {

    /**
     * Unique identifier for the log entry
     */
    private String id;

    private String applicationNo;

    private String rtpiId;

    private String fileStoreId;

    private String tenantId;

    private String status;

    private String responseStatus;

    private String responseJson;

    private String createdby;

    @Builder.Default
    private Long createdtime = Instant.now().toEpochMilli();

    private String lastmodifiedby;

    private Long lastmodifiedtime;

    private JsonNode details;

    private double latitude;

    private double longitude;
}
