package com.ticketing.performance.domain;

import static org.assertj.core.api.Assertions.*;

import com.ticketing.event.domain.Event;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import com.ticketing.venue.domain.Venue;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PerformanceTest {

    @Test
    void 공연_회차를_생성할_수_있다() {
        Event event = Event.create("아이유 콘서트", "설명");
        Venue venue = Venue.create("올림픽홀", "서울");

        LocalDateTime performanceStartAt = LocalDateTime.of(2026, 8, 1, 19, 0);
        LocalDateTime bookingOpenAt = LocalDateTime.of(2026, 7, 10, 20, 0);
        LocalDateTime bookingCloseAt = LocalDateTime.of(2026, 7, 20, 20, 0);

        Performance performance = Performance.create(
                event,
                venue,
                performanceStartAt,
                bookingOpenAt,
                bookingCloseAt
        );

        assertThat(performance.getEvent()).isEqualTo(event);
        assertThat(performance.getVenue()).isEqualTo(venue);
        assertThat(performance.getPerformanceStartAt()).isEqualTo(performanceStartAt);
        assertThat(performance.getBookingOpenAt()).isEqualTo(bookingOpenAt);
        assertThat(performance.getBookingCloseAt()).isEqualTo(bookingCloseAt);
    }

    @Test
    void 예매_오픈_시간은_예매_종료_시간보다_빨라야_한다() {
        Event event = Event.create("아이유 콘서트", "설명");
        Venue venue = Venue.create("올림픽홀", "서울");

        LocalDateTime performanceStartAt = LocalDateTime.of(2026, 8, 1, 19, 0);
        LocalDateTime bookingOpenAt = LocalDateTime.of(2026, 7, 10, 20, 0);
        LocalDateTime bookingCloseAt = LocalDateTime.of(2026, 7, 10, 19, 0);

        assertThatThrownBy(() -> Performance.create(event, venue, performanceStartAt, bookingOpenAt, bookingCloseAt))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_BOOKING_PERIOD.getMessage());
    }

    @Test
    void 예매_오픈_시간과_예매_종료_시간이_같으면_실패한다() {
        Event event = Event.create("아이유 콘서트", "설명");
        Venue venue = Venue.create("올림픽홀", "서울");

        LocalDateTime performanceStartAt = LocalDateTime.of(2026, 8, 1, 19, 0);
        LocalDateTime bookingOpenAt = LocalDateTime.of(2026, 7, 10, 20, 0);
        LocalDateTime bookingCloseAt = LocalDateTime.of(2026, 7, 10, 20, 0);

        assertThatThrownBy(() -> Performance.create(event, venue, performanceStartAt, bookingOpenAt, bookingCloseAt))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_BOOKING_PERIOD.getMessage());

    }

    @Test
    void 예매_종료_시간은_공연_시작_24시간_전이면_허용된다() {
        Event event = Event.create("아이유 콘서트", "설명");
        Venue venue = Venue.create("올림픽홀", "서울");

        LocalDateTime performanceStartAt = LocalDateTime.of(2026, 8, 1, 19, 0);
        LocalDateTime bookingOpenAt = LocalDateTime.of(2026, 7, 10, 20, 0);
        LocalDateTime bookingCloseAt = performanceStartAt.minusDays(1);

        Performance performance = Performance.create(event, venue, performanceStartAt, bookingOpenAt, bookingCloseAt);

        assertThat(performance.getBookingCloseAt()).isEqualTo(bookingCloseAt);

    }

    @Test
    void 예매_종료_시간이_공연_시작_24시간_전보다_늦으면_실패한다() {
        Event event = Event.create("아이유 콘서트", "설명");
        Venue venue = Venue.create("올림픽홀", "서울");

        LocalDateTime performanceStartAt = LocalDateTime.of(2026, 8, 1, 19, 0);
        LocalDateTime bookingOpenAt = LocalDateTime.of(2026, 7, 10, 20, 0);
        LocalDateTime bookingCloseAt = performanceStartAt.minusDays(1).plusMinutes(1);

        assertThatThrownBy(() -> Performance.create(event, venue, performanceStartAt, bookingOpenAt, bookingCloseAt))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_BOOKING_CLOSE_TIME.getMessage());
    }

    @Test
    void 공연_회차_수정_시_예매_종료_시간이_공연_시작_24시간_전보다_늦으면_실패한다() {
        Event event = Event.create("아이유 콘서트", "설명");
        Venue venue = Venue.create("올림픽홀", "서울");

        LocalDateTime performanceStartAt = LocalDateTime.of(2026, 8, 1, 19, 0);
        LocalDateTime bookingOpenAt = LocalDateTime.of(2026, 7, 10, 20, 0);
        LocalDateTime bookingCloseAt = LocalDateTime.of(2026, 7, 20, 20, 0);

        Performance performance = Performance.create(
                event,
                venue,
                performanceStartAt,
                bookingOpenAt,
                bookingCloseAt
        );

        assertThatThrownBy(() -> performance.update(
                venue,
                LocalDateTime.of(2026, 8, 1, 19, 0),
                LocalDateTime.of(2026, 7, 10, 20, 0),
                LocalDateTime.of(2026, 7, 31, 19, 1)
        ))
                .isInstanceOf(CoreException.class)
                .hasMessage(ErrorType.INVALID_BOOKING_CLOSE_TIME.getMessage());
    }
}
