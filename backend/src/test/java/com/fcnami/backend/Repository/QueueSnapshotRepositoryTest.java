package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.QueueRequest.QueueSnapshot;
import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;
import com.fcnami.backend.Model.User;
import com.fcnami.backend.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class QueueSnapshotRepositoryTest {

    @Autowired
    private QueueSnapshotRepository snapshotRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private UserRepository userRepository;

    // =========================
    // BASIC SAVE + FIND BY BATCH
    // =========================

    @Test
    void shouldFindByBatchId() {

        User user = userRepository.save(TestFactory.createUser());
        Request request = requestRepository.save(TestFactory.createRequest(user));

        QueueSnapshot snapshot =
                snapshotRepository.save(
                        TestFactory.createSnapshot(request, QueueType.MAIN, 1)
                );

        List<QueueSnapshot> result =
                snapshotRepository.findByBatchId(snapshot.getBatchId());

        assertEquals(1, result.size());
    }

    // =========================
    // LATEST SNAPSHOT
    // =========================

    @Test
    void shouldFindLatestSnapshot() {

        User user = userRepository.save(TestFactory.createUser());
        Request request = requestRepository.save(TestFactory.createRequest(user));

        QueueSnapshot s1 =
                snapshotRepository.save(
                        TestFactory.createSnapshot(request, QueueType.MAIN, 1)
                );

        QueueSnapshot s2 =
                snapshotRepository.save(
                        TestFactory.createSnapshot(request, QueueType.MAIN, 2)
                );

        Optional<QueueSnapshot> latest =
                snapshotRepository.findTopByOrderBySnapshotTimeDesc();

        assertTrue(latest.isPresent());
        assertEquals(s2.getId(), latest.get().getId());
    }

    // =========================
    // REQUEST RELATION
    // =========================

    @Test
    void shouldFindByRequestId() {

        User user = userRepository.save(TestFactory.createUser());
        Request request = requestRepository.save(TestFactory.createRequest(user));

        snapshotRepository.save(
                TestFactory.createSnapshot(request, QueueType.MAIN, 1)
        );

        List<QueueSnapshot> result =
                snapshotRepository.findByRequest_Id(request.getId());

        assertEquals(1, result.size());
    }

    // =========================
    // TIME RANGE QUERY
    // =========================

    @Test
    void shouldFindByTimeRange() {

        User user = userRepository.save(TestFactory.createUser());
        Request request = requestRepository.save(TestFactory.createRequest(user));

        QueueSnapshot snapshot =
                snapshotRepository.save(
                        TestFactory.createSnapshot(request, QueueType.MAIN, 1)
                );

        LocalDateTime now = LocalDateTime.now();

        List<QueueSnapshot> result =
                snapshotRepository.findBySnapshotTimeBetween(
                        now.minusMinutes(1),
                        now.plusMinutes(1)
                );

        assertFalse(result.isEmpty());
    }

    // =========================
    // QUEUE TYPE FILTER
    // =========================

    @Test
    void shouldFilterByQueueType() {

        User user = userRepository.save(TestFactory.createUser());
        Request request = requestRepository.save(TestFactory.createRequest(user));

        snapshotRepository.save(
                TestFactory.createSnapshot(request, QueueType.MAIN, 1)
        );

        List<QueueSnapshot> result =
                snapshotRepository.findByQueueType(QueueType.MAIN);

        assertFalse(result.isEmpty());
    }

    // =========================
    // BATCH + TYPE
    // =========================

    @Test
    void shouldFindByQueueTypeAndBatchId() {

        User user = userRepository.save(TestFactory.createUser());
        Request request = requestRepository.save(TestFactory.createRequest(user));

        QueueSnapshot snapshot =
                snapshotRepository.save(
                        TestFactory.createSnapshot(request, QueueType.MAIN, 1)
                );

        List<QueueSnapshot> result =
                snapshotRepository.findByQueueTypeAndBatchId(
                        QueueType.MAIN,
                        snapshot.getBatchId()
                );

        assertEquals(1, result.size());
    }
    @Test
    void shouldReturnOnlySameBatch() {

        User user = userRepository.save(TestFactory.createUser());
        Request request = requestRepository.save(TestFactory.createRequest(user));

        String batchId = TestFactory.createBatchId();

        QueueSnapshot s1 =
                snapshotRepository.save(
                        TestFactory.createSnapshot(request, QueueType.MAIN, 1, batchId)
                );

        QueueSnapshot s2 =
                snapshotRepository.save(
                        TestFactory.createSnapshot(request, QueueType.MAIN, 2, batchId)
                );

        List<QueueSnapshot> result =
                snapshotRepository.findByBatchId(batchId);

        assertEquals(2, result.size());

        assertTrue(
                result.stream().allMatch(s -> s.getBatchId().equals(batchId))
        );
    }

    @Test
    void shouldReturnEmptyWhenBatchNotFound() {

        List<QueueSnapshot> result =
                snapshotRepository.findByBatchId("invalid_batch");

        assertTrue(result.isEmpty());
    }
    @Test
    void shouldNotReturnOutsideTimeRange() {

        User user = userRepository.save(TestFactory.createUser());
        Request request = requestRepository.save(TestFactory.createRequest(user));

        snapshotRepository.save(TestFactory.createSnapshot(request, QueueType.MAIN, 1));

        List<QueueSnapshot> result =
                snapshotRepository.findBySnapshotTimeBetween(
                        LocalDateTime.now().plusHours(1),
                        LocalDateTime.now().plusHours(2)
                );

        assertTrue(result.isEmpty());
    }

}