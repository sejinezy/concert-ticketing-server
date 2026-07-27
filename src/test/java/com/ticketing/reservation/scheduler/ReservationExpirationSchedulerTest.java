package com.ticketing.reservation.scheduler;

import static org.mockito.Mockito.*;

import com.ticketing.reservation.application.ReservationExpirationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationExpirationSchedulerTest {

    @Mock
    private ReservationExpirationService expirationService;

    @InjectMocks
    private ReservationExpirationScheduler scheduler;

    @Test
    void 예약_만료_배치_서비스를_호출한다() {
        scheduler.expireReservations();
        verify(expirationService).expireReservations();

    }

}