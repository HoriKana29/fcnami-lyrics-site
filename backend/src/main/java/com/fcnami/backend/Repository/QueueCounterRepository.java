package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.QueueRequest.QueueCounter;
import com.fcnami.backend.Model.QueueRequest.QueueType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QueueCounterRepository extends JpaRepository<QueueCounter, QueueType> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM QueueCounter q WHERE q.queueType = :type")
    QueueCounter findForUpdate(@Param("type") QueueType type);
}
