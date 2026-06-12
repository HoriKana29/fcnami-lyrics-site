package com.fcnami.backend.Api;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * QueueDtos serves as a namespace container for Data Transfer Objects (DTOs)
 * related to the queue system, preventing name cluttering.
 */
public final class QueueDtos {

    /**
     * Private constructor to prevent instantiation of this utility namespace class.
     *
     * Precondition: None.
     * Postcondition: Throws an AssertionError if instantiation is attempted.
     * Side-effect: None.
     */
    private QueueDtos() {
        throw new AssertionError("No instances of QueueDtos should be created.");
    }

    /**
     * QueueItemResponse represents a single item in the translation queue.
     *
     * @param tier the queue priority tier (e.g., standard, vip)
     * @param queueNumber the order or position number in the queue
     * @param songTitle the title of the song being requested
     * @param requester the name or identifier of the user who made the request
     * @param songLinkLabel optional label for the song's reference link
     * @param songUrl the URL link to the requested song/source
     * @param status the current status of the request (e.g., in_progress, pending)
     * @param translated flag indicating if translation has been completed
     * @param requestDate the date the request was originally made
     * @param waitingDays the number of days the request has been waiting
     * @param rawRow the raw mapped key-value pairs representing the Google Sheet row data
     */
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

    /**
     * QueueResponse represents the complete translation queue state, including metadata.
     *
     * @param syncedAt the timestamp when the local queue was last synced with the external source
     * @param total the total number of items in the queue response
     * @param items the list of queue items
     */
    public record QueueResponse(Instant syncedAt, int total, List<QueueItemResponse> items) {
    }
}

