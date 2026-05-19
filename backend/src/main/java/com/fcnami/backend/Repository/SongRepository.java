package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.SongTags.Song;
import com.fcnami.backend.Model.SongTags.SongStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {
    Optional<Song> findByNormalizedKey(String normalizedKey);
    Optional<Song> findBySlug(String slug);
    boolean existsByNormalizedKey(String normalizedKey);

    // Method 'existsBySlug(java.lang.String)' is never used
    boolean existsBySlug(String slug);

    // Search
    List<Song> findByTitleContainingIgnoreCase(String title);
    List<Song> findByArtistContainingIgnoreCase(String artist);
    List<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(String title, String artist);

    List<Song> findByStatus(SongStatus status);

    // Spring เข้าใจแบบนี้ใช่มั้ย
    //(status = A AND title LIKE ?)
    //OR
    //(status = B AND artist LIKE ?)
    Page<Song> findByStatusAndTitleContainingIgnoreCaseOrStatusAndArtistContainingIgnoreCase(
            SongStatus titleStatus,
            String title,
            SongStatus artistStatus,
            String artist,
            Pageable pageable
    );
    Page<Song> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(
            String title,
            String artist,
            Pageable pageable
    );
    List<Song> findByStatusAndTitleContainingIgnoreCase(SongStatus status, String title);
    List<Song> findByStatusAndArtistContainingIgnoreCase(SongStatus status, String artist);

    // protect heavy query
    Page<Song> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Song> findByArtistContainingIgnoreCase(String artist, Pageable pageable);
    Page<Song> findByStatus(SongStatus status, Pageable pageable);

    // มี Tag ไหนก็ได้
    List<Song> findByTags_NormalizedNameIn(List<String> tags);

    // ต้องมีครบทุก Tag
    @Query("""
        SELECT s FROM Song s
        JOIN s.tags t
        WHERE t.normalizedName IN :tags
        GROUP BY s.id
        HAVING COUNT(DISTINCT t.id) = :tagCount
    """)
    List<Song> findByAllTags(List<String> tags, long tagCount);

    List<Song> findByTags_Id(Long tagId);

    List<Song> findTop10ByOrderByCreatedAtDesc();
    List<Song> findTop10ByOrderByIdDesc();
}
