package com.fcnami.backend.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {
    // Spring เอาไปเก็บใน container และ inject ได้
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
