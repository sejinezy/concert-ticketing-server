package com.ticketing.reservation.scheduler;

import com.ticketing.reservation.application.ReservationExpirationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "reservation.expiration",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ReservationExpirationScheduler {

    private final ReservationExpirationService expirationService;

    public ReservationExpirationScheduler(ReservationExpirationService expirationService) {
        this.expirationService = expirationService;
    }

    @Scheduled(
            fixedDelayString = "${reservation.expiration.fixed-delay-ms:10000}",
            initialDelayString = "${reservation.expiration.initial-delay-ms:10000}"
    )
    public void expireReservations() {
        try {
            expirationService.expireReservations();
        } catch (RuntimeException exception) {
            log.error(
                    "예약 만료 배치 실행 중 오류가 발생했습니다.",
                    exception
            );
        }
    }
}
