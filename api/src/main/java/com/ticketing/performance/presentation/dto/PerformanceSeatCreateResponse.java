package com.ticketing.performance.presentation.dto;

import com.ticketing.performance.application.result.PerformanceSeatCreateResult;

public record PerformanceSeatCreateResponse(
        Long performanceId,
        Long createdSeatCount
) {

    public static PerformanceSeatCreateResponse from(
            PerformanceSeatCreateResult result
    ) {
        return new PerformanceSeatCreateResponse(
                result.performanceId(),
                result.createdSeatCount()
        );
    }
}
