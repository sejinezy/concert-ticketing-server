package com.ticketing.performance.repository;

import com.ticketing.performance.domain.PerformanceSeat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceSeatRepository extends JpaRepository<PerformanceSeat, Long>,
        PerformanceSeatQueryRepository {


    boolean existsByPerformance_Id(Long performanceId);
}
