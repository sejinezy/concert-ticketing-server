package com.ticketing.reservation.batch.expiration;

import java.time.LocalDateTime;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;

public record ReservationExpirationJobParameters(
        LocalDateTime cutoffAt
) {

    public static final String CUTOFF_AT = "cutoffAt";

    public ReservationExpirationJobParameters {
        if (cutoffAt == null) {
            throw new IllegalArgumentException("cutoffAt은 필수입니다.");
        }
    }

    /**
     * 예약 만료 Job 실행에 사용할 Spring Batch JobParameters를 생성한다.
     * <p>
     * cutoffAt은 JobInstance를 식별하는 identifying parameter로 사용한다.
     */
    public JobParameters toJobParameters() {
        return new JobParametersBuilder()
                .addLocalDateTime(
                        CUTOFF_AT,
                        cutoffAt,
                        true
                )
                .toJobParameters();
    }

    /**
     * 실행 중인 Job의 JobParameter를 예약 만료 Job 전용 파라미터 객체로 변환한다.
     */
    public static ReservationExpirationJobParameters from(
            JobParameters jobParameters
    ) {
        if (jobParameters == null) {
            throw new IllegalArgumentException("cutoffAt JobParameter는 필수입니다.");
        }

        LocalDateTime cutoffAt = jobParameters.getLocalDateTime(CUTOFF_AT);

        if (cutoffAt == null) {
            throw new IllegalArgumentException("cutoffAt JobParameter는 필수입니다.");
        }
        return new ReservationExpirationJobParameters(cutoffAt);
    }

}
