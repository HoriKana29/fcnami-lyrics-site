package com.fcnami.backend.Api;

import com.fcnami.backend.Model.SongTags.SongStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * SongDtos serves as a namespace container for Data Transfer Objects (DTOs)
 * related to songs and lyrics management.
 */
public final class SongDtos {

    /**
     * Private constructor to prevent instantiation of this utility namespace class.
     *
     * Precondition: None.
     * Postcondition: Throws an AssertionError if instantiation is attempted.
     * Side-effect: None.
     */
    private SongDtos() {
        throw new AssertionError("No instances of SongDtos should be created.");
    }

    /**
     * SongUpsertRequest represents the incoming payload used to create or update a song.
     *
     * @param title the title of the song (must not be blank)
     * @param titleJapanese optional Japanese title of the song
     * @param artist the artist or singer of the song (must not be blank)
     * @param sourceAnimeOrGame optional source anime, game, or franchise
     * @param youtubeUrl optional full YouTube video link
     * @param youtubeVideoId optional parsed YouTube video ID
     * @param thumbnailUrl optional custom thumbnail image URL
     * @param status the publishing status of the song
     * @param tags associated tag names (genres, categories)
     * @param moods associated mood names
     * @param kanji the Japanese kanji lyrics content
     * @param romaji the Romanized romaji lyrics content
     * @param thai the translated Thai lyrics content
     * @param notes optional comments, notes, or explanations for the song or lyrics
     * @param publishedAt optional timestamp for when the song was originally published (e.g., on YouTube)
     * @param viewCount optional view count for the song (e.g., from YouTube)
     */
    public record SongUpsertRequest(
            @NotBlank String title,
            String titleJapanese,
            @NotBlank String artist,
            String sourceAnimeOrGame,
            String youtubeUrl,
            String youtubeVideoId,
            String thumbnailUrl,
            SongStatus status,
            Set<String> tags,
            Set<String> moods,
            String kanji,
            String romaji,
            String thai,
            String notes,
            LocalDateTime publishedAt,
            Long viewCount
    ) {
    }

    /**
     * LyricsResponse represents the formatted lyrics and notes metadata in API responses.
     *
     * @param kanji the Japanese kanji lyrics content
     * @param romaji the Romanized romaji lyrics content
     * @param thai the translated Thai lyrics content
     * @param notes optional notes or translator comments
     * @param updatedAt the timestamp when the lyrics were last updated
     */
    public record LyricsResponse(String kanji, String romaji, String thai, String notes, LocalDateTime updatedAt) {
    }

    /**
     * SongResponse represents the complete song details returned by public and admin endpoints.
     *
     * @param id the unique primary key identifier of the song
     * @param slug the URL-friendly unique slug representation of the song title
     * @param title the title of the song
     * @param titleJapanese optional Japanese title
     * @param artist the song artist
     * @param sourceAnimeOrGame optional source franchise
     * @param youtubeUrl optional full YouTube video link
     * @param youtubeVideoId optional YouTube video identifier
     * @param thumbnailUrl optional thumbnail image URL
     * @param status the publishing status of the song
     * @param tags set of general tag names associated with the song
     * @param moods set of mood tag names associated with the song
     * @param createdAt the timestamp when the song entry was created
     * @param updatedAt the timestamp when the song details were last modified
     * @param publishedAt optional timestamp when the song was published
     * @param viewCount optional view count for the song
     * @param lyrics the detailed lyrics object
     */
    public record SongResponse(
            Long id,
            String slug,
            String title,
            String titleJapanese,
            String artist,
            String sourceAnimeOrGame,
            String youtubeUrl,
            String youtubeVideoId,
            String thumbnailUrl,
            SongStatus status,
            Set<String> tags,
            Set<String> moods,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime publishedAt,
            Long viewCount,
            LyricsResponse lyrics
    ) {
    }

    /**
     * PageResponse represents a standardized paginated response wrapper.
     *
     * @param <T> the type of elements contained in the page content
     * @param content the list of elements for the current page
     * @param totalElements the grand total number of elements matching the query
     * @param totalPages the total number of pages available
     * @param page the zero-based current page index
     * @param size the size of the current page
     * @param first true if this is the first page, false otherwise
     * @param last true if this is the last page, false otherwise
     */
    public record PageResponse<T>(
            List<T> content,
            long totalElements,
            int totalPages,
            int page,
            int size,
            boolean first,
            boolean last
    ) {
    }
}

