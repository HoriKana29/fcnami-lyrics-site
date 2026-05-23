package com.fcnami.backend.Api;

import com.fcnami.backend.Api.SongDtos.SongResponse;
import com.fcnami.backend.Api.SongDtos.SongUpsertRequest;
import com.fcnami.backend.Service.SongCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// เป็น Controller return JSON
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
    public SongResponse update(@PathVariable Long id //รับ id จาก URL
                               , @Valid @RequestBody SongUpsertRequest request) {
        return songCatalogService.update(id, request);
    }

    // ไม่มี response status ชัดเจน Example 201
}
