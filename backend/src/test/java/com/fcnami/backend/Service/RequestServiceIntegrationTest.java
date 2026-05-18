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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RequestServiceIntegrationTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User user;

    @BeforeEach
    void setup() {
        requestRepository.deleteAll();
        userRepository.deleteAll();

        jdbcTemplate.update("UPDATE queue_counter SET last_order = 0");

        user = userRepository.save(TestFactory.createUser());
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
        assertEquals(1000, list.getFirst().getRequestOrder());
    }

    @Test
    void shouldMaintainOrder_whenMultipleRequests() {

        requestService.createRequest(user.getId(), "s1", "a1", QueueType.MAIN);
        requestService.createRequest(user.getId(), "s2", "a2", QueueType.MAIN);
        requestService.createRequest(user.getId(), "s3", "a3", QueueType.MAIN);

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        // ✅ normalize → 1000,2000,3000
        assertEquals(3, list.size());

        assertEquals(1000, list.get(0).getRequestOrder());
        assertEquals(2000, list.get(1).getRequestOrder());
        assertEquals(3000, list.get(2).getRequestOrder());
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

        // 🔥 FIX: gap-based system ไม่ compact
        assertEquals(1000, list.get(0).getRequestOrder());
        assertEquals(3000, list.get(1).getRequestOrder());
    }

    @Test
    void shouldInsertAtTop_andShiftOthers() {

        requestService.createRequest(user.getId(), "s1", "a", QueueType.MAIN);
        requestService.createRequest(user.getId(), "s2", "a", QueueType.MAIN);

        requestService.insertAtTop(user.getId(), "TOP", "a", QueueType.MAIN);

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(3, list.size());

        // ✅ TOP = 500
        assertEquals("TOP", list.get(0).getSongTitle());
        assertEquals(500, list.get(0).getRequestOrder());

        // ✅ shifted by normalize
        assertEquals(1000, list.get(1).getRequestOrder());
        assertEquals(2000, list.get(2).getRequestOrder());
    }

    // Test boundary case: insert/delete ตอน queue ว่าง

    @Test
    void shouldHandleEmptyQueue_popNext_returnsNull() {
        Request result = requestService.popNext(QueueType.MAIN);
        assertNull(result);
    }

    // delete request ที่ “ไม่มีอยู่”
    @Test
    void shouldThrow_whenDeleteNonExistentRequest() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                requestService.deleteRequest(999L)
        );

        assertEquals("Request not found", ex.getMessage());
    }

    @Test
    void shouldCorrectlyUpdateActiveRequestCount_lifecycle() {
        Request r = requestService.createRequest(user.getId(), "s", "a", QueueType.MAIN);

        assertEquals(1, userRepository.findById(user.getId()).get().getActiveRequests());

        requestService.deleteRequest(r.getId());

        assertEquals(0, userRepository.findById(user.getId()).get().getActiveRequests());
    }

    @Test
    void shouldIsolateQueueType() {
        // arrange
        seedQueueCounter();

        // act
        requestService.createRequest(user.getId(), "a", "a", QueueType.MAIN);
        requestService.createRequest(user.getId(), "b", "b", QueueType.SECONDARY);

        // assert
        assertEquals(1, requestRepository
                .findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN).size());

        assertEquals(1, requestRepository
                .findByQueueTypeOrderByRequestOrderAsc(QueueType.SECONDARY).size());
    }

    @Test
    void shouldMaintainGapStrategy_consistency() {

        seedQueueCounter();

        List<Integer> orders = new ArrayList<>();

        orders.add(requestService.createRequest(user.getId(), "s1", "a1", QueueType.MAIN).getRequestOrder());
        orders.add(requestService.createRequest(user.getId(), "s2", "a2", QueueType.MAIN).getRequestOrder());
        orders.add(requestService.createRequest(user.getId(), "s3", "a3", QueueType.MAIN).getRequestOrder());

        assertEquals(3, orders.size());

        assertEquals(1000, orders.get(1) - orders.get(0));
        assertEquals(1000, orders.get(2) - orders.get(1));
    }


    private void seedQueueCounter() {
        jdbcTemplate.update("""
        INSERT INTO queue_counter(queue_type, last_order)
        VALUES ('MAIN', 0)
        ON DUPLICATE KEY UPDATE last_order = 0
    """);

        jdbcTemplate.update("""
        INSERT INTO queue_counter(queue_type, last_order)
        VALUES ('SECONDARY', 0)
        ON DUPLICATE KEY UPDATE last_order = 0
    """);
    }
}
