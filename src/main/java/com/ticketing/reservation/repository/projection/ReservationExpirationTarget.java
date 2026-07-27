package com.ticketing.reservation.repository.projection;

public record ReservationExpirationTarget(
        Long reservationId,
        Long performanceSeatId
) {
}
