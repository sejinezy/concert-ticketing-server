package com.ticketing.reservation.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.repository.projection.ReservationExpirationTarget;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationExpirationServiceTest {

    private static final int BATCH_SIZE = 100;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationExpirationProcessor expirationProcessor;

    private ReservationExpirationService expirationService;

    @BeforeEach
    void setUp() {
        expirationService = new ReservationExpirationService(
                reservationRepository,
                expirationProcessor,
                BATCH_SIZE
        );
    }

    @Test
    void 만료_대상이_없으면_단건_처리를_호출하지_않는다() {
        when(reservationRepository.findExpirationTargets(
                any(LocalDateTime.class),
                eq(BATCH_SIZE)
        )).thenReturn(List.of());

        expirationService.expireReservations();

        verifyNoInteractions(expirationProcessor);
    }

    @Test
    void 조회된_예약을_각각_만료_처리한다() {
        ReservationExpirationTarget first = new ReservationExpirationTarget(1L, 10L);
        ReservationExpirationTarget second = new ReservationExpirationTarget(2L, 20L);

        when(reservationRepository.findExpirationTargets(
                any(LocalDateTime.class),
                eq(BATCH_SIZE)
        )).thenReturn(List.of(first, second));

        when(expirationProcessor.expire(
                eq(first),
                any(LocalDateTime.class)
        )).thenReturn(ReservationExpirationResult.EXPIRED);

        when(expirationProcessor.expire(
                eq(second),
                any(LocalDateTime.class)
        )).thenReturn(ReservationExpirationResult.SKIPPED);

        expirationService.expireReservations();

        verify(expirationProcessor).expire(
                eq(first),
                any(LocalDateTime.class)
        );

        verify(expirationProcessor).expire(
                eq(second),
                any(LocalDateTime.class)
        );
    }

    @Test
    void 한_예약의_만료_처리가_실패해도_다음_예약을_계속_처리한다() {
        ReservationExpirationTarget failedTarget = new ReservationExpirationTarget(1L, 10L);
        ReservationExpirationTarget nextTarget = new ReservationExpirationTarget(2L, 20L);

        when(reservationRepository.findExpirationTargets(
                any(LocalDateTime.class),
                eq(BATCH_SIZE)
        )).thenReturn(List.of(failedTarget, nextTarget));

        when(expirationProcessor.expire(
                eq(failedTarget),
                any(LocalDateTime.class)
        )).thenThrow(new RuntimeException("단건 처리 실패"));

        when(expirationProcessor.expire(
                eq(nextTarget),
                any(LocalDateTime.class)
        )).thenReturn(ReservationExpirationResult.EXPIRED);

        assertThatCode(
                expirationService::expireReservations
        ).doesNotThrowAnyException();

        verify(expirationProcessor).expire(
                eq(nextTarget),
                any(LocalDateTime.class)
        );
    }


}