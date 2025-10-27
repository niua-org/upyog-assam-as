package org.upyog.gis;

import org.egov.tracer.config.TracerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * Main Spring Boot application class for GIS service.
 * 
 * <p>This service handles GIS operations including KML file processing,
 * GISTCP API integration, WFS queries, and Kafka-based logging.</p>
 */
@Import({ TracerConfiguration.class })
@SpringBootApplication
public class GisApplication {

    public static void main(String[] args) {
        SpringApplication.run(GisApplication.class, args);
    }
}
