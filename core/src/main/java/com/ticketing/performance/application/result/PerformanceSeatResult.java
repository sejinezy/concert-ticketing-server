package com.ticketing.performance.application.result;

import com.ticketing.performance.domain.PerformanceSeatStatus;

public record PerformanceSeatResult(
        Long performanceSeatId,
        Long venueSeatId,
        String section,
        String rowLevel,
        String seatNo,
        String seatCode,
        PerformanceSeatStatus status
) {
}
