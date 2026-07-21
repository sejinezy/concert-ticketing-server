package com.ticketing.reservation.repository;

import com.ticketing.reservation.domain.ReservationStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationStatusHistoryRepository extends JpaRepository<ReservationStatusHistory, Long> {

    List<ReservationStatusHistory> findAllByReservation_IdOrderByChangedAtAscIdAsc(Long reservationId);
}
