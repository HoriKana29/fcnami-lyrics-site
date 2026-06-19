package com.fcnami.backend.Service;

import com.fcnami.backend.Api.SongDtos.SongUpsertRequest;
import com.fcnami.backend.Model.SongTags.SongStatus;
import com.fcnami.backend.Repository.SongRepository;
import com.fcnami.backend.Support.SongTitleParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

/**
 * Service for performing expensive tagging operations asynchronously in the background.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncTaggingService {

    private final SongResearchService songResearchService;
    private final TagClassificationService tagClassificationService;
    private final SongRepository songRepository;
    private final SongCatalogService songCatalogService;

    /**
     * Performs background research and classification for a song.
     * Updates the database once complete.
     */
    @Async
    public void processAdvancedTagging(String videoId, String title, String artist, String description, String source, String thumbnailUrl, LocalDateTime publishedAt, Long viewCount) {
        log.info("Starting background advanced tagging for video: {} ({})", title, videoId);
        try {
            // 1. Research
            String research = songResearchService.researchSong(title, artist);
            
            // 2. Classify
            String fullContext = title + " " + description + " " + research;
            Set<String> advancedTags = tagClassificationService.classify(fullContext);
            
            // 3. Extract description tags
            advancedTags.addAll(SongTitleParser.extractTags(description));

            // 4. Update Database
            var existing = songRepository.findByYoutubeVideoId(videoId);
            
            SongUpsertRequest request = new SongUpsertRequest(
                    title, null, artist, source != null ? source : "YouTube",
                    "https://youtu.be/" + videoId,
                    videoId, thumbnailUrl, SongStatus.PUBLISHED,
                    advancedTags, Collections.emptySet(),
                    "", "", "", "", publishedAt, viewCount
            );

            if (existing.isPresent()) {
                songCatalogService.update(existing.get().getId(), request);
                log.info("Background tagging complete: Updated existing song ID {}", existing.get().getId());
            } else {
                songCatalogService.create(request);
                log.info("Background tagging complete: Created new song entry for {}", title);
            }
        } catch (Exception e) {
            log.error("Background advanced tagging failed for {}: {}", title, e.getMessage());
        }
    }
}
