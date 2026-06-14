package com.fcnami.backend.Support;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Utility class for generating URL-friendly slugs and normalized keys.
 */
public final class SlugUtil {
    
    /**
     * Precondition: None.
     * Postcondition: An instance of SlugUtil cannot be instantiated.
     * Side-effect: Throws an AssertionError if invoked via reflection.
     */
    private SlugUtil() {
        throw new AssertionError("No SlugUtil instances for you!");
    }

    /**
     * Precondition: A value string to slugify is provided.
     * Postcondition: Returns a sanitized, lowercase, hyphen-separated slug string.
     * Side-effect: None.
     */
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

    /**
     * Precondition: Title and artist strings are provided.
     * Postcondition: Returns a normalized, combined slug representing the song key.
     * Side-effect: None.
     */
    public static String normalizedKey(String title, String artist) {
        return slugify((title == null ? "" : title) + "-" + (artist == null ? "" : artist));
    }
}
