package com.fcnami.backend.Api;

import com.fcnami.backend.Api.QueueDtos.QueueResponse;
import com.fcnami.backend.Service.GoogleSheetQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {
    private final GoogleSheetQueueService queueService;

    @GetMapping
    public QueueResponse list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String tier
    ) {
        return queueService.getQueue(search, status, tier);
    }

    @PostMapping("/sync")
    public QueueResponse sync() {
        return queueService.refresh();
    }
}
