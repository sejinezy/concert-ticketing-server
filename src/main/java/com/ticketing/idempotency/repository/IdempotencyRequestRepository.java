package com.ticketing.idempotency.repository;

import com.ticketing.idempotency.domain.IdempotencyRequest;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdempotencyRequestRepository extends JpaRepository<IdempotencyRequest, Long> {

    @Modifying(flushAutomatically = true)
    @Query(
            value = """
                    INSERT IGNORE INTO idempotency_requests (
                        idempotency_key,
                        operation,
                        request_hash,
                        status,
                        created_at
                    ) VALUES (
                        :idempotencyKey,
                        :operation,
                        :requestHash,
                        :status,
                        :createdAt
                    )
                    """,
            nativeQuery = true
    )
    int insertIfAbsent(
            @Param("idempotencyKey")
            String idempotencyKey,

            @Param("operation")
            String operation,

            @Param("requestHash")
            String requestHash,

            @Param("status")
            String status,

            @Param("createdAt")
            LocalDateTime createdAt
    );

    Optional<IdempotencyRequest> findByIdempotencyKey(
            String idempotencyKey
    );
}
