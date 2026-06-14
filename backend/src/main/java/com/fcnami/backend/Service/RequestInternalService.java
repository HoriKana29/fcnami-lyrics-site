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
    public Request createRequestInternal(
            Long userId,
            String title,
            String artist,
            QueueType type
    ) {
        User user = findUser(userId);
        ensureUserCanRequest(user);

        Request request = RequestFactory.create(user, title, artist, type);
        ensureUniqueRequest(request);
        QueueCounter counter = lockQueueCounter(type);
        request.setRequestOrder(nextQueueOrder(counter));
        Request saved = requestRepository.save(request);

        user.setActiveRequests(safeIncrement(user.getActiveRequests()));
        userRepository.save(user);

        return saved;
    }

    private QueueCounter lockQueueCounter(QueueType type) {
        QueueCounter counter = queueCounterRepository.findForUpdate(type);
        if (counter == null) {
            return queueCounterRepository.save(QueueCounter.builder()
                    .queueType(type)
                    .lastOrder(0L)
                    .build());
        }
        return counter;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void ensureUserCanRequest(User user) {
        Integer maxRequests = user.getMaxRequests();
        if (maxRequests != null && safeCount(user.getActiveRequests()) >= maxRequests) {
            throw new IllegalStateException("User request limit reached");
        }
    }

    private void ensureUniqueRequest(Request request) {
        if (requestRepository.existsByNormalizedKey(request.getNormalizedKey())) {
            throw new IllegalStateException("Duplicate request");
        }
    }

    private int nextQueueOrder(QueueCounter counter) {
        long newOrder = counter.getLastOrder() + 1000;
        counter.setLastOrder(newOrder);
        queueCounterRepository.save(counter);
        return (int) newOrder;
    }

    private int safeCount(Integer value) {
        return value == null ? 0 : value;
    }

    private int safeIncrement(Integer value) {
        return (value == null ? 0 : value) + 1;
    }
}
