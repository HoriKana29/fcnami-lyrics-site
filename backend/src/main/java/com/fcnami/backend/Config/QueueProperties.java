package com.fcnami.backend.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// ให้เช็คว่าได้ enable รึยัง และ ยังขาด validation
@ConfigurationProperties(prefix = "fcnami.queue")
public record QueueProperties(String sheetCsvUrl, long cacheTtlSeconds) {
}
