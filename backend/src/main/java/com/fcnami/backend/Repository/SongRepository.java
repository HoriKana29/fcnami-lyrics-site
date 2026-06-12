package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.SongTags.Song;
import com.fcnami.backend.Model.SongTags.SongStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link Song} entities.
 * Handles database operations for song cataloging, search, filtering, and tagging.
 */
public interface SongRepository extends JpaRepository<Song, Long> {

    /**
     * Precondition: The normalizedKey parameter must be a non-null string.
     * Postcondition: Returns an Optional containing the Song if found, or empty otherwise.
     * Side-effect: None
     */
    Optional<Song> findByNormalizedKey(String normalizedKey);

    /**
     * Precondition: The slug parameter must be a non-null string.
     * Postcondition: Returns an Optional containing the Song if found, or empty otherwise.
     * Side-effect: None
     */
    Optional<Song> findBySlug(String slug);

    /**
     * Precondition: The normalizedKey parameter must be a non-null string.
     * Postcondition: Returns true if a Song with the normalized key exists, false otherwise.
     * Side-effect: None
     */
    boolean existsByNormalizedKey(String normalizedKey);

    /**
     * Precondition: The title parameter must be a non-null string.
     * Postcondition: Returns a list of Songs containing the title string, ignoring case.
     * Side-effect: None
     */
    List<Song> findByTitleContainingIgnoreCase(String title);

    /**
     * Precondition: The artist parameter must be a non-null string.
     * Postcondition: Returns a list of Songs containing the artist string, ignoring case.
     * Side-effect: None
     */
    List<Song> findByArtistContainingIgnoreCase(String artist);

    /**
     * Precondition: The title and artist parameters must be non-null strings.
     * Postcondition: Returns a list of Songs matching either the title or artist search terms, ignoring case.
     * Side-effect: None
     */
    List<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(String title, String artist);

    /**
     * Precondition: The status parameter must be a non-null SongStatus enum.
     * Postcondition: Returns a list of Songs matching the status.
     * Side-effect: None
     */
    List<Song> findByStatus(SongStatus status);

    /**
     * Precondition: All parameters must be non-null and valid.
     * Postcondition: Returns a Page of Songs filtered by status and matching either title or artist, ignoring case.
     * Side-effect: None
     */
    Page<Song> findByStatusAndTitleContainingIgnoreCaseOrStatusAndArtistContainingIgnoreCase(
            SongStatus titleStatus,
            String title,
            SongStatus artistStatus,
            String artist,
            Pageable pageable
    );

    /**
     * Precondition: All parameters must be non-null and valid.
     * Postcondition: Returns a Page of Songs containing title or artist search terms, ignoring case.
     * Side-effect: None
     */
    Page<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
            String title,
            String artist,
            Pageable pageable
    );

    /**
     * Precondition: All parameters must be non-null and valid.
     * Postcondition: Returns a list of Songs matching the status and containing the title string, ignoring case.
     * Side-effect: None
     */
    List<Song> findByStatusAndTitleContainingIgnoreCase(SongStatus status, String title);

    /**
     * Precondition: All parameters must be non-null and valid.
     * Postcondition: Returns a list of Songs matching the status and containing the artist string, ignoring case.
     * Side-effect: None
     */
    List<Song> findByStatusAndArtistContainingIgnoreCase(SongStatus status, String artist);

    /**
     * Precondition: All parameters must be non-null and valid.
     * Postcondition: Returns a Page of Songs containing the title term, ignoring case.
     * Side-effect: None
     */
    Page<Song> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Precondition: All parameters must be non-null and valid.
     * Postcondition: Returns a Page of Songs containing the artist term, ignoring case.
     * Side-effect: None
     */
    Page<Song> findByArtistContainingIgnoreCase(String artist, Pageable pageable);

    /**
     * Precondition: All parameters must be non-null and valid.
     * Postcondition: Returns a Page of Songs matching the status.
     * Side-effect: None
     */
    Page<Song> findByStatus(SongStatus status, Pageable pageable);

    /**
     * Precondition: The tags parameter must be a non-null, non-empty list of normalized tag names.
     * Postcondition: Returns a list of Songs containing any of the given tag names.
     * Side-effect: None
     */
    List<Song> findByTags_NormalizedNameIn(List<String> tags);

    /**
     * Precondition: The tags parameter must be non-null/non-empty, and tagCount must be greater than 0.
     * Postcondition: Returns a list of Songs that possess all specified tags (exact match on tag set size).
     * Side-effect: None
     */
    @Query("""
        SELECT s FROM Song s
        JOIN s.tags t
        WHERE t.normalizedName IN :tags
        GROUP BY s.id
        HAVING COUNT(DISTINCT t.id) = :tagCount
    """)
    List<Song> findByAllTags(List<String> tags, long tagCount);

    /**
     * Precondition: The tagId parameter must be a non-null identifier.
     * Postcondition: Returns a list of Songs matching the tag ID.
     * Side-effect: None
     */
    List<Song> findByTags_Id(Long tagId);

    /**
     * Precondition: None
     * Postcondition: Returns the top 10 songs ordered by creation time in descending order.
     * Side-effect: None
     */
    List<Song> findTop10ByOrderByCreatedAtDesc();

    /**
     * Precondition: None
     * Postcondition: Returns the top 10 songs ordered by ID in descending order.
     * Side-effect: None
     */
    List<Song> findTop10ByOrderByIdDesc();
}
