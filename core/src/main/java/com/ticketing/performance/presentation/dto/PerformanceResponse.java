package com.ticketing.performance.presentation.dto;

import java.time.LocalDateTime;

public record PerformanceResponse(
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
