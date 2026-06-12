package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.SongTags.Tag;
import com.fcnami.backend.Model.SongTags.TagType;
import com.fcnami.backend.Support.SlugUtil;

import java.util.UUID;

/**
 * Factory class for creating Tag instances.
 */
public class TagFactory {
    
    /**
     * Precondition: Name must be non-null and non-blank. TagType must not be null.
     * Postcondition: Returns a new Tag instance.
     * Side-effect: None.
     */
    public static Tag create(String name, TagType type) {
        Tag tag = new Tag();

        tag.setName(requireText(name, "name"));
        tag.setNormalizedName(SlugUtil.slugify(name));
        tag.setType(requireType(type));

        return tag;
    }

    /**
     * Precondition: None.
     * Postcondition: Returns a new Tag instance with a randomly generated name and TagType.GENRE.
     * Side-effect: None.
     */
    public static Tag createDefault() {
        String name = "tag_" + UUID.randomUUID();
        return create(name, TagType.GENRE);
    }

    /**
     * Precondition: Value and field name are provided.
     * Postcondition: Returns the trimmed value, or throws IllegalArgumentException if empty/null.
     * Side-effect: None.
     */
    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    /**
     * Precondition: TagType is provided.
     * Postcondition: Returns the TagType, or throws IllegalArgumentException if null.
     * Side-effect: None.
     */
    private static TagType requireType(TagType type) {
        if (type == null) {
            throw new IllegalArgumentException("type is required");
        }
        return type;
    }
}
