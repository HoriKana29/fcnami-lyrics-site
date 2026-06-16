package com.fcnami.backend.Service;

import com.fcnami.backend.Api.SongDtos.LyricsResponse;
import com.fcnami.backend.Api.SongDtos.SongResponse;
import com.fcnami.backend.Api.SongDtos.SongUpsertRequest;
import com.fcnami.backend.Model.SongTags.*;
import com.fcnami.backend.Repository.SongRepository;
import com.fcnami.backend.Repository.TagRepository;
import com.fcnami.backend.Support.SlugUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SongCatalogService {
    private static final Pattern YOUTUBE_ID_PATTERN = Pattern.compile(
            "(?:youtu\\.be/|youtube\\.com/(?:watch\\?v=|embed/|shorts/|live/))([A-Za-z0-9_-]{11})"
    );

    private final SongRepository songRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public Page<SongResponse> listPublicSongs(String search, SongStatus status, Pageable pageable) {
        Page<Song> songs;
        if (hasText(search) && status != null) {
            songs = songRepository.findByStatusAndTitleContainingIgnoreCaseOrStatusAndArtistContainingIgnoreCase(
                    status, search, status, search, pageable
            );
        } else if (hasText(search)) {
            songs = songRepository.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(search, search, pageable);
        } else if (status != null) {
            songs = songRepository.findByStatus(status, pageable);
        } else {
            songs = songRepository.findAll(pageable);
        }
        return songs.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public SongResponse getBySlug(String slug) {
        return songRepository.findBySlug(slug)
                .map(this::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Song not found: " + slug));
    }

    @Transactional
    public SongResponse create(SongUpsertRequest request) {
        if (hasText(request.youtubeVideoId())) {
            var existing = songRepository.findByYoutubeVideoId(request.youtubeVideoId());
            if (existing.isPresent()) {
                // If it already exists, we might want to update it or just return it.
                // For sync purposes, returning existing is safer than creating a duplicate.
                return toResponse(existing.get());
            }
        }
        
        Song song = new Song();
        apply(song, request);
        song.setSlug(uniqueSlug(request.title(), null));
        song.setNormalizedKey(uniqueNormalizedKey(request.title(), request.artist(), null));
        return toResponse(songRepository.save(song));
    }

    @Transactional
    public SongResponse update(Long id, SongUpsertRequest request) {
        Song song = songRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Song not found: " + id));
        apply(song, request);
        song.setSlug(uniqueSlug(request.title(), id));
        song.setNormalizedKey(uniqueNormalizedKey(request.title(), request.artist(), id));
        return toResponse(songRepository.save(song));
    }

    private void apply(Song song, SongUpsertRequest request) {
        song.setTitle(request.title());
        song.setTitleJapanese(request.titleJapanese());
        song.setArtist(request.artist());
        song.setSourceAnimeOrGame(request.sourceAnimeOrGame());
        song.setYoutubeUrl(request.youtubeUrl());
        song.setYoutubeVideoId(hasText(request.youtubeVideoId()) ? request.youtubeVideoId() : extractYoutubeId(request.youtubeUrl()));
        song.setThumbnailUrl(request.thumbnailUrl());
        song.setViewCount(request.viewCount());
        SongStatus nextStatus = request.status() == null ? SongStatus.DRAFT : request.status();
        song.setStatus(nextStatus);
        
        // Handle publishedAt: Prefer request, then existing, then now (if published)
        if (request.publishedAt() != null) {
            song.setPublishedAt(request.publishedAt());
        } else if (nextStatus == SongStatus.PUBLISHED && song.getPublishedAt() == null) {
            song.setPublishedAt(LocalDateTime.now());
        }

        Lyrics lyrics = song.getLyrics();
        if (lyrics == null) {
            lyrics = new Lyrics();
            lyrics.setSong(song);
            song.setLyrics(lyrics);
        }
        lyrics.setKanji(request.kanji());
        lyrics.setRomaji(request.romaji());
        lyrics.setThai(request.thai());
        lyrics.setNotes(request.notes());

        Set<Tag> tags = new LinkedHashSet<>();
        addTags(tags, request.tags(), TagType.GENRE);
        addTags(tags, request.moods(), TagType.MOOD);
        song.setTags(tags);
    }

    private void addTags(Set<Tag> target, Set<String> names, TagType type) {
        if (names == null) {
            return;
        }
        for (String name : names) {
            if (!hasText(name)) {
                continue;
            }
            target.add(findOrCreateTag(name, type));
        }
    }

    private Tag findOrCreateTag(String name, TagType type) {
        String normalized = SlugUtil.slugify(name);
        return tagRepository.findByNormalizedName(normalized).orElseGet(() -> createTag(name, normalized, type));
    }

    private Tag createTag(String name, String normalized, TagType type) {
        Tag created = new Tag();
        created.setName(name.trim());
        created.setNormalizedName(normalized);
        created.setType(type);
        return tagRepository.save(created);
    }

    private SongResponse toResponse(Song song) {
        Set<Tag> songTags = safeTags(song);
        Set<String> tags = songTags.stream()
                .filter(tag -> tag.getType() != TagType.MOOD)
                .map(Tag::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> moods = songTags.stream()
                .filter(tag -> tag.getType() == TagType.MOOD)
                .map(Tag::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Lyrics lyrics = song.getLyrics();
        LyricsResponse lyricsResponse = lyrics == null
                ? new LyricsResponse(song.getKanjiLyrics(), song.getRomajiLyrics(), song.getTranslationTh(), null, song.getUpdatedAt())
                : new LyricsResponse(lyrics.getKanji(), lyrics.getRomaji(), lyrics.getThai(), lyrics.getNotes(), lyrics.getUpdatedAt());
        return new SongResponse(
                song.getId(), song.getSlug(), song.getTitle(), song.getTitleJapanese(), song.getArtist(),
                song.getSourceAnimeOrGame(), song.getYoutubeUrl(), song.getYoutubeVideoId(), song.getThumbnailUrl(),
                song.getStatus(), tags, moods, song.getCreatedAt(), song.getUpdatedAt(), song.getPublishedAt(),
                song.getViewCount(), lyricsResponse
        );
    }

    private Set<Tag> safeTags(Song song) {
        return song.getTags() == null ? Collections.emptySet() : song.getTags();
    }

    private String uniqueSlug(String title, Long currentSongId) {
        String base = SlugUtil.slugify(title);
        String candidate = base;
        int suffix = 2;
        while (slugTaken(candidate, currentSongId)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private boolean slugTaken(String slug, Long currentSongId) {
        return songRepository.findBySlug(slug)
                .map(song -> !song.getId().equals(currentSongId))
                .orElse(false);
    }

    private String uniqueNormalizedKey(String title, String artist, Long currentSongId) {
        String base = SlugUtil.normalizedKey(title, artist);
        String candidate = base;
        int suffix = 2;
        while (normalizedKeyTaken(candidate, currentSongId)) {
            candidate = base + "-" + suffix++;
        }
        return candidate;
    }

    private boolean normalizedKeyTaken(String normalizedKey, Long currentSongId) {
        return songRepository.findByNormalizedKey(normalizedKey)
                .map(song -> !song.getId().equals(currentSongId))
                .orElse(false);
    }

    private String extractYoutubeId(String youtubeUrl) {
        if (!hasText(youtubeUrl)) {
            return null;
        }
        Matcher matcher = YOUTUBE_ID_PATTERN.matcher(youtubeUrl.trim());
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
