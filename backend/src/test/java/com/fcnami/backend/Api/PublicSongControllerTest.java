package com.fcnami.backend.Api;

import com.fcnami.backend.Api.SongDtos.SongResponse;
import com.fcnami.backend.Model.SongTags.SongStatus;
import com.fcnami.backend.Service.SongCatalogService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PublicSongControllerTest {
    @Mock
    private SongCatalogService songCatalogService;
    @Mock
    private com.fcnami.backend.Service.YouTubeSyncService youTubeSyncService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PublicSongController(songCatalogService, youTubeSyncService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void shouldListSongsWithDefaultCreatedAtSort() throws Exception {
        when(songCatalogService.listPublicSongs(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(response())));

        mockMvc.perform(get("/api/songs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].slug").value("blue-bird"));

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(songCatalogService).listPublicSongs(eq(null), eq(null), pageable.capture());

        Sort.Order order = pageable.getValue().getSort().getOrderFor("createdAt");
        assertEquals(20, pageable.getValue().getPageSize());
        assertTrue(order != null && order.isDescending());
    }

    @Test
    void shouldPassSearchAndStatusFilters() throws Exception {
        when(songCatalogService.listPublicSongs(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/songs")
                        .param("search", "blue")
                        .param("status", "PUBLISHED")
                        .param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(songCatalogService).listPublicSongs(eq("blue"), eq(SongStatus.PUBLISHED), pageable.capture());
        assertEquals(5, pageable.getValue().getPageSize());
    }

    @Test
    void shouldReturnSongDetail() throws Exception {
        when(songCatalogService.getBySlug("blue-bird")).thenReturn(response());

        mockMvc.perform(get("/api/songs/blue-bird"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Blue Bird"));

        verify(songCatalogService).getBySlug("blue-bird");
    }

    @Test
    void shouldReturnNotFoundForMissingSong() throws Exception {
        when(songCatalogService.getBySlug("missing")).thenThrow(new EntityNotFoundException("Song not found: missing"));

        mockMvc.perform(get("/api/songs/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("not_found"));
    }

    private SongResponse response() {
        return new SongResponse(
                1L,
                "blue-bird",
                "Blue Bird",
                "ブルーバード",
                "Ikimono Gakari",
                "Naruto Shippuden",
                "https://youtu.be/KpsJWFuVTdI",
                "KpsJWFuVTdI",
                null,
                SongStatus.PUBLISHED,
                Set.of("anime"),
                Set.of("uplifting"),
                null,
                null,
                null,
                0L,
                null
        );
    }
}
