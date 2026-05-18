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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestInternalServiceTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QueueCounterRepository queueCounterRepository;

    @InjectMocks
    private RequestInternalService requestInternalService;

    private User user;

    @BeforeEach
    void setUp() {
        user = TestFactory.createUser();
    }

    // =========================================================
    // SUCCESS - EMPTY QUEUE (first insert)
    // =========================================================
    @Test
    void shouldCreateRequest_success_emptyQueue() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByNormalizedKey(any())).thenReturn(false);
        when(queueCounterRepository.findForUpdate(QueueType.MAIN))
                .thenReturn(TestFactory.createQueueCounter(QueueType.MAIN, 0L));
        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Request result = requestInternalService
                .createRequestInternal(1L, "song", "artist", QueueType.MAIN);

        assertNotNull(result);
        assertEquals(RequestStatus.WAITING, result.getStatus());
        assertEquals(1000, result.getRequestOrder());
        assertEquals(1, user.getActiveRequests());

        verify(requestRepository).save(any());
        verify(userRepository).save(user);
    }

    // =========================================================
    // SUCCESS - EXISTING QUEUE
    // =========================================================
    @Test
    void shouldCreateRequest_withExistingQueue() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByNormalizedKey(any())).thenReturn(false);

        when(queueCounterRepository.findForUpdate(QueueType.MAIN))
                .thenReturn(TestFactory.createQueueCounter(QueueType.MAIN, 10L));

        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Request result = requestInternalService
                .createRequestInternal(1L, "song", "artist", QueueType.MAIN);

        assertEquals(1010, result.getRequestOrder());
        assertEquals(RequestStatus.WAITING, result.getStatus());
    }

    // =========================================================
    // DUPLICATE REQUEST
    // =========================================================
    @Test
    void shouldThrow_whenDuplicateRequest() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByNormalizedKey(any())).thenReturn(true);

        assertThrows(IllegalStateException.class, () ->
                requestInternalService.createRequestInternal(
                        1L, "song", "artist", QueueType.MAIN
                )
        );

        verify(requestRepository, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    // =========================================================
    // USER NOT FOUND
    // =========================================================
    @Test
    void shouldThrow_whenUserNotFound() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                requestInternalService.createRequestInternal(
                        1L, "song", "artist", QueueType.MAIN
                )
        );
    }

    // =========================================================
    // NULL QUEUE COUNTER SAFETY
    // =========================================================
    @Test
    void shouldHandleNullQueueCounter_asZero() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByNormalizedKey(any())).thenReturn(false);

        when(queueCounterRepository.findForUpdate(QueueType.MAIN))
                .thenReturn(TestFactory.createQueueCounter(QueueType.MAIN, 0L));

        when(requestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Request result = requestInternalService
                .createRequestInternal(1L, "song", "artist", QueueType.MAIN);

        assertEquals(1000, result.getRequestOrder());
    }

    // =========================================================
    // DATABASE CONFLICT SIMULATION
    // =========================================================
    @Test
    void shouldThrowOnDataIntegrityViolation() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.existsByNormalizedKey(any())).thenReturn(false);
        when(queueCounterRepository.findForUpdate(QueueType.MAIN))
                .thenReturn(TestFactory.createQueueCounter(QueueType.MAIN, 0L));

        when(requestRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException("dup"));

        assertThrows(DataIntegrityViolationException.class, () ->
                requestInternalService.createRequestInternal(
                        1L, "song", "artist", QueueType.MAIN
                )
        );
    }
}