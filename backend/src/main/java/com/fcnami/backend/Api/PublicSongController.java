package com.fcnami.backend.Api;

import com.fcnami.backend.Api.SongDtos.SongResponse;
import com.fcnami.backend.Model.SongTags.SongStatus;
import com.fcnami.backend.Service.SongCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
public class PublicSongController {
    private final SongCatalogService songCatalogService;

    @GetMapping
    public Page<SongResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SongStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        return songCatalogService.listPublicSongs(search, status, pageable);
    }

    @GetMapping("/{slug}")
    public SongResponse detail(@PathVariable String slug) {
        return songCatalogService.getBySlug(slug);
    }

    // ควรมี validation param
    // sort default อาจไม่พอ เพิ่ม popularity/desc

}
