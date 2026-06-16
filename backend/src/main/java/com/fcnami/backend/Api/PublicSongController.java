package com.fcnami.backend.Api;

import com.fcnami.backend.Api.SongDtos.PageResponse;
import com.fcnami.backend.Api.SongDtos.SongResponse;
import com.fcnami.backend.Model.SongTags.SongStatus;
import com.fcnami.backend.Service.SongCatalogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * PublicSongController provides public REST endpoints for searching and retrieving song information.
 * All endpoints here are accessible to any viewer without administrative privileges.
 */
@RestController
@RequestMapping("/api/songs")
public class PublicSongController {

    /**
     * The default page size for paginated song listings.
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    private final SongCatalogService songCatalogService;
    private final com.fcnami.backend.Service.YouTubeSyncService youTubeSyncService;

    /**
     * Constructs a PublicSongController with the required services.
     *
     * Precondition: services must not be null.
     * Postcondition: A new PublicSongController instance is successfully created.
     * Side-effect: None.
     */
    public PublicSongController(SongCatalogService songCatalogService, com.fcnami.backend.Service.YouTubeSyncService youTubeSyncService) {
        this.songCatalogService = songCatalogService;
        this.youTubeSyncService = youTubeSyncService;
    }

    /**
     * Lists songs directly from YouTube API, bypassing the local database.
     * Precondition: None.
     * Postcondition: Returns the current state of the YouTube playlist.
     */
    @GetMapping("/youtube-direct")
    public ResponseEntity<?> listDirect(
            @RequestParam(required = false) String playlistId,
            @RequestParam(defaultValue = "false") boolean refresh) {
        try {
            List<SongResponse> results = youTubeSyncService.fetchDirectFromYouTube(playlistId, refresh);
            return ResponseEntity.ok(results);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Configuration Error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "YouTube Sync Failed", "message", e.getMessage()));
        }
    }

    /**
     * Lists and searches public songs with pagination and filtering.
     *
     * Precondition: pageable must be a valid pagination configuration.
     * Postcondition: Returns a paginated list of songs matching the search criteria and status.
     * Side-effect: None.
     *
     * @param search optional search query (matching song title or artist)
     * @param status optional song status filter
     * @param pageable the pageable information for database querying
     * @return a page response of song details
     */
    @GetMapping
    public PageResponse<SongResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SongStatus status,
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<SongResponse> songs = songCatalogService.listPublicSongs(search, status, pageable);
        return new PageResponse<>(
                songs.getContent(),
                songs.getTotalElements(),
                songs.getTotalPages(),
                songs.getNumber(),
                songs.getSize(),
                songs.isFirst(),
                songs.isLast()
        );
    }

    /**
     * Retrieves the details of a specific public song by its URL slug.
     *
     * Precondition: slug must not be null or empty, and must correspond to an existing song.
     * Postcondition: The requested song details are returned.
     * Side-effect: None.
     *
     * @param slug the unique URL-friendly identifier of the song
     * @return the song details
     */
    @GetMapping("/{slug}")
    public SongResponse detail(@PathVariable String slug) {
        return songCatalogService.getBySlug(slug);
    }
}

