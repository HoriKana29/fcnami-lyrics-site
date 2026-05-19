package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.SongTags.Lyrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// ตอนนี้ no usages อยู่
public interface LyricsRepository extends JpaRepository<Lyrics, Long> {

    // หา Lyrics จาก songId
    Optional<Lyrics> findBySong_Id(Long songId);

    // Spring ตีความแบบนี้ถูกมั้ย??
//    SELECT * FROM lyrics
//    WHERE song_id = ?
}
