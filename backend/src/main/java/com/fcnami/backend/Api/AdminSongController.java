package com.fcnami.backend.Api;

import com.fcnami.backend.Api.SongDtos.SongResponse;
import com.fcnami.backend.Api.SongDtos.SongUpsertRequest;
import com.fcnami.backend.Service.SongCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/songs")
@RequiredArgsConstructor
public class AdminSongController {
    private final SongCatalogService songCatalogService;

    @PostMapping
    public SongResponse create(@Valid @RequestBody SongUpsertRequest request) {
        return songCatalogService.create(request);
    }

    @PutMapping("/{id}")
    public SongResponse update(@PathVariable Long id, @Valid @RequestBody SongUpsertRequest request) {
        return songCatalogService.update(id, request);
    }
}
