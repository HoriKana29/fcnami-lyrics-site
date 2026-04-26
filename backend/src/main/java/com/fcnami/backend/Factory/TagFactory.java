package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.SongTags.Tag;
import com.fcnami.backend.Model.SongTags.TagType;

import java.text.Normalizer;
import java.util.UUID;

public class TagFactory {
    public static Tag create(String name, TagType type) {
        Tag tag = new Tag();

        tag.setName(name);
        tag.setNormalizedName(normalize(name));
        tag.setType(type);

        return tag;
    }
    public static Tag createDefault() {
        String name = "tag_" + UUID.randomUUID();
        return create(name, TagType.GENRE);
    }
    private static String normalize(String input) {
        if (input == null) return null;

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", ""); // remove accents

        return normalized
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-z0-9_]", ""); // remove special chars
    }
}
