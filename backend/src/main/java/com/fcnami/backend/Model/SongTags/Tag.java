package com.fcnami.backend.Model.SongTags;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "tags",indexes = {
        @Index(name = "idx_tag_name", columnList = "name")
})
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,unique = true)
    private String name;

    @Column(unique = true)
    private String normalizedName;

    @Enumerated(EnumType.STRING)
    private TagType type;

    @ManyToMany(mappedBy = "tags",fetch = FetchType.LAZY)
    private Set<Song> songs;
}