package org.upyog.gis.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing the GISTCP API response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GistcpResponse {

    @JsonProperty("landuse")
    private String landuse;
    
    @JsonProperty("masterplan")
    private String masterplan;
    
    @JsonProperty("district")
    private String district;
    
    @JsonProperty("daag_no")
    private String daagNo;
    
    @JsonProperty("area_sqkm")
    private String areaSqkm;
    
    @JsonProperty("area_ha")
    private String areaHa;
    
    @JsonProperty("ward_no")
    private String wardNo;
    
    @JsonProperty("village")
    private String village;
    
    @JsonProperty("qmasterplan")
    private String qMasterplan;
}

