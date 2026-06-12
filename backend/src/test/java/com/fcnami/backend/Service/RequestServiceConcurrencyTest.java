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

import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RequestServiceConcurrencyTest {

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
    void clean() {

        requestRepository.deleteAll();
        userRepository.deleteAll();

        jdbcTemplate.execute("TRUNCATE TABLE queue_counter");

        jdbcTemplate.update("""
        INSERT INTO queue_counter(queue_type, last_order)
        VALUES ('MAIN', 0)
    """);

        user = userRepository.save(TestFactory.createUser());
    }

    // =========================================================
    // 🔥 CONCURRENT CREATE REQUEST (GAP ORDER SAFE)
    // =========================================================
    @Test
    void shouldHandleConcurrentRequests_withoutDuplicateOrder() throws Exception {

        int threads = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            int index = i;

            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();

                requestService.createRequest(
                        user.getId(),
                        "song_" + index,
                        "artist",
                        QueueType.MAIN
                );

                return null;
            }));
        }

        ready.await();
        start.countDown();

        for (Future<?> f : futures) {
            f.get();
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(threads, list.size());

        // ✅ RULE: order must be unique (NOT sequential!)
        Set<Integer> orders = new HashSet<>();
        for (Request r : list) {
            assertTrue(
                    orders.add(r.getRequestOrder()),
                    "Duplicate order detected: " + r.getRequestOrder()
            );

            assertTrue(r.getRequestOrder() >= 1000,
                    "Order must follow GAP rule");
        }
    }

    // =========================================================
    // 🔥 CONCURRENT INSERT AT TOP (NO SHIFT GUARANTEE)
    // =========================================================
    @Test
    void shouldHandleConcurrentInsertAtTop() throws Exception {

        int threads = 5;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            int index = i;

            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();

                requestService.insertAtTop(
                        user.getId(),
                        "top_" + index,
                        "artist",
                        QueueType.MAIN
                );

                return null;
            }));
        }

        ready.await();
        start.countDown();

        for (Future<?> f : futures) {
            f.get();
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(threads, list.size());

        // ✅ must be unique (no collision)
        Set<Integer> orders = new HashSet<>();
        for (Request r : list) {
            assertTrue(orders.add(r.getRequestOrder()));
        }
    }

    // =========================================================
    // 🔥 CONCURRENT POP NEXT (CRITICAL SECTION TEST)
    // =========================================================
    @Test
    void shouldHandleConcurrentPopNext() throws Exception {

        int initial = 10;

        for (int i = 0; i < initial; i++) {
            requestService.createRequest(
                    user.getId(),
                    "song_" + i,
                    "artist",
                    QueueType.MAIN
            );
        }

        int threads = 5;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Request>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {

            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();

                return requestService.popNext(QueueType.MAIN);
            }));
        }

        ready.await();
        start.countDown();

        Set<Long> popped = new HashSet<>();

        for (Future<Request> f : futures) {
            Request r = f.get();

            assertNotNull(r);

            assertTrue(
                    popped.add(r.getId()),
                    "Duplicate pop detected: " + r.getId()
            );
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        List<Request> remaining =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(initial - threads, remaining.size());
    }

    @Test
    void shouldNotBreak_whenCreateAndDeleteConcurrent() throws Exception {

        requestService.createRequest(user.getId(), "s1", "a", QueueType.MAIN);
        requestService.createRequest(user.getId(), "s2", "a", QueueType.MAIN);
        requestService.createRequest(user.getId(), "s3", "a", QueueType.MAIN);
        requestService.createRequest(user.getId(), "s4", "a", QueueType.MAIN);

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(4, list.size());

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Future<?> deleteTask = executor.submit(() -> {
            ready.countDown();
            start.await();

            requestService.deleteRequest(list.get(1).getId());
            return null;
        });

        Future<?> createTask = executor.submit(() -> {
            ready.countDown();
            start.await();

            requestService.createRequest(user.getId(), "s5", "a", QueueType.MAIN);
            return null;
        });

        ready.await();
        start.countDown();

        deleteTask.get();
        createTask.get();

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        List<Request> finalList =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        // ต้องไม่ crash + size ต้อง consistent (3 หรือ 4 แล้วแต่ race)
        assertTrue(finalList.size() >= 3);
        assertTrue(finalList.size() <= 4);

        // order ต้องยัง unique
        Set<Integer> orders = new HashSet<>();
        for (Request r : finalList) {
            assertTrue(orders.add(r.getRequestOrder()));
        }
    }
    @Test
    void shouldRemainConsistent_whenPopAndInsertAtTopConcurrent() throws Exception {

        for (int i = 0; i < 5; i++) {
            requestService.createRequest(user.getId(), "s" + i, "a", QueueType.MAIN);
        }

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Future<Request> popTask = executor.submit(() -> {
            ready.countDown();
            start.await();
            return requestService.popNext(QueueType.MAIN);
        });

        Future<?> insertTask = executor.submit(() -> {
            ready.countDown();
            start.await();
            requestService.insertAtTop(user.getId(), "TOP", "a", QueueType.MAIN);
            return null;
        });

        ready.await();
        start.countDown();

        Request popped = popTask.get();
        insertTask.get();

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertNotNull(popped);

        // system ต้องยังไม่พัง
        assertTrue(list.size() >= 4 && list.size() <= 6);

        Set<Integer> orders = new HashSet<>();
        for (Request r : list) {
            assertTrue(orders.add(r.getRequestOrder()));
        }
    }

    ///  *** เพิ่ม Test ดังต่อไปนี้้เข้าไปด้วย
    @Test
    void stressTest_createRequests_many() throws Exception {

        int threads = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            int index = i;

            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();

                requestService.createRequest(
                        user.getId(),
                        "song_" + index,
                        "artist",
                        QueueType.MAIN
                );

                return null;
            }));
        }

        ready.await();
        start.countDown();

        for (Future<?> f : futures) {
            f.get();
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(threads, list.size());

        // validate uniqueness
        Set<Integer> orders = new HashSet<>();
        for (Request r : list) {
            assertTrue(orders.add(r.getRequestOrder()));
            assertTrue(r.getRequestOrder() >= 1000);
        }
    }

    @Test
    void shouldNotExceedUserLimit_underConcurrency() throws Exception {

        user.setMaxRequests(3);
        userRepository.save(user);

        int threads = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(executor.submit(() -> {
                ready.countDown();
                start.await();

                try {
                    requestService.createRequest(
                            user.getId(),
                            "song",
                            "artist",
                            QueueType.MAIN
                    );
                } catch (Exception ignored) {}

                return null;
            }));
        }

        ready.await();
        start.countDown();

        for (Future<?> f : futures) f.get();

        executor.shutdown();

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertTrue(list.size() <= 3);
    }

    @Test
    void shouldHandleConcurrentDeleteSameRequest() throws Exception {

        Request req = requestService.createRequest(
                user.getId(), "song", "a", QueueType.MAIN
        );

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Future<?> f1 = executor.submit(() -> {
            ready.countDown();
            start.await();
            requestService.deleteRequest(req.getId());
            return null;
        });

        Future<?> f2 = executor.submit(() -> {
            ready.countDown();
            start.await();
            requestService.deleteRequest(req.getId());
            return null;
        });

        ready.await();
        start.countDown();

        f1.get();
        f2.get();

        executor.shutdown();

        List<Request> list = requestRepository.findAll();

        assertTrue(list.isEmpty());
    }

    @Test
    void shouldNotMixOrderBetweenQueueTypes() throws Exception {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Future<?> f1 = executor.submit(() ->
                requestService.createRequest(user.getId(), "A", "a", QueueType.MAIN)
        );

        Future<?> f2 = executor.submit(() ->
                requestService.createRequest(user.getId(), "B", "a", QueueType.RESERVE)
        );

        f1.get();
        f2.get();

        List<Request> main =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        List<Request> reserve =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.RESERVE);

        assertEquals(1, main.size());
        assertEquals(1, reserve.size());
    }

    @Test
    void shouldRebalanceWhenGapTooSmall() {

        for (int i = 0; i < 50; i++) {
            requestService.insertAtTop(user.getId(), "s" + i, "a", QueueType.MAIN);
        }

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        Set<Integer> orders = new HashSet<>();

        for (Request r : list) {
            assertTrue(orders.add(r.getRequestOrder()));
        }
    }

    @Test
    void shouldRollback_whenCreateFails() {

        assertThrows(Exception.class, () -> {
            requestService.createRequest(
                    user.getId(),
                    null, // force error
                    "artist",
                    QueueType.MAIN
            );
        });

        List<Request> list = requestRepository.findAll();

        assertTrue(list.isEmpty());
    }
}
