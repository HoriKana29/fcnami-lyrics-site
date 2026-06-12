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
    private static final int QUEUE_GAP = 1000;
    private static final int TOP_INSERT_ORDER = QUEUE_GAP / 2;
    private static final int RETRY_MAX_ATTEMPTS = 5;
    private static final int RETRY_INITIAL_DELAY_MS = 50;
    private static final int RETRY_MAX_DELAY_MS = 1000;
    private static final double RETRY_MULTIPLIER = 2;

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final QueueCounterRepository queueCounterRepository;
    private final RequestInternalService internalService;

    @Retryable(
            retryFor = {
                    DataIntegrityViolationException.class,
                    CannotAcquireLockException.class
            },
            maxAttempts = RETRY_MAX_ATTEMPTS,
            backoff = @Backoff(
                    delay = RETRY_INITIAL_DELAY_MS,
                    multiplier = RETRY_MULTIPLIER,
                    maxDelay = RETRY_MAX_DELAY_MS
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

    public List<Request> getQueue(QueueType type) {
        return requestRepository.findByQueueTypeOrderByRequestOrderAsc(type);
    }

    public List<Request> getQueueByStatus(QueueType type, RequestStatus status) {
        return requestRepository.findByQueueTypeAndStatusOrderByRequestOrderAsc(type, status);
    }

    @Transactional
    public void deleteRequest(Long requestId) {
        Request request = requestRepository.findById(requestId).orElse(null);
        if (request == null) {
            return;
        }

        User user = request.getUser();

        int deleted = requestRepository.deleteExistingById(requestId);
        if (deleted > 0 && user != null) {
            user.setActiveRequests(safeDecrement(user.getActiveRequests()));
            userRepository.save(user);
        }
    }

    @Transactional
    public Request insertAtTop(Long userId, String title, String artist, QueueType type) {

        User user = userRepository.findById(userId)
                .orElseThrow();

        queueCounterRepository.findForUpdate(type);
        List<Request> existing = requestRepository.lockQueue(type);
        for (int i = 0; i < existing.size(); i++) {
            existing.get(i).setRequestOrder((i + 1) * QUEUE_GAP);
        }
        requestRepository.saveAll(existing);
        requestRepository.flush();

        Request request = RequestFactory.create(user, title, artist, type);
        request.setRequestOrder(TOP_INSERT_ORDER);
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

        Request newRequest = RequestFactory.replace(
                original.getUser(),
                original,
                title,
                artist
        );

        newRequest.setQueueType(original.getQueueType());

        newRequest.setRequestOrder(original.getRequestOrder() + 1);

        return requestRepository.save(newRequest);
    }

    @Transactional
    public Request popNext(QueueType type) {
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
}
