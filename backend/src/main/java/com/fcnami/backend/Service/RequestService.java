package com.fcnami.backend.Service;

import com.fcnami.backend.Factory.RequestFactory;
import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;
import com.fcnami.backend.Model.QueueRequest.RequestStatus;
import com.fcnami.backend.Model.User;
import com.fcnami.backend.Repository.QueueCounterRepository;
import com.fcnami.backend.Repository.RequestRepository;
import com.fcnami.backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final QueueCounterRepository queueCounterRepository;
    private final RequestInternalService internalService;

    // แก้ปัญหา Database lock ไม่ได้/ชน/race condition
    @Retryable(
            retryFor = {
                    DataIntegrityViolationException.class,
                    CannotAcquireLockException.class
            },
            maxAttempts = 5,
            backoff = @Backoff(
                    delay = 50,
                    multiplier = 2,
                    maxDelay = 1000
            )
    )
    @Transactional
    public Request createRequest(Long userId, String title, String artist, QueueType type) {
        return internalService.createRequestInternal(
                userId,
                title,
                artist,
                type
        );
    }

    // Return value of the method is never used
    public List<Request> getQueue(QueueType type) {
        return requestRepository.findByQueueTypeOrderByRequestOrderAsc(type);
    }

    // Return value of the method is never used
    public List<Request> getQueueByStatus(QueueType type, RequestStatus status) {
        return requestRepository.findByQueueTypeAndStatusOrderByRequestOrderAsc(type, status);
    }

    @Transactional
    public void deleteRequest(Long requestId) {

        // หา Request
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User user = request.getUser();

        // ลบ request
        requestRepository.delete(request);

        // ลด activeRequests ของ user
        if (user != null) {
            user.setActiveRequests(safeDecrement(user.getActiveRequests()));
            userRepository.save(user);
        }
    }

    // แทรก Request ขึ้นหัวคิว
    @Transactional
    public Request insertAtTop(Long userId, String title, String artist, QueueType type) {

        User user = userRepository.findById(userId)
                .orElseThrow();

        int GAP = 1000;

        // lock ทั้งคิวก่อน
        queueCounterRepository.lockQueue(type);
        List<Request> existing = requestRepository.lockQueue(type);
        // reorder ใหม่หมด
        for (int i = 0; i < existing.size(); i++) {
            existing.get(i).setRequestOrder((i + 1) * GAP);
        }
        requestRepository.saveAll(existing);
        // ส่งไป Database
        requestRepository.flush();

        // 4. insert ใหม่
        Request request = RequestFactory.create(user, title, artist, type);
        request.setRequestOrder(GAP/2);
        request.setStatus(RequestStatus.WAITING);

        Request saved = requestRepository.save(request);

        user.setActiveRequests(safeIncrement(user.getActiveRequests()));
        userRepository.save(user);

        return saved;
    }

    // สร้างใหม่จากของเดิม และแทรกไว้ติดที่เก่า
    // ** ฝากดูความเสี่ยงเรื่อง order แน่นและ อาจต้อง re-balance
    @Transactional
    public Request replaceRequest(Long requestId, String title, String artist) {

        Request original = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Original request not found"));

        Request newRequest = RequestFactory.replace(
                original.getUser(),
                original,
                title,
                artist
        );

        newRequest.setQueueType(original.getQueueType());

        // 🔥 insert next to original (gap-based)
        newRequest.setRequestOrder(original.getRequestOrder() + 1);

        return requestRepository.save(newRequest);
    }

    // ดึงคิวถัดไปออกจากคิว
    @Transactional
    public Request popNext(QueueType type) {

        // ล็อคก่อนทำ
        List<Request> queue = requestRepository.lockQueue(type);

        if (queue.isEmpty()) return null;

        Request next = queue.getFirst();

        requestRepository.delete(next);

        User user = next.getUser();
        if (user != null) {
            user.setActiveRequests(safeDecrement(user.getActiveRequests()));
            userRepository.save(user);
        }

        return next;
    }

    public List<Request> getByRequesterId(String requesterId) {
        return requestRepository.findByRequesterId(requesterId);
    }

    public List<Request> getByStatus(RequestStatus status) {
        return requestRepository.findByStatus(status);
    }

    public List<Request> getByDepth(Integer depth) {
        return requestRepository.findByDepthLevel(depth);
    }

    public Request getByNormalizedKey(String key) {
        return requestRepository.findByNormalizedKey(key)
                .orElseThrow(() -> new RuntimeException("Request not found"));
    }

    private int safeDecrement(Integer value) {
        return Math.max(0, (value == null ? 0 : value) - 1);
    }
    private int safeIncrement(Integer value) {
        return (value == null ? 0 : value) + 1;
    }

    // *** ต้อง ensure ว่า lockQueue() ใช้ FOR UPDATE
}
