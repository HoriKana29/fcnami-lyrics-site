package com.fcnami.backend.Support;

import java.text.Normalizer;
import java.util.Locale;

// กัน slug+key ซ้ำทั้งระบบ
public final class SlugUtil {
    private SlugUtil() {
    }

    public static String slugify(String value) {
        if (value == null || value.isBlank()) {
            return "untitled";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        return normalized.isBlank() ? "untitled" : normalized;
    }

    public static String normalizedKey(String title, String artist) {
        return slugify((title == null ? "" : title) + "-" + (artist == null ? "" : artist));
    }
    // ภาษาไทย / ญี่ปุ่น → จะหายหมด ซึ่งไม่น่ามีปัญหาสำหรับ slug
}
