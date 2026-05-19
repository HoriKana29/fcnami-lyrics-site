package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.SongTags.Tag;
import com.fcnami.backend.Model.SongTags.TagType;

import java.text.Normalizer;
import java.util.UUID;

// Class 'TagFactory' is never used
public class TagFactory {
    public static Tag create(String name, TagType type) {
        Tag tag = new Tag();

        tag.setName(name);
        tag.setNormalizedName(normalize(name));
        tag.setType(type);

        return tag;
    }
    // Method 'createDefault()' is never used
    public static Tag createDefault() {
        String name = "tag_" + UUID.randomUUID();
        return create(name, TagType.GENRE);
    }
    private static String normalize(String input) {
        // เสี่ยง crash ตอน save จาก nullable = false
        if (input == null) return null;

        // เอา accent ออก
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); // remove accents

        return normalized
                .toLowerCase()
                .trim()
                // แทน Space ด้วย _
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", ""); // remove special chars
        // underscore vs no underscore จะไม่เท่ากันแล้ว
        // ภาษา non-latin หายหมด เราทำช่องเพลงญี่ปุ่นนะ คงมีภาษาญี่ปุ่นโผล่มาบ้างนะ
    }
}
