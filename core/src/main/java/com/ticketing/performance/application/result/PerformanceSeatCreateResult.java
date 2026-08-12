package com.ticketing.performance.application.result;

public record PerformanceSeatCreateResult(
        Long performanceId,
        Long createdSeatCount
) {
}
