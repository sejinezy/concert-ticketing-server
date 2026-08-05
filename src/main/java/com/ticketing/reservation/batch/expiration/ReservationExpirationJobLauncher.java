package com.ticketing.reservation.batch.expiration;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReservationExpirationJobLauncher {

    private final JobOperator jobOperator;
    private final Job reservationExpirationJob;

    public ReservationExpirationJobLauncher(JobOperator jobOperator, Job reservationExpirationJob) {
        this.jobOperator = jobOperator;
        this.reservationExpirationJob = reservationExpirationJob;
    }

    public JobExecution launch(
            LocalDateTime cutoffAt
    ) throws JobExecutionException {

        JobParameters jobParameters = new ReservationExpirationJobParameters(cutoffAt).toJobParameters();

        log.info(
                "예약 만료 Job 실행을 시작합니다. "
                        + "jobName={}, cutoffAt={}",
                reservationExpirationJob.getName(),
                cutoffAt
        );

        JobExecution jobExecution = jobOperator.start(reservationExpirationJob, jobParameters);

        log.info(
                "예약 만료 Job 실행 결과를 반환받았습니다. "
                        + "jobExecutionId={}, status={}, "
                        + "exitStatus={}, cutoffAt={}",
                jobExecution.getId(),
                jobExecution.getStatus(),
                jobExecution.getExitStatus(),
                cutoffAt
        );

        return jobExecution;

    }
}
