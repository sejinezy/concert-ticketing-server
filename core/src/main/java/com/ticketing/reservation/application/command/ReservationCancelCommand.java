package com.ticketing.reservation.application.command;

import java.util.UUID;

public record ReservationCancelCommand(
        Long reservationId,
        UUID queueEntryId
) {
}
