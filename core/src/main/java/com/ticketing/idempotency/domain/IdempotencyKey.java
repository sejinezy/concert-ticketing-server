package com.ticketing.idempotency.domain;

import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import java.util.UUID;
import lombok.Getter;
import org.springframework.util.StringUtils;

// HTTP 헤더에서 받은 문자열을 그대로 서비스 전체에서 사용하지 않고, 먼저 유효한 멱등성 키인지 검증하는 값 객체
@Getter
public final class IdempotencyKey {

    private final String value;

    private IdempotencyKey(String value) {
        this.value = value;
    }

    public static IdempotencyKey from(String rawValue) {

        if (!StringUtils.hasText(rawValue)) {
            throw new CoreException(ErrorType.IDEMPOTENCY_KEY_REQUIRED);
        }

        try {
            UUID parsedValue = UUID.fromString(rawValue.trim());

            return new IdempotencyKey(parsedValue.toString());
        } catch (IllegalArgumentException exception) {
            throw new CoreException(ErrorType.INVALID_IDEMPOTENCY_KEY);
        }
    }

}
