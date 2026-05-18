package com.fcnami.backend.Model.QueueRequest;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "queue_snapshots",
        indexes = {
                @Index(name = "idx_snapshot_batch", columnList = "batchId"),
                @Index(name = "idx_snapshot_request", columnList = "request_id"),
                @Index(name = "idx_snapshot_time", columnList = "snapshotTime"),
                @Index(name = "idx_batch_time", columnList = "batchId, snapshotTime"),
                @Index(name = "idx_batch_queue", columnList = "batchId, queueType")
        })
public class QueueSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // relation
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    // snapshot value
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueType queueType;

    @Column(nullable = false)
    private Integer position = 0;

    // group snapshot
    @Column(nullable = false)
    private String batchId;

    // time
    @Column(nullable = false, updatable = false)
    private LocalDateTime snapshotTime;

    @PrePersist
    protected void onCreate() {
        snapshotTime = LocalDateTime.now();
    }
}
