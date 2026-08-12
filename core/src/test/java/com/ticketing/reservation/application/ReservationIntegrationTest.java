package com.ticketing.reservation.application;


import static org.assertj.core.api.Assertions.*;

import com.ticketing.event.domain.Event;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.idempotency.repository.IdempotencyRequestRepository;
import com.ticketing.performance.domain.Performance;
import com.ticketing.performance.domain.PerformanceSeat;
import com.ticketing.performance.domain.PerformanceSeatStatus;
import com.ticketing.performance.repository.PerformanceRepository;
import com.ticketing.performance.repository.PerformanceSeatRepository;
import com.ticketing.reservation.application.command.ReservationCancelCommand;
import com.ticketing.reservation.application.command.ReservationCreateCommand;
import com.ticketing.reservation.application.result.ReservationResult;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.reservation.domain.ReservationStatusChangeActorType;
import com.ticketing.reservation.domain.ReservationStatusChangeReason;
import com.ticketing.reservation.domain.ReservationStatusHistory;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.repository.ReservationStatusHistoryRepository;
import com.ticketing.reservation.repository.projection.ReservationExpirationTarget;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import com.ticketing.support.testcontainer.MySqlTestContainerConfig;
import com.ticketing.venue.domain.Venue;
import com.ticketing.venue.domain.VenueSeat;
import com.ticketing.venue.repository.VenueRepository;
import com.ticketing.venue.repository.VenueSeatRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@ActiveProfiles("test")
@Import(MySqlTestContainerConfig.class)
public class ReservationIntegrationTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 18, 12, 0);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ReservationExpirationProcessor expirationProcessor;

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

    @Autowired
    private ReservationStatusHistoryRepository statusHistoryRepository;

    @Autowired
    private IdempotencyRequestRepository idempotencyRequestRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    private Long venueId;
    private Long performanceId;
    private int seatSequence;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        cleanDatabase();

        seatSequence = 0;
        createCommonFixture();
    }


    @Test
    void 예약_생성_시_예약과_좌석_상태를_변경하고_생성_이력을_저장한다() {
        Long performanceSeatId =
                transactionTemplate.execute(status ->
                        createPerformanceSeat(NOW, false)
                );

        UUID queueEntryId = UUID.randomUUID();
        String idempotencyKey = UUID.randomUUID().toString();

        ReservationCreateCommand command =
                new ReservationCreateCommand(
                        idempotencyKey,
                        performanceSeatId,
                        queueEntryId
                );

        ReservationResult result =
                reservationService.create(command);

        Reservation reservation =
                findReservation(result.reservationId());

        PerformanceSeat performanceSeat =
                findPerformanceSeat(performanceSeatId);

        List<ReservationStatusHistory> histories =
                findHistories(result.reservationId());

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.RESERVED);

        assertThat(performanceSeat.getStatus())
                .isEqualTo(PerformanceSeatStatus.RESERVED);

        assertThat(histories).hasSize(1);

        ReservationStatusHistory history =
                histories.getFirst();

        assertThat(history.getPreviousStatus()).isNull();
        assertThat(history.getChangedStatus())
                .isEqualTo(ReservationStatus.RESERVED);
        assertThat(history.getChangeReason())
                .isEqualTo(
                        ReservationStatusChangeReason.RESERVATION_CREATED
                );
        assertThat(history.getActorType())
                .isEqualTo(
                        ReservationStatusChangeActorType.QUEUE_ENTRY
                );
        assertThat(history.getActorReference())
                .isEqualTo(queueEntryId.toString());
        assertThat(history.getChangedAt()).isNotNull();
    }

    @Test
    void 예약_취소_시_예약은_CANCELLED_좌석은_AVAILABLE로_변경되고_취소_이력이_저장된다() {
        ReservationData data =
                createReservation(
                        NOW.minusMinutes(1),
                        true
                );

        reservationService.cancel(
                new ReservationCancelCommand(
                        data.reservationId(),
                        data.queueEntryId()
                )
        );

        Reservation reservation =
                findReservation(data.reservationId());

        PerformanceSeat performanceSeat =
                findPerformanceSeat(data.performanceSeatId());

        List<ReservationStatusHistory> histories =
                findHistories(data.reservationId());

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CANCELLED);

        assertThat(performanceSeat.getStatus())
                .isEqualTo(PerformanceSeatStatus.AVAILABLE);

        assertThat(histories).hasSize(1);

        ReservationStatusHistory history =
                histories.getFirst();

        assertThat(history.getPreviousStatus())
                .isEqualTo(ReservationStatus.RESERVED);
        assertThat(history.getChangedStatus())
                .isEqualTo(ReservationStatus.CANCELLED);
        assertThat(history.getChangeReason())
                .isEqualTo(
                        ReservationStatusChangeReason.CUSTOMER_CANCELLED
                );
        assertThat(history.getActorType())
                .isEqualTo(
                        ReservationStatusChangeActorType.QUEUE_ENTRY
                );
        assertThat(history.getActorReference())
                .isEqualTo(data.queueEntryId().toString());
    }

    @Test
    void 만료된_예약은_EXPIRED_좌석은_AVAILABLE로_변경되고_만료_이력이_저장된다() {
        ReservationData data =
                createReservation(
                        NOW.minusMinutes(20),
                        true
                );

        ReservationExpirationTarget target =
                createExpirationTarget(data);

        ReservationExpirationResult result =
                expirationProcessor.expire(target, NOW);

        Reservation reservation =
                findReservation(data.reservationId());

        PerformanceSeat performanceSeat =
                findPerformanceSeat(data.performanceSeatId());

        List<ReservationStatusHistory> histories =
                findHistories(data.reservationId());

        assertThat(result)
                .isEqualTo(ReservationExpirationResult.EXPIRED);

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.EXPIRED);

        assertThat(performanceSeat.getStatus())
                .isEqualTo(PerformanceSeatStatus.AVAILABLE);

        assertThat(histories).hasSize(1);

        ReservationStatusHistory history =
                histories.getFirst();

        assertThat(history.getPreviousStatus())
                .isEqualTo(ReservationStatus.RESERVED);
        assertThat(history.getChangedStatus())
                .isEqualTo(ReservationStatus.EXPIRED);
        assertThat(history.getChangeReason())
                .isEqualTo(
                        ReservationStatusChangeReason.RESERVATION_EXPIRED
                );
        assertThat(history.getActorType())
                .isEqualTo(
                        ReservationStatusChangeActorType.SYSTEM
                );
        assertThat(history.getActorReference()).isNull();
        assertThat(history.getChangedAt()).isEqualTo(NOW);
    }

    @Test
    void 좌석_복구_실패_시_예약_상태_변경과_이력_저장이_함께_롤백된다() {
        /*
         * 정합성이 깨진 데이터를 만든다.
         *
         * Reservation     = RESERVED
         * PerformanceSeat = AVAILABLE
         */
        ReservationData data =
                createReservation(
                        NOW.minusMinutes(20),
                        false
                );

        ReservationExpirationTarget target =
                createExpirationTarget(data);

        assertThatThrownBy(() ->
                expirationProcessor.expire(target, NOW)
        )
                .isInstanceOfSatisfying(
                        CoreException.class,
                        exception -> assertThat(exception.getErrorType())
                                .isEqualTo(
                                        ErrorType.PERFORMANCE_SEAT_RELEASE_FAILED
                                )
                );

        Reservation reservation =
                findReservation(data.reservationId());

        PerformanceSeat performanceSeat =
                findPerformanceSeat(data.performanceSeatId());

        List<ReservationStatusHistory> histories =
                findHistories(data.reservationId());

        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.RESERVED);

        assertThat(performanceSeat.getStatus())
                .isEqualTo(PerformanceSeatStatus.AVAILABLE);

        assertThat(histories).isEmpty();
    }

    @Test
    @DisplayName(
            "취소와 만료가 동시에 실행돼도 하나의 상태 전이와 하나의 이력만 저장된다."
    )
    void onlyOneStateTransitionAndHistorySucceedsWhenCancellationAndExpirationRunConcurrently()
            throws Exception {

        /*
         * 만료 대상이면서 취소도 가능한 정상 예약을 생성한다.
         *
         * Reservation     = RESERVED
         * PerformanceSeat = RESERVED
         * expiresAt       <= NOW
         */
        ReservationData data =
                createReservation(
                        NOW.minusMinutes(20),
                        true
                );

        ReservationExpirationTarget target =
                new ReservationExpirationTarget(
                        data.reservationId(),
                        data.performanceSeatId()
                );

        ExecutorService executorService =
                Executors.newFixedThreadPool(2);

        CountDownLatch readyLatch =
                new CountDownLatch(2);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        try {
            Future<TransitionResult> cancellationFuture =
                    executorService.submit(() -> {
                        readyLatch.countDown();
                        startLatch.await();

                        try {
                            reservationService.cancel(
                                    new ReservationCancelCommand(
                                            data.reservationId(),
                                            data.queueEntryId()
                                    )
                            );

                            return TransitionResult.CANCELLED;
                        } catch (CoreException exception) {
                            /*
                             * 만료가 먼저 성공했다면 예약은 더 이상
                             * RESERVED가 아니므로 취소 조건부 갱신이 실패한다.
                             */
                            if (exception.getErrorType()
                                    == ErrorType.RESERVATION_NOT_CANCELLABLE) {
                                return TransitionResult.SKIPPED;
                            }

                            throw exception;
                        }
                    });

            Future<TransitionResult> expirationFuture =
                    executorService.submit(() -> {
                        readyLatch.countDown();
                        startLatch.await();

                        ReservationExpirationResult result =
                                expirationProcessor.expire(
                                        target,
                                        NOW
                                );

                        if (result
                                == ReservationExpirationResult.EXPIRED) {
                            return TransitionResult.EXPIRED;
                        }

                        return TransitionResult.SKIPPED;
                    });

            /*
             * 두 작업 스레드가 모두 준비될 때까지 기다린 후
             * 같은 시점에 실행을 시작한다.
             */
            boolean allThreadsReady =
                    readyLatch.await(10, TimeUnit.SECONDS);

            assertThat(allThreadsReady).isTrue();

            startLatch.countDown();

            TransitionResult cancellationResult =
                    cancellationFuture.get(
                            20,
                            TimeUnit.SECONDS
                    );

            TransitionResult expirationResult =
                    expirationFuture.get(
                            20,
                            TimeUnit.SECONDS
                    );

            int successCount = 0;
            int skippedCount = 0;

            if (cancellationResult == TransitionResult.SKIPPED) {
                skippedCount++;
            } else {
                successCount++;
            }

            if (expirationResult == TransitionResult.SKIPPED) {
                skippedCount++;
            } else {
                successCount++;
            }

            /*
             * 취소와 만료 중 하나만 성공하고,
             * 나머지 하나는 조건부 갱신 실패로 건너뛴다.
             */
            assertThat(successCount).isEqualTo(1);
            assertThat(skippedCount).isEqualTo(1);

            TransitionResult successfulTransition =
                    cancellationResult != TransitionResult.SKIPPED
                            ? cancellationResult
                            : expirationResult;

            ReservationStatus expectedStatus =
                    successfulTransition == TransitionResult.CANCELLED
                            ? ReservationStatus.CANCELLED
                            : ReservationStatus.EXPIRED;

            Reservation reservation =
                    findReservation(data.reservationId());

            PerformanceSeat performanceSeat =
                    findPerformanceSeat(data.performanceSeatId());

            List<ReservationStatusHistory> histories =
                    findHistories(data.reservationId());

            assertThat(reservation.getStatus())
                    .isEqualTo(expectedStatus);

            /*
             * 취소와 만료 중 어느 쪽이 성공하더라도
             * 좌석은 다시 예약 가능한 상태로 복구돼야 한다.
             */
            assertThat(performanceSeat.getStatus())
                    .isEqualTo(PerformanceSeatStatus.AVAILABLE);

            /*
             * 실제 상태 변경에 성공한 처리만 이력을 저장해야 한다.
             */
            assertThat(histories).hasSize(1);

            assertThat(histories.getFirst().getPreviousStatus())
                    .isEqualTo(ReservationStatus.RESERVED);

            assertThat(histories.getFirst().getChangedStatus())
                    .isEqualTo(expectedStatus);
        } finally {
            /*
             * readyLatch 대기 또는 검증 과정에서 실패하더라도
             * 대기 중인 작업 스레드가 종료될 수 있도록 해제한다.
             */
            startLatch.countDown();
            executorService.shutdownNow();
            executorService.awaitTermination(
                    5,
                    TimeUnit.SECONDS
            );
        }
    }

    private enum TransitionResult {
        CANCELLED,
        EXPIRED,
        SKIPPED
    }

    private void createCommonFixture() {
        transactionTemplate.executeWithoutResult(status ->{
            Event event = eventRepository.save(
                    Event.create("테스트 공연", "예약 통합 테스트")
            );

            Venue venue = venueRepository.save(
                    Venue.create("테스트 공연장", "서울특별시")
            );

            LocalDateTime performanceStartAt = NOW.plusDays(30);

            Performance performance = performanceRepository.saveAndFlush(
                    Performance.create(
                            event,
                            venue,
                            performanceStartAt,
                            NOW.minusDays(1),
                            performanceStartAt.minusDays(2)
                    )
            );

            venueId = venue.getId();
            performanceId = performance.getId();
        });
    }

    /**
     * 취소와 만료 테스트는 예약 생성 이력을 검증하는 테스트가 아니다.
     *
     * 따라서 ReservationService.create()를 사용하지 않고 예약을 직접 저장하여,
     * 해당 테스트에서 발생한 취소 또는 만료 이력만 조회되도록 한다.
     */
    private ReservationData createReservation(
            LocalDateTime reservedAt,
            boolean reserveSeat
    ) {
        return transactionTemplate.execute(status -> {
            Long performanceSeatId = createPerformanceSeat(
                    reservedAt, reserveSeat
            );

            PerformanceSeat performanceSeat = performanceSeatRepository
                    .findById(performanceSeatId).orElseThrow();

            UUID queueEntryId = UUID.randomUUID();

            Reservation reservation = reservationRepository.saveAndFlush(
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
        });
    }

    private Long createPerformanceSeat(
            LocalDateTime reservedAt,
            boolean reserveSeat
    ) {
        Venue venue = entityManager.getReference(
                Venue.class,
                venueId
        );

        Performance performance = entityManager.getReference(
                Performance.class,
                performanceId
        );

        String seatNo = String.valueOf(++seatSequence);

        VenueSeat venueSeat = venueSeatRepository.saveAndFlush(
                VenueSeat.create(
                        venue,
                        "A",
                        "1",
                        seatNo
                )
        );

        PerformanceSeat performanceSeat = performanceSeatRepository.saveAndFlush(
                PerformanceSeat.create(
                        performance,
                        venueSeat
                )
        );

        if (reserveSeat) {
            long updatedCount = performanceSeatRepository.reserveIfAvailable(
                    performanceSeat.getId(),
                    reservedAt
            );

            assertThat(updatedCount).isEqualTo(1L);

            entityManager.refresh(performanceSeat);

        }

        return performanceSeat.getId();
    }

    private ReservationExpirationTarget createExpirationTarget(
            ReservationData data
    ) {
        return new ReservationExpirationTarget(
                data.reservationId,
                data.performanceSeatId
        );
    }

    private Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId).orElseThrow();
    }

    private PerformanceSeat findPerformanceSeat(Long performanceSeatId) {
        return performanceSeatRepository.findById(performanceSeatId).orElseThrow();

    }

    private List<ReservationStatusHistory> findHistories(
            Long reservationId
    ) {
        return statusHistoryRepository
                .findAllByReservation_IdOrderByChangedAtAscIdAsc(
                        reservationId
                );
    }

    private void cleanDatabase() {
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.clear();

            idempotencyRequestRepository.deleteAllInBatch();
            statusHistoryRepository.deleteAllInBatch();
            reservationRepository.deleteAllInBatch();
            performanceSeatRepository.deleteAllInBatch();
            venueSeatRepository.deleteAllInBatch();
            performanceRepository.deleteAllInBatch();
            eventRepository.deleteAllInBatch();
            venueRepository.deleteAllInBatch();
        });
    }

    private record ReservationData(
            Long reservationId,
            Long performanceSeatId,
            UUID queueEntryId
    ) {

    }
}
