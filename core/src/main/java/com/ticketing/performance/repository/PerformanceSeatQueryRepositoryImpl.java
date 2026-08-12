package com.ticketing.performance.repository;

import static com.ticketing.performance.domain.QPerformanceSeat.performanceSeat;
import static com.ticketing.venue.domain.QVenueSeat.venueSeat;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.performance.application.result.PerformanceSeatResult;
import com.ticketing.performance.domain.PerformanceSeatStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PerformanceSeatQueryRepositoryImpl implements PerformanceSeatQueryRepository {


    private final JPAQueryFactory queryFactory;


    @Override
    public List<PerformanceSeatResult> findAllByPerformanceId(Long performanceId) {
        return queryFactory
                .select(Projections.constructor(
                        PerformanceSeatResult.class,
                        performanceSeat.id,
                        venueSeat.id,
                        venueSeat.section,
                        venueSeat.rowLabel,
                        venueSeat.seatNo,
                        venueSeat.seatCode,
                        performanceSeat.status

                ))
                .from(performanceSeat)
                .join(performanceSeat.venueSeat, venueSeat)
                .where(performanceSeat.performance.id.eq(performanceId))
                .orderBy(
                        venueSeat.section.asc(),
                        venueSeat.rowLabel.asc(),
                        venueSeat.seatNo.asc()
                )
                .fetch();
    }

    @Override
    public long reserveIfAvailable(Long performanceSeatId, LocalDateTime updatedAt) {
        return queryFactory
                .update(performanceSeat)
                .set(
                        performanceSeat.status,
                        PerformanceSeatStatus.RESERVED
                )
                .set(performanceSeat.updatedAt, updatedAt)
                .where(
                        performanceSeat.id.eq(performanceSeatId),
                        performanceSeat.status.eq(PerformanceSeatStatus.AVAILABLE)
                )
                .execute();
    }

    @Override
    public long releaseIfReserved(Long performanceSeatId, LocalDateTime updatedAt) {
        return queryFactory
                .update(performanceSeat)
                .set(
                        performanceSeat.status,
                        PerformanceSeatStatus.AVAILABLE
                )
                .set(performanceSeat.updatedAt, updatedAt)
                .where(
                        performanceSeat.id.eq(performanceSeatId),
                        performanceSeat.status.eq(PerformanceSeatStatus.RESERVED)
                )
                .execute();
    }
}
