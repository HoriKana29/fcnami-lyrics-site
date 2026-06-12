package com.fcnami.backend.Api;

import com.fcnami.backend.Model.SongTags.SongStatus;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class SongDtos {
    private SongDtos() {
    }

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
            String notes
    ) {
    }

    public record LyricsResponse(String kanji, String romaji, String thai, String notes, LocalDateTime updatedAt) {
    }

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
            LyricsResponse lyrics
    ) {
    }

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
