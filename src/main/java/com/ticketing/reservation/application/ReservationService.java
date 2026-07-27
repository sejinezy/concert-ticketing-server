package com.ticketing.reservation.application;

import com.ticketing.performance.domain.PerformanceSeat;
import com.ticketing.performance.repository.PerformanceSeatRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.presentation.dto.ReservationCreateRequest;
import com.ticketing.reservation.presentation.dto.ReservationResponse;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PerformanceSeatRepository performanceSeatRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              PerformanceSeatRepository performanceSeatRepository) {
        this.reservationRepository = reservationRepository;
        this.performanceSeatRepository = performanceSeatRepository;
    }

    @Transactional
    public ReservationResponse create(
            Long performanceSeatId,
            ReservationCreateRequest request
    ) {
        LocalDateTime reservedAt = LocalDateTime.now();

        long updatedCount = performanceSeatRepository.reserveIfAvailable(
                performanceSeatId,
                reservedAt
        );

        validateReservationResult(performanceSeatId, updatedCount);

        PerformanceSeat performanceSeat = performanceSeatRepository.getReferenceById(performanceSeatId);

        Reservation reservation = Reservation.create(request.queueEntryId(), performanceSeat, reservedAt);

        Reservation save = reservationRepository.save(reservation);

        return ReservationResponse.from(save);
    }

    public ReservationResponse get(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CoreException(ErrorType.RESERVATION_NOT_FOUND));

        return ReservationResponse.from(reservation);
    }

    private void validateReservationResult(Long performanceSeatId, long updatedCount) {
        if (updatedCount == 1) {
            return;
        }

        if (!performanceSeatRepository.existsById(performanceSeatId)) {
            throw new CoreException(ErrorType.PERFORMANCE_SEAT_NOT_FOUND);
        }

        throw new CoreException(ErrorType.PERFORMANCE_SEAT_ALREADY_RESERVED);
    }
}
