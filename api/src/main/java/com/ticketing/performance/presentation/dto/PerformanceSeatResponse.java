package com.ticketing.performance.presentation.dto;

import com.ticketing.performance.application.result.PerformanceSeatResult;
import com.ticketing.performance.domain.PerformanceSeatStatus;

public record PerformanceSeatResponse(
        Long performanceSeatId,
        Long venueSeatId,
        String section,
        String rowLevel,
        String seatNo,
        String seatCode,
        PerformanceSeatStatus status
) {

    public static PerformanceSeatResponse from(
            PerformanceSeatResult result
    ) {
        return new PerformanceSeatResponse(
                result.performanceSeatId(),
                result.venueSeatId(),
                result.section(),
                result.rowLevel(),
                result.seatNo(),
                result.seatCode(),
                result.status()
        );
    }
}
