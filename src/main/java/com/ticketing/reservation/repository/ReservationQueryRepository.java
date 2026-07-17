package com.ticketing.reservation.repository;

import com.ticketing.reservation.repository.projection.ReservationExpirationTarget;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationQueryRepository {

    long cancelIfReserved(
            Long reservationId,
            LocalDateTime updatedAt
    );

    long expireIfReserved(
            Long reservationId,
            LocalDateTime now
    );

    List<ReservationExpirationTarget> findExpirationTargets(
            LocalDateTime now,
            int batchSize
    );
}
