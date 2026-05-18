package com.fcnami.backend.Service;

import com.fcnami.backend.Api.QueueDtos.QueueItemResponse;
import com.fcnami.backend.Api.QueueDtos.QueueResponse;
import com.fcnami.backend.Config.QueueProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GoogleSheetQueueService {
    private final QueueProperties properties;
    private final RestClient.Builder restClientBuilder;

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
        List<QueueItemResponse> items = normalize(parseCsv(csv == null ? "" : csv));
        cached = new QueueResponse(Instant.now(), items.size(), items);
        cachedAt = Instant.now();
        return cached;
    }

    private QueueResponse refreshIfNeeded() {
        long ttl = Math.max(30, properties.cacheTtlSeconds());
        if (cached == null || Duration.between(cachedAt, Instant.now()).getSeconds() > ttl) {
            return refresh();
        }
        return cached;
    }

    private List<QueueItemResponse> normalize(List<Map<String, String>> rows) {
        List<QueueItemResponse> items = new ArrayList<>();
        String currentTier = "main-road";
        int generatedOrder = 1;

        for (Map<String, String> row : rows) {
            String tierMarker = firstValue(row, "section", "tier", "queue", "type");
            if (hasText(tierMarker) && looksLikeTier(tierMarker)) {
                currentTier = normalizeTier(tierMarker);
            }

            String title = firstValue(row, "song", "song title", "title", "เพลง", "ชื่อเพลง");
            if (!hasText(title)) {
                String firstCell = row.values().stream().filter(this::hasText).findFirst().orElse("");
                if (looksLikeTier(firstCell)) {
                    currentTier = normalizeTier(firstCell);
                }
                continue;
            }

            Integer queueNumber = parseInteger(firstValue(row, "no", "number", "#", "queue number", "ลำดับ"));
            String requester = firstValue(row, "requester", "name", "user", "ผู้รีเควส", "คนรีเควส");
            String link = firstValue(row, "link", "url", "song url", "youtube", "ลิงก์");
            String translatedValue = firstValue(row, "translated", "done", "status", "แปลแล้ว");
            Boolean translated = parseTranslated(translatedValue);
            String normalizedStatus = normalizeStatus(translatedValue, translated);
            Integer waitingDays = parseInteger(firstValue(row, "waiting days", "days", "จำนวนวัน"));

            items.add(new QueueItemResponse(
                    currentTier,
                    queueNumber == null ? generatedOrder : queueNumber,
                    title,
                    requester,
                    hasText(link) ? title : null,
                    link,
                    normalizedStatus,
                    translated,
                    firstValue(row, "request date", "date", "วันที่"),
                    waitingDays,
                    row
            ));
            generatedOrder++;
        }
        return items;
    }

    private List<Map<String, String>> parseCsv(String csv) {
        List<List<String>> table = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder cell = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < csv.length(); i++) {
            char c = csv.charAt(i);
            if (quoted) {
                if (c == '"' && i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
                    cell.append('"');
                    i++;
                } else if (c == '"') {
                    quoted = false;
                } else {
                    cell.append(c);
                }
            } else if (c == '"') {
                quoted = true;
            } else if (c == ',') {
                row.add(cell.toString().trim());
                cell.setLength(0);
            } else if (c == '\n') {
                row.add(cell.toString().trim());
                cell.setLength(0);
                table.add(row);
                row = new ArrayList<>();
            } else if (c != '\r') {
                cell.append(c);
            }
        }
        row.add(cell.toString().trim());
        table.add(row);

        if (table.isEmpty()) {
            return List.of();
        }
        List<String> headers = table.getFirst().stream().map(this::normalizeHeader).toList();
        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 1; i < table.size(); i++) {
            Map<String, String> mapped = new LinkedHashMap<>();
            List<String> values = table.get(i);
            for (int j = 0; j < values.size(); j++) {
                String header = j < headers.size() && hasText(headers.get(j)) ? headers.get(j) : "column_" + (j + 1);
                mapped.put(header, values.get(j));
            }
            rows.add(mapped);
        }
        return rows;
    }

    private String normalizeHeader(String header) {
        return header == null ? "" : header.trim().toLowerCase(Locale.ROOT);
    }

    private String firstValue(Map<String, String> row, String... keys) {
        for (String key : keys) {
            String value = row.get(normalizeHeader(key));
            if (hasText(value)) {
                return value;
            }
        }
        return "";
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
        return !hasText(expected) || Objects.equals(normalizeTier(value), normalizeTier(expected));
    }

    private boolean looksLikeTier(String value) {
        if (!hasText(value)) {
            return false;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.contains("main")
                || normalized.contains("2nd")
                || normalized.contains("second")
                || normalized.contains("3rd")
                || normalized.contains("third")
                || normalized.contains("4th")
                || normalized.contains("fourth")
                || normalized.contains("reserve")
                || normalized.contains("deeper");
    }

    private String normalizeTier(String value) {
        if (!hasText(value)) {
            return "";
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        if (normalized.contains("main")) {
            return "main-road";
        }
        if (normalized.contains("2nd") || normalized.contains("second")) {
            return "second";
        }
        if (normalized.contains("3rd") || normalized.contains("third")) {
            return "third";
        }
        if (normalized.contains("4th") || normalized.contains("fourth")) {
            return "fourth";
        }
        if (normalized.contains("deeper")) {
            return "deeper";
        }
        return normalized.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private String normalizeStatus(String raw, Boolean translated) {
        if (translated != null && translated) {
            return "done";
        }
        if (!hasText(raw)) {
            return "waiting";
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        if (normalized.contains("done") || normalized.equals("true")) {
            return "done";
        }
        if (normalized.contains("progress")) {
            return "in-progress";
        }
        return "waiting";
    }

    private Boolean parseTranslated(String raw) {
        if (!hasText(raw)) {
            return null;
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        if (normalized.equals("true") || normalized.equals("done") || normalized.equals("yes")) {
            return true;
        }
        if (normalized.equals("false") || normalized.equals("no")) {
            return false;
        }
        return null;
    }

    private Integer parseInteger(String raw) {
        if (!hasText(raw)) {
            return null;
        }
        try {
            return Integer.parseInt(raw.replaceAll("[^0-9-]", ""));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
