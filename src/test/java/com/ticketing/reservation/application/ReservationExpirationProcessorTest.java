package com.ticketing.reservation.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.ticketing.performance.repository.PerformanceSeatRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.reservation.domain.ReservationStatusChangeActorType;
import com.ticketing.reservation.domain.ReservationStatusChangeReason;
import com.ticketing.reservation.domain.ReservationStatusHistory;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.repository.ReservationStatusHistoryRepository;
import com.ticketing.reservation.repository.projection.ReservationExpirationTarget;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationExpirationProcessorTest {

    private static final Long RESERVATION_ID = 1L;
    private static final Long PERFORMANCE_SEAT_ID = 10L;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PerformanceSeatRepository performanceSeatRepository;

    @Mock
    private ReservationStatusHistoryRepository statusHistoryRepository;

    @Mock
    private Reservation reservation;

    @InjectMocks
    private ReservationExpirationProcessor expirationProcessor;

    @Test
    void 만료_대상_예약을_만료하고_좌석을_복구한_뒤_만료_이력을_저장한다() {
        LocalDateTime now = LocalDateTime.now();

        ReservationExpirationTarget target =
                new ReservationExpirationTarget(
                        RESERVATION_ID,
                        PERFORMANCE_SEAT_ID
                );

        when(reservationRepository.expireIfReserved(
                RESERVATION_ID,
                now
        )).thenReturn(1L);

        when(performanceSeatRepository.releaseIfReserved(
                PERFORMANCE_SEAT_ID,
                now
        )).thenReturn(1L);

        when(reservationRepository.getReferenceById(
                RESERVATION_ID
        )).thenReturn(reservation);

        ReservationExpirationResult result =
                expirationProcessor.expire(target, now);

        assertThat(result)
                .isEqualTo(
                        ReservationExpirationResult.EXPIRED
                );

        InOrder inOrder = inOrder(
                reservationRepository,
                performanceSeatRepository,
                statusHistoryRepository
        );

        inOrder.verify(reservationRepository)
                .expireIfReserved(RESERVATION_ID, now);

        inOrder.verify(performanceSeatRepository)
                .releaseIfReserved(PERFORMANCE_SEAT_ID, now);

        inOrder.verify(reservationRepository)
                .getReferenceById(RESERVATION_ID);

        ArgumentCaptor<ReservationStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(
                        ReservationStatusHistory.class
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
                .isEqualTo(ReservationStatus.EXPIRED);
        assertThat(history.getChangeReason())
                .isEqualTo(
                        ReservationStatusChangeReason.RESERVATION_EXPIRED
                );
        assertThat(history.getActorType())
                .isEqualTo(
                        ReservationStatusChangeActorType.SYSTEM
                );
        assertThat(history.getActorReference()).isNull();
        assertThat(history.getChangedAt()).isEqualTo(now);
    }

    @Test
    void 실행_시점에_더_이상_만료_대상이_아니면_건너뛰고_이력도_저장하지_않는다() {
        LocalDateTime now = LocalDateTime.now();

        ReservationExpirationTarget target =
                new ReservationExpirationTarget(
                        RESERVATION_ID,
                        PERFORMANCE_SEAT_ID
                );

        when(reservationRepository.expireIfReserved(
                RESERVATION_ID,
                now
        )).thenReturn(0L);

        ReservationExpirationResult result =
                expirationProcessor.expire(target, now);

        assertThat(result)
                .isEqualTo(
                        ReservationExpirationResult.SKIPPED
                );

        verify(performanceSeatRepository, never())
                .releaseIfReserved(
                        PERFORMANCE_SEAT_ID,
                        now
                );

        verifyNoInteractions(statusHistoryRepository);
    }

    @Test
    void 예약은_만료됐지만_좌석_복구에_실패하면_예외가_발생하고_이력을_저장하지_않는다() {
        LocalDateTime now = LocalDateTime.now();

        ReservationExpirationTarget target =
                new ReservationExpirationTarget(
                        RESERVATION_ID,
                        PERFORMANCE_SEAT_ID
                );

        when(reservationRepository.expireIfReserved(
                RESERVATION_ID,
                now
        )).thenReturn(1L);

        when(performanceSeatRepository.releaseIfReserved(
                PERFORMANCE_SEAT_ID,
                now
        )).thenReturn(0L);

        assertThatThrownBy(() ->
                expirationProcessor.expire(target, now)
        )
                .isInstanceOfSatisfying(
                        CoreException.class,
                        exception -> assertThat(exception.getErrorType())
                                .isEqualTo(
                                        ErrorType
                                                .PERFORMANCE_SEAT_RELEASE_FAILED
                                )
                );

        verifyNoInteractions(statusHistoryRepository);
    }
}