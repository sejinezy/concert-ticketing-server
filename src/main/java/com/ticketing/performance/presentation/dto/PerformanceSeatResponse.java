package com.ticketing.performance.presentation.dto;

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
}
