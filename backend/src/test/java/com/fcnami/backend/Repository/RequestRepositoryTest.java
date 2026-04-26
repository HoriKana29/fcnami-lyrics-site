package com.fcnami.backend.Repository;

import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;
import com.fcnami.backend.Model.User;
import com.fcnami.backend.TestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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

}