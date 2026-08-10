package com.ticketing.reservation.domain;

import static org.assertj.core.api.Assertions.*;

import com.ticketing.event.domain.Event;
import com.ticketing.performance.domain.Performance;
import com.ticketing.performance.domain.PerformanceSeat;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import com.ticketing.venue.domain.Venue;
import com.ticketing.venue.domain.VenueSeat;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ReservationTest {

    @Test
    void 예약을_생성할_수_있다() {
        UUID queueEntryId = UUID.randomUUID();
        PerformanceSeat performanceSeat = createPerformanceSeat();
        LocalDateTime reservedAt = LocalDateTime.of(2026, 7, 15, 17, 0);

        Reservation reservation = Reservation.create(
                queueEntryId,
                performanceSeat,
                reservedAt
        );

        assertThat(reservation.getQueueEntryId()).isEqualTo(queueEntryId);
        assertThat(reservation.getPerformanceSeat()).isSameAs(performanceSeat);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
        assertThat(reservation.getExpiresAt()).isEqualTo(reservedAt.plusMinutes(10));
    }

    @Test
    void 대기열_참여_식별자는_필수다() {
        PerformanceSeat performanceSeat = createPerformanceSeat();
        LocalDateTime reservedAt = LocalDateTime.of(2026, 7, 15, 17, 0);

        assertThatThrownBy(() ->
                Reservation.create(
                        null,
                        performanceSeat,
                        reservedAt
                )
        )
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_RESERVATION_QUEUE_ENTRY_ID.getMessage());
    }

    @Test
    void 예약할_회차_좌석은_필수다() {
        UUID queueEntryId = UUID.randomUUID();
        LocalDateTime reservedAt =
                LocalDateTime.of(2026, 7, 15, 17, 0);

        assertThatThrownBy(() ->
                Reservation.create(
                        queueEntryId,
                        null,
                        reservedAt
                )
        )
                .isInstanceOf(CoreException.class)
                .hasMessage(
                        ErrorType.INVALID_RESERVATION_PERFORMANCE_SEAT
                                .getMessage()
                );
    }

    @Test
    void 예약_시각은_필수다() {
        UUID queueEntryId = UUID.randomUUID();
        PerformanceSeat performanceSeat = createPerformanceSeat();

        assertThatThrownBy(() ->
                Reservation.create(
                        queueEntryId,
                        performanceSeat,
                        null
                )
        )
                .isInstanceOf(CoreException.class)
                .hasMessage(
                        ErrorType.INVALID_RESERVATION_RESERVED_AT
                                .getMessage()
                );
    }

    private PerformanceSeat createPerformanceSeat() {
        Event event = Event.create(
                "아이유 콘서트",
                "공연 설명"
        );

        Venue venue = Venue.create(
                "올림픽홀",
                "서울특별시 송파구"
        );

        Performance performance = Performance.create(
                event,
                venue,
                LocalDateTime.of(2026, 8, 1, 19, 0),
                LocalDateTime.of(2026, 7, 10, 20, 0),
                LocalDateTime.of(2026, 7, 20, 20, 0)
        );

        VenueSeat venueSeat = VenueSeat.create(
                venue,
                "A",
                "1",
                "1"
        );

        return PerformanceSeat.create(
                performance,
                venueSeat
        );
    }

}