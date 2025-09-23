package org.upyog.gis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot application class for gis service
 * Transaction management is enabled for database operations so that they can be rolled back in case of errors
 */
@SpringBootApplication
@EnableTransactionManagement
public class GisApplication {

    public static void main(String[] args) {
        SpringApplication.run(GisApplication.class, args);
    }
}
