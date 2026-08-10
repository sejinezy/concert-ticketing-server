package com.ticketing.performance.presentation.dto;

public record PerformanceSeatCreateResponse(
        Long performanceId,
        Long createdSeatCount
) {
}
