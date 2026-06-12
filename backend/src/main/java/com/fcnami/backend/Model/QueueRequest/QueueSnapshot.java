package com.fcnami.backend.Model.QueueRequest;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * QueueSnapshot represents a historical state/snapshot of a request's position in the translation queue
 * at a specific point in time (grouped by a sync batchId).
 */
@Setter
@Getter
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

    /**
     * The default position of a request in the queue snapshot.
     */
    public static final int DEFAULT_POSITION = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueType queueType;

    @Builder.Default
    @Column(nullable = false)
    private Integer position = DEFAULT_POSITION;

    @Column(nullable = false)
    private String batchId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime snapshotTime;

    /**
     * Default constructor for JPA.
     *
     * Precondition: None.
     * Postcondition: A new uninitialized QueueSnapshot instance is created.
     * Side-effect: None.
     */
    public QueueSnapshot() {
    }

    /**
     * Constructs a QueueSnapshot with all fields.
     *
     * Precondition: None.
     * Postcondition: A new QueueSnapshot instance is fully initialized.
     * Side-effect: None.
     *
     * @param id the unique snapshot identifier
     * @param request the request associated with this snapshot
     * @param queueType the queue type of this snapshot
     * @param position the position of the request in the queue
     * @param batchId the synchronization batch identifier
     * @param snapshotTime the timestamp when the snapshot was taken
     */
    public QueueSnapshot(Long id, Request request, QueueType queueType, Integer position, String batchId, LocalDateTime snapshotTime) {
        this.id = id;
        this.request = request;
        this.queueType = queueType;
        this.position = position;
        this.batchId = batchId;
        this.snapshotTime = snapshotTime;
    }

    /**
     * Entity lifecycle callback executed before the record is persisted.
     *
     * Precondition: The entity is about to be saved for the first time.
     * Postcondition: The snapshotTime field is populated with the current date and time.
     * Side-effect: None.
     */
    @PrePersist
    protected void onCreate() {
        snapshotTime = LocalDateTime.now();
    }
}

