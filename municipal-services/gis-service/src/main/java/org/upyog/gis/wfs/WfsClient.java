package org.upyog.gis.wfs;

import org.upyog.gis.model.WfsResponse;

/**
 * Client interface for WFS (Web Feature Service) operations.
 * 
 * <p>This interface provides methods for querying WFS endpoints using spatial
 * intersection queries and returns typed response objects for better type safety.</p>
 * 
 * @author GIS Service Team
 * @version 1.0
 * @since 1.0
 */
public interface WfsClient {

    /**
     * Query WFS service for features that intersect with the given polygon geometry.
     *
     * <p>Performs spatial intersection queries using CQL filters and returns
     * a typed WfsResponse object containing matching features with their properties.</p>
     *
     * @param polygonWkt the polygon geometry in Well-Known Text (WKT) format
     * @return WfsResponse containing matching features and metadata
     * @throws Exception if WFS query fails or response cannot be parsed
     */
    WfsResponse queryFeatures(String polygonWkt) throws Exception;
}
