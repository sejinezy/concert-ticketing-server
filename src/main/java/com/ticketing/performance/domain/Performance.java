package com.ticketing.performance.domain;

import com.ticketing.event.domain.Event;
import com.ticketing.support.entity.BaseEntity;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import com.ticketing.venue.domain.Venue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "performances")
public class Performance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "event_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_performance_event_id")
    )
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "venue_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_performance_venue_id")
    )
    private Venue venue;

    @Column(name = "performance_start_at", nullable = false)
    private LocalDateTime performanceStartAt;

    @Column(name = "booking_open_at", nullable = false)
    private LocalDateTime bookingOpenAt;

    @Column(name = "booking_close_at", nullable = false)
    private LocalDateTime bookingCloseAt;

    private Performance(
            Event event,
            Venue venue,
            LocalDateTime performanceStartAt,
            LocalDateTime bookingOpenAt,
            LocalDateTime bookingCloseAt
    ) {
        validateEvent(event);
        validateVenue(venue);
        validateTimes(performanceStartAt, bookingOpenAt, bookingCloseAt);

        this.event = event;
        this.venue = venue;
        this.performanceStartAt = performanceStartAt;
        this.bookingOpenAt = bookingOpenAt;
        this.bookingCloseAt = bookingCloseAt;

    }

    public static Performance create(
            Event event,
            Venue venue,
            LocalDateTime performanceStartAt,
            LocalDateTime bookingOpenAt,
            LocalDateTime bookingCloseAt
    ) {
        return new Performance(event, venue, performanceStartAt, bookingOpenAt, bookingCloseAt);
    }

    public void update(
            Venue venue,
            LocalDateTime performanceStartAt,
            LocalDateTime bookingOpenAt,
            LocalDateTime bookingCloseAt
    ) {
        validateVenue(venue);
        validateTimes(performanceStartAt, bookingOpenAt, bookingCloseAt);

        this.venue = venue;
        this.performanceStartAt = performanceStartAt;
        this.bookingOpenAt = bookingOpenAt;
        this.bookingCloseAt = bookingCloseAt;
    }

    private void validateEvent(Event event) {
        if (event == null) {
            throw new CoreException(ErrorType.INVALID_PERFORMANCE_EVENT);
        }
    }

    private void validateVenue(Venue venue) {
        if (venue == null) {
            throw new CoreException(ErrorType.INVALID_PERFORMANCE_VENUE);
        }
    }

    private void validateTimes(LocalDateTime performanceStartAt, LocalDateTime bookingOpenAt,
                               LocalDateTime bookingCloseAt) {

        if (performanceStartAt == null) {
            throw new CoreException(ErrorType.INVALID_PERFORMANCE_START_AT);
        }

        if (bookingOpenAt == null) {
            throw new CoreException(ErrorType.INVALID_PERFORMANCE_BOOKING_OPEN_AT);
        }

        if (bookingCloseAt == null) {
            throw new CoreException(ErrorType.INVALID_PERFORMANCE_BOOKING_CLOSE_AT);
        }

        if (!bookingOpenAt.isBefore(bookingCloseAt)) {
            throw new CoreException(ErrorType.INVALID_BOOKING_PERIOD);
        }

        LocalDateTime latestBookingCloseAt = performanceStartAt.minusDays(1);
        if (bookingCloseAt.isAfter(latestBookingCloseAt)) {
            throw new CoreException(ErrorType.INVALID_BOOKING_CLOSE_TIME);
        }

    }

}
