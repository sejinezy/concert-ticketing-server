package com.ticketing.reservation.domain;

import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        indexes = {
                @Index(
                        name = "idx_reservation_status_history_reservation_changed_at",
                        columnList = "reservation_id, changed_at, id"
                )
        }
)
public class ReservationStatusHistory {

    private static final int ACTOR_REFERENCE_MAX_LENGTH = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reservation_id",
            nullable = false,
            updatable = false
    )
    private Reservation reservation;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, updatable = false)
    private ReservationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false, updatable = false)
    private ReservationStatus changedStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40, updatable = false)
    private ReservationStatusChangeReason changeReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private ReservationStatusChangeActorType actorType;

    @Column(length = ACTOR_REFERENCE_MAX_LENGTH, updatable = false)
    private String actorReference;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;

    private ReservationStatusHistory(
            Reservation reservation,
            ReservationStatus previousStatus,
            ReservationStatus changedStatus,
            ReservationStatusChangeReason changeReason,
            ReservationStatusChangeActorType actorType,
            String actorReference,
            LocalDateTime changedAt
    ) {
        validateReservation(reservation);
        validateChangedAt(changedAt);

        this.reservation = reservation;
        this.previousStatus = previousStatus;
        this.changedStatus = changedStatus;
        this.changeReason = changeReason;
        this.actorType = actorType;
        this.actorReference = actorReference;
        this.changedAt = changedAt;
    }

    public static ReservationStatusHistory createdByQueueEntry(
            Reservation reservation,
            LocalDateTime changedAt
    ) {
        return new ReservationStatusHistory(
                reservation,
                null,
                ReservationStatus.RESERVED,
                ReservationStatusChangeReason.RESERVATION_CREATED,
                ReservationStatusChangeActorType.QUEUE_ENTRY,
                toActorReference(reservation),
                changedAt
        );
    }

    public static ReservationStatusHistory cancelledByQueueEntry(
            Reservation reservation,
            LocalDateTime changedAt
    ) {
        return new ReservationStatusHistory(
                reservation,
                ReservationStatus.RESERVED,
                ReservationStatus.CANCELLED,
                ReservationStatusChangeReason.CUSTOMER_CANCELLED,
                ReservationStatusChangeActorType.QUEUE_ENTRY,
                toActorReference(reservation),
                changedAt
        );
    }

    public static ReservationStatusHistory expiredBySystem(
            Reservation reservation,
            LocalDateTime changedAt
    ) {
        return new ReservationStatusHistory(
                reservation,
                ReservationStatus.RESERVED,
                ReservationStatus.EXPIRED,
                ReservationStatusChangeReason.RESERVATION_EXPIRED,
                ReservationStatusChangeActorType.SYSTEM,
                null,
                changedAt
        );
    }

    private static String toActorReference(Reservation reservation) {
        if (reservation == null) {
            throw new CoreException(
                    ErrorType.INVALID_RESERVATION_STATUS_HISTORY_RESERVATION
            );
        }

        UUID queueEntryId = reservation.getQueueEntryId();

        if (queueEntryId == null) {
            throw new CoreException(
                    ErrorType.INVALID_RESERVATION_STATUS_HISTORY_ACTOR_REFERENCE
            );
        }

        return queueEntryId.toString();
    }

    private void validateReservation(Reservation reservation) {
        if (reservation == null) {
            throw new CoreException(ErrorType.INVALID_RESERVATION_STATUS_HISTORY_RESERVATION);
        }
    }

    private void validateChangedAt(LocalDateTime changedAt) {
        if (changedAt == null) {
            throw new CoreException(
                    ErrorType.INVALID_RESERVATION_STATUS_HISTORY_CHANGED_AT
            );
        }
    }
}
