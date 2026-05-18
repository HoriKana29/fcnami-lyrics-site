package com.fcnami.backend.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fcnami.queue")
public record QueueProperties(String sheetCsvUrl, long cacheTtlSeconds) {
}
