package com.ticketing.reservation.batch.expiration;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("batch")
public class ReservationExpirationBatchApplicationRunner implements ApplicationRunner {

    private static final String CUTOFF_AT_OPTION = ReservationExpirationJobParameters.CUTOFF_AT;

    private final ReservationExpirationJobLauncher jobLauncher;
    private final Clock clock;

    public ReservationExpirationBatchApplicationRunner(ReservationExpirationJobLauncher jobLauncher, Clock clock) {
        this.jobLauncher = jobLauncher;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments arguments) throws Exception {

        LocalDateTime cutoffAt = resolveCutoffAt(arguments);

        log.info(
                "예약 만료 Batch 애플리케이션을 시작합니다. "
                        + "cutoffAt={}",
                cutoffAt
        );

        JobExecution jobExecution = jobLauncher.launch(cutoffAt);

        validateCompleted(jobExecution);

        log.info(
                "예약 만료 Batch 애플리케이션이 완료되었습니다. "
                        + "jobExecutionId={}, status={}, cutoffAt={}",
                jobExecution.getId(),
                jobExecution.getStatus(),
                cutoffAt
        );
    }

    private LocalDateTime resolveCutoffAt(
            ApplicationArguments arguments
    ) {
        if (!arguments.containsOption(CUTOFF_AT_OPTION)) {
            return currentTime();
        }

        List<String> values = arguments.getOptionValues(CUTOFF_AT_OPTION);

        if (values == null || values.size() != 1) {
            throw new IllegalArgumentException("cutoffAt은 하나의 값만 전달해야 합니다.");
        }

        String value = values.getFirst();

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("cutoffAt 값은 비어 있을 수 없습니다.");
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(
                    "cutoffAt은 ISO-8601 형식이어야 합니다. "
                            + "예: 2026-07-28T12:00:00",
                    exception
            );
        }
    }

    private LocalDateTime currentTime() {
        return LocalDateTime.now(clock)
                .truncatedTo(ChronoUnit.SECONDS);
    }

    private void validateCompleted(
            JobExecution jobExecution
    ) {
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            return;
        }

        throw new IllegalStateException(
                "예약 만료 Job이 정상적으로 완료되지 않았습니다. "
                        + "jobExecutionId="
                        + jobExecution.getId()
                        + ", status="
                        + jobExecution.getStatus()
                        + ", exitStatus="
                        + jobExecution.getExitStatus()
        );
    }
}
