package com.ticketing.reservation.application.command;

import java.util.UUID;

public record ReservationCreateCommand(
        String idempotencyKey,
        Long performanceSeatId,
        UUID queueEntryId

) {
}
