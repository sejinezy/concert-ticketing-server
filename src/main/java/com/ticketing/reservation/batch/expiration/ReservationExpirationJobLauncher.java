package com.ticketing.reservation.batch.expiration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
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

    public JobExecution launch() throws JobExecutionException {

        log.info(
                "예약 만료 Job 실행을 시작합니다. jobName={}",
                reservationExpirationJob.getName()
        );

        return jobOperator.startNextInstance(
                reservationExpirationJob
        );
    }
}
