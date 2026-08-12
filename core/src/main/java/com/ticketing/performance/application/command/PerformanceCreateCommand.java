package com.ticketing.performance.application.command;

import java.time.LocalDateTime;

public record PerformanceCreateCommand(
        Long eventId,
        Long venueId,
        LocalDateTime performanceStartAt,
        LocalDateTime bookingOpenAt,
        LocalDateTime bookingCloseAt
) {
}
