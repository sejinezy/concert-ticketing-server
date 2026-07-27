package com.ticketing.idempotency.application;


import com.ticketing.idempotency.domain.IdempotencyKey;
import com.ticketing.idempotency.domain.IdempotencyOperation;
import com.ticketing.idempotency.domain.IdempotencyRequest;
import com.ticketing.idempotency.domain.IdempotencyStatus;
import com.ticketing.idempotency.repository.IdempotencyRequestRepository;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    private final IdempotencyRequestRepository idempotencyRequestRepository;


    public IdempotencyService(IdempotencyRequestRepository idempotencyRequestRepository) {
        this.idempotencyRequestRepository = idempotencyRequestRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public IdempotencyExecution begin(
            String rawIdempotencyKey,
            IdempotencyOperation operation,
            String requestHash
    ) {
        IdempotencyKey idempotencyKey = IdempotencyKey.from(rawIdempotencyKey);

        int insertedCount = registerProcessingRequest(idempotencyKey, operation, requestHash);

        IdempotencyRequest idempotencyRequest = getIdempotencyRequest(idempotencyKey);

        if (insertedCount == 1) {
            return IdempotencyExecution.first(idempotencyRequest);
        }

        idempotencyRequest.validateSameRequest(operation, requestHash);

        return resolveExistingRequest(idempotencyRequest);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void complete(
            IdempotencyExecution execution, Long resultId
    ) {

        if (!execution.firstExecution()) {
            throw new CoreException(
                    ErrorType.IDEMPOTENCY_INVALID_STATE
            );
        }

        execution.request().complete(
                resultId,
                LocalDateTime.now()
        );

    }

    private int registerProcessingRequest(
            IdempotencyKey idempotencyKey,
            IdempotencyOperation operation,
            String requestHash
    ) {
        return idempotencyRequestRepository
                .insertIfAbsent(
                        idempotencyKey.getValue(),
                        operation.name(),
                        requestHash,
                        IdempotencyStatus.PROCESSING.name(),
                        LocalDateTime.now()
                );
    }

    private IdempotencyRequest getIdempotencyRequest(
            IdempotencyKey idempotencyKey
    ) {
        return idempotencyRequestRepository
                .findByIdempotencyKey(
                        idempotencyKey.getValue()
                )
                .orElseThrow(() ->
                        new CoreException(
                                ErrorType
                                        .IDEMPOTENCY_REQUEST_NOT_FOUND
                        )
                );
    }

    private IdempotencyExecution resolveExistingRequest(
            IdempotencyRequest idempotencyRequest
    ) {
        if (idempotencyRequest.isCompleted()) {
            return IdempotencyExecution.replay(
                    idempotencyRequest
            );
        }

        if (idempotencyRequest.isProcessing()) {
            throw new CoreException(
                    ErrorType
                            .IDEMPOTENCY_REQUEST_IN_PROGRESS
            );
        }

        throw new CoreException(
                ErrorType.IDEMPOTENCY_INVALID_STATE
        );
    }
}
