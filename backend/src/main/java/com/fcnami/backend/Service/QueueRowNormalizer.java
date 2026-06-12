package com.fcnami.backend.Service;

import com.fcnami.backend.Api.QueueDtos.QueueItemResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
class QueueRowNormalizer {

    List<QueueItemResponse> normalize(List<Map<String, String>> rows) {
        List<QueueItemResponse> items = new ArrayList<>();
        String currentTier = "main-road";
        int generatedOrder = 1;

        for (Map<String, String> row : rows) {
            currentTier = nextTier(currentTier, row);

            String title = firstValue(row, "song", "song title", "title", "เพลง", "ชื่อเพลง");
            if (!hasText(title)) {
                continue;
            }

            String translatedValue = firstValue(row, "translated", "done", "status", "แปลแล้ว", "แปลรึยัง?");
            Boolean translated = parseTranslated(translatedValue);

            items.add(new QueueItemResponse(
                    currentTier,
                    queueNumber(row, generatedOrder),
                    title,
                    firstValue(row, "requester", "name", "user", "ผู้รีเควส", "คนรีเควส"),
                    songLinkLabel(row, title),
                    firstValue(row, "link", "url", "song url", "youtube", "ลิงก์", "ลิงค์เพลง"),
                    normalizeStatus(translatedValue, translated),
                    translated,
                    firstValue(row, "request date", "req.date", "date", "วันที่"),
                    parseInteger(firstValue(row, "waiting days", "days", "จำนวนวัน")),
                    row
            ));
            generatedOrder++;
        }

        return items;
    }

    private String nextTier(String currentTier, Map<String, String> row) {
        String tierMarker = firstValue(row, "section", "tier", "queue", "type");
        if (QueueTierNormalizer.looksLikeTier(tierMarker)) {
            return QueueTierNormalizer.normalize(tierMarker);
        }

        String firstCell = row.values().stream().filter(this::hasText).findFirst().orElse("");
        if (QueueTierNormalizer.looksLikeTier(firstCell)) {
            return QueueTierNormalizer.normalize(firstCell);
        }

        return currentTier;
    }

    private Integer queueNumber(Map<String, String> row, int generatedOrder) {
        Integer parsed = parseInteger(firstValue(row, "no", "number", "#", "queue number", "ลำดับ", "คิว"));
        return parsed == null ? generatedOrder : parsed;
    }

    private String songLinkLabel(Map<String, String> row, String title) {
        String link = firstValue(row, "link", "url", "song url", "youtube", "ลิงก์", "ลิงค์เพลง");
        return hasText(link) ? title : null;
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

    private String normalizeHeader(String header) {
        return header == null ? "" : header.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeStatus(String raw, Boolean translated) {
        if (Boolean.TRUE.equals(translated)) {
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
            String digits = raw.replaceAll("[^0-9-]", "");
            return hasText(digits) ? Integer.parseInt(digits) : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
