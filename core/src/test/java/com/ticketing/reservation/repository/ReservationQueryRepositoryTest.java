package com.ticketing.reservation.repository;


import static org.assertj.core.api.Assertions.*;

import com.ticketing.event.domain.Event;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.performance.domain.Performance;
import com.ticketing.performance.domain.PerformanceSeat;
import com.ticketing.performance.domain.PerformanceSeatStatus;
import com.ticketing.performance.repository.PerformanceRepository;
import com.ticketing.performance.repository.PerformanceSeatRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.support.config.QuerydslConfig;
import com.ticketing.support.testcontainer.MySqlTestContainerConfig;
import com.ticketing.venue.domain.Venue;
import com.ticketing.venue.domain.VenueSeat;
import com.ticketing.venue.repository.VenueRepository;
import com.ticketing.venue.repository.VenueSeatRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({
        QuerydslConfig.class,
        MySqlTestContainerConfig.class
})
class ReservationQueryRepositoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 17, 12, 0);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private VenueSeatRepository venueSeatRepository;

    @Autowired
    private PerformanceSeatRepository performanceSeatRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    private Venue venue;
    private Performance performance;
    private int seatSequence;

    @BeforeEach
    void setUp() {
        seatSequence = 0;

        Event event = eventRepository.save(
                Event.create("테스트 공연", "QueryDSL repository 테스트")
        );

        venue = venueRepository.save(
                Venue.create("테스트 공연장", "서울 특별시")
        );

        LocalDateTime performanceStartAt = NOW.plusDays(30);
        LocalDateTime bookingOpenAt = NOW.minusDays(1);
        LocalDateTime bookingCloseAt = performanceStartAt.minusDays(2);

        performance = performanceRepository.save(
                Performance.create(
                        event,
                        venue,
                        performanceStartAt,
                        bookingOpenAt,
                        bookingCloseAt
                )
        );
    }

    @Test
    void RESERVED_에약은_CANCELLED로_변경된다() {
        ReservationData data = createReservation(
                NOW.minusMinutes(1),
                true
        );

        long updatedCount = reservationRepository.cancelIfReserved(
                data.reservationId,
                NOW
        );

        assertThat(updatedCount).isEqualTo(1L);

        entityManager.clear();

        Reservation reservation = reservationRepository.findById(data.reservationId).orElseThrow();

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void CANCELLED_또는_EXPIRED_예약은_다시_취소할_수_없다() {
        ReservationData cancelledReservation = createReservation(
                NOW.minusMinutes(1),
                true
        );

        long firstCancelCount = reservationRepository.cancelIfReserved(
                cancelledReservation.reservationId,
                NOW
        );

        assertThat(firstCancelCount).isEqualTo(1L);

        ReservationData expiredReservation = createReservation(
                NOW.minusMinutes(20),
                true
        );

        long expireCount = reservationRepository.expireIfReserved(
                expiredReservation.reservationId,
                NOW
        );

        assertThat(expireCount).isEqualTo(1L);

        long cancelledResult = reservationRepository.cancelIfReserved(
                cancelledReservation.reservationId,
                NOW
        );

        long expiredResult = reservationRepository.cancelIfReserved(
                expiredReservation.reservationId,
                NOW
        );

        assertThat(cancelledResult).isZero();
        assertThat(expiredResult).isZero();

    }

    @Test
    void 만료_시간이_지난_RESERVED_에약은_EXPIRED로_변경된다() {
        ReservationData data = createReservation(
                NOW.minusMinutes(20),
                true
        );

        long updatedCount = reservationRepository.expireIfReserved(
                data.reservationId,
                NOW
        );

        assertThat(updatedCount).isEqualTo(1L);

        entityManager.clear();

        Reservation reservation = reservationRepository.findById(data.reservationId).orElseThrow();

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.EXPIRED);

    }

    @Test
    void 만료_시간이_지나지_않은_RESERVED_에약은_변경되지_않는다() {

        ReservationData data = createReservation(
                NOW.minusMinutes(5),
                true
        );

        long updatedCount = reservationRepository.expireIfReserved(
                data.reservationId,
                NOW
        );

        assertThat(updatedCount).isZero();

        entityManager.clear();

        Reservation reservation = reservationRepository.findById(data.reservationId).orElseThrow();

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    void RESERVED_좌석만_AVAILABLE로_복구된다() {
        Long reservedSeatId = createPerformanceSeat(true);
        Long availableSeatId = createPerformanceSeat(false);

        long reservedSeatResult = performanceSeatRepository.releaseIfReserved(reservedSeatId, NOW);

        long availableSeatResult = performanceSeatRepository.releaseIfReserved(availableSeatId, NOW);

        assertThat(reservedSeatResult).isEqualTo(1L);
        assertThat(availableSeatResult).isZero();

        entityManager.clear();

        PerformanceSeat releasedSeat = performanceSeatRepository.findById(reservedSeatId).orElseThrow();

        PerformanceSeat unchangedSeat = performanceSeatRepository.findById(availableSeatId).orElseThrow();

        assertThat(releasedSeat.getStatus()).isEqualTo(PerformanceSeatStatus.AVAILABLE);
        assertThat(unchangedSeat.getStatus()).isEqualTo(PerformanceSeatStatus.AVAILABLE);
    }



    private ReservationData createReservation(
            LocalDateTime reservedAt,
            boolean reserveSeat
    ) {

        Long performanceSeatId = createPerformanceSeat(reserveSeat);

        PerformanceSeat performanceSeat = performanceSeatRepository
                .findById(performanceSeatId)
                .orElseThrow();

        UUID queueEntryId = UUID.randomUUID();

        Reservation reservation = entityManager.persistAndFlush(
                Reservation.create(
                        queueEntryId,
                        performanceSeat,
                        reservedAt
                )
        );

        return new ReservationData(
                reservation.getId(),
                performanceSeatId,
                queueEntryId
        );
    }

    private Long createPerformanceSeat(boolean reserved) {
        String seatNo = String.valueOf(++seatSequence);

        VenueSeat venueSeat = entityManager.persistAndFlush(
                VenueSeat.create(
                        venue,
                        "A",
                        "1",
                        seatNo
                )
        );

        PerformanceSeat performanceSeat = entityManager.persistAndFlush(
                PerformanceSeat.create(
                        performance,
                        venueSeat
                )
        );

        if (reserved) {
            long updatedCount = performanceSeatRepository.reserveIfAvailable(
                    performanceSeat.getId(),
                    NOW
            );

            assertThat(updatedCount).isEqualTo(1L);
            entityManager.refresh(performanceSeat);
        }
        return performanceSeat.getId();
    }

    private record ReservationData(
            Long reservationId,
            Long performanceSeatId,
            UUID queueEntryId
    ) {

    }
}