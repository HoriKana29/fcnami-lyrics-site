package com.fcnami.backend.Service;

import java.util.Locale;

final class QueueTierNormalizer {
    private QueueTierNormalizer() {
    }

    static boolean looksLikeTier(String value) {
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

    static String normalize(String value) {
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

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
