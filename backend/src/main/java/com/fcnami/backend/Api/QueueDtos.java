package com.fcnami.backend.Api;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class QueueDtos {
    private QueueDtos() {
    }

    public record QueueItemResponse(
            String tier,
            Integer queueNumber,
            String songTitle,
            String requester,
            String songLinkLabel,
            String songUrl,
            String status,
            Boolean translated,
            String requestDate,
            Integer waitingDays,
            Map<String, String> rawRow
    ) {
    }

    public record QueueResponse(Instant syncedAt, int total, List<QueueItemResponse> items) {
    }
}
