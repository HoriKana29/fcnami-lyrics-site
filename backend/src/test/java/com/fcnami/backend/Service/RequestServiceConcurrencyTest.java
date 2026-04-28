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

    // =========================================================
    // 🔥 CONCURRENT CREATE REQUEST
    // =========================================================
    @Test
    void shouldHandleConcurrentRequests_withoutDuplicateOrder() throws Exception {

        int threads = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            int index = i;

            futures.add(executor.submit(() -> {
                ready.countDown();     // thread นี้พร้อมแล้ว
                start.await();         // รอให้ยิงพร้อมกัน

                requestService.createRequest(
                        user.getId(),
                        "song_" + index,
                        "artist",
                        QueueType.MAIN
                );

                return null;
            }));
        }

        // รอให้ทุก thread พร้อม
        ready.await();

        // 🔥 ยิงพร้อมกัน
        start.countDown();

        // รอทุก task ทำเสร็จ + check exception
        for (Future<Void> f : futures) {
            f.get();
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(threads, list.size());

        // 🔥 order ต้องไม่ซ้ำ
        Set<Integer> orders = new HashSet<>();
        for (Request r : list) {
            assertTrue(orders.add(r.getRequestOrder()),
                    "Duplicate order found: " + r.getRequestOrder());
        }
    }

    // =========================================================
    // 🔥 CONCURRENT INSERT AT TOP
    // =========================================================
    @Test
    void shouldHandleConcurrentInsertAtTop() throws Exception {

        int threads = 5;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);

        List<Future<Void>> futures = new ArrayList<>();

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

        for (Future<Void> f : futures) {
            f.get();
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        List<Request> list =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(threads, list.size());

        // order ต้อง unique
        Set<Integer> orders = new HashSet<>();
        for (Request r : list) {
            assertTrue(orders.add(r.getRequestOrder()));
        }
    }

    // =========================================================
    // 🔥 CONCURRENT POP NEXT
    // =========================================================
    @Test
    void shouldHandleConcurrentPopNext() throws Exception {

        int initial = 10;

        // เตรียม queue ก่อน
        for (int i = 0; i < initial; i++) {
            requestService.createRequest(user.getId(), "s" + i, "a", QueueType.MAIN);
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

        Set<Long> poppedIds = new HashSet<>();

        for (Future<Request> f : futures) {
            Request r = f.get();
            assertNotNull(r);
            assertTrue(poppedIds.add(r.getId()),
                    "Duplicate pop detected");
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        List<Request> remaining =
                requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);

        assertEquals(initial - threads, remaining.size());
    }
}