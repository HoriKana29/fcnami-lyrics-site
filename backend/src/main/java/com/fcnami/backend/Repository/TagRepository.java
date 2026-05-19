package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.SongTags.Tag;
import com.fcnami.backend.Model.SongTags.TagType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNormalizedName(String normalizedName);
    boolean existsByNormalizedName(String normalizedName);

    // Type Filter
    // UI อาจเรียงมั่ว ได้นะ
    List<Tag> findByType(TagType type);

    List<Tag> findByNameContainingIgnoreCase(String name);
}
