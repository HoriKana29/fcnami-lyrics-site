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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    @Transactional
    public Request createRequest(Long userId, String title, String artist, QueueType type) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Factory create
        Request request = RequestFactory.create(user, title, artist, type);

        // duplicate check
        if (requestRepository.existsByNormalizedKey(request.getNormalizedKey())) {
            throw new IllegalStateException("Duplicate request");
        }

        List<Request> lockedQueue = requestRepository.findQueueForUpdate(type);

        int maxOrder = lockedQueue.stream()
                .mapToInt(r -> r.getRequestOrder() == null ? 0 : r.getRequestOrder())
                .max()
                .orElse(0);

        request.setRequestOrder(maxOrder + 1);

        Request saved = requestRepository.save(request);

        // update user stats
        user.setActiveRequests(
                user.getActiveRequests() == null ? 1 : user.getActiveRequests() + 1
        );
        userRepository.save(user);

        return saved;
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

        requestRepository.delete(request);

        requestRepository.decrementOrderAfter(type, order);

        User user = request.getUser();
        if (user != null) {
            user.setActiveRequests(
                    Math.max(0, user.getActiveRequests() - 1)
            );
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

        user.setActiveRequests(
                user.getActiveRequests() == null ? 1 : user.getActiveRequests() + 1
        );
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

        newRequest.setDepthLevel(
                original.getDepthLevel() == null ? 1 : original.getDepthLevel() + 1
        );

        newRequest.setRequestOrder(original.getRequestOrder() + 1);

        requestRepository.incrementOrderForQueue(original.getQueueType());

        return requestRepository.save(newRequest);
    }

    @Transactional
    public Request popNext(QueueType type) {

        List<Request> queue = requestRepository.findQueueForUpdate(type);

        if (queue.isEmpty()) return null;

        Request next = queue.getFirst();

        requestRepository.delete(next);

        requestRepository.decrementOrderAfter(type, next.getRequestOrder());

        User user = next.getUser();
        if (user != null) {
            user.setActiveRequests(
                    Math.max(0, user.getActiveRequests() - 1)
            );
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
}
