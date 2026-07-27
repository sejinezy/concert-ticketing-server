package com.ticketing.reservation.batch.expiration;

import com.ticketing.reservation.application.ReservationExpirationService;
import java.time.LocalDateTime;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ReservationExpirationJobConfig {

    public static final String JOB_NAME = "reservationExpirationJob";
    public static final String STEP_NAME = "reservationExpirationStep";

    private static final String CUTOFF_AT_PARAMETER = "cutoffAt";

    @Bean(name = JOB_NAME)
    public Job reservationExpirationJob(
            JobRepository jobRepository,
            @Qualifier(STEP_NAME) Step reservationExpirationStep
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(reservationExpirationStep)
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step reservationExpirationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ReservationExpirationService reservationExpirationService
    ) {
        return new StepBuilder(STEP_NAME, jobRepository)
                .tasklet(
                        ((contribution, chunkContext) -> {
                            Object parameter = chunkContext
                                    .getStepContext()
                                    .getJobParameters()
                                    .get(CUTOFF_AT_PARAMETER);

                            LocalDateTime cutoffAt = parseCutoffAt(parameter);

                            reservationExpirationService.expireReservations(cutoffAt);

                            return RepeatStatus.FINISHED;
                        }),
                        transactionManager
                )
                .build();
    }

    private LocalDateTime parseCutoffAt(Object parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("cutoffAt JobParameter는 필수입니다.");
        }

        try {
            return LocalDateTime.parse(parameter.toString());
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "cutoffAt은 ISO-8601 형식이어야 합니다. "
                            + "예: 2026-07-27T12:00:00",
                    exception
            );
        }
    }
}
