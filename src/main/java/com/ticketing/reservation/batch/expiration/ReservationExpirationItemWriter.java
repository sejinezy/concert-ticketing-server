package com.ticketing.reservation.batch.expiration;


import com.ticketing.reservation.application.ReservationExpirationProcessor;
import com.ticketing.reservation.application.ReservationExpirationResult;
import com.ticketing.reservation.repository.projection.ReservationExpirationTarget;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class ReservationExpirationItemWriter implements ItemWriter<ReservationExpirationTarget> {

    private final ReservationExpirationProcessor expirationProcessor;
    private final LocalDateTime cutoffAt;

    public ReservationExpirationItemWriter(
            ReservationExpirationProcessor expirationProcessor,
            @Value("#{jobParameters['cutoffAt']}")
            LocalDateTime cutoffAt
    ) {
        if (cutoffAt == null) {
            throw new IllegalArgumentException("cutoffAt JobParameter는 필수입니다.");
        }

        this.expirationProcessor = expirationProcessor;
        this.cutoffAt = cutoffAt;
    }

    @Override
    public void write(Chunk<? extends ReservationExpirationTarget> chunk) {

        int expiredCount = 0;
        int skippedCount = 0;

        for (ReservationExpirationTarget target : chunk) {
            ReservationExpirationResult result = expirationProcessor.expire(target, cutoffAt);

            if (result == ReservationExpirationResult.EXPIRED) {
                expiredCount++;
            } else {
                skippedCount++;
            }
        }

        log.info(
                "예약 만료 Chunk 처리가 완료되었습니다. "
                        + "targetCount={}, expiredCount={}, "
                        + "skippedCount={}, cutoffAt={}",
                chunk.size(),
                expiredCount,
                skippedCount,
                cutoffAt
        );
    }
}
