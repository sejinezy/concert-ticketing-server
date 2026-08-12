package com.ticketing.reservation.application.result;

import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResult(
        Long reservationId,
        UUID queueEntryId,
        Long performanceSeatId,
        ReservationStatus status,
        LocalDateTime expiresAt
) {

    public static ReservationResult from(Reservation reservation) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getQueueEntryId(),
                reservation.getPerformanceSeat().getId(),
                reservation.getStatus(),
                reservation.getExpiresAt()
        );
    }

}
