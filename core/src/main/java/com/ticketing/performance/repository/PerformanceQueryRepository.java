package com.ticketing.performance.repository;

import com.ticketing.performance.presentation.dto.PerformanceResponse;
import java.util.List;
import java.util.Optional;

public interface PerformanceQueryRepository {

    Optional<PerformanceResponse> findResponseById(Long performanceId);

    List<PerformanceResponse> findResponsesByEventId(Long eventId);
}
