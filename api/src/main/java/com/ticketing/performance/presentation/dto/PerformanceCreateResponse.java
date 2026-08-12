package com.ticketing.performance.presentation.dto;

import com.ticketing.performance.application.result.PerformanceCreateResult;

public record PerformanceCreateResponse(
        Long id
) {

    public static PerformanceCreateResponse from(
            PerformanceCreateResult result
    ) {
        return new PerformanceCreateResponse(
                result.id()
        );
    }
}
