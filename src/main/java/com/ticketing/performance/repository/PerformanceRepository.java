package com.ticketing.performance.repository;

import com.ticketing.performance.domain.Performance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerformanceRepository extends JpaRepository<Performance, Long>, PerformanceQueryRepository {

}
