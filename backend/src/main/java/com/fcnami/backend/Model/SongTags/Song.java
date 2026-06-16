package com.fcnami.backend.Model.SongTags;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fcnami.backend.Support.SlugUtil;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "songs",
        indexes = {
                @Index(name = "idx_song_title", columnList = "title"),
                @Index(name = "idx_song_artist", columnList = "artist"),
                @Index(name = "idx_song_normalized", columnList = "normalizedKey"),
                @Index(name = "idx_song_slug", columnList = "slug"),
                @Index(name = "idx_song_status", columnList = "status")
        }
)
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String titleJapanese;

    @Column(nullable = false)
    private String artist;

    @Column(nullable = false, unique = true)
    private String normalizedKey;

    @Column(nullable = false)
    private String slug;

    private String sourceAnimeOrGame;

    private String youtubeUrl;
    private String youtubeVideoId;
    private String thumbnailUrl;

    // Legacy inline lyrics kept for older rows; new writes use the Lyrics entity.
    @Column(columnDefinition = "TEXT")
    private String kanjiLyrics;

    @Column(columnDefinition = "TEXT")
    private String romajiLyrics;

    @Column(columnDefinition = "TEXT")
    private String translationTh;

    @Column(columnDefinition = "TEXT")
    private String translationEn;

    @OneToOne(mappedBy = "song", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Lyrics lyrics;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "song_tags",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SongStatus status = SongStatus.IDEA;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    private Long viewCount;

    @PrePersist
    protected void onCreate() {
        if (slug == null || slug.isBlank()) {
            slug = SlugUtil.slugify(title + "-" + artist);
        }

        if (normalizedKey == null || normalizedKey.isBlank()) {
            normalizedKey = SlugUtil.normalizedKey(title, artist);
        }

        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        if (slug == null || slug.isBlank()) {
            slug = SlugUtil.slugify(title + "-" + artist);
        }
        if (normalizedKey == null || normalizedKey.isBlank()) {
            normalizedKey = SlugUtil.normalizedKey(title, artist);
        }
        updatedAt = LocalDateTime.now();
    }
}
