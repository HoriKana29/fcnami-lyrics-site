package com.fcnami.backend.Service;

import com.fcnami.backend.Factory.RequestFactory;
import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;
import com.fcnami.backend.Model.QueueRequest.RequestStatus;
import com.fcnami.backend.Model.User;
import com.fcnami.backend.Repository.RequestRepository;
import com.fcnami.backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Request createRequestInternal(Long userId, String title, String artist, QueueType type) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🔒 LOCK ทั้ง queue ก่อน
        requestRepository.findQueueForUpdate(type);

        Request request = RequestFactory.create(user, title, artist, type);

        Integer maxOrder = requestRepository.findMaxOrder(type);
        int nextOrder = (maxOrder == null ? 1 : maxOrder + 1);

        request.setRequestOrder(nextOrder);

        Request saved = requestRepository.save(request);

        user.setActiveRequests(safeIncrement(user.getActiveRequests()));
        userRepository.save(user);

        return saved;
    }

    public Request createRequest(Long userId, String title, String artist, QueueType type) {

        int retry = 0;

        while (true) {
            try {
                return createRequestInternal(userId, title, artist, type);

            } catch (org.springframework.dao.DataIntegrityViolationException |
                     org.springframework.dao.CannotAcquireLockException e) {

                if (++retry > 5) {
                    throw e;
                }

                try {
                    Thread.sleep(50); // backoff กันชน
                } catch (InterruptedException ignored) {}
            }
        }
    }

    public List<Request> getQueue(QueueType type) {
        return requestRepository.findByQueueTypeOrderByRequestOrderAsc(type);
    }

    public List<Request> getQueueByStatus(QueueType type, RequestStatus status) {
        return requestRepository.findByQueueTypeAndStatusOrderByRequestOrderAsc(type, status);
    }

    @Transactional
    public void deleteRequest(Long requestId) {

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        QueueType type = request.getQueueType();
        int order = request.getRequestOrder();

        User user = request.getUser();

        if (user != null && user.getRequests() != null) {
            user.getRequests().remove(request);
        }

        requestRepository.delete(request);
        requestRepository.flush(); // ensure delete before bulk
        requestRepository.decrementOrderAfter(type, order);

        if (user != null) {
            user.setActiveRequests(safeDecrement(user.getActiveRequests()));
            userRepository.save(user);
        }
    }

    @Transactional
    public Request insertAtTop(Long userId, String title, String artist, QueueType type) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Request request = RequestFactory.create(user, title, artist, type);

        requestRepository.findQueueForUpdate(type);
        requestRepository.incrementOrderForQueue(type);

        request.setRequestOrder(1);
        request.setStatus(RequestStatus.WAITING);

        Request saved = requestRepository.save(request);

        user.setActiveRequests(safeIncrement(user.getActiveRequests()));
        userRepository.save(user);

        return saved;
    }

    @Transactional
    public Request replaceRequest(Long requestId, String title, String artist) {

        Request original = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Original request not found"));

        // lock queue
        requestRepository.findQueueForUpdate(original.getQueueType());

        int insertOrder = original.getRequestOrder() + 1;

        // shift
        requestRepository.incrementAfter(original.getQueueType(), original.getRequestOrder());

        Request newRequest = RequestFactory.replace(
                original.getUser(),
                original,
                title,
                artist
        );

        newRequest.setQueueType(original.getQueueType());
        newRequest.setDepthLevel(
                original.getDepthLevel() == null ? 1 : original.getDepthLevel() + 1
        );
        newRequest.setRequestOrder(insertOrder);

        return requestRepository.save(newRequest);
    }

    @Transactional
    public Request popNext(QueueType type) {

        List<Request> queue = requestRepository.findQueueForUpdate(type);

        if (queue.isEmpty()) return null;

        Request next = queue.getFirst();

        //  ดึงค่าที่ต้องใช้ก่อน clear
        User user = next.getUser();
        int order = next.getRequestOrder();

        requestRepository.delete(next);
        requestRepository.flush();

        requestRepository.decrementOrderAfter(type, order);

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

}
