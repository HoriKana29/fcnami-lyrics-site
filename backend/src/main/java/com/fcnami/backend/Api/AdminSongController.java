package com.fcnami.backend.Api;

import com.fcnami.backend.Service.YouTubeSyncService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AdminSongController provides administrative REST endpoints for managing songs.
 * This includes endpoints for creating and updating song information in the catalog.
 */
@RestController
@RequestMapping("/api/admin/songs")
@RequiredArgsConstructor
public class AdminSongController {
    private final SongCatalogService songCatalogService;
    private final YouTubeSyncService youTubeSyncService;

    /**
     * Creates a new song in the catalog.
     * ...
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SongResponse create(@Valid @RequestBody SongUpsertRequest request) {
        return songCatalogService.create(request);
    }

    /**
     * Updates an existing song in the catalog by its unique identifier.
     * ...
     */
    @PutMapping("/{id}")
    public SongResponse update(@PathVariable Long id, @Valid @RequestBody SongUpsertRequest request) {
        return songCatalogService.update(id, request);
    }

    /**
     * Syncs songs from the YouTube channel.
     *
     * Precondition: System must be authorized to access YouTube API if key provided.
     * Postcondition: Returns the count of synced/updated videos.
     * Side-effect: Updates the song catalog database.
     */
    @PostMapping("/sync")
    public Map<String, Object> sync() {
        int count = youTubeSyncService.syncChannelVideos();
        return Map.of("syncedCount", count, "message", "YouTube sync completed successfully");
    }
}


