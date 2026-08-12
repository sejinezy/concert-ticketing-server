package com.ticketing.performance.application.command;

import java.time.LocalDateTime;

public record PerformanceUpdateCommand(
        Long performanceId,
        Long venueId,
        LocalDateTime performanceStartAt,
        LocalDateTime bookingOpenAt,
        LocalDateTime bookingCloseAt
) {
}
