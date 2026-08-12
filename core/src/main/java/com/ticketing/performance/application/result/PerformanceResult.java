package com.ticketing.performance.application.result;

import java.time.LocalDateTime;

public record PerformanceResult(
        Long id,
        Long eventId,
        String eventTitle,
        Long venueId,
        String venueName,
        LocalDateTime performanceStartAt,
        LocalDateTime bookingOpenAt,
        LocalDateTime bookingCloseAt
) {
}
