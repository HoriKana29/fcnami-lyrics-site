package com.fcnami.backend.Service;

import com.fcnami.backend.Factory.RequestFactory;
import com.fcnami.backend.Model.QueueRequest.*;
import com.fcnami.backend.Model.User;
import com.fcnami.backend.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestInternalService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final QueueCounterRepository queueCounterRepository;

    @Transactional
    // method นี้จะทำงานใน transaction เดียว
    // error -> rollback กลับทั้งหมด
    public Request createRequestInternal(
            Long userId,
            String title,
            String artist,
            QueueType type
    ) {

        // 1. lock user
        // ไม่ใช่ lock จริง แต่ transaction จะช่วย consistency
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. build request
        Request request = RequestFactory.create(user, title, artist, type);

        // 3. duplicate check
        if (requestRepository.existsByNormalizedKey(request.getNormalizedKey())) {
            throw new IllegalStateException("Duplicate request");
        }

        // 4. 🔥 LOCK counter row (critical section)
        // lock row ของ counter ตาม type
        QueueCounter counter = queueCounterRepository.findForUpdate(type);

        // เช็ค null counter
        if (counter == null) {
            throw new IllegalStateException("QueueCounter not initialized");
        }

        // 5. atomic increment (NO race condition anymore)
        // กระบวนการของแต่ละ Tread จะอัปเดตเสมอ (1000 เผื่อแทรกคิวในอนาคต)
        long newOrder = counter.getLastOrder() + 1000;
        counter.setLastOrder(newOrder);

        queueCounterRepository.save(counter);

        request.setRequestOrder((int) newOrder);

        // 6. save request
        Request saved = requestRepository.save(request);

        // 7. update user stats
        user.setActiveRequests(safeIncrement(user.getActiveRequests()));
        userRepository.save(user);

        return saved;
    }

    private int safeIncrement(Integer value) {
        return (value == null ? 0 : value) + 1;
    }
}