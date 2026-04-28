package com.fcnami.backend.Service;

import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;
import com.fcnami.backend.Model.QueueRequest.RequestStatus;
import com.fcnami.backend.Model.User;
import com.fcnami.backend.Repository.RequestRepository;
import com.fcnami.backend.Repository.UserRepository;
import com.fcnami.backend.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RequestService requestService;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestFactory.createUser();
    }

    // =========================================================
    // CREATE REQUEST
    // =========================================================

    @Test
    void shouldCreateRequest_success_emptyQueue() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByNormalizedKey(any())).thenReturn(false);
        when(requestRepository.findQueueForUpdate(any())).thenReturn(List.of());
        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Request result = requestService.createRequest(1L, "song", "artist", QueueType.MAIN);

        assertNotNull(result);
        assertEquals(1, result.getRequestOrder());
        assertEquals(1, user.getActiveRequests());

        verify(requestRepository).findQueueForUpdate(QueueType.MAIN);
        verify(requestRepository).save(any());
        verify(userRepository).save(user);
        assertEquals(RequestStatus.WAITING, result.getStatus());
    }

    @Test
    void shouldCreateRequest_withExistingQueue() {
        Request r1 = TestFactory.createRequest(user, QueueType.MAIN, 1);
        Request r2 = TestFactory.createRequest(user, QueueType.MAIN, 3);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByNormalizedKey(any())).thenReturn(false);
        when(requestRepository.findQueueForUpdate(any())).thenReturn(List.of(r1, r2));
        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Request result = requestService.createRequest(1L, "song", "artist", QueueType.MAIN);

        assertEquals(4, result.getRequestOrder());
    }

    @Test
    void shouldThrow_whenDuplicateRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByNormalizedKey(any())).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                requestService.createRequest(1L, "song", "artist", QueueType.MAIN)
        );
    }

    @Test
    void shouldThrow_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                requestService.createRequest(1L, "song", "artist", QueueType.MAIN)
        );
    }

    @Test
    void shouldHandleNullOrderInQueue() {
        Request r1 = TestFactory.createRequest(user, QueueType.MAIN, 1);
        Request r2 = TestFactory.createRequest(user, QueueType.MAIN, 0);
        r2.setRequestOrder(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByNormalizedKey(any())).thenReturn(false);
        when(requestRepository.findQueueForUpdate(any())).thenReturn(List.of(r1, r2));
        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Request result = requestService.createRequest(1L, "song", "artist", QueueType.MAIN);

        assertEquals(2, result.getRequestOrder());
    }

    // =========================================================
    // DELETE REQUEST
    // =========================================================

    @Test
    void shouldDeleteRequest_andReorderQueue() {
        Request req = TestFactory.createRequest(user, QueueType.MAIN, 2);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req));

        requestService.deleteRequest(1L);

        verify(requestRepository).delete(req);
        verify(requestRepository).decrementOrderAfter(QueueType.MAIN, 2);
        verify(userRepository).save(user);

        assertEquals(0, user.getActiveRequests());
    }

    @Test
    void shouldThrow_whenDeleteRequestNotFound() {
        when(requestRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                requestService.deleteRequest(1L)
        );
    }

    @Test
    void shouldNotGoNegativeActiveRequests() {
        user.setActiveRequests(0);

        Request req = TestFactory.createRequest(user, QueueType.MAIN, 1);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req));

        requestService.deleteRequest(1L);

        assertEquals(0, user.getActiveRequests());
    }

    // =========================================================
    // INSERT AT TOP
    // =========================================================

    @Test
    void shouldInsertAtTop() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Request result = requestService.insertAtTop(1L, "song", "artist", QueueType.MAIN);

        assertEquals(1, result.getRequestOrder());
        assertEquals(RequestStatus.WAITING, result.getStatus());
        assertEquals(1, user.getActiveRequests());

        verify(requestRepository).findQueueForUpdate(QueueType.MAIN);
        verify(requestRepository).incrementOrderForQueue(QueueType.MAIN);
        verify(requestRepository).save(any());
    }

    // =========================================================
    // REPLACE REQUEST
    // =========================================================

    @Test
    void shouldReplaceRequest() {
        Request original = TestFactory.createRequest(user, QueueType.MAIN, 3);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(original));
        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Request result = requestService.replaceRequest(1L, "new", "artist");

        assertEquals(original.getQueueType(), result.getQueueType());
        assertEquals(original.getRequestOrder() + 1, result.getRequestOrder());
        assertEquals(1, result.getDepthLevel());
        assertEquals(original.getUser(), result.getUser());
        assertEquals(RequestStatus.WAITING, result.getStatus());

        verify(requestRepository).incrementAfter(QueueType.MAIN, original.getRequestOrder());
    }

    @Test
    void shouldIncreaseDepthWhenReplacingChain() {
        Request original = TestFactory.createRequest(user, QueueType.MAIN, 3);
        original.setDepthLevel(2);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(original));
        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Request result = requestService.replaceRequest(1L, "new", "artist");

        assertEquals(3, result.getDepthLevel());
    }

    @Test
    void shouldCreateNewRequestWithoutDeletingOriginal() {
        Request original = TestFactory.createRequest(user, QueueType.MAIN, 2);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(original));
        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Request result = requestService.replaceRequest(1L, "new", "artist");

        assertNotEquals(original, result);
        verify(requestRepository, never()).delete(any());
    }

    // =========================================================
    // POP NEXT
    // =========================================================

    @Test
    void shouldPopNext_success() {
        Request r1 = TestFactory.createRequest(user, QueueType.MAIN, 1);
        Request r2 = TestFactory.createRequest(user, QueueType.MAIN, 2);

        when(requestRepository.findQueueForUpdate(QueueType.MAIN))
                .thenReturn(List.of(r1, r2));

        Request result = requestService.popNext(QueueType.MAIN);

        assertEquals(r1, result);

        verify(requestRepository).findQueueForUpdate(QueueType.MAIN);
        verify(requestRepository).delete(r1);
        verify(requestRepository).decrementOrderAfter(QueueType.MAIN, 1);
        verify(userRepository).save(user);
    }

    @Test
    void shouldPopNext_withoutUser() {
        Request r1 = TestFactory.createRequest(user, QueueType.MAIN, 1);
        r1.setUser(null);

        when(requestRepository.findQueueForUpdate(QueueType.MAIN))
                .thenReturn(List.of(r1));

        requestService.popNext(QueueType.MAIN);

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldReturnNull_whenQueueEmpty() {
        when(requestRepository.findQueueForUpdate(QueueType.MAIN))
                .thenReturn(List.of());

        Request result = requestService.popNext(QueueType.MAIN);

        assertNull(result);
    }

    @Test
    void shouldHandleNullActiveRequestsOnPop() {
        user.setActiveRequests(null);

        Request r1 = TestFactory.createRequest(user, QueueType.MAIN, 1);

        when(requestRepository.findQueueForUpdate(QueueType.MAIN))
                .thenReturn(List.of(r1));

        requestService.popNext(QueueType.MAIN);

        assertEquals(0, user.getActiveRequests());
    }

    @Test
    void shouldDeleteRequest_withoutUser() {
        Request req = TestFactory.createRequest(user, QueueType.MAIN, 1);
        req.setUser(null);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req));

        requestService.deleteRequest(1L);

        verify(userRepository, never()).save(any());
    }

    // =========================================================
    // GETTERS
    // =========================================================

    @Test
    void shouldGetQueue() {
        requestService.getQueue(QueueType.MAIN);
        verify(requestRepository).findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);
    }

    @Test
    void shouldGetQueueByStatus() {
        requestService.getQueueByStatus(QueueType.MAIN, RequestStatus.WAITING);
        verify(requestRepository)
                .findByQueueTypeAndStatusOrderByRequestOrderAsc(QueueType.MAIN, RequestStatus.WAITING);
    }

    @Test
    void shouldGetByNormalizedKey() {
        Request req = TestFactory.createRequest(user);

        when(requestRepository.findByNormalizedKey("key"))
                .thenReturn(Optional.of(req));

        Request result = requestService.getByNormalizedKey("key");

        assertEquals(req, result);
    }

    @Test
    void shouldThrow_whenNormalizedKeyNotFound() {
        when(requestRepository.findByNormalizedKey("key"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                requestService.getByNormalizedKey("key")
        );
    }

    @Test
    void shouldReturnQueueResult() {
        List<Request> list = List.of(TestFactory.createRequest(user));

        when(requestRepository.findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN))
                .thenReturn(list);

        List<Request> result = requestService.getQueue(QueueType.MAIN);

        assertEquals(list, result);
    }

    // Addition
    @Test
    void shouldLockQueueWhenCreatingRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByNormalizedKey(any())).thenReturn(false);
        when(requestRepository.findQueueForUpdate(QueueType.MAIN)).thenReturn(List.of());

        requestService.createRequest(1L, "song", "artist", QueueType.MAIN);

        verify(requestRepository).findQueueForUpdate(QueueType.MAIN);
    }

    @Test
    void shouldShiftOrdersWhenInsertAtTop() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        requestService.insertAtTop(1L, "song", "artist", QueueType.MAIN);

        verify(requestRepository).incrementOrderForQueue(QueueType.MAIN);
    }
}