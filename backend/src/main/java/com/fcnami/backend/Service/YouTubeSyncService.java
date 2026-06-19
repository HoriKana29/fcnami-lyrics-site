package com.fcnami.backend.Service;

import com.fcnami.backend.Api.SongDtos;
import com.fcnami.backend.Api.SongDtos.SongUpsertRequest;
import com.fcnami.backend.Model.SongTags.SongStatus;
import com.fcnami.backend.Repository.SongRepository;
import com.fcnami.backend.Support.SongTitleParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeSyncService {

    private static final int MAX_RESULTS = 50;
    private static final String YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3";

    private final RestClient.Builder restClientBuilder;
    private final SongCatalogService songCatalogService;
    private final SongRepository songRepository;

    @Value("${fcnami.youtube.api-key:}")
    private String apiKey;

    @Value("${fcnami.youtube.channel-id:UC8P0dc0Zn2gf8L6tJi_k6xg}")
    private String channelId;

    @Value("${fcnami.youtube.playlist-id-all:PL4wsZBSs9fgM30xS51w471hbiMvu6_HlW}")
    private String playlistIdAll;

    @Value("${fcnami.youtube.playlist-id-recommend:PL4wsZBSs9fgMUEGPldykTC5o-dvh561X8}")
    private String playlistIdRecommend;

    @Value("${fcnami.youtube.playlist-id-long-play:PL4wsZBSs9fgNl_NpEHpUbX2_zFyDHCyIR}")
    private String playlistIdLongPlay;

    @Value("${fcnami.youtube.playlist-id-bandori:PL4wsZBSs9fgPcrdcgxv_0QgcebGyA2Bms}")
    private String playlistIdBandori;

    @Value("${fcnami.youtube.playlist-id-requested:PL4wsZBSs9fgNUrzogGpFb7bhwzmwnqe-O}")
    private String playlistIdRequested;

    private Map<String, List<SongDtos.SongResponse>> directFetchCacheMap = new HashMap<>();
    private Map<String, LocalDateTime> lastFetchTimeMap = new HashMap<>();
    private static final int CACHE_MINUTES = 5;

    public List<SongDtos.SongResponse> fetchDirectFromYouTube(String playlistId, boolean refresh) {
        String targetPlaylistId = playlistId != null ? playlistId : playlistIdAll;
        
        if (!refresh && directFetchCacheMap.containsKey(targetPlaylistId) && lastFetchTimeMap.containsKey(targetPlaylistId) && 
            lastFetchTimeMap.get(targetPlaylistId).plusMinutes(CACHE_MINUTES).isAfter(LocalDateTime.now())) {
            var cache = directFetchCacheMap.get(targetPlaylistId);
            log.info("Returning cached YouTube playlist: {} ({} items)", targetPlaylistId, cache.size());
            return cache;
        }

        if (refresh) {
            log.info("Forced refresh requested for playlist: {}. Bypassing cache.", targetPlaylistId);
        }

        if (apiKey == null || apiKey.isBlank()) {
            log.error("YouTube API key is missing. Cannot fetch direct data.");
            throw new IllegalStateException("YouTube API key is not configured.");
        }

        List<SongDtos.SongResponse> results = new ArrayList<>();
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(YOUTUBE_API_BASE_URL)
                    .build();

            log.info("Directly fetching YouTube playlist: {} using API Key: {}...", targetPlaylistId, 
                    (apiKey != null && apiKey.length() > 5) ? apiKey.substring(0, 5) + "..." : "MISSING");
            
            String nextPageToken = null;
            long mockId = 0;

            do {
                final String currentToken = nextPageToken;
                final String finalTargetPlaylistId = targetPlaylistId;
                log.info("Requesting playlistItems for page token: {}", currentToken);
                
                Map playlistResponse = restClient.get()
                        .uri(uriBuilder -> {
                            var builder = uriBuilder
                                .path("/playlistItems")
                                .queryParam("part", "snippet,contentDetails")
                                .queryParam("playlistId", finalTargetPlaylistId)
                                .queryParam("maxResults", MAX_RESULTS)
                                .queryParam("key", apiKey);
                            if (currentToken != null) {
                                builder.queryParam("pageToken", currentToken);
                            }
                            return builder.build();
                        })
                        .retrieve()
                        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (request, response) -> {
                            byte[] body = response.getBody().readAllBytes();
                            String errorMsg = new String(body);
                            log.error("YouTube API error: {} {} - Body: {}", response.getStatusCode(), response.getStatusText(), errorMsg);
                            throw new RuntimeException("YouTube API returned " + response.getStatusCode() + " " + response.getStatusText() + ": " + errorMsg);
                        })
                        .body(Map.class);

                if (playlistResponse == null) {
                    log.error("YouTube API returned null response for playlistId: {}", targetPlaylistId);
                    break;
                }

                log.info("API Response received. Keys: {}", playlistResponse.keySet());

                List playlistItems = (List) playlistResponse.get("items");
                if (playlistItems == null) {
                    log.warn("No 'items' key found in API response for token: {}", currentToken);
                    break;
                }

                log.info("Fetched {} items from YouTube API page", playlistItems.size());

                // Fetch view counts for the batch
                Map<String, Long> viewCounts = fetchViewCounts(playlistItems, restClient);

                for (Object itemObj : playlistItems) {
                    Map item = (Map) itemObj;
                    Map snippet = (Map) item.get("snippet");
                    if (snippet == null) continue;

                    String rawTitle = (String) snippet.get("title");
                    String videoId = null;
                    if (snippet.containsKey("resourceId")) {
                        Map resourceId = (Map) snippet.get("resourceId");
                        if (resourceId != null) videoId = (String) resourceId.get("videoId");
                    }
                    if (videoId == null) continue;

                    String thumbnailUrl = null;
                    if (snippet.containsKey("thumbnails")) {
                        Map thumbnails = (Map) snippet.get("thumbnails");
                        if (thumbnails != null && thumbnails.containsKey("high")) {
                            thumbnailUrl = (String) ((Map) thumbnails.get("high")).get("url");
                        }
                    }

                    LocalDateTime publishedAt = null;
                    String publishedAtStr = (String) snippet.get("publishedAt");
                    if (publishedAtStr != null) {
                        try {
                            publishedAt = OffsetDateTime.parse(publishedAtStr).toLocalDateTime();
                        } catch (Exception ignored) {}
                    }

                    Set<String> tags = new HashSet<>();
                    if (targetPlaylistId.equals(playlistIdRecommend)) {
                        tags.add("recommend");
                    } else if (targetPlaylistId.equals(playlistIdLongPlay)) {
                        tags.add("long-play");
                    } else if (targetPlaylistId.equals(playlistIdBandori)) {
                        tags.add("bandori");
                    } else if (targetPlaylistId.equals(playlistIdRequested)) {
                        tags.add("requested");
                    }
                    if (rawTitle != null) {
                        if (rawTitle.toLowerCase().contains("bandori")) tags.add("bandori");
                        if (rawTitle.toLowerCase().contains("request")) tags.add("requested");
                        if (rawTitle.toLowerCase().contains("long play") || rawTitle.toLowerCase().contains("long track")) tags.add("long-play");
                    }

                    SongTitleParser.ParsedTitle parsed = SongTitleParser.parse(rawTitle);

                    results.add(new SongDtos.SongResponse(
                            ++mockId, 
                            "yt-" + videoId, 
                            parsed.getTitle(), 
                            null, 
                            parsed.getArtist(),
                            parsed.getSource(),
                            "https://youtu.be/" + videoId,
                            videoId,
                            thumbnailUrl,
                            SongStatus.PUBLISHED,
                            tags,
                            Collections.emptySet(),
                            LocalDateTime.now(),
                            LocalDateTime.now(),
                            publishedAt,
                            viewCounts.getOrDefault(videoId, 0L),
                            new SongDtos.LyricsResponse(null, null, null, null, null)
                    ));
                }

                nextPageToken = (String) playlistResponse.get("nextPageToken");
            } while (nextPageToken != null);

            log.info("Direct fetch complete. Total items: {}", results.size());
            
            // Update cache
            this.directFetchCacheMap.put(targetPlaylistId, results);
            this.lastFetchTimeMap.put(targetPlaylistId, LocalDateTime.now());
            
            return results;
        } catch (Exception e) {
            log.error("Critical failure during direct YouTube fetch: {}", e.getMessage(), e);
            throw e; // Rethrow to let the controller handle it
        }
    }

    /**
     * Syncs videos from a specific YouTube playlist.
     * Precondition: apiKey must be valid for real sync.
     * Postcondition: Songs are updated/created in the database.
     */
    public int syncChannelVideos() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("YouTube API key is missing. Skipping real sync.");
            return seedMockData();
        }

        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(YOUTUBE_API_BASE_URL)
                    .build();

            log.info("Starting YouTube sync for playlist: {} using provided API key", playlistIdAll);
            
            // Fetch all videos from playlist with pagination
            int totalSynced = 0;
            String nextPageToken = null;

            do {
                final String currentToken = nextPageToken;
                Map playlistResponse = restClient.get()
                        .uri(uriBuilder -> {
                            uriBuilder
                                .path("/playlistItems")
                                .queryParam("part", "snippet,contentDetails")
                                .queryParam("playlistId", playlistIdAll)
                                .queryParam("maxResults", MAX_RESULTS)
                                .queryParam("key", apiKey);
                            if (currentToken != null) {
                                uriBuilder.queryParam("pageToken", currentToken);
                            }
                            return uriBuilder.build();
                        })
                        .retrieve()
                        .body(Map.class);

                if (playlistResponse == null) break;

                List playlistItems = (List) playlistResponse.get("items");
                if (playlistItems == null) {
                    log.warn("No items found in playlist response for token: {}", currentToken);
                    break;
                }

                // Fetch view counts for the batch
                Map<String, Long> viewCounts = fetchViewCounts(playlistItems, restClient);

                log.info("Processing {} items from playlist page (token: {})", playlistItems.size(), currentToken);

                int pageCount = 0;
                for (Object itemObj : playlistItems) {
                    pageCount++;
                    Map item = (Map) itemObj;
                    Map snippet = (Map) item.get("snippet");
                    if (snippet == null) {
                        log.warn("Item {} has no snippet, skipping", pageCount);
                        continue;
                    }

                    String rawTitle = (String) snippet.get("title");
                    
                    String videoId = null;
                    if (snippet.containsKey("resourceId")) {
                        Map resourceId = (Map) snippet.get("resourceId");
                        if (resourceId != null) {
                            videoId = (String) resourceId.get("videoId");
                        }
                    }

                    if (videoId == null) {
                        log.warn("Item {} ('{}') has no valid videoId in resourceId, skipping", pageCount, rawTitle);
                        continue;
                    }

                    String thumbnailUrl = null;
                    if (snippet.containsKey("thumbnails")) {
                        Map thumbnails = (Map) snippet.get("thumbnails");
                        if (thumbnails != null && thumbnails.containsKey("high")) {
                            thumbnailUrl = (String) ((Map) thumbnails.get("high")).get("url");
                        }
                    }

                    // Use the FULL title as requested to prevent truncation
                    String songTitle = rawTitle;
                    
                    SongTitleParser.ParsedTitle parsed = SongTitleParser.parse(rawTitle);
                    String artist = parsed.getArtist();

                    LocalDateTime publishedAt = null;
                    String publishedAtStr = (String) snippet.get("publishedAt");
                    if (publishedAtStr != null) {
                        try {
                            publishedAt = OffsetDateTime.parse(publishedAtStr).toLocalDateTime();
                        } catch (Exception e) {
                            log.warn("Failed to parse publishedAt for '{}' (ID: {}): {}", rawTitle, videoId, publishedAtStr);
                        }
                    }

                    Set<String> tags = new HashSet<>();
                    if (rawTitle != null) {
                        if (rawTitle.toLowerCase().contains("bandori")) tags.add("bandori");
                        if (rawTitle.toLowerCase().contains("request")) tags.add("requested");
                        if (rawTitle.toLowerCase().contains("long play") || rawTitle.toLowerCase().contains("long track")) tags.add("long-play");
                    }
                    
                    log.info("Syncing item {}: {} ({})", pageCount, songTitle, videoId);
                    
                    if (upsertFromYouTube(videoId, songTitle, artist, thumbnailUrl, tags, publishedAt, viewCounts.getOrDefault(videoId, 0L), parsed.getSource())) {
                        totalSynced++;
                    } else {
                        log.error("Failed to upsert video: {} ({})", songTitle, videoId);
                    }
                }

                nextPageToken = (String) playlistResponse.get("nextPageToken");
                if (nextPageToken != null) {
                    log.info("Page complete. Total synced so far: {}. Moving to next page...", totalSynced);
                }
            } while (nextPageToken != null);

            log.info("YouTube sync completed. Total songs processed/synced: {}", totalSynced);
            return totalSynced;
        } catch (Exception e) {
            log.error("Failed to sync YouTube videos", e);
            throw new RuntimeException("YouTube sync failed: " + e.getMessage(), e);
        }
    }

    private boolean upsertFromYouTube(String videoId, String title, String artist, String thumbnailUrl, Set<String> tags, LocalDateTime publishedAt, Long viewCount, String source) {
        var existing = songRepository.findByYoutubeVideoId(videoId);
        
        SongUpsertRequest request = new SongUpsertRequest(
                title, null, artist, source != null ? source : "YouTube",
                "https://youtu.be/" + videoId,
                videoId, thumbnailUrl, SongStatus.PUBLISHED,
                tags, Collections.emptySet(),
                "", "", "", "", publishedAt, viewCount
        );

        try {
            if (existing.isPresent()) {
                songCatalogService.update(existing.get().getId(), request);
            } else {
                songCatalogService.create(request);
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to upsert song from YouTube video: {}", title, e);
            return false;
        }
    }

    private Map<String, Long> fetchViewCounts(List playlistItems, RestClient restClient) {
        List<String> videoIds = new ArrayList<>();
        for (Object itemObj : playlistItems) {
            Map item = (Map) itemObj;
            Map snippet = (Map) item.get("snippet");
            if (snippet != null && snippet.containsKey("resourceId")) {
                Map resourceId = (Map) snippet.get("resourceId");
                if (resourceId != null) {
                    String id = (String) resourceId.get("videoId");
                    if (id != null) videoIds.add(id);
                }
            }
        }

        if (videoIds.isEmpty()) return Collections.emptyMap();

        try {
            Map response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/videos")
                            .queryParam("part", "statistics")
                            .queryParam("id", String.join(",", videoIds))
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("items")) return Collections.emptyMap();

            Map<String, Long> viewCounts = new HashMap<>();
            List items = (List) response.get("items");
            for (Object itemObj : items) {
                Map item = (Map) itemObj;
                String id = (String) item.get("id");
                Map statistics = (Map) item.get("statistics");
                if (statistics != null && statistics.containsKey("viewCount")) {
                    viewCounts.put(id, Long.parseLong((String) statistics.get("viewCount")));
                }
            }
            return viewCounts;
        } catch (Exception e) {
            log.warn("Failed to fetch view counts for batch: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private int seedMockData() {
        log.info("Seeding mock data from real FCNami channel videos...");
        List<SongUpsertRequest> seeds = List.of(
            new SongUpsertRequest("Brave Shine", "ブレイブシャイン", "Aimer", "Fate/stay night [UBW]", "https://youtu.be/KpsJWFuVTdI", "KpsJWFuVTdI", "https://i.ytimg.com/vi/KpsJWFuVTdI/hqdefault.jpg", SongStatus.PUBLISHED, Set.of("Anime", "Recommend"), Set.of("Uplifting"), "歌詞", "Romaji", "คำแปล", "Notes", LocalDateTime.now(), 1500000L),
            new SongUpsertRequest("Long Play Japanese Tracks vol.1", null, "FCNami T_T", "Mix", "https://youtu.be/example1", "example1", "https://i.ytimg.com/vi/KpsJWFuVTdI/hqdefault.jpg", SongStatus.PUBLISHED, Set.of("long-play", "Study"), Set.of("Relaxing"), "", "", "รวมเพลงยาวๆ", "", LocalDateTime.now(), 500000L),
            new SongUpsertRequest("Neo-Aspect", null, "Roselia", "Bandori", "https://youtu.be/example2", "example2", "https://i.ytimg.com/vi/KpsJWFuVTdI/hqdefault.jpg", SongStatus.PUBLISHED, Set.of("bandori", "Rhythm Game"), Set.of("Emotional"), "", "", "คำแปลเพลง Roselia", "", LocalDateTime.now(), 750000L),
            new SongUpsertRequest("Viewer's Request Song #42", null, "Various Artists", "Request", "https://youtu.be/example3", "example3", "https://i.ytimg.com/vi/KpsJWFuVTdI/hqdefault.jpg", SongStatus.PUBLISHED, Set.of("requested"), Set.of("Uplifting"), "", "", "เพลงที่คุณขอมา", "", LocalDateTime.now(), 120000L)
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

    public SongDtos.SongResponse fetchSingleVideoDirect(String videoId) {
        // First check in cache map
        for (List<SongDtos.SongResponse> cachedList : directFetchCacheMap.values()) {
            for (SongDtos.SongResponse song : cachedList) {
                if (song.youtubeVideoId().equals(videoId)) {
                    log.info("Returning cached single video details for videoId: {}", videoId);
                    return song;
                }
            }
        }

        // If not in cache, fetch from YouTube API
        if (apiKey == null || apiKey.isBlank()) {
            log.error("YouTube API key is missing. Cannot fetch single video details.");
            throw new IllegalStateException("YouTube API key is not configured.");
        }

        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(YOUTUBE_API_BASE_URL)
                    .build();

            log.info("Fetching single video details from YouTube API for videoId: {}...", videoId);

            Map response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/videos")
                            .queryParam("part", "snippet,statistics")
                            .queryParam("id", videoId)
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (response == null || !response.containsKey("items")) {
                throw new NoSuchElementException("Video not found on YouTube: " + videoId);
            }

            List items = (List) response.get("items");
            if (items == null || items.isEmpty()) {
                throw new NoSuchElementException("Video not found on YouTube: " + videoId);
            }

            Map item = (Map) items.get(0);
            Map snippet = (Map) item.get("snippet");
            Map statistics = (Map) item.get("statistics");

            String rawTitle = snippet != null ? (String) snippet.get("title") : "Unknown Title";
            String thumbnailUrl = null;
            if (snippet != null && snippet.containsKey("thumbnails")) {
                Map thumbnails = (Map) snippet.get("thumbnails");
                if (thumbnails != null && thumbnails.containsKey("high")) {
                    thumbnailUrl = (String) ((Map) thumbnails.get("high")).get("url");
                }
            }

            LocalDateTime publishedAt = null;
            if (snippet != null) {
                String publishedAtStr = (String) snippet.get("publishedAt");
                if (publishedAtStr != null) {
                    try {
                        publishedAt = OffsetDateTime.parse(publishedAtStr).toLocalDateTime();
                    } catch (Exception ignored) {}
                }
            }

            long viewCount = 0;
            if (statistics != null && statistics.containsKey("viewCount")) {
                viewCount = Long.parseLong((String) statistics.get("viewCount"));
            }

            SongTitleParser.ParsedTitle parsed = SongTitleParser.parse(rawTitle);

            // Create tag set based on title heuristics
            Set<String> tags = new HashSet<>();
            if (rawTitle != null) {
                if (rawTitle.toLowerCase().contains("bandori")) tags.add("bandori");
                if (rawTitle.toLowerCase().contains("request")) tags.add("requested");
                if (rawTitle.toLowerCase().contains("long play") || rawTitle.toLowerCase().contains("long track")) tags.add("long-play");
            }

            return new SongDtos.SongResponse(
                    0L, 
                    "yt-" + videoId, 
                    parsed.getTitle(), 
                    null, 
                    parsed.getArtist(),
                    parsed.getSource(),
                    "https://youtu.be/" + videoId,
                    videoId,
                    thumbnailUrl,
                    SongStatus.PUBLISHED,
                    tags,
                    Collections.emptySet(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    publishedAt,
                    viewCount,
                    new SongDtos.LyricsResponse(null, null, null, null, null)
            );
        } catch (Exception e) {
            log.error("Failed to fetch single video direct: {}", e.getMessage(), e);
            throw new NoSuchElementException("Failed to retrieve video details from YouTube: " + e.getMessage());
        }
    }
}

