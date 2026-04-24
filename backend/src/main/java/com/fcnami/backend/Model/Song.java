package com.fcnami.backend.Model;

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
@Table(name = "songs")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String artist;

    @Column(columnDefinition = "TEXT")
    private String kanjiLyrics;

    @Column(columnDefinition = "TEXT")
    private String romajiLyrics;

    @Column(columnDefinition = "TEXT")
    private String translationTh;

    @Column(columnDefinition = "TEXT")
    private String translationEn;

    @ManyToMany
    @JoinTable(
            name = "song_tags",
            joinColumns = @JoinColumn(name = "song_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    @Enumerated(EnumType.STRING)
    private SongStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
