package com.ticketing.reservation.repository;

import static com.ticketing.reservation.domain.QReservation.reservation;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.reservation.repository.projection.ReservationExpirationTarget;
import java.time.LocalDateTime;
import java.util.List;

public class ReservationQueryRepositoryImpl implements ReservationQueryRepository {

    private final JPAQueryFactory queryFactory;

    public ReservationQueryRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public long cancelIfReserved(Long reservationId, LocalDateTime updatedAt) {
        return queryFactory
                .update(reservation)
                .set(
                        reservation.status,
                        ReservationStatus.CANCELLED
                )
                .set(reservation.updatedAt, updatedAt)
                .where(
                        reservation.id.eq(reservationId),
                        reservation.status.eq(ReservationStatus.RESERVED)
                )
                .execute();
    }

    @Override
    public long expireIfReserved(Long reservationId, LocalDateTime now) {
        return queryFactory
                .update(reservation)
                .set(
                        reservation.status,
                        ReservationStatus.EXPIRED
                )
                .set(reservation.updatedAt, now)
                .where(
                        reservation.id.eq(reservationId),
                        reservation.status.eq(ReservationStatus.RESERVED),
                        reservation.expiresAt.loe(now)
                )
                .execute();
    }

    @Override
    public List<ReservationExpirationTarget> findExpirationTargets(LocalDateTime now, int batchSize) {
        return queryFactory
                .select(Projections.constructor(
                        ReservationExpirationTarget.class,
                        reservation.id,
                        reservation.performanceSeat.id
                ))
                .from(reservation)
                .where(
                        reservation.status.eq(ReservationStatus.RESERVED),
                        reservation.expiresAt.loe(now)
                )
                .orderBy(
                        reservation.expiresAt.asc(),
                        reservation.id.asc()
                )
                .limit(batchSize)
                .fetch();
    }
}
