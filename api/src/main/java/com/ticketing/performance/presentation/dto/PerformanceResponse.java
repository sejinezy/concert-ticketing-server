package com.ticketing.performance.presentation.dto;

import com.ticketing.performance.application.result.PerformanceResult;
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

    public static PerformanceResponse from(
            PerformanceResult result
    ) {
        return new PerformanceResponse(
                result.id(),
                result.eventId(),
                result.eventTitle(),
                result.venueId(),
                result.venueName(),
                result.performanceStartAt(),
                result.bookingOpenAt(),
                result.bookingCloseAt()
        );
    }
}
