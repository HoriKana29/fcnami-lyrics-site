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

/**
 * Repository interface for managing {@link Request} entities.
 * Supports operations for request ordering, locking, status updates, and batch shifting.
 */
public interface RequestRepository extends JpaRepository<Request, Long> {

    /**
     * Precondition: The userId parameter must be a non-null identifier.
     * Postcondition: Returns a list of Requests belonging to the specified user, sorted by creation time.
     * Side-effect: None
     */
    List<Request> findByUser_IdOrderByCreatedAtAsc(Long userId);

    /**
     * Precondition: The requesterId parameter must be a non-null string.
     * Postcondition: Returns a list of Requests matching the specified requester identifier.
     * Side-effect: None
     */
    List<Request> findByRequesterId(String requesterId);

    /**
     * Precondition: The queueType parameter must be a non-null QueueType.
     * Postcondition: Returns a list of Requests matching the queue type, sorted by request order ascending.
     * Side-effect: None
     */
    List<Request> findByQueueTypeOrderByRequestOrderAsc(QueueType queueType);

    /**
     * Precondition: The queueType and status parameters must be non-null.
     * Postcondition: Returns a list of Requests in the specified queue with the specified status, ordered by request order ascending.
     * Side-effect: None
     */
    List<Request> findByQueueTypeAndStatusOrderByRequestOrderAsc(
            QueueType queueType,
            RequestStatus status
    );

    /**
     * Precondition: The normalizedKey parameter must be a non-null string.
     * Postcondition: Returns an Optional containing the Request if found, or empty otherwise.
     * Side-effect: None
     */
    Optional<Request> findByNormalizedKey(String normalizedKey);

    /**
     * Precondition: The normalizedKey parameter must be a non-null string.
     * Postcondition: Returns true if a Request with the normalized key exists, false otherwise.
     * Side-effect: None
     */
    boolean existsByNormalizedKey(String normalizedKey);

    /**
     * Precondition: The depthLevel parameter must be non-null.
     * Postcondition: Returns a list of Requests matching the depth level.
     * Side-effect: None
     */
    List<Request> findByDepthLevel(Integer depthLevel);

    /**
     * Precondition: The status parameter must be non-null.
     * Postcondition: Returns a list of Requests matching the status.
     * Side-effect: None
     */
    List<Request> findByStatus(RequestStatus status);

    /**
     * Precondition: The requestId parameter must be a non-null identifier.
     * Postcondition: Returns a list of Requests that were replaced by the specified request ID.
     * Side-effect: None
     */
    List<Request> findByReplacedRequest_Id(Long requestId);

    /**
     * Precondition: The id parameter must be a non-null identifier.
     * Postcondition: Deletes the Request with the given ID. Returns the count of deleted rows.
     * Side-effect: Modifies the requests table by removing a record.
     */
    @Modifying
    @Query("DELETE FROM Request r WHERE r.id = :id")
    int deleteExistingById(@Param("id") Long id);

    /**
     * Precondition: The type parameter must be non-null.
     * Postcondition: Returns the first Request in the queue type ordered by request order.
     * Side-effect: None
     */
    Optional<Request> findTopByQueueTypeOrderByRequestOrderAsc(QueueType type);

    /**
     * Precondition: The type parameter must be non-null.
     * Postcondition: Returns the minimum request order value for the specified queue type.
     * Side-effect: None
     */
    @Query("""
        SELECT MIN(r.requestOrder)
        FROM Request r
        WHERE r.queueType = :type
    """)
    Integer findMinOrder(@Param("type") QueueType type);

    /**
     * Precondition: The type parameter must be non-null.
     * Postcondition: Returns the maximum request order value for the specified queue type.
     * Side-effect: None
     */
    @Query("""
        SELECT MAX(r.requestOrder)
        FROM Request r
        WHERE r.queueType = :type
    """)
    Integer findMaxOrder(@Param("type") QueueType type);

    /**
     * Precondition: The type parameter must be non-null, and gap can be any integer.
     * Postcondition: Shifts the request order of all Requests in the specified queue by the gap amount.
     * Side-effect: Modifies the request_order column in the requests table for the specified queue.
     */
    @Modifying
    @Query("""
    UPDATE Request r
    SET r.requestOrder = r.requestOrder + :gap
    WHERE r.queueType = :type
""")
    void shiftAll(@Param("type") QueueType type,
                  @Param("gap") int gap);

    /**
     * Precondition: The type parameter must be non-null, and temp can be any integer.
     * Postcondition: Shifts the request order of all Requests in the queue type to a temporary range by adding the temp offset.
     * Side-effect: Modifies the request_order column in the requests table for the specified queue.
     */
    @Modifying
    @Query("""
UPDATE Request r
SET r.requestOrder = r.requestOrder + :temp
WHERE r.queueType = :type
""")
    void shiftToTemp(@Param("type") QueueType type,
                     @Param("temp") int temp);

    /**
     * Precondition: The type parameter must be a non-null string corresponding to a valid queue type, and gap must be positive.
     * Postcondition: Normalizes and compacts the ordering of requests in the specified queue using a gap multiplier.
     * Side-effect: Updates request_order column for all matching records in the database.
     */
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

    /**
     * Precondition: The type parameter must be non-null.
     * Postcondition: Locks and returns all Requests of the specified queue type ordered by request order.
     * Side-effect: Acquires a pessimistic write lock on the matching records in the database.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT r FROM Request r
    WHERE r.queueType = :type
    ORDER BY r.requestOrder ASC
""")
    List<Request> lockQueue(@Param("type") QueueType type);
}
