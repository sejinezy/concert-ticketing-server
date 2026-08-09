package com.ticketing.reservation.repository;

import java.time.LocalDateTime;

public interface ReservationQueryRepository {

    long cancelIfReserved(
            Long reservationId,
            LocalDateTime updatedAt
    );

    long expireIfReserved(
            Long reservationId,
            LocalDateTime now
    );
}
