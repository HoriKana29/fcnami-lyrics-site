package com.fcnami.backend.Config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the queue system, binding fields with prefix 'fcnami.queue'.
 */
@Validated
@ConfigurationProperties(prefix = "fcnami.queue")
public record QueueProperties(
        @NotBlank String sheetCsvUrl,
        @Min(QueueProperties.MIN_CACHE_TTL) long cacheTtlSeconds
) {
    public static final long MIN_CACHE_TTL = 30L;
}
