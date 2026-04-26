package com.fcnami.backend.Model.SongTags;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

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
                @Index(name = "idx_song_status", columnList = "status")
        }
)
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String artist;
    @Column(nullable = false,unique = true)
    private String normalizedKey;

    @Column(columnDefinition = "TEXT")
    private String kanjiLyrics;

    @Column(columnDefinition = "TEXT")
    private String romajiLyrics;

    @Column(columnDefinition = "TEXT")
    private String translationTh;

    @Column(columnDefinition = "TEXT")
    private String translationEn;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "song_tags",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SongStatus status = SongStatus.IDEA;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
