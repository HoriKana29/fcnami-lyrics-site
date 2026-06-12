package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.SongTags.Lyrics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LyricsRepository extends JpaRepository<Lyrics, Long> {

    Optional<Lyrics> findBySong_Id(Long songId);
}
