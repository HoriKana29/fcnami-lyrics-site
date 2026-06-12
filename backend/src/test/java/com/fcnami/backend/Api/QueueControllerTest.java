package com.fcnami.backend.Api;

import com.fcnami.backend.Api.QueueDtos.QueueItemResponse;
import com.fcnami.backend.Api.QueueDtos.QueueResponse;
import com.fcnami.backend.Service.GoogleSheetQueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QueueControllerTest {
    @Mock
    private GoogleSheetQueueService queueService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new QueueController(queueService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void shouldListQueueWithFilters() throws Exception {
        when(queueService.getQueue("brave", "waiting", "main")).thenReturn(response());

        mockMvc.perform(get("/api/queue")
                        .param("search", "brave")
                        .param("status", "waiting")
                        .param("tier", "main"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].songTitle").value("Brave Shine"));

        verify(queueService).getQueue("brave", "waiting", "main");
    }

    @Test
    void shouldSyncQueue() throws Exception {
        when(queueService.refresh()).thenReturn(response());

        mockMvc.perform(post("/api/queue/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].tier").value("Main Road"));

        verify(queueService).refresh();
    }

    private QueueResponse response() {
        QueueItemResponse item = new QueueItemResponse(
                "Main Road",
                1,
                "Brave Shine",
                "viewer",
                "YouTube",
                "https://youtu.be/example",
                "waiting",
                false,
                "2026-05-25",
                0,
                Map.of("เพลง", "Brave Shine")
        );
        return new QueueResponse(Instant.parse("2026-05-25T00:00:00Z"), 1, List.of(item));
    }
}
