package com.ticketing.idempotency.domain;


import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_idempotency_requests_idempotency_key",
                        columnNames = "idempotency_key"
                )
        }
)
public class IdempotencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 36)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 50)
    private IdempotencyOperation operation;

    @Column(nullable = false, updatable = false, length = 64, columnDefinition = "char(64)")
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private IdempotencyStatus status;

    private Long resultId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    public void validateSameRequest(
            IdempotencyOperation operation,
            String requestHash
    ) {

        boolean sameOperation =
                this.operation == operation;

        boolean sameRequestHash =
                this.requestHash.equals(requestHash);

        if (!sameOperation || !sameRequestHash) {
            throw new CoreException(
                    ErrorType.IDEMPOTENCY_KEY_CONFLICT
            );
        }
    }

    public boolean isProcessing() {
        return status == IdempotencyStatus.PROCESSING;
    }

    public boolean isCompleted() {
        return status == IdempotencyStatus.COMPLETED;
    }

    public void complete(
            Long resultId,
            LocalDateTime completedAt
    ) {

        if (!isProcessing()) {
            throw new CoreException(
                    ErrorType.IDEMPOTENCY_INVALID_STATE
            );
        }

        if (resultId == null || completedAt == null) {
            throw new CoreException(
                    ErrorType.IDEMPOTENCY_RESULT_NOT_FOUND
            );
        }

        this.resultId = resultId;
        this.status = IdempotencyStatus.COMPLETED;
        this.completedAt = completedAt;
    }
}
