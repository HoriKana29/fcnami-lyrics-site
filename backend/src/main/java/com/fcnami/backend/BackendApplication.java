package com.fcnami.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Main entry point for the Spring Boot backend application.
 * This class starts and configures the application context.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class BackendApplication {

    /**
     * Precondition: Command line arguments must be supplied (can be empty).
     * Postcondition: The Spring Boot application is initialized and running.
     * Side-effect: Starts the embedded web server and registers beans in the Spring context.
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

}
