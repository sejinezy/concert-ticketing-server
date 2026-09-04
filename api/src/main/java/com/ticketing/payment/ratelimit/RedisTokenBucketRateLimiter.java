package com.ticketing.payment.ratelimit;

import com.ticketing.payment.ratelimit.exception.RateLimiterUnavailableException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTokenBucketRateLimiter implements DistributedRateLimiter {

    private static final String KEY_PREFIX = "rate_limit:";

    private final StringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> tokenBucketScript;

    @Value("${external.payment.rate-limit.capacity}")
    private long capacity;

    @Value("${external.payment.rate-limit.refill-rate}")
    private double refillRate;

    @Override
    public boolean tryAcquire(String key) {

        try {
            String redisKey = KEY_PREFIX + key;

            long nowMillis = System.currentTimeMillis();

            log.info(
                    "Token Bucket 실행 - key={}, capacity={}, refillRate={}",
                    redisKey,
                    capacity,
                    refillRate
            );

            Long result = redisTemplate.execute(
                    tokenBucketScript,
                    List.of(redisKey),
                    String.valueOf(capacity),
                    String.valueOf(refillRate),
                    "1",
                    String.valueOf(nowMillis)
            );

            log.info(
                    "Token Bucket 결과 - key={}, result={}",
                    redisKey,
                    result
            );

            return Long.valueOf(1L).equals(result);

        // Redis Fail-closed -> 외부 Payment API 호출 자체를 막는다.
        } catch (RuntimeException e) {
            log.error("Redis Token Bucket 실행 실패", e);
            throw new RateLimiterUnavailableException(e);
        }
    }
}
