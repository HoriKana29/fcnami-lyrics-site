package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.QueueRequest.QueueSnapshot;
import com.fcnami.backend.Model.QueueRequest.QueueType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QueueSnapshotRepository extends JpaRepository<QueueSnapshot, Long> {

    List<QueueSnapshot> findByBatchId(String batchId);

    Optional<QueueSnapshot> findTopByBatchIdOrderBySnapshotTimeDesc(String batchId);

    List<QueueSnapshot> findByRequest_Id(Long requestId);

    Optional<QueueSnapshot> findTopByOrderBySnapshotTimeDesc();

    List<QueueSnapshot> findBySnapshotTimeBetween(
            java.time.LocalDateTime start,
            java.time.LocalDateTime end
    );

    List<QueueSnapshot> findByQueueType(QueueType queueType);
    List<QueueSnapshot> findByQueueTypeAndBatchId(QueueType queueType, String batchId);
}
