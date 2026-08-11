package com.ticketing.reservation.domain;

import com.ticketing.performance.domain.PerformanceSeat;
import com.ticketing.support.entity.BaseEntity;
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
@Table(name = "reservations")
public class Reservation extends BaseEntity {

    private static final long RESERVATION_DURATION_MINUTES = 10L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "queue_entry_id", nullable = false)
    private UUID queueEntryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "performance_seat_id",
            nullable = false
    )
    private PerformanceSeat performanceSeat;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private Reservation(
            UUID queueEntryId,
            PerformanceSeat performanceSeat,
            LocalDateTime reservedAt
    ) {
        validateQueueEntryId(queueEntryId);
        validatePerformanceSeat(performanceSeat);
        validateReservedAt(reservedAt);

        this.queueEntryId = queueEntryId;
        this.performanceSeat = performanceSeat;
        this.status = ReservationStatus.RESERVED;
        this.expiresAt = reservedAt.plusMinutes(RESERVATION_DURATION_MINUTES);
    }

    public static Reservation create(
            UUID queueEntryId,
            PerformanceSeat performanceSeat,
            LocalDateTime reservedAt
    ) {
        return new Reservation(queueEntryId, performanceSeat, reservedAt);
    }

    public void validateOwner(UUID queueEntryId) {
        if (!this.queueEntryId.equals(queueEntryId)) {
            throw new CoreException(ErrorType.RESERVATION_ACCESS_DENIED);
        }
    }

    private void validateQueueEntryId(UUID queueEntryId) {
        if (queueEntryId == null) {
            throw new CoreException(ErrorType.INVALID_RESERVATION_QUEUE_ENTRY_ID);
        }
    }

    private void validatePerformanceSeat(PerformanceSeat performanceSeat) {
        if (performanceSeat == null) {
            throw new CoreException(ErrorType.INVALID_RESERVATION_PERFORMANCE_SEAT);
        }
    }

    private void validateReservedAt(LocalDateTime reservedAt) {
        if (reservedAt == null) {
            throw new CoreException(ErrorType.INVALID_RESERVATION_RESERVED_AT);
        }
    }

}
