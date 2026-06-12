package com.fcnami.backend.Api;

import com.fcnami.backend.Api.SongDtos.LyricsResponse;
import com.fcnami.backend.Api.SongDtos.SongResponse;
import com.fcnami.backend.Api.SongDtos.SongUpsertRequest;
import com.fcnami.backend.Model.SongTags.SongStatus;
import com.fcnami.backend.Service.SongCatalogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminSongControllerTest {
    @Mock
    private SongCatalogService songCatalogService;

    private LocalValidatorFactoryBean validator;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminSongController(songCatalogService))
                .setControllerAdvice(new ApiExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void shouldCreateSongWithCreatedStatus() throws Exception {
        SongResponse response = response();
        when(songCatalogService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/admin/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("blue-bird"))
                .andExpect(jsonPath("$.title").value("Blue Bird"))
                .andExpect(jsonPath("$.lyrics.thai").value("คำแปลไทย"));

        verify(songCatalogService).create(any());
    }

    @Test
    void shouldRejectInvalidCreatePayload() throws Exception {
        mockMvc.perform(post("/api/admin/songs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("validation_error"))
                .andExpect(jsonPath("$.errorId").isString());

        verify(songCatalogService, never()).create(any());
    }

    @Test
    void shouldUpdateSong() throws Exception {
        when(songCatalogService.update(any(), any())).thenReturn(response());

        mockMvc.perform(put("/api/admin/songs/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(songCatalogService).update(12L, request());
    }

    private SongUpsertRequest request() {
        return new SongUpsertRequest(
                "Blue Bird",
                "ブルーバード",
                "Ikimono Gakari",
                "Naruto Shippuden",
                "https://youtu.be/KpsJWFuVTdI",
                null,
                null,
                SongStatus.PUBLISHED,
                Set.of("anime"),
                Set.of("uplifting"),
                "漢字",
                "romaji",
                "คำแปลไทย",
                "note"
        );
    }

    private String validRequestJson() {
        return """
                {
                  "title": "Blue Bird",
                  "titleJapanese": "ブルーバード",
                  "artist": "Ikimono Gakari",
                  "sourceAnimeOrGame": "Naruto Shippuden",
                  "youtubeUrl": "https://youtu.be/KpsJWFuVTdI",
                  "status": "PUBLISHED",
                  "tags": ["anime"],
                  "moods": ["uplifting"],
                  "kanji": "漢字",
                  "romaji": "romaji",
                  "thai": "คำแปลไทย",
                  "notes": "note"
                }
                """;
    }

    private String invalidRequestJson() {
        return """
                {
                  "title": "",
                  "artist": ""
                }
                """;
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
                new LyricsResponse("漢字", "romaji", "คำแปลไทย", "note", null)
        );
    }
}
