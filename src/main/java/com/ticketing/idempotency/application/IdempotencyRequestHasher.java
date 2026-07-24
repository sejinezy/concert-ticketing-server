package com.ticketing.idempotency.application;

import com.ticketing.idempotency.domain.IdempotencyOperation;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyRequestHasher {

    private static final String HASH_VERSION = "v1";
    private static final String HASH_ALGORITHM = "SHA-256";

    public String hashReservationCreate(
            Long performanceSeatId,
            UUID queueEntryId
    ) {

        Objects.requireNonNull(
                performanceSeatId,
                "performanceSeatId must not be null"
        );

        Objects.requireNonNull(
                queueEntryId,
                "queueEntryId must not be null"
        );

        String canonicalRequest = String.join(
                "|",
                HASH_VERSION,
                "operation="
                        + IdempotencyOperation
                        .CREATE_RESERVATION
                        .name(),
                "performanceSeatId="
                        + performanceSeatId,
                "queueEntryId="
                        + queueEntryId
        );

        return sha256(canonicalRequest);
    }

    private String sha256(String source) {

        try {
            MessageDigest messageDigest = MessageDigest.getInstance(HASH_ALGORITHM);

            byte[] digest = messageDigest.digest(
                    source.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(digest);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available.",
                    exception
            );
        }
    }
}
