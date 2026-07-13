package com.ticketing.performance.domain;

import com.ticketing.support.entity.BaseEntity;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import com.ticketing.venue.domain.VenueSeat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        name = "performance_seats",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_performance_seats_performance_venue_seat",
                        columnNames = {"performance_id", "venue_seat_id"}
                )
        }
)
public class PerformanceSeat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "performance_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_performance_seats_performance_id")
    )
    private Performance performance;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "venue_seat_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_performance_seats_venue_seat_id")
    )
    private VenueSeat venueSeat;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PerformanceSeatStatus status;

    private PerformanceSeat(
            Performance performance,
            VenueSeat venueSeat,
            PerformanceSeatStatus status
    ) {
        validatePerformance(performance);
        validateVenueSeat(venueSeat);

        this.performance = performance;
        this.venueSeat = venueSeat;
        this.status = status;
    }

    public static PerformanceSeat create(
            Performance performance,
            VenueSeat venueSeat
    ) {
        return new PerformanceSeat(performance, venueSeat, PerformanceSeatStatus.AVAILABLE);
    }

    private void validatePerformance(Performance performance) {
        if (performance == null) {
            throw new CoreException(ErrorType.INVALID_PERFORMANCE_SEAT_PERFORMANCE);
        }
    }

    private void validateVenueSeat(VenueSeat venueSeat) {
        if (venueSeat == null) {
            throw new CoreException(ErrorType.INVALID_PERFORMANCE_SEAT_VENUE_SEAT);
        }
    }
}
