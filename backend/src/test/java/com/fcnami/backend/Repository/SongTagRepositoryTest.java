package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.SongTags.Song;
import com.fcnami.backend.Model.SongTags.SongStatus;
import com.fcnami.backend.Model.SongTags.Tag;
import com.fcnami.backend.TestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SongTagRepositoryTest {
    @Autowired
    private SongRepository songRepository;
    @Autowired
    private TagRepository tagRepository;

    // =========================
    // BASIC SAVE + FIND
    // =========================

    @Test
    @DisplayName("should save and find song by normalizedKey")
    void shouldFindByNormalizedKey() {
        Tag tag = tagRepository.save(TestFactory.createTag("rock"));
        Song song = TestFactory.createSong();
        song.setTags(Set.of(tag));
        songRepository.save(song);

        Optional<Song> result =
                songRepository.findByNormalizedKey(song.getNormalizedKey());

        assertTrue(result.isPresent());
        assertEquals(song.getNormalizedKey(), result.get().getNormalizedKey());
    }

    // =========================
    // EXISTS CHECK
    // =========================

    @Test
    @DisplayName("should return true when normalizedKey exists")
    void shouldCheckExistsByNormalizedKey() {
        Tag tag = tagRepository.save(TestFactory.createTag("rock"));
        Song song = TestFactory.createSong();
        song.setTags(Set.of(tag));

        songRepository.save(song);

        assertTrue(songRepository.existsByNormalizedKey(song.getNormalizedKey()));
    }

    // =========================
    // TITLE SEARCH (case insensitive)
    // =========================

    @Test
    @DisplayName("should find song by title containing ignore case")
    void shouldFindByTitle() {
        songRepository.save(TestFactory.createSong("Hello World", "A"));
        songRepository.save(TestFactory.createSong("Another Song", "B"));

        List<Song> result =
                songRepository.findByTitleContainingIgnoreCase("hello");

        assertEquals(1, result.size());
        assertEquals("Hello World", result.getFirst().getTitle());
    }

    // =========================
    // ARTIST SEARCH
    // =========================

    @Test
    void shouldFindByArtist() {
        songRepository.save(
                TestFactory.createSongWithTitleAndArtist("Song1", "YOASOBI")
        );
        songRepository.save(
                TestFactory.createSongWithTitleAndArtist("Song2", "RADWIMPS")
        );
        List<Song> result =
                songRepository.findByArtistContainingIgnoreCase("yoa");
        assertEquals(1, result.size());
        assertEquals("YOASOBI", result.getFirst().getArtist());
    }

    // =========================
    // STATUS FILTER
    // =========================

    @Test
    void shouldFindByStatus() {
        songRepository.save(TestFactory.createSongWithStatus(SongStatus.IDEA));
        songRepository.save(TestFactory.createSongWithStatus(SongStatus.READY_TO_UPLOAD));

        List<Song> result =
                songRepository.findByStatus(SongStatus.IDEA);
        assertEquals(1, result.size());
        assertTrue(result.stream().allMatch(s -> s.getStatus() == SongStatus.IDEA));
    }

    // =========================
    // TOP 10 ORDER
    // =========================

    @Test
    void shouldReturnTop10Songs() {

        for (int i = 0; i < 15; i++) {
            songRepository.save(
                    TestFactory.createSongWithTitleAndStatus(
                            "Song " + i,
                            SongStatus.IDEA
                    )
            );
        }

        List<Song> result =
                songRepository.findTop10ByOrderByIdDesc();

        assertEquals(10, result.size());

        // ตรวจว่าเรียงจากใหม่ -> เก่า
        for (int i = 1; i < result.size(); i++) {
            assertTrue(result.get(i - 1).getId() > result.get(i).getId());
        }
    }

    // =========================
    // TAG RELATION (MANY TO MANY)
    // =========================

    @Test
    @DisplayName("should find song by tag id")
    void shouldFindByTagId() {
        Tag tag = tagRepository.save(TestFactory.createTag("rock"));

        Song song = TestFactory.createSong();
        song.setTags(Set.of(tag));

        songRepository.save(song);

        List<Song> result =
                songRepository.findByTags_Id(tag.getId());

        assertEquals(1, result.size());
    }

    // =========================
    // TAG NORMALIZED SEARCH
    // =========================

    @Test
    void shouldFindByTagNormalizedNameIn() {
        Tag t1 = tagRepository.save(TestFactory.createTag("rock"));
        Tag t2 = tagRepository.save(TestFactory.createTag("jpop"));

        Song song = TestFactory.createSong();
        song.setTags(Set.of(t1, t2));

        songRepository.save(song);

        List<Song> result =
                songRepository.findByTags_NormalizedNameIn(
                        List.of("rock", "jpop")
                );

        assertFalse(result.isEmpty());
    }

    // =========================
    // MULTI TAG STRICT MATCH (IMPORTANT QUERY)
    // =========================

    @Test
    @DisplayName("should find song that has all tags (HAVING COUNT)")
    void shouldFindByAllTags() {
        Tag t1 = tagRepository.save(TestFactory.createTag("rock"));
        Tag t2 = tagRepository.save(TestFactory.createTag("anime"));

        Song song = TestFactory.createSong();
        song.setTags(Set.of(t1, t2));

        songRepository.save(song);

        List<Song> result =
                songRepository.findByAllTags(
                        List.of("rock", "anime"),
                        2
                );

        assertEquals(1, result.size());
    }

    // =========================
    // LATEST SONGS
    // =========================

    @Test
    void shouldReturnLatestSongs() {
        for (int i = 0; i < 12; i++) {
            songRepository.save(TestFactory.createSongWithoutTags());
        }

        List<Song> result =
                songRepository.findTop10ByOrderByCreatedAtDesc();

        assertEquals(10, result.size());
    }

    @Test
    void shouldReturnEmptyWhenNoSongFound() {
        List<Song> result =
                songRepository.findByTitleContainingIgnoreCase("not_exist");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotMixDifferentStatusSongs() {
        songRepository.save(TestFactory.createSongWithTitleAndStatus("song A", SongStatus.IDEA));
        songRepository.save(TestFactory.createSongWithTitleAndStatus("song B", SongStatus.READY_TO_UPLOAD));
        List<Song> result =
                songRepository.findByStatusAndTitleContainingIgnoreCase(
                        SongStatus.IDEA,
                        "song"
                );

        assertTrue(result.stream().allMatch(s -> s.getStatus() == SongStatus.IDEA));
    }

    @Test
    void shouldHandleTagCaseConsistency() {
        Tag tag = tagRepository.save(TestFactory.createTag("Rock"));

        Song song = TestFactory.createSong();
        song.setTags(Set.of(tag));

        songRepository.save(song);

        List<Song> result =
                songRepository.findByTags_NormalizedNameIn(List.of("rock"));

        assertFalse(result.isEmpty());
    }

    @Test
    void shouldReturnPagedResult() {

        for (int i = 0; i < 20; i++) {
            songRepository.save(
                    TestFactory.createSongWithTitleAndStatus("Song " + i, SongStatus.IDEA)
            );
        }

        Page<Song> page =
                songRepository.findByTitleContainingIgnoreCase(
                        "Song",
                        org.springframework.data.domain.PageRequest.of(0, 10)
                );

        assertEquals(10, page.getContent().size());

        // optional but important
        assertTrue(page.getTotalElements() >= 20);
    }

    @Test
    void shouldHandleSongWithNoTags() {
        Song song = TestFactory.createSong();
        song.setTags(Set.of());

        songRepository.save(song);

        assertNotNull(song.getId());
    }
}