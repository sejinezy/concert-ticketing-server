package com.ticketing.reservation.application;

import static org.assertj.core.api.Assertions.*;

import com.ticketing.event.domain.Event;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.performance.domain.Performance;
import com.ticketing.performance.domain.PerformanceSeat;
import com.ticketing.performance.domain.PerformanceSeatStatus;
import com.ticketing.performance.repository.PerformanceRepository;
import com.ticketing.performance.repository.PerformanceSeatRepository;
import com.ticketing.reservation.presentation.dto.ReservationCreateRequest;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import com.ticketing.support.testcontainer.MySqlTestContainerConfig;
import com.ticketing.venue.domain.Venue;
import com.ticketing.venue.domain.VenueSeat;
import com.ticketing.venue.repository.VenueRepository;
import com.ticketing.venue.repository.VenueSeatRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig.class)
public class ReservationConcurrencyTest {

    private static final int THREAD_COUNT = 20;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PerformanceSeatRepository performanceSeatRepository;

    @Autowired
    private PerformanceRepository performanceRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private VenueSeatRepository venueSeatRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        cleanDatabase();
    }

    @Test
    void 동일한_좌석에_동시에_예약하면_하나만_성공한다() throws Exception {

        Long performanceSeatId = createPerformanceSeat();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        CountDownLatch readyLatch = new CountDownLatch(THREAD_COUNT);

        CountDownLatch startLatch = new CountDownLatch(1);

        List<Future<Boolean>> futures = new ArrayList<>();

        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                futures.add(
                        executorService.submit(() -> {
                            readyLatch.countDown();
                            startLatch.await();

                            try {
                                reservationService.create(
                                        performanceSeatId,
                                        new ReservationCreateRequest(UUID.randomUUID())
                                );
                                return true;
                            } catch (CoreException exception) {
                                if (exception.getErrorType() != ErrorType.PERFORMANCE_SEAT_ALREADY_RESERVED) {
                                    throw exception;
                                }
                                return false;
                            }
                        })
                );
            }

            boolean allThreadReady = readyLatch.await(10, TimeUnit.SECONDS);

            assertThat(allThreadReady).isTrue();

            startLatch.countDown();

            int successCount = 0;
            int failureCount = 0;

            for (Future<Boolean> future : futures) {
                boolean success = getResult(future);

                if (success) {
                    successCount++;
                } else {
                    failureCount++;
                }
            }

            assertThat(successCount).isEqualTo(1);
            assertThat(failureCount).isEqualTo(THREAD_COUNT - 1);

            assertThat(reservationRepository.count()).isEqualTo(1);

            PerformanceSeat performanceSeat = performanceSeatRepository.findById(performanceSeatId).orElseThrow();

            assertThat(performanceSeat.getStatus()).isEqualTo(PerformanceSeatStatus.RESERVED);
        } finally {
            startLatch.countDown();
            executorService.shutdown();
        }
    }

    private boolean getResult(Future<Boolean> future)
            throws ExecutionException, InterruptedException, TimeoutException {
        return future.get(20, TimeUnit.SECONDS);
    }

    private Long createPerformanceSeat() {
        return transactionTemplate.execute(status -> {
            Event event = eventRepository.save(Event.create("동시성 테스트 공연", "좌석 예약 동시성 테스트"));

            Venue venue = venueRepository.save(Venue.create("테스트 공연장", "서울특별시 송파구"));

            VenueSeat venueSeat = venueSeatRepository.save(VenueSeat.create(venue, "A", "1", "1"));

            Performance performance = performanceRepository.save(Performance.create(
                    event,
                    venue,
                    LocalDateTime.of(
                            2026, 9, 1, 19, 0
                    ),
                    LocalDateTime.of(
                            2026, 7, 1, 10, 0
                    ),
                    LocalDateTime.of(
                            2026, 8, 30, 23, 59
                    )
            ));

            PerformanceSeat performanceSeat = performanceSeatRepository.saveAndFlush(
                    PerformanceSeat.create(performance, venueSeat)
            );

            return performanceSeat.getId();

        });
    }

    private void cleanDatabase() {
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.clear();

            reservationRepository.deleteAllInBatch();
            performanceSeatRepository.deleteAllInBatch();
            venueSeatRepository.deleteAllInBatch();
            performanceRepository.deleteAllInBatch();
            eventRepository.deleteAllInBatch();
            venueRepository.deleteAllInBatch();
        });
    }
}
