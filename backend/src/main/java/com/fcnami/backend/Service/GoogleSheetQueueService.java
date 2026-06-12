package com.fcnami.backend.Service;

import com.fcnami.backend.Api.QueueDtos.QueueItemResponse;
import com.fcnami.backend.Api.QueueDtos.QueueResponse;
import com.fcnami.backend.Config.QueueProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GoogleSheetQueueService {
    private final QueueProperties properties;
    private final RestClient.Builder restClientBuilder;
    private final QueueCsvParser csvParser;
    private final QueueRowNormalizer rowNormalizer;

    private QueueResponse cached;
    private Instant cachedAt = Instant.EPOCH;

    public synchronized QueueResponse getQueue(String search, String status, String tier) {
        QueueResponse snapshot = refreshIfNeeded();
        List<QueueItemResponse> filtered = snapshot.items().stream()
                .filter(item -> matchesSearch(item, search))
                .filter(item -> matches(item.status(), status))
                .filter(item -> matches(item.tier(), tier))
                .toList();
        return new QueueResponse(snapshot.syncedAt(), filtered.size(), filtered);
    }

    public synchronized QueueResponse refresh() {
        String csv = restClientBuilder.build()
                .get()
                .uri(properties.sheetCsvUrl())
                .retrieve()
                .body(String.class);
        List<QueueItemResponse> items = rowNormalizer.normalize(csvParser.parse(csv));
        cached = new QueueResponse(Instant.now(), items.size(), items);
        cachedAt = Instant.now();
        return cached;
    }

    private QueueResponse refreshIfNeeded() {
        if (cached == null || cacheExpired()) {
            return refresh();
        }
        return cached;
    }

    private boolean cacheExpired() {
        return Duration.between(cachedAt, Instant.now()).getSeconds() > properties.cacheTtlSeconds();
    }

    private boolean matchesSearch(QueueItemResponse item, String search) {
        if (!hasText(search)) {
            return true;
        }
        String needle = search.toLowerCase(Locale.ROOT);
        return contains(item.songTitle(), needle)
                || contains(item.requester(), needle)
                || contains(item.status(), needle)
                || contains(item.tier(), needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private boolean matches(String value, String expected) {
        return !hasText(expected) || Objects.equals(QueueTierNormalizer.normalize(value), QueueTierNormalizer.normalize(expected));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
