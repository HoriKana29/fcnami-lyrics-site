package com.fcnami.backend.Service;

import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;
import com.fcnami.backend.Model.User;
import com.fcnami.backend.Repository.RequestRepository;
import com.fcnami.backend.Repository.UserRepository;
import com.fcnami.backend.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Transactional
public class RequestServiceIntegrationTest {
    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        user = userRepository.save(TestFactory.createUser());
    }

    @BeforeEach
    void clean() {
        requestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateRequest_inDatabase() {
        Request r = requestService.createRequest(
                user.getId(), "song", "artist", QueueType.MAIN
        );

        assertNotNull(r.getId());

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(1, list.size());
        assertEquals(1, list.getFirst().getRequestOrder());
    }
    @Test
    void shouldMaintainOrder_whenMultipleRequests() {
        requestService.createRequest(user.getId(), "s1", "a1", QueueType.MAIN);
        requestService.createRequest(user.getId(), "s2", "a2", QueueType.MAIN);
        requestService.createRequest(user.getId(), "s3", "a3", QueueType.MAIN);

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(1, list.get(0).getRequestOrder());
        assertEquals(2, list.get(1).getRequestOrder());
        assertEquals(3, list.get(2).getRequestOrder());
    }
    @Test
    void shouldShiftQueue_whenDelete() {
        Request r1 = requestService.createRequest(user.getId(), "s1", "a", QueueType.MAIN);
        Request r2 = requestService.createRequest(user.getId(), "s2", "a", QueueType.MAIN);
        Request r3 = requestService.createRequest(user.getId(), "s3", "a", QueueType.MAIN);

        requestService.deleteRequest(r2.getId());

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(2, list.size());
        assertEquals(1, list.get(0).getRequestOrder());
        assertEquals(2, list.get(1).getRequestOrder());
    }

    @Test
    void shouldInsertAtTop_andShiftOthers() {
        requestService.createRequest(user.getId(), "s1", "a", QueueType.MAIN);
        requestService.createRequest(user.getId(), "s2", "a", QueueType.MAIN);

        requestService.insertAtTop(user.getId(), "TOP", "a", QueueType.MAIN);

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals("TOP", list.get(0).getSongTitle());
        assertEquals(1, list.get(0).getRequestOrder());
        assertEquals(2, list.get(1).getRequestOrder());
        assertEquals(3, list.get(2).getRequestOrder());
    }
}
