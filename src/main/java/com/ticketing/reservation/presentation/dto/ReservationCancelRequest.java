package com.ticketing.reservation.presentation.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReservationCancelRequest(
        @NotNull UUID queueEntryId
        ) {
}
