package org.upyog.gis.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for GIS service including GISTCP API, WFS, and Filestore settings.
 */
@Data
@Component
@ConfigurationProperties(prefix = "gis")
public class GisProperties {

    // GISTCP API configuration
    private String gistcpApiUrl;
    private String districtAttribute = "district";
    private String landuseAttribute = "landuse";
    private String wardAttribute = "ward_no";
    private String villageAttribute = "village";
    private String masterplanAttribute = "masterplan";
    
    // WFS configuration (kept for future use)
    private String wfsUrl;
    private String wfsTypeName = "topp:states";
    private String wfsGeometryColumn = "the_geom";
    private String wfsDistrictAttribute = "STATE_NAME";
    private String wfsZoneAttribute = "STATE_ABBR";
    
    // HTTP timeout configuration
    private int connectionTimeoutSeconds = 10;
    private int readTimeoutSeconds = 30;
    private int maxRetries = 2;
    
    // Filestore configuration from different property sources
    @Value("${egov.filestore.host}")
    private String filestoreHost;
    
    private String filestoreEndpoint;
    
    /**
     * Gets the complete filestore URL by combining host and endpoint
     * @return complete filestore URL
     */
    public String getFilestoreUrl() {
        return filestoreHost + filestoreEndpoint;
    }
}
