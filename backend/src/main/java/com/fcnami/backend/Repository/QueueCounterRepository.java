package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.QueueRequest.QueueCounter;
import com.fcnami.backend.Model.QueueRequest.QueueType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// Non-null type argument is expected
public interface QueueCounterRepository extends JpaRepository<QueueCounter, QueueType> {

    // ดึง QueueCounter พร้อม Lock แถวไว้
    // PESSIMISTIC_WRITE = ห้าม transaction อื่นมาอ่าน/เขียนแถวนี้จนกว่าจะ commit
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM QueueCounter q WHERE q.queueType = :type")
    QueueCounter findForUpdate(@Param("type") QueueType type);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT q FROM QueueCounter q WHERE q.queueType = :type
""")
    QueueCounter lockQueue(@Param("type") QueueType type);

    // ทั้งสอง Method นี้ซ้ำกันรึเปล่า ถ้าทำงานซ้ำกันให้ยุบเหลืออันเดียว
}
