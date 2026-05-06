package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;
import com.fcnami.backend.Model.QueueRequest.RequestStatus;
import com.fcnami.backend.Model.User;
import com.fcnami.backend.TestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@DataJpaTest
class RequestRepositoryTest {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    // =========================
    // BASIC FIND
    // =========================

    @Test
    @DisplayName("should find by normalized key")
    void shouldFindByNormalizedKey() {
        User user = userRepository.save(TestFactory.createUser());

        Request request = requestRepository.save(TestFactory.createRequest(user));

        Optional<Request> found =
                requestRepository.findByNormalizedKey(request.getNormalizedKey());

        assertTrue(found.isPresent());
    }

    @Test
    void shouldExistsByNormalizedKey() {
        User user = userRepository.save(TestFactory.createUser());

        Request request = requestRepository.save(TestFactory.createRequest(user));

        assertTrue(requestRepository.existsByNormalizedKey(request.getNormalizedKey()));
    }

    // =========================
    // ORDERING (CORE OF OPTION A)
    // =========================

    @Test
    void shouldReturnRequestsOrderedByRequestOrder() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 50));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 10));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 30));

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(10, list.get(0).getRequestOrder());
        assertEquals(30, list.get(1).getRequestOrder());
        assertEquals(50, list.get(2).getRequestOrder());
    }

    // =========================
    // MIN / MAX ORDER (OPTION A CORE)
    // =========================

    @Test
    void shouldFindMinOrder() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 100));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 50));

        Integer min = requestRepository.findMinOrder(QueueType.MAIN);

        assertEquals(50, min);
    }

    @Test
    void shouldFindMaxOrder() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 100));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 200));

        Integer max = requestRepository.findMaxOrder(QueueType.MAIN);

        assertEquals(200, max);
    }

    // =========================
    // QUEUE TYPE ISOLATION
    // =========================

    @Test
    void shouldSeparateQueueTypes() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 10));
        requestRepository.save(TestFactory.createRequest(user, QueueType.RESERVE, 10));

        List<Request> main =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(1, main.size());
        assertEquals(QueueType.MAIN, main.getFirst().getQueueType());
    }

    // =========================
    // STATUS FILTER
    // =========================

    @Test
    void shouldFindByStatus() {
        User user = userRepository.save(TestFactory.createUser());

        Request r1 = TestFactory.createRequest(user);
        Request r2 = TestFactory.createRequest(user);

        r1.setStatus(RequestStatus.WAITING);
        r2.setStatus(RequestStatus.DONE);

        requestRepository.save(r1);
        requestRepository.save(r2);

        List<Request> result =
                requestRepository.findByStatus(RequestStatus.WAITING);

        assertEquals(1, result.size());
        assertEquals(RequestStatus.WAITING, result.getFirst().getStatus());
    }

    // =========================
    // DEPTH LEVEL
    // =========================

    @Test
    void shouldFindByDepthLevel() {
        User user = userRepository.save(TestFactory.createUser());

        Request r1 = TestFactory.createRequest(user);
        Request r2 = TestFactory.createRequest(user);

        r1.setDepthLevel(1);
        r2.setDepthLevel(2);

        requestRepository.save(r1);
        requestRepository.save(r2);

        List<Request> result = requestRepository.findByDepthLevel(1);

        assertEquals(1, result.size());
        assertEquals(1, result.getFirst().getDepthLevel());
    }

    // =========================
    // REPLACED REQUEST
    // =========================

    @Test
    void shouldFindByReplacedRequestId() {
        User user = userRepository.save(TestFactory.createUser());

        Request original = requestRepository.save(TestFactory.createRequest(user));
        Request replaced = requestRepository.save(
                TestFactory.createReplacedRequest(user, original)
        );

        List<Request> result =
                requestRepository.findByReplacedRequest_Id(original.getId());

        assertEquals(1, result.size());
        assertEquals(replaced.getId(), result.getFirst().getId());
    }

    // =========================
    // USER RELATION
    // =========================

    @Test
    void shouldFindByUserOrderedByCreatedAt() {
        User user = userRepository.save(TestFactory.createUser());

        Request r1 = requestRepository.save(TestFactory.createRequest(user));
        Request r2 = requestRepository.save(TestFactory.createRequest(user));

        List<Request> list =
                requestRepository.findByUser_IdOrderByCreatedAtAsc(user.getId());

        assertEquals(2, list.size());
        assertEquals(r1.getId(), list.getFirst().getId());
    }

    // =========================
    // UNIQUE KEY
    // =========================

    @Test
    void shouldNotAllowDuplicateNormalizedKey() {
        User user = userRepository.save(TestFactory.createUser());

        String key = "same_key";

        requestRepository.save(TestFactory.createRequestWithKey(user, key));

        assertThrows(DataIntegrityViolationException.class, () ->
                requestRepository.saveAndFlush(
                        TestFactory.createRequestWithKey(user, key)
                )
        );
    }

    // =========================
    // POP BASIC BEHAVIOR
    // =========================

    @Test
    void shouldFindTopByQueueType() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 10));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 5));

        Optional<Request> top =
                requestRepository.findTopByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertTrue(top.isPresent());
        assertEquals(5, top.get().getRequestOrder());
    }

    @Test
    void shouldReturnEmptyWhenQueueEmpty() {
        Optional<Request> result =
                requestRepository.findTopByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertTrue(result.isEmpty());
    }

    // EMPTY EDGE CASES (MIN / MAX)

    @Test
    void shouldReturnNullWhenFindMinOrderOnEmptyQueue() {
        Integer min = requestRepository.findMinOrder(QueueType.MAIN);
        assertNull(min);
    }

    @Test
    void shouldReturnNullWhenFindMaxOrderOnEmptyQueue() {
        Integer max = requestRepository.findMaxOrder(QueueType.MAIN);
        assertNull(max);
    }

    // UPDATE / STATE TRANSITION
    @Test
    void shouldUpdateStatusSuccessfully() {
        User user = userRepository.save(TestFactory.createUser());

        Request r = requestRepository.save(TestFactory.createRequest(user));

        r.setStatus(RequestStatus.DONE);
        requestRepository.saveAndFlush(r);

        Request found = requestRepository.findById(r.getId()).orElseThrow();

        assertEquals(RequestStatus.DONE, found.getStatus());
    }
    @Test
    void shouldUpdateDepthLevelSuccessfully() {
        User user = userRepository.save(TestFactory.createUser());

        Request r = requestRepository.save(TestFactory.createRequest(user));

        r.setDepthLevel(5);
        requestRepository.saveAndFlush(r);

        Request found = requestRepository.findById(r.getId()).orElseThrow();

        assertEquals(5, found.getDepthLevel());
    }

    // REPLACE CHAIN CONSISTENCY

    @Test
    void shouldMaintainReplaceRelationshipChain() {
        User user = userRepository.save(TestFactory.createUser());

        Request original = requestRepository.save(TestFactory.createRequest(user));

        Request replaced1 = requestRepository.save(
                TestFactory.createReplacedRequest(user, original)
        );

        Request replaced2 = requestRepository.save(
                TestFactory.createReplacedRequest(user, replaced1)
        );

        List<Request> chain =
                requestRepository.findByReplacedRequest_Id(original.getId());

        assertEquals(1, chain.size());
        assertEquals(replaced1.getId(), chain.getFirst().getId());
    }
}