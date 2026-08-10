package com.ticketing.reservation.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ticketing.idempotency.application.IdempotencyExecution;
import com.ticketing.idempotency.application.IdempotencyRequestHasher;
import com.ticketing.idempotency.application.IdempotencyService;
import com.ticketing.idempotency.domain.IdempotencyOperation;
import com.ticketing.idempotency.domain.IdempotencyRequest;
import com.ticketing.performance.domain.PerformanceSeat;
import com.ticketing.performance.repository.PerformanceSeatRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.reservation.domain.ReservationStatusChangeActorType;
import com.ticketing.reservation.domain.ReservationStatusChangeReason;
import com.ticketing.reservation.domain.ReservationStatusHistory;
import com.ticketing.reservation.presentation.dto.ReservationCancelRequest;
import com.ticketing.reservation.presentation.dto.ReservationCreateRequest;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.repository.ReservationStatusHistoryRepository;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final Long RESERVATION_ID = 1L;
    private static final Long PERFORMANCE_SEAT_ID = 10L;
    private static final String IDEMPOTENCY_KEY = "7cb0d4b8-88ac-4e1e-91a3-4b875c67e840";
    private static final String REQUEST_HASH = "a".repeat(64);


    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PerformanceSeatRepository performanceSeatRepository;

    @Mock
    private Reservation reservation;

    @Mock
    private PerformanceSeat performanceSeat;

    @Mock
    private ReservationStatusHistoryRepository statusHistoryRepository;

    @Mock
    private IdempotencyService idempotencyService;

    @Mock
    private IdempotencyRequestHasher idempotencyRequestHasher;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void 예약을_생성하고_RESERVED_상태_이력을_저장한다() {
        UUID queueEntryId = UUID.randomUUID();

        ReservationCreateRequest request =
                new ReservationCreateRequest(queueEntryId);

        IdempotencyExecution execution =
                IdempotencyExecution.first(
                        mock(IdempotencyRequest.class)
                );

        when(idempotencyRequestHasher
                .hashReservationCreate(
                        PERFORMANCE_SEAT_ID,
                        queueEntryId
                ))
                .thenReturn(REQUEST_HASH);

        when(idempotencyService.begin(
                IDEMPOTENCY_KEY,
                IdempotencyOperation.CREATE_RESERVATION,
                REQUEST_HASH
        ))
                .thenReturn(execution);

        when(performanceSeatRepository.reserveIfAvailable(
                eq(PERFORMANCE_SEAT_ID),
                any(LocalDateTime.class)
        )).thenReturn(1L);

        when(performanceSeatRepository.getReferenceById(
                PERFORMANCE_SEAT_ID
        )).thenReturn(performanceSeat);

        when(performanceSeat.getId())
                .thenReturn(PERFORMANCE_SEAT_ID);

        when(reservationRepository.save(
                any(Reservation.class)
        ))
                .thenAnswer(invocation -> {
                    Reservation savedReservation =
                            invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            savedReservation,
                            "id",
                            RESERVATION_ID
                    );

                    return savedReservation;
                });

        reservationService.create(IDEMPOTENCY_KEY, PERFORMANCE_SEAT_ID, request);

        ArgumentCaptor<ReservationStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(
                        ReservationStatusHistory.class
                );

        verify(statusHistoryRepository)
                .save(historyCaptor.capture());

        verify(idempotencyRequestHasher)
                .hashReservationCreate(
                        PERFORMANCE_SEAT_ID,
                        queueEntryId
                );

        verify(idempotencyService)
                .begin(
                        IDEMPOTENCY_KEY,
                        IdempotencyOperation.CREATE_RESERVATION,
                        REQUEST_HASH
                );

        verify(idempotencyService)
                .complete(
                        execution,
                        RESERVATION_ID
                );

        ReservationStatusHistory history =
                historyCaptor.getValue();

        assertThat(history.getPreviousStatus()).isNull();
        assertThat(history.getChangedStatus())
                .isEqualTo(ReservationStatus.RESERVED);
        assertThat(history.getChangeReason())
                .isEqualTo(
                        ReservationStatusChangeReason.RESERVATION_CREATED
                );
        assertThat(history.getActorType())
                .isEqualTo(
                        ReservationStatusChangeActorType.QUEUE_ENTRY
                );
        assertThat(history.getActorReference())
                .isEqualTo(queueEntryId.toString());
    }

    @Test
    void 예약을_취소하고_회차_좌석을_복구하고_상태_이력을_저장한다() {
        UUID queueEntryId = UUID.randomUUID();
        ReservationCancelRequest request = new ReservationCancelRequest(queueEntryId);

        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(reservation));

        when(reservation.getQueueEntryId())
                .thenReturn(queueEntryId);

        when(reservation.getPerformanceSeat())
                .thenReturn(performanceSeat);

        when(performanceSeat.getId())
                .thenReturn(PERFORMANCE_SEAT_ID);

        when(reservationRepository.cancelIfReserved(
                eq(RESERVATION_ID),
                any(LocalDateTime.class)
        )).thenReturn(1L);

        when(performanceSeatRepository.releaseIfReserved(
                eq(PERFORMANCE_SEAT_ID),
                any(LocalDateTime.class)
        )).thenReturn(1L);

        reservationService.cancel(RESERVATION_ID, request);

        verify(reservation).validateOwner(queueEntryId);

        ArgumentCaptor<ReservationStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(ReservationStatusHistory.class);

        InOrder inOrder = inOrder(
                reservationRepository,
                performanceSeatRepository,
                statusHistoryRepository
        );

        inOrder.verify(reservationRepository)
                .cancelIfReserved(
                        eq(RESERVATION_ID),
                        any(LocalDateTime.class)
                );

        inOrder.verify(performanceSeatRepository)
                .releaseIfReserved(
                        eq(PERFORMANCE_SEAT_ID),
                        any(LocalDateTime.class)
                );

        inOrder.verify(statusHistoryRepository)
                .save(historyCaptor.capture());

        ReservationStatusHistory history =
                historyCaptor.getValue();

        assertThat(history.getReservation())
                .isSameAs(reservation);

        assertThat(history.getPreviousStatus())
                .isEqualTo(ReservationStatus.RESERVED);

        assertThat(history.getChangedStatus())
                .isEqualTo(ReservationStatus.CANCELLED);

        assertThat(history.getChangeReason())
                .isEqualTo(
                        ReservationStatusChangeReason.CUSTOMER_CANCELLED
                );

        assertThat(history.getActorType())
                .isEqualTo(
                        ReservationStatusChangeActorType.QUEUE_ENTRY
                );

        assertThat(history.getActorReference())
                .isEqualTo(queueEntryId.toString());

        assertThat(history.getChangedAt())
                .isNotNull();
    }

    @Test
    void 존재하지_않는_예약은_취소할_수_없고_이력도_저장하지_않는다() {
        ReservationCancelRequest request =
                new ReservationCancelRequest(UUID.randomUUID());

        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                reservationService.cancel(RESERVATION_ID, request))
                .isInstanceOfSatisfying(
                        CoreException.class,
                        exception -> assertThat(exception.getErrorType())
                                .isEqualTo(ErrorType.RESERVATION_NOT_FOUND)
                );

        verify(reservationRepository).findById(RESERVATION_ID);

        verify(reservationRepository, never())
                .cancelIfReserved(
                        any(Long.class),
                        any(LocalDateTime.class)
                );

        verifyNoInteractions(
                performanceSeatRepository,
                statusHistoryRepository
        );
    }

    @Test
    void 예약_생성자와_queueEntryId가_다르면_취소할_수_없고_이력도_저장하지_않는다() {
        UUID queueEntryId = UUID.randomUUID();
        ReservationCancelRequest request = new ReservationCancelRequest(queueEntryId);

        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(reservation));

        doThrow(new CoreException(ErrorType.RESERVATION_ACCESS_DENIED))
                .when(reservation).validateOwner(queueEntryId);

        assertThatThrownBy(() ->
                reservationService.cancel(RESERVATION_ID, request))
                .isInstanceOfSatisfying(
                        CoreException.class,
                        exception -> assertThat(exception.getErrorType())
                                .isEqualTo(
                                        ErrorType.RESERVATION_ACCESS_DENIED
                                )
                );

        verify(reservationRepository, never())
                .cancelIfReserved(
                        any(Long.class),
                        any(LocalDateTime.class)
                );

        verifyNoInteractions(
                performanceSeatRepository,
                statusHistoryRepository
        );
    }

    @Test
    void 이미_상태가_변경된_예약은_취소할_수_없고_이력도_저장하지_않는다() {
        UUID queueEntryId = UUID.randomUUID();

        ReservationCancelRequest request =
                new ReservationCancelRequest(queueEntryId);

        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.cancelIfReserved(
                eq(RESERVATION_ID),
                any(LocalDateTime.class)
        )).thenReturn(0L);

        assertThatThrownBy(() ->
                reservationService.cancel(RESERVATION_ID, request))
                .isInstanceOfSatisfying(
                        CoreException.class,
                        exception -> assertThat(exception.getErrorType())
                                .isEqualTo(ErrorType.RESERVATION_NOT_CANCELLABLE)
                );

        verifyNoInteractions(
                performanceSeatRepository,
                statusHistoryRepository
        );
    }

    @Test
    void 좌석_복구에_실패하면_예외가_발생하고_이력도_저장하지_않는다() {
        UUID queueEntryId = UUID.randomUUID();
        ReservationCancelRequest request = new ReservationCancelRequest(queueEntryId);

        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(reservation));

        when(reservation.getPerformanceSeat())
                .thenReturn(performanceSeat);

        when(performanceSeat.getId())
                .thenReturn(PERFORMANCE_SEAT_ID);

        when(reservationRepository.cancelIfReserved(
                eq(RESERVATION_ID),
                any(LocalDateTime.class)
        )).thenReturn(1L);

        when(performanceSeatRepository.releaseIfReserved(
                eq(PERFORMANCE_SEAT_ID),
                any(LocalDateTime.class)
        )).thenReturn(0L);

        assertThatThrownBy(() ->
                reservationService.cancel(RESERVATION_ID, request))
                .isInstanceOfSatisfying(
                        CoreException.class,
                        exception -> assertThat(exception.getErrorType())
                                .isEqualTo(ErrorType.PERFORMANCE_SEAT_RELEASE_FAILED)
                );

        verifyNoInteractions(statusHistoryRepository);
    }
}