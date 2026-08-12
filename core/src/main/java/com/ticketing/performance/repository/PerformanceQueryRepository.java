package com.ticketing.performance.repository;

import com.ticketing.performance.application.result.PerformanceResult;
import java.util.List;
import java.util.Optional;

public interface PerformanceQueryRepository {

    Optional<PerformanceResult> findResponseById(Long performanceId);

    List<PerformanceResult> findResponsesByEventId(Long eventId);
}
