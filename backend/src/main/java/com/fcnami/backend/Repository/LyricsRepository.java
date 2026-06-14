package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.SongTags.Lyrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for managing {@link Lyrics} entities.
 * Facilitates CRUD operations and retrieval of lyrics associated with a song.
 */
public interface LyricsRepository extends JpaRepository<Lyrics, Long> {

    /**
     * Precondition: The songId parameter must be a non-null identifier.
     * Postcondition: Returns an Optional containing the Lyrics of the specified song ID if found, or empty otherwise.
     * Side-effect: None
     */
    Optional<Lyrics> findBySong_Id(Long songId);
}
