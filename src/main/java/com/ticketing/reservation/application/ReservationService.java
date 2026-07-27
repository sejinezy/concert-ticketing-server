package com.ticketing.reservation.application;

import com.ticketing.idempotency.application.IdempotencyExecution;
import com.ticketing.idempotency.application.IdempotencyRequestHasher;
import com.ticketing.idempotency.application.IdempotencyService;
import com.ticketing.idempotency.domain.IdempotencyOperation;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final ReservationStatusHistoryRepository statusHistoryRepository;
    private final IdempotencyService idempotencyService;
    private final IdempotencyRequestHasher idempotencyRequestHasher;

    public ReservationService(ReservationRepository reservationRepository,
                              PerformanceSeatRepository performanceSeatRepository,
                              ReservationStatusHistoryRepository statusHistoryRepository,
                              IdempotencyService itempotencyService, IdempotencyRequestHasher idempotencyRequestHasher) {
        this.reservationRepository = reservationRepository;
        this.performanceSeatRepository = performanceSeatRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.idempotencyService = itempotencyService;
        this.idempotencyRequestHasher = idempotencyRequestHasher;
    }

    @Transactional
    public ReservationResponse create(
            String idempotencyKey,
            Long performanceSeatId,
            ReservationCreateRequest request
    ) {
        IdempotencyExecution execution = beginIdempotentReservation(idempotencyKey, performanceSeatId,
                request);

        if (execution.isReplay()) {
            return replayReservation(execution);
        }

        Reservation reservation = createAndRecordReservation(performanceSeatId, request);

        completeIdempotentReservation(execution, reservation);

        return ReservationResponse.from(reservation);
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

    private IdempotencyExecution beginIdempotentReservation(
            String idempotencyKey,
            Long performanceSeatId,
            ReservationCreateRequest request
    ) {
        String requestHash =
                idempotencyRequestHasher
                        .hashReservationCreate(
                                performanceSeatId,
                                request.queueEntryId()
                        );

        return idempotencyService.begin(
                idempotencyKey,
                IdempotencyOperation.CREATE_RESERVATION,
                requestHash
        );
    }

    private ReservationResponse replayReservation(
            IdempotencyExecution execution
    ) {
        return getIdempotentReservationResult(
                execution.getCompletedResultId()
        );
    }

    private Reservation createAndRecordReservation(
            Long performanceSeatId,
            ReservationCreateRequest request
    ) {
        LocalDateTime reservedAt =
                LocalDateTime.now();

        Reservation reservation =
                createReservation(
                        performanceSeatId,
                        request,
                        reservedAt
                );

        saveReservationCreatedHistory(
                reservation,
                reservedAt
        );

        return reservation;
    }

    private void saveReservationCreatedHistory(
            Reservation reservation,
            LocalDateTime reservedAt
    ) {
        statusHistoryRepository.save(
                ReservationStatusHistory
                        .createdByQueueEntry(
                                reservation,
                                reservedAt
                        )
        );
    }

    private void completeIdempotentReservation(
            IdempotencyExecution execution,
            Reservation reservation
    ) {
        idempotencyService.complete(
                execution,
                reservation.getId()
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

    private ReservationResponse getIdempotentReservationResult(Long reservationId) {
        Reservation reservation =
                reservationRepository
                        .findById(reservationId)
                        .orElseThrow(() ->
                                new CoreException(
                                        ErrorType
                                                .IDEMPOTENCY_RESULT_NOT_FOUND
                                )
                        );

        return ReservationResponse.from(
                reservation
        );
    }

    private Reservation createReservation(
            Long performanceSeatId,
            ReservationCreateRequest request,
            LocalDateTime reservedAt
    ) {
        long updatedCount =
                performanceSeatRepository
                        .reserveIfAvailable(
                                performanceSeatId,
                                reservedAt
                        );

        validateReservationResult(
                performanceSeatId,
                updatedCount
        );

        PerformanceSeat performanceSeat =
                performanceSeatRepository
                        .getReferenceById(
                                performanceSeatId
                        );

        Reservation reservation =
                Reservation.create(
                        request.queueEntryId(),
                        performanceSeat,
                        reservedAt
                );

        return reservationRepository.save(
                reservation
        );
    }

}
