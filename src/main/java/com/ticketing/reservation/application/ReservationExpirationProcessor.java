package com.ticketing.reservation.application;

import com.ticketing.performance.repository.PerformanceSeatRepository;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.repository.projection.ReservationExpirationTarget;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationExpirationProcessor {

    private final ReservationRepository reservationRepository;
    private final PerformanceSeatRepository performanceSeatRepository;

    public ReservationExpirationProcessor(ReservationRepository reservationRepository,
                                          PerformanceSeatRepository performanceSeatRepository) {
        this.reservationRepository = reservationRepository;
        this.performanceSeatRepository = performanceSeatRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReservationExpirationResult expire(
            ReservationExpirationTarget target,
            LocalDateTime now
    ) {
        boolean expired = expireReservationIfStillEligible(target.reservationId(), now);

        if (!expired) {
            return ReservationExpirationResult.SKIPPED;
        }

        releasePerformanceSeat(target.performanceSeatId(), now);

        return ReservationExpirationResult.EXPIRED;
    }

    private boolean expireReservationIfStillEligible(Long reservationId, LocalDateTime now) {
        long updatedCount = reservationRepository.expireIfReserved(reservationId, now);

        return updatedCount == 1L;

    }

    private void releasePerformanceSeat(Long performanceSeatId, LocalDateTime now) {
        long releasedCount = performanceSeatRepository.releaseIfReserved(performanceSeatId, now);

        if (releasedCount != 1L) {
            throw new CoreException(ErrorType.PERFORMANCE_SEAT_RELEASE_FAILED);
        }
    }
}
