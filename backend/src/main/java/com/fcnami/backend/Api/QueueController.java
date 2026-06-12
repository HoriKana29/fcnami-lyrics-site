package com.fcnami.backend.Api;

import com.fcnami.backend.Api.QueueDtos.QueueResponse;
import com.fcnami.backend.Service.GoogleSheetQueueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * QueueController provides REST endpoints to query and synchronize the translation request queue.
 * This includes endpoints to search the queue and trigger manual synchronization with the external Google Sheet.
 */
@RestController
@RequestMapping("/api/queue")
public class QueueController {
    private final GoogleSheetQueueService queueService;

    /**
     * Constructs a QueueController with the required GoogleSheetQueueService.
     *
     * Precondition: queueService must not be null.
     * Postcondition: A new QueueController instance is successfully created.
     * Side-effect: None.
     *
     * @param queueService the service used to manage and query the translation queue
     */
    public QueueController(GoogleSheetQueueService queueService) {
        this.queueService = queueService;
    }

    /**
     * Retrieves the list of queue items matching the search, status, and tier filters.
     *
     * Precondition: None.
     * Postcondition: Returns the filtered queue details including totals.
     * Side-effect: None.
     *
     * @param search optional search query (matching song title or artist)
     * @param status optional status filter (e.g., in_progress, pending)
     * @param tier optional tier filter (e.g., standard, vip)
     * @return the queue response details
     */
    @GetMapping
    public QueueResponse list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tier
    ) {
        return queueService.getQueue(search, status, tier);
    }

    /**
     * Triggers a manual synchronization of the translation queue with the external Google Sheet.
     *
     * Precondition: The external Google Sheet must be accessible and return valid CSV data.
     * Postcondition: The local queue snapshot and statistics are updated, and the new state is returned.
     * Side-effect: The database is updated with the fetched queue information, overwriting the previous snapshot.
     *
     * @return the synchronized queue details
     */
    @PostMapping("/sync")
    public QueueResponse sync() {
        return queueService.refresh();
    }
}

