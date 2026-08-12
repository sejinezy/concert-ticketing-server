package com.ticketing.performance.repository;

import static com.ticketing.event.domain.QEvent.event;
import static com.ticketing.performance.domain.QPerformance.performance;
import static com.ticketing.venue.domain.QVenue.venue;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.performance.application.result.PerformanceResult;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PerformanceQueryRepositoryImpl implements PerformanceQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<PerformanceResult> findResponseById(Long performanceId) {
        PerformanceResult result = queryFactory
                .select(Projections.constructor(
                        PerformanceResult.class,
                        performance.id,
                        event.id,
                        event.title,
                        venue.id,
                        venue.name,
                        performance.performanceStartAt,
                        performance.bookingOpenAt,
                        performance.bookingCloseAt
                ))
                .from(performance)
                .join(performance.event, event)
                .join(performance.venue, venue)
                .where(performance.id.eq(performanceId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public List<PerformanceResult> findResponsesByEventId(Long eventId) {
        return queryFactory
                .select(Projections.constructor(
                        PerformanceResult.class,
                        performance.id,
                        event.id,
                        event.title,
                        venue.id,
                        venue.name,
                        performance.performanceStartAt,
                        performance.bookingOpenAt,
                        performance.bookingCloseAt
                ))
                .from(performance)
                .join(performance.event, event)
                .join(performance.venue, venue)
                .where(event.id.eq(eventId))
                .orderBy(performance.performanceStartAt.asc())
                .fetch();
    }
}
