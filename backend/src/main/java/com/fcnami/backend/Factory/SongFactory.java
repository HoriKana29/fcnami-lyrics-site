package com.fcnami.backend.Factory;

import com.fcnami.backend.Model.SongTags.Song;
import com.fcnami.backend.Model.SongTags.SongStatus;
import com.fcnami.backend.Model.SongTags.Tag;
import com.fcnami.backend.Support.SlugUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Factory class for creating Song instances.
 */
public class SongFactory {
    
    /**
     * Precondition: Title and artist must be non-null and non-blank.
     * Postcondition: Returns a new Song instance with status IDEA and an empty set of tags.
     * Side-effect: None.
     */
    public static Song create(String title, String artist) {
        Song song = new Song();

        song.setTitle(requireText(title, "title"));
        song.setArtist(requireText(artist, "artist"));

        song.setNormalizedKey(SlugUtil.normalizedKey(title, artist));
        song.setSlug(SlugUtil.slugify(title + "-" + artist));

        song.setStatus(SongStatus.IDEA);
        song.setTags(new HashSet<>());

        return song;
    }

    /**
     * Precondition: None.
     * Postcondition: Returns a Song instance with a randomly generated title and artist.
     * Side-effect: None.
     */
    public static Song createDefault() {
        return create(
                "title_" + UUID.randomUUID(),
                "artist_" + UUID.randomUUID()
        );
    }

    /**
     * Precondition: Title and artist must be non-null and non-blank. SongStatus must not be null.
     * Postcondition: Returns a Song instance with the specified status.
     * Side-effect: None.
     */
    public static Song createWithStatus(String title, String artist, SongStatus status) {
        Song song = create(title, artist);
        song.setStatus(status);
        return song;
    }

    /**
     * Precondition: Title and artist must be non-null and non-blank.
     * Postcondition: Returns a Song instance with the specified tags.
     * Side-effect: None.
     */
    public static Song createWithTags(String title, String artist, Set<Tag> tags) {
        Song song = create(title, artist);
        song.setTags(tags != null ? tags : new HashSet<>());
        return song;
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
}
