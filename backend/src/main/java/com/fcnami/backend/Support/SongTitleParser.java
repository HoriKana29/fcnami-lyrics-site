package com.fcnami.backend.Support;

import lombok.Data;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing YouTube video titles into structured song data.
 */
public class SongTitleParser {

    @Data
    public static class ParsedTitle {
        private String title;
        private String artist;
        private String coveredBy;
        private String source;
    }

    /**
     * Parses a raw string into a ParsedTitle object based on specific delimiters and keywords.
     * 
     * Rules:
     * - Source: Enclosed in square brackets [ ].
     * - Covered By: Following "Covered by" or "ซับไทย Covered by".
     * - Title/Artist: Separated by " / " or " - ".
     * 
     * @param rawTitle The raw title string from YouTube or other sources.
     * @return A ParsedTitle object with trimmed fields.
     */
    public static ParsedTitle parse(String rawTitle) {
        if (rawTitle == null || rawTitle.isBlank()) {
            return new ParsedTitle();
        }

        ParsedTitle result = new ParsedTitle();
        String current = rawTitle;

        // 1. Extract Source [ Source ]
        Pattern sourcePattern = Pattern.compile("\\[(.*?)\\]");
        Matcher sourceMatcher = sourcePattern.matcher(current);
        if (sourceMatcher.find()) {
            result.setSource(sourceMatcher.group(1).trim());
            current = current.replace(sourceMatcher.group(0), "");
        }

        // 2. Extract Covered By (handles "ซับไทย Covered by" or just "Covered by")
        // We use case-insensitive matching for "covered by"
        Pattern coveredPattern = Pattern.compile("(?i)(?:ซับไทย\\s+)?covered\\s+by\\s+(.*)", Pattern.CASE_INSENSITIVE);
        Matcher coveredMatcher = coveredPattern.matcher(current);
        if (coveredMatcher.find()) {
            result.setCoveredBy(coveredMatcher.group(1).trim());
            current = current.substring(0, coveredMatcher.start()).trim();
        } else {
            // Check for just "ซับไทย" if "Covered by" isn't present
            Pattern subThaiPattern = Pattern.compile("ซับไทย.*");
            Matcher subThaiMatcher = subThaiPattern.matcher(current);
            if (subThaiMatcher.find()) {
                current = current.substring(0, subThaiMatcher.start()).trim();
            }
        }

        // 3. Split Title and Artist
        // Try " / " first, then " - "
        String[] parts = current.split("\\s*[/-]\\s*", 2);
        if (parts.length == 2) {
            result.setTitle(parts[0].trim());
            result.setArtist(parts[1].trim());
        } else {
            result.setTitle(current.trim());
            result.setArtist("FCNami T_T"); // Default artist if none found
        }

        return result;
    }

    /**
     * Extracts hashtags from a given text block.
     * 
     * Precondition: text can be null or any string value.
     * Postcondition: returns a set of extracted tags (without the '#' symbol).
     * Side-effect: none.
     * 
     * @param text The text block to extract tags from.
     * @return A set of tags found in the text.
     */
    public static java.util.Set<String> extractTags(String text) {
        if (text == null || text.isBlank()) {
            return java.util.Collections.emptySet();
        }
        java.util.Set<String> tags = new java.util.HashSet<>();
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            tags.add(matcher.group(1));
        }
        return tags;
    }
}
