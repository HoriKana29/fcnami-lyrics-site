package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;
import com.fcnami.backend.Model.QueueRequest.RequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    List<Request> findByUser_IdOrderByCreatedAtAsc(Long userId);

    List<Request> findByRequesterId(String requesterId);

    List<Request> findByQueueTypeOrderByRequestOrderAsc(QueueType queueType);

    List<Request> findByQueueTypeAndStatusOrderByRequestOrderAsc(
            QueueType queueType,
            RequestStatus status
    );

    Optional<Request> findByNormalizedKey(String normalizedKey);

    boolean existsByNormalizedKey(String normalizedKey);

    List<Request> findByDepthLevel(Integer depthLevel);

    List<Request> findByStatus(RequestStatus status);

    List<Request> findByReplacedRequest_Id(Long requestId);

    @Modifying
    @Query("DELETE FROM Request r WHERE r.id = :id")
    int deleteExistingById(@Param("id") Long id);

    Optional<Request> findTopByQueueTypeOrderByRequestOrderAsc(QueueType type);

    @Query("""
        SELECT MIN(r.requestOrder)
        FROM Request r
        WHERE r.queueType = :type
    """)
    Integer findMinOrder(@Param("type") QueueType type);

    @Query("""
        SELECT MAX(r.requestOrder)
        FROM Request r
        WHERE r.queueType = :type
    """)
    Integer findMaxOrder(@Param("type") QueueType type);

    @Modifying
    @Query("""
    UPDATE Request r
    SET r.requestOrder = r.requestOrder + :gap
    WHERE r.queueType = :type
""")
    void shiftAll(@Param("type") QueueType type,
                  @Param("gap") int gap);

    @Modifying
    @Query("""
UPDATE Request r
SET r.requestOrder = r.requestOrder + :temp
WHERE r.queueType = :type
""")
    void shiftToTemp(@Param("type") QueueType type,
                     @Param("temp") int temp);

    @Modifying
    @Query(value = """
UPDATE requests r
JOIN (
    SELECT id,
           ROW_NUMBER() OVER (ORDER BY request_order) * :gap AS new_order
    FROM requests
    WHERE queue_type = :type
) x ON r.id = x.id
SET r.request_order = x.new_order
""", nativeQuery = true)
    void normalize(@Param("type") String type,
                   @Param("gap") int gap);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT r FROM Request r
    WHERE r.queueType = :type
    ORDER BY r.requestOrder ASC
""")
    List<Request> lockQueue(@Param("type") QueueType type);
}
