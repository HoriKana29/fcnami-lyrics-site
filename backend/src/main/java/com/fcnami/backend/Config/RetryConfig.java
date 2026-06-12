package com.fcnami.backend.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuration class to enable and configure spring-retry mechanisms.
 */
@Configuration
@EnableRetry
public class RetryConfig { 
}
