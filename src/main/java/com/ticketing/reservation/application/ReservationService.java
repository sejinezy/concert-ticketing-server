package com.ticketing.reservation.application;

import com.ticketing.performance.domain.PerformanceSeat;
import com.ticketing.performance.repository.PerformanceSeatRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatusHistory;
import com.ticketing.reservation.presentation.dto.ReservationCancelRequest;
import com.ticketing.reservation.presentation.dto.ReservationCreateRequest;
import com.ticketing.reservation.presentation.dto.ReservationResponse;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.repository.ReservationStatusHistoryRepository;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final ReservationStatusHistoryRepository statusHistoryRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              PerformanceSeatRepository performanceSeatRepository,
                              ReservationStatusHistoryRepository statusHistoryRepository) {
        this.reservationRepository = reservationRepository;
        this.performanceSeatRepository = performanceSeatRepository;
        this.statusHistoryRepository = statusHistoryRepository;
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

        statusHistoryRepository.save(
                ReservationStatusHistory.createdByQueueEntry(save, reservedAt)
        );

        return ReservationResponse.from(save);
    }

    public ReservationResponse get(Long reservationId) {
        Reservation reservation = getReservation(reservationId);

        return ReservationResponse.from(reservation);
    }

    @Transactional
    public void cancel(
            Long reservationId,
            ReservationCancelRequest request
    ) {
        Reservation reservation = getReservation(reservationId);
        reservation.validateOwner(request.queueEntryId());

        LocalDateTime now = LocalDateTime.now();

        cancelReservation(reservationId, now);
        releasePerformanceSeat(reservation.getPerformanceSeat().getId(), now);

        statusHistoryRepository.save(
                ReservationStatusHistory.cancelledByQueueEntry(reservation, now)
        );

    }

    private Reservation getReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() ->
                        new CoreException(ErrorType.RESERVATION_NOT_FOUND)
                );
    }

    private void cancelReservation(Long reservationId, LocalDateTime now) {
        long cancelledCount = reservationRepository.cancelIfReserved(reservationId, now);

        if (cancelledCount != 1L) {
            throw new CoreException(ErrorType.RESERVATION_NOT_CANCELLABLE);
        }
    }

    private void releasePerformanceSeat(Long performanceSeatId, LocalDateTime now) {
        long releasedSeatCount = performanceSeatRepository.releaseIfReserved(performanceSeatId, now);

        if (releasedSeatCount != 1L) {
            throw new CoreException(ErrorType.PERFORMANCE_SEAT_RELEASE_FAILED);
        }
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
