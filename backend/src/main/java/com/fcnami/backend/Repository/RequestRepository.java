package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;
import com.fcnami.backend.Model.QueueRequest.RequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request,Long> {

    // หา request ของ user (เรียงลำดับ)
    List<Request> findByUser_IdOrderByCreatedAtAsc(Long userId);

    // หา request ตาม requesterId
    List<Request> findByRequesterId(String requesterId);

    // หา Main queue
    List<Request> findByQueueTypeOrderByRequestOrderAsc(QueueType queueType);
    List<Request> findByQueueTypeAndStatusOrderByRequestOrderAsc(QueueType queueType, RequestStatus status);

    // หา max order (ใช้ generate order ใหม่)
    Optional<Request> findTopByQueueTypeOrderByRequestOrderDesc(QueueType queueType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Request r WHERE r.queueType = :queueType ORDER BY r.requestOrder DESC")
    List<Request> findTopForUpdate(QueueType queueType);

    // หา request ด้วย normalizedKey (กันซ้ำ / ดึงคิว)
    Optional<Request> findByNormalizedKey(String normalizedKey);
    boolean existsByNormalizedKey(String normalizedKey);

    // หา request ที่อยู่ใน depth เดียวกัน
    List<Request> findByDepthLevel(Integer depthLevel);

    // หา request ที่ถูก replace
    List<Request> findByReplacedRequest(Request request);
    List<Request> findByReplacedRequest_Id(Long requestId);

    // หาจาก Status
    List<Request> findByStatus(RequestStatus status);

    // เพิ่ม order ทั้งหมด +1 (ใช้ตอนแทรกหน้าคิว)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Request r
        SET r.requestOrder = r.requestOrder + 1
        WHERE r.queueType = :queueType
    """)
    int incrementOrderForQueue(QueueType queueType);

    // ลด order ทั้งหมด -1 (ใช้ตอนลบ)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Request r
        SET r.requestOrder = r.requestOrder - 1
        WHERE r.queueType = :queueType
        AND r.requestOrder > :order
    """)
    int decrementOrderAfter(QueueType queueType, int order);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT r FROM Request r
    WHERE r.queueType = :queueType
    ORDER BY r.requestOrder ASC
""")
    List<Request> findQueueForUpdate(QueueType queueType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    UPDATE Request r
    SET r.requestOrder = r.requestOrder + 1
    WHERE r.queueType = :queueType
    AND r.requestOrder > :order
""")
    void incrementAfter(QueueType queueType, int order);

    @Query("SELECT MAX(r.requestOrder) FROM Request r WHERE r.queueType = :type")
    Integer findMaxOrder(QueueType type);
}
