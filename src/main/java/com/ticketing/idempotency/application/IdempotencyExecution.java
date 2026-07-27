package com.ticketing.idempotency.application;

import com.ticketing.idempotency.domain.IdempotencyRequest;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;

public record IdempotencyExecution(
        IdempotencyRequest request,
        boolean firstExecution
) {

    public static IdempotencyExecution first(
            IdempotencyRequest request
    ) {
        return new IdempotencyExecution(request, true);
    }

    public static IdempotencyExecution replay(
            IdempotencyRequest request
    ) {
        return new IdempotencyExecution(request, false);
    }

    public boolean isReplay() {
        return !firstExecution;
    }

    public Long getCompletedResultId() {

        if (!request.isCompleted()
                || request.getResultId() == null) {

            throw new CoreException(
                    ErrorType.IDEMPOTENCY_RESULT_NOT_FOUND
            );
        }

        return request.getResultId();
    }

}
