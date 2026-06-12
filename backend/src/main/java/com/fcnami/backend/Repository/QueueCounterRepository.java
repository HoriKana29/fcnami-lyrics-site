package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.QueueRequest.QueueCounter;
import com.fcnami.backend.Model.QueueRequest.QueueType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for managing {@link QueueCounter} entities.
 * Used for maintaining thread-safe counters with database pessimistic locking.
 */
public interface QueueCounterRepository extends JpaRepository<QueueCounter, QueueType> {

    /**
     * Precondition: The type parameter must be a non-null QueueType enum.
     * Postcondition: Returns the QueueCounter for the given queue type.
     * Side-effect: Acquires a pessimistic write lock on the matching row to block concurrent modifications.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM QueueCounter q WHERE q.queueType = :type")
    QueueCounter findForUpdate(@Param("type") QueueType type);
}
