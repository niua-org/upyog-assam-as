package org.upyog.gis.client;

import org.upyog.gis.model.GistcpResponse;

/**
 * Client interface for querying the GISTCP API.
 * 
 * <p>The GISTCP API provides geospatial information for locations in Assam
 * based on latitude, longitude, and masterplan (tenant ID).</p>
 */
public interface GistcpClient {
    
    /**
     * Queries the GISTCP API for location information.
     * 
     * @param latitude the latitude coordinate
     * @param longitude the longitude coordinate
     * @param masterplan the masterplan/tenant ID (e.g., "tinsukia")
     * @return GISTCP response containing district, ward, landuse, etc.
     * @throws Exception if the API call fails
     */
    GistcpResponse queryLocation(double latitude, double longitude, String masterplan) throws Exception;
}

