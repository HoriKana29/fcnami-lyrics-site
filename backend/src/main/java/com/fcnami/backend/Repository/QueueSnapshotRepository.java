package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.QueueRequest.QueueSnapshot;
import com.fcnami.backend.Model.QueueRequest.QueueType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
// ใช้ดึงประวัติคิวในรูปแบบต่างๆ
public interface QueueSnapshotRepository extends JpaRepository<QueueSnapshot, Long> {

    // ดึง Snapshot ทั้งชุด ที่อยู่ Batch เดียวกัน
    List<QueueSnapshot> findByBatchId(String batchId);

    // ดึง snapshot ล่าสุดของ batch นั้น
    // (เอา record ที่ snapshotTime มากสุด)
    Optional<QueueSnapshot> findTopByBatchIdOrderBySnapshotTimeDesc(String batchId);

    // ดึง Snapshot ทั้งหมดของ Request หนึ่ง
    // → ใช้ดู timeline ของ request นั้น
    List<QueueSnapshot> findByRequest_Id(Long requestId);

    // Snapshot ล่าสุดของระบบ
    Optional<QueueSnapshot> findTopByOrderBySnapshotTimeDesc();
    // ดึงตามช่วงเวลา
    List<QueueSnapshot> findBySnapshotTimeBetween(
            java.time.LocalDateTime start,
            java.time.LocalDateTime end
    );

    // Queue Filter
    List<QueueSnapshot> findByQueueType(QueueType queueType);
    List<QueueSnapshot> findByQueueTypeAndBatchId(QueueType queueType, String batchId);
}
