package com.ticketing.reservation.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.ticketing.performance.repository.PerformanceSeatRepository;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.repository.projection.ReservationExpirationTarget;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @InjectMocks
    private ReservationExpirationProcessor expirationProcessor;

    @Test
    void 만료_대상_예약을_만료하고_좌석을_복구한다() {
        LocalDateTime now = LocalDateTime.now();

        ReservationExpirationTarget target = new ReservationExpirationTarget(RESERVATION_ID,
                PERFORMANCE_SEAT_ID);

        when(reservationRepository.expireIfReserved(
                RESERVATION_ID,
                now
        )).thenReturn(1L);

        when(performanceSeatRepository.releaseIfReserved(
                PERFORMANCE_SEAT_ID,
                now
        )).thenReturn(1L);

        ReservationExpirationResult result = expirationProcessor.expire(target, now);

        assertThat(result).isEqualTo(ReservationExpirationResult.EXPIRED);

        verify(performanceSeatRepository).releaseIfReserved(PERFORMANCE_SEAT_ID, now);
    }

    @Test
    void 실행_시점에_더_이상_만료_대상이_아니면_건너뛴다() {
        LocalDateTime now = LocalDateTime.now();

        ReservationExpirationTarget target = new ReservationExpirationTarget(RESERVATION_ID,
                PERFORMANCE_SEAT_ID);

        when(reservationRepository.expireIfReserved(
                RESERVATION_ID,
                now
        )).thenReturn(0L);

        ReservationExpirationResult result = expirationProcessor.expire(target, now);

        assertThat(result).isEqualTo(ReservationExpirationResult.SKIPPED);

        verify(performanceSeatRepository, never())
                .releaseIfReserved(PERFORMANCE_SEAT_ID, now);
    }

    @Test
    void 예약은_만료됐지만_좌석_복구에_실패하면_예외가_발생한다() {
        LocalDateTime now = LocalDateTime.now();

        ReservationExpirationTarget target = new ReservationExpirationTarget(RESERVATION_ID,
                PERFORMANCE_SEAT_ID);

        when(reservationRepository.expireIfReserved(
                RESERVATION_ID,
                now
        )).thenReturn(1L);

        when(performanceSeatRepository.releaseIfReserved(
                PERFORMANCE_SEAT_ID,
                now
        )).thenReturn(0L);

        assertThatThrownBy(() ->
                expirationProcessor.expire(target, now))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> {
                    CoreException coreException = (CoreException) exception;

                    assertThat(coreException.getErrorType())
                            .isEqualTo(ErrorType.PERFORMANCE_SEAT_RELEASE_FAILED);
                });
    }



}