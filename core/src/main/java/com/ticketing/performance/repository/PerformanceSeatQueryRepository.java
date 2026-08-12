package com.ticketing.performance.repository;

import com.ticketing.performance.application.result.PerformanceSeatResult;
import java.time.LocalDateTime;
import java.util.List;

public interface PerformanceSeatQueryRepository {

    List<PerformanceSeatResult> findAllByPerformanceId(Long performanceId);

    long reserveIfAvailable(Long performanceSeatId, LocalDateTime updatedAt);

    long releaseIfReserved(Long performanceSeatId, LocalDateTime updatedAt);
}
