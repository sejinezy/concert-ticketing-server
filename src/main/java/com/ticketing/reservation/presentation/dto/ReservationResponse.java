package com.ticketing.reservation.presentation.dto;

import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        Long reservationId,
        UUID queueEntryId,
        Long performanceSeatId,
        ReservationStatus status,
        LocalDateTime expiresAt
) {

    public static ReservationResponse from(
            Reservation reservation
    ) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getQueueEntryId(),
                reservation.getPerformanceSeat().getId(),
                reservation.getStatus(),
                reservation.getExpiresAt()
        );
    }
}
