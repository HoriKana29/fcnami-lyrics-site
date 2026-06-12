package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.QueueRequest.QueueSnapshot;
import com.fcnami.backend.Model.QueueRequest.QueueType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link QueueSnapshot} entities.
 * Supports capturing and retrieving historic snapshots of queue state.
 */
public interface QueueSnapshotRepository extends JpaRepository<QueueSnapshot, Long> {

    /**
     * Precondition: The batchId parameter must be a non-null string.
     * Postcondition: Returns a list of QueueSnapshots matching the batch ID.
     * Side-effect: None
     */
    List<QueueSnapshot> findByBatchId(String batchId);

    /**
     * Precondition: The batchId parameter must be a non-null string.
     * Postcondition: Returns an Optional containing the latest QueueSnapshot for the specified batch ID based on snapshot time.
     * Side-effect: None
     */
    Optional<QueueSnapshot> findTopByBatchIdOrderBySnapshotTimeDesc(String batchId);

    /**
     * Precondition: The requestId parameter must be a non-null identifier.
     * Postcondition: Returns a list of QueueSnapshots associated with the specified request ID.
     * Side-effect: None
     */
    List<QueueSnapshot> findByRequest_Id(Long requestId);

    /**
     * Precondition: None
     * Postcondition: Returns an Optional containing the latest captured snapshot.
     * Side-effect: None
     */
    Optional<QueueSnapshot> findTopByOrderBySnapshotTimeDesc();

    /**
     * Precondition: Both start and end timestamps must be non-null.
     * Postcondition: Returns a list of QueueSnapshots captured within the specified time range.
     * Side-effect: None
     */
    List<QueueSnapshot> findBySnapshotTimeBetween(
            java.time.LocalDateTime start,
            java.time.LocalDateTime end
    );

    /**
     * Precondition: The queueType parameter must be non-null.
     * Postcondition: Returns a list of QueueSnapshots matching the specified queue type.
     * Side-effect: None
     */
    List<QueueSnapshot> findByQueueType(QueueType queueType);

    /**
     * Precondition: Both queueType and batchId must be non-null.
     * Postcondition: Returns a list of QueueSnapshots matching the queue type and batch ID.
     * Side-effect: None
     */
    List<QueueSnapshot> findByQueueTypeAndBatchId(QueueType queueType, String batchId);
}
