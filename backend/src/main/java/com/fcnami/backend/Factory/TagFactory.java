package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.SongTags.Tag;
import com.fcnami.backend.Model.SongTags.TagType;
import com.fcnami.backend.Support.SlugUtil;

import java.util.UUID;

public class TagFactory {
    public static Tag create(String name, TagType type) {
        Tag tag = new Tag();

        tag.setName(requireText(name, "name"));
        tag.setNormalizedName(SlugUtil.slugify(name));
        tag.setType(requireType(type));

        return tag;
    }

    public static Tag createDefault() {
        String name = "tag_" + UUID.randomUUID();
        return create(name, TagType.GENRE);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static TagType requireType(TagType type) {
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
        return type;
    }
}
