package com.ticketing.reservation.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.ticketing.performance.domain.PerformanceSeat;
import com.ticketing.performance.repository.PerformanceSeatRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.presentation.dto.ReservationCancelRequest;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final Long RESERVATION_ID = 1L;
    private static final Long PERFORMANCE_SEAT_ID = 10L;


    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PerformanceSeatRepository performanceSeatRepository;

    @Mock
    private Reservation reservation;

    @Mock
    private PerformanceSeat performanceSeat;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void 예약을_취소하고_회차_좌석을_복구한다() {
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
        )).thenReturn(1L);

        reservationService.cancel(RESERVATION_ID, request);

        verify(reservation).validateOwner(queueEntryId);

        InOrder inOrder = inOrder(reservationRepository, performanceSeatRepository);

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
    }

    @Test
    void 존재하지_않는_예약은_취소할_수_없다() {
        ReservationCancelRequest request = new ReservationCancelRequest(UUID.randomUUID());

        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                        reservationService.cancel(RESERVATION_ID, request))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;

                    assertThat(coreException.getErrorType())
                            .isEqualTo(ErrorType.RESERVATION_NOT_FOUND);
                });

        verify(reservationRepository).findById(RESERVATION_ID);

        verify(reservationRepository, never())
                .cancelIfReserved(
                        any(Long.class),
                        any(LocalDateTime.class)
                );

        verifyNoInteractions(performanceSeatRepository);
    }

    @Test
    void 예약_생성자와_queueEntryId가_다르면_취소할_수_없다() {
        UUID queueEntryId = UUID.randomUUID();
        ReservationCancelRequest request = new ReservationCancelRequest(queueEntryId);

        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(reservation));

        doThrow(new CoreException(ErrorType.RESERVATION_ACCESS_DENIED))
                .when(reservation).validateOwner(queueEntryId);

        assertThatThrownBy(() ->
                reservationService.cancel(RESERVATION_ID, request))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;

                    assertThat(coreException.getErrorType())
                            .isEqualTo(ErrorType.RESERVATION_ACCESS_DENIED);
                });

        verify(reservationRepository, never())
                .cancelIfReserved(
                        any(Long.class),
                        any(LocalDateTime.class)
                );

        verifyNoInteractions(performanceSeatRepository);
    }

    @Test
    void 이미_상태가_변경된_예약은_취소할_수_없다() {
        UUID queueEntryId = UUID.randomUUID();
        ReservationCancelRequest request = new ReservationCancelRequest(queueEntryId);

        when(reservationRepository.findById(RESERVATION_ID))
                .thenReturn(Optional.of(reservation));

        when(reservationRepository.cancelIfReserved(
                eq(RESERVATION_ID),
                any(LocalDateTime.class)
        )).thenReturn(0L);

        assertThatThrownBy(() ->
                reservationService.cancel(RESERVATION_ID, request))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;

                    assertThat(coreException.getErrorType())
                            .isEqualTo(ErrorType.RESERVATION_NOT_CANCELLABLE);
                });

        verifyNoInteractions(performanceSeatRepository);
    }

    @Test
    void 좌석_복구에_실패하면_예외가_발생한다() {
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
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;

                    assertThat(coreException.getErrorType())
                            .isEqualTo(ErrorType.PERFORMANCE_SEAT_RELEASE_FAILED);
                });
    }
}