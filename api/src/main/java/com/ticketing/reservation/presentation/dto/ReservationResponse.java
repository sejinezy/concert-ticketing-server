package com.ticketing.reservation.presentation.dto;

import com.ticketing.reservation.application.result.ReservationResult;
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

    public static ReservationResponse from(ReservationResult result) {
        return new ReservationResponse(
                result.reservationId(),
                result.queueEntryId(),
                result.performanceSeatId(),
                result.status(),
                result.expiresAt()
        );
    }
}
