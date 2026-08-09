package com.ticketing.reservation.batch.expiration;

import com.ticketing.reservation.repository.projection.ReservationExpirationTarget;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ReservationExpirationJobConfig {

    public static final String JOB_NAME = "reservationExpirationJob";
    public static final String STEP_NAME = "reservationExpirationStep";

    @Bean(name = JOB_NAME)
    public Job reservationExpirationJob(
            JobRepository jobRepository,
            @Qualifier(STEP_NAME) Step reservationExpirationStep
    ) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(reservationExpirationStep)
                .build();
    }

    @Bean(name = STEP_NAME)
    public Step reservationExpirationStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ReservationExpirationItemReader itemReader,
            ReservationExpirationItemWriter itemWriter,
            ReservationExpirationStepListener listener,
            @Value("${reservation.expiration.chunk-size:100}")
            int chunkSize
    ) {

        return new StepBuilder(STEP_NAME, jobRepository)
                .<ReservationExpirationTarget, ReservationExpirationTarget>chunk(chunkSize)
                .reader(itemReader)
                .writer(itemWriter)
                .listener(listener)
                .transactionManager(transactionManager)
                .build();
    }
}
