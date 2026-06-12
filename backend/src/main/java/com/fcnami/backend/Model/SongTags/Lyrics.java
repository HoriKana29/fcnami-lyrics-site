package com.fcnami.backend.Model.SongTags;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "lyrics")
public class Lyrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_id", nullable = false, unique = true)
    private Song song;

    @Column(columnDefinition = "TEXT")
    private String kanji;

    @Column(columnDefinition = "TEXT")
    private String romaji;

    @Column(columnDefinition = "TEXT")
    private String thai;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void touch() {
        updatedAt = LocalDateTime.now();
    }
}
