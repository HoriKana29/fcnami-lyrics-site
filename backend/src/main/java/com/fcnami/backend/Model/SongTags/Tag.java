package com.fcnami.backend.Model.SongTags;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Setter
@Getter
@Table(name = "tags",
        indexes = {
                @Index(name = "idx_tag_name", columnList = "name"),
                @Index(name = "idx_tag_normalized", columnList = "normalizedName"),
                @Index(name = "idx_tag_type", columnList = "type")
        })
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;

    @Column(unique = true,nullable = false)
    private String normalizedName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TagType type;

    @ManyToMany(mappedBy = "tags",fetch = FetchType.LAZY)
    private Set<Song> songs;
}