package com.ticketing.performance.repository;

import com.ticketing.performance.presentation.dto.PerformanceSeatResponse;
import java.util.List;

public interface PerformanceSeatQueryRepository {

    List<PerformanceSeatResponse> findAllByPerformanceId(Long performanceId);
}
