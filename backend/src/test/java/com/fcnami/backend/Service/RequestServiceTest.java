package com.fcnami.backend.Service;

import com.fcnami.backend.Model.QueueRequest.QueueType;
import com.fcnami.backend.Model.QueueRequest.Request;
import com.fcnami.backend.Model.QueueRequest.RequestStatus;
import com.fcnami.backend.Model.User;
import com.fcnami.backend.Repository.QueueCounterRepository;
import com.fcnami.backend.Repository.RequestRepository;
import com.fcnami.backend.Repository.UserRepository;
import com.fcnami.backend.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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

    @Mock
    private QueueCounterRepository queueCounterRepository;

    @Mock
    private RequestInternalService internalService;

    @InjectMocks
    private RequestService requestService;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestFactory.createUser();

        requestService = new RequestService(
                requestRepository,
                userRepository,
                queueCounterRepository,
                internalService
        );
    }

    // =========================================================
    // CREATE REQUEST
    // =========================================================

    @Test
    void shouldCreateRequestSuccessfully() {

        Request req = TestFactory.createRequest(user);

        when(internalService.createRequestInternal(any(), any(), any(), any()))
                .thenReturn(req);

        Request result = requestService.createRequest(
                1L, "song", "artist", QueueType.MAIN
        );

        assertNotNull(result);
        verify(internalService).createRequestInternal(any(), any(), any(), any());
    }

    // =========================================================
    // DELETE REQUEST
    // =========================================================

    @Test
    void shouldDeleteRequestAndDecreaseUserCount() {

        user.setActiveRequests(2);

        Request req = TestFactory.createRequest(user, QueueType.MAIN, 2);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req));
        when(requestRepository.deleteExistingById(1L)).thenReturn(1);

        requestService.deleteRequest(1L);

        verify(requestRepository).deleteExistingById(1L);
        verify(userRepository).save(user);
        assertEquals(1, user.getActiveRequests());
    }

    @Test
    void shouldNotFailWhenUserIsNullDuringDelete() {

        Request req = TestFactory.createRequest(user, QueueType.MAIN, 1);
        req.setUser(null);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(req));
        when(requestRepository.deleteExistingById(1L)).thenReturn(1);

        requestService.deleteRequest(1L);

        verify(userRepository, never()).save(any());
    }

    // =========================================================
    // INSERT AT TOP
    // =========================================================

    @Test
    void shouldInsertAtTopAndIncreaseUserCounter() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Request result = requestService.insertAtTop(
                1L, "song", "artist", QueueType.MAIN
        );

        assertNotNull(result);
        assertEquals(RequestStatus.WAITING, result.getStatus());
        verify(queueCounterRepository).findForUpdate(QueueType.MAIN);
        verify(requestRepository).lockQueue(QueueType.MAIN);
        verify(requestRepository).saveAll(List.of());
        verify(requestRepository).flush();
    }

    @Test
    void shouldThrowWhenUserNotFoundOnInsert() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                requestService.insertAtTop(1L, "song", "artist", QueueType.MAIN)
        );
    }

    // =========================================================
    // REPLACE REQUEST
    // =========================================================

    @Test
    void shouldReplaceRequestSuccessfully() {

        Request original = TestFactory.createRequest(user, QueueType.MAIN, 10);

        when(requestRepository.findById(1L)).thenReturn(Optional.of(original));
        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Request result = requestService.replaceRequest(
                1L, "new song", "artist"
        );

        assertNotNull(result);
        assertEquals(original.getQueueType(), result.getQueueType());
        assertEquals(original.getRequestOrder() + 1, result.getRequestOrder());
    }

    // =========================================================
    // POP NEXT (UPDATED LOGIC)
    // =========================================================

    @Test
    void shouldPopFirstRequestFromQueue() {

        Request r1 = TestFactory.createRequest(user, QueueType.MAIN, 1);
        Request r2 = TestFactory.createRequest(user, QueueType.MAIN, 2);

        when(requestRepository.lockQueue(QueueType.MAIN))
                .thenReturn(List.of(r1, r2));

        Request result = requestService.popNext(QueueType.MAIN);

        assertEquals(r1, result);
        verify(requestRepository).delete(r1);
        verify(userRepository).save(user);
    }

    @Test
    void shouldReturnNullWhenQueueEmpty() {

        when(requestRepository.lockQueue(QueueType.MAIN))
                .thenReturn(List.of());

        Request result = requestService.popNext(QueueType.MAIN);

        assertNull(result);
        verify(requestRepository, never()).delete(any());
    }

    @Test
    void shouldNotUpdateUserWhenPopWithoutUser() {

        Request r = TestFactory.createRequest(user, QueueType.MAIN, 1);
        r.setUser(null);

        when(requestRepository.lockQueue(QueueType.MAIN))
                .thenReturn(List.of(r));

        requestService.popNext(QueueType.MAIN);

        verify(userRepository, never()).save(any());
    }

    // =========================================================
    // GETTERS
    // =========================================================

    @Test
    void shouldReturnQueue() {

        requestService.getQueue(QueueType.MAIN);

        verify(requestRepository)
                .findByQueueTypeOrderByRequestOrderAsc(QueueType.MAIN);
    }

    @Test
    void shouldReturnQueueByStatus() {

        requestService.getQueueByStatus(QueueType.MAIN, RequestStatus.WAITING);

        verify(requestRepository)
                .findByQueueTypeAndStatusOrderByRequestOrderAsc(
                        QueueType.MAIN,
                        RequestStatus.WAITING
                );
    }

    @Test
    void shouldFindByNormalizedKey() {

        Request req = TestFactory.createRequest(user);

        when(requestRepository.findByNormalizedKey("key"))
                .thenReturn(Optional.of(req));

        Request result = requestService.getByNormalizedKey("key");

        assertEquals(req, result);
    }

    @Test
    void shouldThrowWhenNormalizedKeyNotFound() {

        when(requestRepository.findByNormalizedKey("key"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                requestService.getByNormalizedKey("key")
        );
    }

    @Test
    void shouldReturnRequestsByRequesterId() {

        when(requestRepository.findByRequesterId("req"))
                .thenReturn(List.of());

        assertTrue(requestService.getByRequesterId("req").isEmpty());
    }

    @Test
    void shouldReturnRequestsByStatus() {

        when(requestRepository.findByStatus(RequestStatus.WAITING))
                .thenReturn(List.of());

        assertTrue(requestService.getByStatus(RequestStatus.WAITING).isEmpty());
    }

    @Test
    void shouldReturnRequestsByDepth() {

        when(requestRepository.findByDepthLevel(1))
                .thenReturn(List.of());

        assertTrue(requestService.getByDepth(1).isEmpty());
    }
}
