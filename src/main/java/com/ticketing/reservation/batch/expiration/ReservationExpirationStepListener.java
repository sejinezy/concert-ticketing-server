package com.ticketing.reservation.batch.expiration;


import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationExpirationStepListener
        implements StepExecutionListener {

    public static final String CUTOFF_AT =
            "reservationExpiration.cutoffAt";

    private final Clock clock;
    private final JobRepository jobRepository;

    @Override
    public void beforeStep(StepExecution stepExecution) {

        ExecutionContext context =
                stepExecution.getExecutionContext();

        if (context.containsKey(CUTOFF_AT)) {
            return;
        }

        LocalDateTime cutoffAt =
                LocalDateTime.now(clock)
                        .truncatedTo(ChronoUnit.SECONDS);

        context.putString(
                CUTOFF_AT,
                cutoffAt.toString()
        );

        jobRepository.updateExecutionContext(stepExecution);
    }
}
