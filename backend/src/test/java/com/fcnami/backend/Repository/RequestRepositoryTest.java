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

    @Test
    @DisplayName("should save and find request by normalizedKey")
    void shouldFindByNormalizedKey() {
        User user = userRepository.save(TestFactory.createUser());

        Request request = TestFactory.createRequest(user);
        requestRepository.save(request);

        Optional<Request> found =
                requestRepository.findByNormalizedKey(request.getNormalizedKey());

        assertTrue(found.isPresent());
    }

    @Test
    void shouldCheckExistsByNormalizedKey() {
        User user = userRepository.save(TestFactory.createUser());

        Request request = TestFactory.createRequest(user);
        requestRepository.save(request);

        assertTrue(requestRepository.existsByNormalizedKey(request.getNormalizedKey()));
    }

    @Test
    void shouldReturnRequestsOrderedByRequestOrder() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 2));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 1));

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(1, list.get(0).getRequestOrder());
        assertEquals(2, list.get(1).getRequestOrder());
    }

    @Test
    void shouldFindMaxOrder() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 1));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 5));

        Optional<Request> top =
                requestRepository.findTopByQueueTypeOrderByRequestOrderDesc(QueueType.MAIN);

        assertTrue(top.isPresent());
        assertEquals(5, top.get().getRequestOrder());
    }

    // =========================
    // USER RELATION
    // =========================

    @Test
    void shouldFindByUserIdOrderedByCreatedAt() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user));
        requestRepository.save(TestFactory.createRequest(user));

        List<Request> list =
                requestRepository.findByUser_IdOrderByCreatedAtAsc(user.getId());

        assertEquals(2, list.size());
    }

    // =========================
    // REPLACED REQUEST
    // =========================

    @Test
    void shouldFindByReplacedRequest() {
        User user = userRepository.save(TestFactory.createUser());

        Request original = requestRepository.save(TestFactory.createRequest(user));
        Request replaced = requestRepository.save(
                TestFactory.createReplacedRequest(user, original)
        );

        List<Request> list =
                requestRepository.findByReplacedRequest(original);

        assertEquals(1, list.size());
        assertEquals(replaced.getId(), list.getFirst().getId());
    }

    // =========================
    // UNIQUE KEY
    // =========================

    @Test
    void shouldNotAllowDuplicateNormalizedKey() {
        User user = userRepository.save(TestFactory.createUser());

        String key = "same_key";

        requestRepository.save(TestFactory.createRequestWithKey(user, key));

        assertThrows(DataIntegrityViolationException.class, () -> requestRepository.saveAndFlush(
                TestFactory.createRequestWithKey(user, key)
        ));
    }

    @Test
    void shouldReturnEmptyWhenNormalizedKeyNotFound() {
        Optional<Request> result =
                requestRepository.findByNormalizedKey("not_exist");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenNoRequestInQueue() {
        Optional<Request> result =
                requestRepository.findTopByQueueTypeOrderByRequestOrderDesc(QueueType.MAIN);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSeparateQueueTypes() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 1));
        requestRepository.save(TestFactory.createRequest(user, QueueType.RESERVE, 1));

        List<Request> mainList =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(1, mainList.size());
        assertEquals(QueueType.MAIN, mainList.getFirst().getQueueType());
    }

    @Test
    void shouldHandleSameOrderValues() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 1));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 1));

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(2, list.size());
    }
    @Test
    void shouldOrderByCreatedAt() {
        User user = userRepository.save(TestFactory.createUser());

        Request r1 = requestRepository.save(TestFactory.createRequest(user));
        Request r2 = requestRepository.save(TestFactory.createRequest(user));

        List<Request> list =
                requestRepository.findByUser_IdOrderByCreatedAtAsc(user.getId());

        assertEquals(r1.getId(), list.getFirst().getId());
    }
    @Test
    void shouldFindByReplacedRequestId() {
        User user = userRepository.save(TestFactory.createUser());

        Request original = requestRepository.save(TestFactory.createRequest(user));
        Request replaced = requestRepository.save(
                TestFactory.createReplacedRequest(user, original)
        );

        List<Request> list =
                requestRepository.findByReplacedRequest_Id(original.getId());

        assertEquals(1, list.size());
    }

    @Test
    void shouldFindByRequesterId() {
        User user = userRepository.save(TestFactory.createUser());

        Request r1 = TestFactory.createRequest(user);
        Request r2 = TestFactory.createRequest(user);

        r1.setRequesterId("req-1");
        r2.setRequesterId("req-2");

        requestRepository.save(r1);
        requestRepository.save(r2);

        List<Request> result = requestRepository.findByRequesterId("req-1");

        assertEquals(1, result.size());
        assertEquals("req-1", result.getFirst().getRequesterId());
    }
    @Test
    void shouldFindByQueueTypeAndStatusOrdered() {
        User user = userRepository.save(TestFactory.createUser());

        Request r1 = TestFactory.createRequest(user, QueueType.MAIN, 2);
        Request r2 = TestFactory.createRequest(user, QueueType.MAIN, 1);

        r1.setStatus(RequestStatus.WAITING);
        r2.setStatus(RequestStatus.WAITING);

        requestRepository.save(r1);
        requestRepository.save(r2);

        List<Request> result =
                requestRepository.findByQueueTypeAndStatusOrderByRequestOrderAsc(
                        QueueType.MAIN,
                        RequestStatus.WAITING
                );

        assertEquals(1, result.get(0).getRequestOrder());
        assertEquals(2, result.get(1).getRequestOrder());
    }
    @Test
    void shouldFindTopForUpdate() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 1));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 5));

        List<Request> result = requestRepository.findTopForUpdate(QueueType.MAIN);

        assertFalse(result.isEmpty());
        assertEquals(5, result.get(0).getRequestOrder());
    }
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
    @Test
    void shouldFindByStatus() {
        User user = userRepository.save(TestFactory.createUser());

        Request r1 = TestFactory.createRequest(user);
        Request r2 = TestFactory.createRequest(user);

        r1.setStatus(RequestStatus.WAITING);
        r2.setStatus(RequestStatus.DONE);

        requestRepository.save(r1);
        requestRepository.save(r2);

        List<Request> result = requestRepository.findByStatus(RequestStatus.WAITING);

        assertEquals(1, result.size());
        assertEquals(RequestStatus.WAITING, result.getFirst().getStatus());
    }
    @Test
    void shouldIncrementOrderForQueue() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 1));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 2));

        int updated = requestRepository.incrementOrderForQueue(QueueType.MAIN);

        assertEquals(2, updated);
    }
    @Test
    void shouldDecrementOrderAfter() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 1));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 2));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 3));

        int updated = requestRepository.decrementOrderAfter(QueueType.MAIN, 1);

        assertEquals(2, updated);
    }

    @Test
    void shouldFindQueueForUpdateOrderedAsc() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 3));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 1));
        requestRepository.save(TestFactory.createRequest(user, QueueType.MAIN, 2));

        List<Request> result =
                requestRepository.findQueueForUpdate(QueueType.MAIN);

        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getRequestOrder());
        assertEquals(2, result.get(1).getRequestOrder());
        assertEquals(3, result.get(2).getRequestOrder());
    }

    @Test
    void shouldStillReturnDataWithPessimisticLock() {
        User user = userRepository.save(TestFactory.createUser());

        requestRepository.save(TestFactory.createRequest(user));

        List<Request> result =
                requestRepository.findQueueForUpdate(QueueType.MAIN);

        assertFalse(result.isEmpty());
    }
}