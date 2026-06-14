package com.fcnami.backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration class for HTTP client beans.
 */
@Configuration
public class HttpClientConfig {
    
    /**
     * Precondition: Spring application context is initializing.
     * Postcondition: Returns a configured RestClient.Builder instance.
     * Side-effect: Registers the RestClient.Builder bean in the application context.
     */
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
