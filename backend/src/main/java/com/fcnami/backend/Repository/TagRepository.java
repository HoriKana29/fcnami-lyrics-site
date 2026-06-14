package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.SongTags.Tag;
import com.fcnami.backend.Model.SongTags.TagType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Tag} entities.
 * Supports querying song tags by name, normalized key, and type.
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Precondition: The normalizedName parameter must be a non-null string.
     * Postcondition: Returns an Optional containing the Tag if found, or empty if not found.
     * Side-effect: None
     */
    Optional<Tag> findByNormalizedName(String normalizedName);

    /**
     * Precondition: The normalizedName parameter must be a non-null string.
     * Postcondition: Returns true if a Tag with the specified normalized name exists, false otherwise.
     * Side-effect: None
     */
    boolean existsByNormalizedName(String normalizedName);

    /**
     * Precondition: The type parameter must be a non-null TagType enum.
     * Postcondition: Returns a list of Tags matching the specified type.
     * Side-effect: None
     */
    List<Tag> findByType(TagType type);

    /**
     * Precondition: The name parameter must be a non-null string.
     * Postcondition: Returns a list of Tags whose names contain the given string, ignoring case.
     * Side-effect: None
     */
    List<Tag> findByNameContainingIgnoreCase(String name);
}
