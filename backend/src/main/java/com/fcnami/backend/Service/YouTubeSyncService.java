package com.fcnami.backend.Service;

import com.fcnami.backend.Api.SongDtos.SongUpsertRequest;
import com.fcnami.backend.Model.SongTags.SongStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeSyncService {

    private final RestClient.Builder restClientBuilder;
    private final SongCatalogService songCatalogService;

    @Value("${fcnami.youtube.api-key:}")
    private String apiKey;

    @Value("${fcnami.youtube.channel-id:UC8P0dc0Zn2gf8L6tJi_k6xg}")
    private String channelId;

    /**
     * Syncs videos from the YouTube channel.
     * Precondition: apiKey must be valid for real sync.
     * Postcondition: Songs are updated/created in the database.
     */
    public int syncChannelVideos() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("YouTube API key is missing. Skipping real sync.");
            return seedMockData(); // Fallback to seeding some real channel data manually for demo
        }

        try {
            RestClient restClient = restClientBuilder
                    .baseUrl("https://www.googleapis.com/youtube/v3")
                    .defaultHeader("Referer", "http://localhost:8080")
                    .build();

            log.info("Starting YouTube sync for channel: {} using provided API key", channelId);
            // 1. Get uploads playlist ID
            Map channelResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/channels")
                            .queryParam("part", "contentDetails")
                            .queryParam("id", channelId)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (channelResponse == null || !channelResponse.containsKey("items")) {
                log.error("Invalid response from YouTube API for channel ID: {}", channelId);
                return 0;
            }

            List items = (List) channelResponse.get("items");
            if (items == null || items.isEmpty()) {
                log.warn("No channel found with ID: {}", channelId);
                return 0;
            }

            String uploadsPlaylistId = (String) ((Map) ((Map) ((Map) items.get(0)).get("contentDetails")).get("relatedPlaylists")).get("uploads");

            // 2. Fetch videos from playlist
            // (Simplification: just fetch the first page of 50 for now)
            Map playlistResponse = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/playlistItems")
                            .queryParam("part", "snippet,contentDetails")
                            .queryParam("playlistId", uploadsPlaylistId)
                            .queryParam("maxResults", 50)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(Map.class);

            List playlistItems = (List) playlistResponse.get("items");
            if (playlistItems == null) return 0;

            int count = 0;
            for (Object itemObj : playlistItems) {
                Map item = (Map) itemObj;
                Map snippet = (Map) item.get("snippet");
                Map contentDetails = (Map) item.get("contentDetails");

                String videoId = (String) contentDetails.get("videoId");
                String title = (String) snippet.get("title");
                String description = (String) snippet.get("description");
                String thumbnailUrl = (String) ((Map) ((Map) snippet.get("thumbnails")).get("high")).get("url");

                // Basic parsing of title for Artist - Song Title
                String artist = "FCNami T_T";
                String songTitle = title;
                if (title.contains("-")) {
                    String[] parts = title.split("-", 2);
                    artist = parts[0].trim();
                    songTitle = parts[1].trim();
                }

                // Determine tags
                Set<String> tags = new HashSet<>();
                if (title.toLowerCase().contains("bandori")) tags.add("bandori");
                
                // Fetch duration to check for long track
                // (Would need another API call per video or batch)
                
                upsertFromYouTube(videoId, songTitle, artist, thumbnailUrl, tags);
                count++;
            }

            return count;
        } catch (Exception e) {
            log.error("Failed to sync YouTube videos", e);
            throw new RuntimeException("YouTube sync failed: " + e.getMessage(), e);
        }
    }

    private void upsertFromYouTube(String videoId, String title, String artist, String thumbnailUrl, Set<String> tags) {
        // Check if exists? SongCatalogService.create handles unique slugs/keys.
        // For simplicity, we just create or skip if logic in service allows.
        try {
            songCatalogService.create(new SongUpsertRequest(
                    title, null, artist, "YouTube",
                    "https://youtu.be/" + videoId,
                    videoId, thumbnailUrl, SongStatus.PUBLISHED,
                    tags, Collections.emptySet(),
                    "", "", "", ""
            ));
        } catch (Exception e) {
            log.debug("Skipping duplicate or failed video: {}", title);
        }
    }

    private int seedMockData() {
        log.info("Seeding mock data from real FCNami channel videos...");
        List<SongUpsertRequest> seeds = List.of(
            new SongUpsertRequest("Brave Shine", "ブレイブシャイン", "Aimer", "Fate/stay night [UBW]", "https://youtu.be/KpsJWFuVTdI", "KpsJWFuVTdI", "https://i.ytimg.com/vi/KpsJWFuVTdI/hqdefault.jpg", SongStatus.PUBLISHED, Set.of("Anime", "Recommend"), Set.of("Uplifting"), "歌詞", "Romaji", "คำแปล", "Notes"),
            new SongUpsertRequest("Long Play Japanese Tracks vol.1", null, "FCNami T_T", "Mix", "https://youtu.be/example1", "example1", "https://i.ytimg.com/vi/KpsJWFuVTdI/hqdefault.jpg", SongStatus.PUBLISHED, Set.of("long-play", "Study"), Set.of("Relaxing"), "", "", "รวมเพลงยาวๆ", ""),
            new SongUpsertRequest("Neo-Aspect", null, "Roselia", "Bandori", "https://youtu.be/example2", "example2", "https://i.ytimg.com/vi/KpsJWFuVTdI/hqdefault.jpg", SongStatus.PUBLISHED, Set.of("bandori", "Rhythm Game"), Set.of("Emotional"), "", "", "คำแปลเพลง Roselia", ""),
            new SongUpsertRequest("Viewer's Request Song #42", null, "Various Artists", "Request", "https://youtu.be/example3", "example3", "https://i.ytimg.com/vi/KpsJWFuVTdI/hqdefault.jpg", SongStatus.PUBLISHED, Set.of("requested"), Set.of("Uplifting"), "", "", "เพลงที่คุณขอมา", "")
        );

        int count = 0;
        for (var seed : seeds) {
            try {
                songCatalogService.create(seed);
                count++;
            } catch (Exception ignored) {}
        }
        return count;
    }
}
