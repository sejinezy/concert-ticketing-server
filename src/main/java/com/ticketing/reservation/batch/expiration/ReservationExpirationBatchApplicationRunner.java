package com.ticketing.reservation.batch.expiration;

import java.time.Clock;
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

    private final ReservationExpirationJobLauncher jobLauncher;
    private final Clock clock;

    public ReservationExpirationBatchApplicationRunner(ReservationExpirationJobLauncher jobLauncher, Clock clock) {
        this.jobLauncher = jobLauncher;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments arguments) throws Exception {

        log.info("예약 만료 Batch 애플리케이션을 시작합니다.");

        JobExecution jobExecution = jobLauncher.launch();

        validateCompleted(jobExecution);

        log.info(
                "예약 만료 Batch 애플리케이션이 완료되었습니다. "
                        + "jobExecutionId={}, status={}",
                jobExecution.getId(),
                jobExecution.getStatus()
        );
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
