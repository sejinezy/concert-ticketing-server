package com.ticketing.reservation.presentation.dto;

import com.ticketing.reservation.application.command.ReservationCancelCommand;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReservationCancelRequest(
        @NotNull UUID queueEntryId
) {

        public ReservationCancelCommand toCommand(Long reservationId) {
                return new ReservationCancelCommand(
                        reservationId,
                        queueEntryId
                );
        }
}
