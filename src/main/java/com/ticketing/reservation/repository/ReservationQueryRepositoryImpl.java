package com.ticketing.reservation.repository;

import static com.ticketing.reservation.domain.QReservation.reservation;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ticketing.reservation.domain.ReservationStatus;
import java.time.LocalDateTime;

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
    public long expireIfReserved(Long reservationId, LocalDateTime cutoffAt) {
        return queryFactory
                .update(reservation)
                .set(
                        reservation.status,
                        ReservationStatus.EXPIRED
                )
                .set(reservation.updatedAt, cutoffAt)
                .where(
                        reservation.id.eq(reservationId),
                        reservation.status.eq(ReservationStatus.RESERVED),
                        reservation.expiresAt.loe(cutoffAt)
                )
                .execute();
    }

}
