package com.fcnami.backend.Config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "fcnami.queue")
public record QueueProperties(
        @NotBlank String sheetCsvUrl,
        @Min(30) long cacheTtlSeconds
) {
}
