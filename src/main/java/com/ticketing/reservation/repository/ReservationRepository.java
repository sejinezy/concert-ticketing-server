package com.ticketing.reservation.repository;

import com.ticketing.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long>,ReservationQueryRepository {

}
