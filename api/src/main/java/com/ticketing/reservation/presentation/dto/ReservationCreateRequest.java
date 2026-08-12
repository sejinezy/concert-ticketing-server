package com.ticketing.reservation.presentation.dto;

import com.ticketing.reservation.application.command.ReservationCreateCommand;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReservationCreateRequest(
        @NotNull UUID queueEntryId
        ) {

        public ReservationCreateCommand toCommand(
                String idempotencyKey,
                Long performanceSeatId
        ) {
                return new ReservationCreateCommand(
                        idempotencyKey,
                        performanceSeatId,
                        queueEntryId
                );
        }
}
