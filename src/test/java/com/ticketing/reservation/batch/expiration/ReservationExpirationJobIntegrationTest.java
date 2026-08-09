package com.ticketing.reservation.batch.expiration;

import static org.assertj.core.api.Assertions.assertThat;

import com.ticketing.event.domain.Event;
import com.ticketing.event.repository.EventRepository;
import com.ticketing.performance.domain.Performance;
import com.ticketing.performance.domain.PerformanceSeat;
import com.ticketing.performance.domain.PerformanceSeatStatus;
import com.ticketing.performance.repository.PerformanceRepository;
import com.ticketing.performance.repository.PerformanceSeatRepository;
import com.ticketing.reservation.domain.Reservation;
import com.ticketing.reservation.domain.ReservationStatus;
import com.ticketing.reservation.domain.ReservationStatusChangeActorType;
import com.ticketing.reservation.domain.ReservationStatusChangeReason;
import com.ticketing.reservation.domain.ReservationStatusHistory;
import com.ticketing.reservation.repository.ReservationRepository;
import com.ticketing.reservation.repository.ReservationStatusHistoryRepository;
import com.ticketing.support.testcontainer.MySqlTestContainerConfig;
import com.ticketing.venue.domain.Venue;
import com.ticketing.venue.domain.VenueSeat;
import com.ticketing.venue.repository.VenueRepository;
import com.ticketing.venue.repository.VenueSeatRepository;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(
        properties = {
                "reservation.expiration.chunk-size=2"
        }
)
@ActiveProfiles("test")
@Import({
        MySqlTestContainerConfig.class,
        ReservationExpirationJobIntegrationTest.TestClockConfig.class
})
class ReservationExpirationJobIntegrationTest {

    /*
     * Batch 테스트에서 사용하는 고정된 현재 시각.
     *
     * 운영에서는 실제 현재 시각을 사용하지만,
     * 테스트에서는 실행 시각에 따라 결과가 달라지지 않도록
     * Clock을 BASE_CUTOFF_AT으로 고정한다.
     */
    private static final LocalDateTime BASE_CUTOFF_AT =
            LocalDateTime.of(
                    2026,
                    8,
                    5,
                    17,
                    0
            );

    private static final ZoneId TEST_ZONE_ID =
            ZoneId.of("Asia/Seoul");

    @Autowired
    private ReservationExpirationJobLauncher jobLauncher;

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
    private PlatformTransactionManager transactionManager;

    @Autowired
    private EntityManager entityManager;

    private TransactionTemplate transactionTemplate;

    private Long venueId;
    private Long performanceId;
    private int seatSequence;

    @BeforeEach
    void setUp() {
        transactionTemplate =
                new TransactionTemplate(transactionManager);

        cleanDatabase();

        seatSequence = 0;

        createCommonFixture();
    }

    @Test
    void 만료_대상만_처리한다() throws Exception {

        ReservationData expiredBeforeCutoff =
                createReservation(
                        BASE_CUTOFF_AT.minusMinutes(20),
                        true
                );

        /*
         * 예약 지속 시간이 10분이므로
         * reservedAt = cutoffAt - 10분이면
         * expiresAt == cutoffAt이다.
         *
         * Reader 조건이 expiresAt <= cutoffAt이므로
         * 경계값도 만료 대상에 포함돼야 한다.
         */
        ReservationData expiredAtCutoff =
                createReservation(
                        BASE_CUTOFF_AT.minusMinutes(10),
                        true
                );

        ReservationData notExpired =
                createReservation(
                        BASE_CUTOFF_AT.minusMinutes(9),
                        true
                );

        JobExecution jobExecution =
                jobLauncher.launch();

        StepExecution stepExecution =
                findExpirationStep(jobExecution);

        assertThat(jobExecution.getStatus())
                .isEqualTo(BatchStatus.COMPLETED);

        assertThat(stepExecution.getStatus())
                .isEqualTo(BatchStatus.COMPLETED);

        assertThat(stepExecution.getReadCount())
                .isEqualTo(2L);

        assertThat(stepExecution.getWriteCount())
                .isEqualTo(2L);

        assertExpired(
                expiredBeforeCutoff,
                BASE_CUTOFF_AT
        );

        assertExpired(
                expiredAtCutoff,
                BASE_CUTOFF_AT
        );

        assertReservationState(
                notExpired,
                ReservationStatus.RESERVED,
                PerformanceSeatStatus.RESERVED
        );

        assertThat(
                findHistories(
                        notExpired.reservationId()
                )
        ).isEmpty();
    }

    @Test
    void chunk_size보다_많은_만료_대상을_모두_처리한다()
            throws Exception {

        List<ReservationData> expirationTargets =
                new ArrayList<>();

        for (int index = 0; index < 5; index++) {
            expirationTargets.add(
                    createReservation(
                            BASE_CUTOFF_AT
                                    .minusMinutes(20)
                                    .plusSeconds(index),
                            true
                    )
            );
        }

        JobExecution jobExecution =
                jobLauncher.launch();

        StepExecution stepExecution =
                findExpirationStep(jobExecution);

        assertThat(jobExecution.getStatus())
                .isEqualTo(BatchStatus.COMPLETED);

        /*
         * chunk-size와 pageSize가 2인데 대상은 5건이다.
         *
         * 첫 페이지만 읽고 끝나지 않고
         * 2건 + 2건 + 1건을 모두 처리했는지 확인한다.
         */
        assertThat(stepExecution.getReadCount())
                .isEqualTo(5L);

        assertThat(stepExecution.getWriteCount())
                .isEqualTo(5L);

        assertThat(stepExecution.getRollbackCount())
                .isZero();

        assertThat(expirationTargets)
                .allSatisfy(
                        target ->
                                assertExpired(
                                        target,
                                        BASE_CUTOFF_AT
                                )
                );
    }

    @Test
    void 만료_대상이_없으면_정상_완료한다()
            throws Exception {

        ReservationData notExpired =
                createReservation(
                        BASE_CUTOFF_AT.minusMinutes(5),
                        true
                );

        JobExecution jobExecution =
                jobLauncher.launch();

        StepExecution stepExecution =
                findExpirationStep(jobExecution);

        assertThat(jobExecution.getStatus())
                .isEqualTo(BatchStatus.COMPLETED);

        assertThat(stepExecution.getStatus())
                .isEqualTo(BatchStatus.COMPLETED);

        assertThat(stepExecution.getReadCount())
                .isZero();

        assertThat(stepExecution.getWriteCount())
                .isZero();

        assertThat(stepExecution.getRollbackCount())
                .isZero();

        assertReservationState(
                notExpired,
                ReservationStatus.RESERVED,
                PerformanceSeatStatus.RESERVED
        );

        assertThat(
                findHistories(
                        notExpired.reservationId()
                )
        ).isEmpty();
    }

    @Test
    void 실패한_chunk만_롤백하고_이전_chunk는_유지한다()
            throws Exception {

        /*
         * chunk-size = 2
         *
         * 첫 번째 Chunk:
         * 1번, 2번 모두 정상 데이터
         *
         * 두 번째 Chunk:
         * 3번은 정상 데이터
         * 4번은 Reservation=RESERVED,
         * PerformanceSeat=AVAILABLE인 비정상 데이터
         */
        ReservationData firstChunkFirst =
                createReservation(
                        BASE_CUTOFF_AT.minusMinutes(20),
                        true
                );

        ReservationData firstChunkSecond =
                createReservation(
                        BASE_CUTOFF_AT.minusMinutes(20),
                        true
                );

        ReservationData secondChunkFirst =
                createReservation(
                        BASE_CUTOFF_AT.minusMinutes(20),
                        true
                );

        ReservationData secondChunkFailureTarget =
                createReservation(
                        BASE_CUTOFF_AT.minusMinutes(20),
                        false
                );

        JobExecution jobExecution =
                jobLauncher.launch();

        StepExecution stepExecution =
                findExpirationStep(jobExecution);

        assertThat(jobExecution.getStatus())
                .isEqualTo(BatchStatus.FAILED);

        assertThat(stepExecution.getStatus())
                .isEqualTo(BatchStatus.FAILED);

        assertThat(stepExecution.getRollbackCount())
                .isGreaterThan(0L);

        /*
         * 첫 번째 Chunk는 두 건 모두 성공한 뒤 이미 커밋됐다.
         */
        assertExpired(
                firstChunkFirst,
                BASE_CUTOFF_AT
        );

        assertExpired(
                firstChunkSecond,
                BASE_CUTOFF_AT
        );

        /*
         * 두 번째 Chunk에서는 3번 예약 처리가 먼저 성공하지만,
         * 4번 좌석 복구가 실패하면서 Chunk 전체가 롤백된다.
         *
         * 따라서 3번 예약도 RESERVED 상태로 되돌아가야 한다.
         */
        assertReservationState(
                secondChunkFirst,
                ReservationStatus.RESERVED,
                PerformanceSeatStatus.RESERVED
        );

        assertThat(
                findHistories(
                        secondChunkFirst.reservationId()
                )
        ).isEmpty();

        /*
         * 실패를 유발한 4번 예약도 예약 상태 변경이 롤백되고,
         * 원래 AVAILABLE이었던 좌석 상태가 유지돼야 한다.
         */
        assertReservationState(
                secondChunkFailureTarget,
                ReservationStatus.RESERVED,
                PerformanceSeatStatus.AVAILABLE
        );

        assertThat(
                findHistories(
                        secondChunkFailureTarget.reservationId()
                )
        ).isEmpty();

        /*
         * 성공적으로 커밋된 첫 번째 Chunk의 두 건만
         * writeCount에 반영돼야 한다.
         */
        assertThat(stepExecution.getWriteCount())
                .isEqualTo(2L);
    }

    @Test
    void 실행_기준_시각을_StepExecutionContext에_저장한다()
            throws Exception {

        JobExecution jobExecution =
                jobLauncher.launch();

        StepExecution stepExecution =
                findExpirationStep(jobExecution);

        assertThat(jobExecution.getStatus())
                .isEqualTo(BatchStatus.COMPLETED);

        assertThat(stepExecution.getStatus())
                .isEqualTo(BatchStatus.COMPLETED);

        /*
         * cutoffAt은 더 이상 JobParameter가 아니다.
         *
         * Step 시작 시 Clock을 통해 결정한 실행 기준 시각을
         * StepExecutionContext에 저장하고,
         * Reader와 Writer가 동일한 값을 사용한다.
         */
        String cutoffAt =
                stepExecution
                        .getExecutionContext()
                        .getString(
                                ReservationExpirationStepListener
                                        .CUTOFF_AT
                        );

        assertThat(LocalDateTime.parse(cutoffAt))
                .isEqualTo(BASE_CUTOFF_AT);
    }

    private void createCommonFixture() {
        transactionTemplate.executeWithoutResult(
                status -> {
                    Event event =
                            eventRepository.save(
                                    Event.create(
                                            "테스트 공연",
                                            "예약 만료 Batch 테스트"
                                    )
                            );

                    Venue venue =
                            venueRepository.save(
                                    Venue.create(
                                            "테스트 공연장",
                                            "서울특별시"
                                    )
                            );

                    LocalDateTime performanceStartAt =
                            BASE_CUTOFF_AT.plusDays(30);

                    Performance performance =
                            performanceRepository.saveAndFlush(
                                    Performance.create(
                                            event,
                                            venue,
                                            performanceStartAt,
                                            BASE_CUTOFF_AT
                                                    .minusDays(1),
                                            performanceStartAt
                                                    .minusDays(2)
                                    )
                            );

                    venueId = venue.getId();
                    performanceId = performance.getId();
                }
        );
    }

    /*
     * 예약 생성 이력은 이번 Batch 테스트의 검증 대상이 아니다.
     *
     * 따라서 ReservationService.create()를 호출하지 않고
     * Reservation을 직접 저장해 만료 이력만 확인한다.
     */
    private ReservationData createReservation(
            LocalDateTime reservedAt,
            boolean reserveSeat
    ) {
        return transactionTemplate.execute(
                status -> {
                    Long performanceSeatId =
                            createPerformanceSeat(
                                    reservedAt,
                                    reserveSeat
                            );

                    PerformanceSeat performanceSeat =
                            performanceSeatRepository
                                    .findById(performanceSeatId)
                                    .orElseThrow();

                    Reservation reservation =
                            reservationRepository.saveAndFlush(
                                    Reservation.create(
                                            UUID.randomUUID(),
                                            performanceSeat,
                                            reservedAt
                                    )
                            );

                    return new ReservationData(
                            reservation.getId(),
                            performanceSeatId
                    );
                }
        );
    }

    private Long createPerformanceSeat(
            LocalDateTime reservedAt,
            boolean reserveSeat
    ) {
        Venue venue =
                entityManager.getReference(
                        Venue.class,
                        venueId
                );

        Performance performance =
                entityManager.getReference(
                        Performance.class,
                        performanceId
                );

        VenueSeat venueSeat =
                venueSeatRepository.saveAndFlush(
                        VenueSeat.create(
                                venue,
                                "A",
                                "1",
                                String.valueOf(++seatSequence)
                        )
                );

        PerformanceSeat performanceSeat =
                performanceSeatRepository.saveAndFlush(
                        PerformanceSeat.create(
                                performance,
                                venueSeat
                        )
                );

        if (reserveSeat) {
            long updatedCount =
                    performanceSeatRepository
                            .reserveIfAvailable(
                                    performanceSeat.getId(),
                                    reservedAt
                            );

            assertThat(updatedCount)
                    .isEqualTo(1L);

            /*
             * 조건부 UPDATE는 영속성 컨텍스트를 우회하므로
             * DB에 반영된 RESERVED 상태를 다시 읽는다.
             */
            entityManager.refresh(performanceSeat);
        }

        return performanceSeat.getId();
    }

    private StepExecution findExpirationStep(
            JobExecution jobExecution
    ) {
        return jobExecution
                .getStepExecutions()
                .stream()
                .filter(
                        stepExecution ->
                                stepExecution
                                        .getStepName()
                                        .equals(
                                                ReservationExpirationJobConfig
                                                        .STEP_NAME
                                        )
                )
                .findFirst()
                .orElseThrow();
    }

    private void assertExpired(
            ReservationData data,
            LocalDateTime cutoffAt
    ) {
        assertReservationState(
                data,
                ReservationStatus.EXPIRED,
                PerformanceSeatStatus.AVAILABLE
        );

        List<ReservationStatusHistory> histories =
                findHistories(data.reservationId());

        assertThat(histories)
                .hasSize(1);

        ReservationStatusHistory history =
                histories.getFirst();

        assertThat(history.getPreviousStatus())
                .isEqualTo(ReservationStatus.RESERVED);

        assertThat(history.getChangedStatus())
                .isEqualTo(ReservationStatus.EXPIRED);

        assertThat(history.getChangeReason())
                .isEqualTo(
                        ReservationStatusChangeReason
                                .RESERVATION_EXPIRED
                );

        assertThat(history.getActorType())
                .isEqualTo(
                        ReservationStatusChangeActorType.SYSTEM
                );

        assertThat(history.getActorReference())
                .isNull();

        /*
         * Step 시작 시 결정한 cutoffAt을
         * Reader와 만료 처리 전체에서 동일하게 사용했는지 확인한다.
         */
        assertThat(history.getChangedAt())
                .isEqualTo(cutoffAt);
    }

    private void assertReservationState(
            ReservationData data,
            ReservationStatus expectedReservationStatus,
            PerformanceSeatStatus expectedSeatStatus
    ) {
        Reservation reservation =
                findReservation(data.reservationId());

        PerformanceSeat performanceSeat =
                findPerformanceSeat(
                        data.performanceSeatId()
                );

        assertThat(reservation.getStatus())
                .isEqualTo(expectedReservationStatus);

        assertThat(performanceSeat.getStatus())
                .isEqualTo(expectedSeatStatus);
    }

    private Reservation findReservation(
            Long reservationId
    ) {
        return reservationRepository
                .findById(reservationId)
                .orElseThrow();
    }

    private PerformanceSeat findPerformanceSeat(
            Long performanceSeatId
    ) {
        return performanceSeatRepository
                .findById(performanceSeatId)
                .orElseThrow();
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
        transactionTemplate.executeWithoutResult(
                status -> {
                    entityManager.clear();

                    statusHistoryRepository.deleteAllInBatch();
                    reservationRepository.deleteAllInBatch();
                    performanceSeatRepository.deleteAllInBatch();
                    venueSeatRepository.deleteAllInBatch();
                    performanceRepository.deleteAllInBatch();
                    eventRepository.deleteAllInBatch();
                    venueRepository.deleteAllInBatch();
                }
        );
    }

    private record ReservationData(
            Long reservationId,
            Long performanceSeatId
    ) {
    }

    /*
     * 테스트에서는 실제 시스템 시간을 사용하지 않는다.
     *
     * 예약 만료 여부가 "현재 시각"에 의존하기 때문에
     * Clock을 고정해서 경계값 테스트를 항상 동일하게 재현한다.
     */
    @TestConfiguration(proxyBeanMethods = false)
    static class TestClockConfig {

        @Bean
        @Primary
        Clock testClock() {
            return Clock.fixed(
                    BASE_CUTOFF_AT
                            .atZone(TEST_ZONE_ID)
                            .toInstant(),
                    TEST_ZONE_ID
            );
        }
    }
}