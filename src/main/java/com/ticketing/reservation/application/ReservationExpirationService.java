package com.ticketing.reservation.application;

import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.repository.projection.ReservationExpirationTarget;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReservationExpirationService {

    private final ReservationRepository reservationRepository;
    private final ReservationExpirationProcessor expirationProcessor;
    private final int batchSize;


    public ReservationExpirationService(ReservationRepository reservationRepository,
                                        ReservationExpirationProcessor expirationProcessor,
                                        @Value("${reservation.expiration.batch-size:100}")
                                        int batchSize) {
        this.reservationRepository = reservationRepository;
        this.expirationProcessor = expirationProcessor;
        this.batchSize = batchSize;
    }

    public void expireReservations() {
        LocalDateTime now = LocalDateTime.now();

        List<ReservationExpirationTarget> targets = reservationRepository.findExpirationTargets(now,
                batchSize);

        if (targets.isEmpty()) {
            return;
        }

        int expiredCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (ReservationExpirationTarget target : targets) {
            try {
                ReservationExpirationResult result = expirationProcessor.expire(target, now);

                if (result == ReservationExpirationResult.EXPIRED) {
                    expiredCount++;
                } else {
                    skippedCount++;
                }
            } catch (RuntimeException exception) {
                failedCount++;

                log.error(
                        "예약 만료 처리에 실패했습니다. reservationId={}, performanceSeatId={}",
                        target.reservationId(),
                        target.performanceSeatId(),
                        exception
                );
            }
        }
        log.info(
                "예약 만료 배치가 완료되었습니다. targetCount={}, expiredCount={}, "
                        + "skippedCount={}, failedCount={}",
                targets.size(),
                expiredCount,
                skippedCount,
                failedCount
        );
    }
}
