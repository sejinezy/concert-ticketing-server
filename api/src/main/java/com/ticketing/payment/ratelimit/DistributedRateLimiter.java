package com.ticketing.payment.ratelimit;

/**
 * 외부 시스템 별로 따로 limiter를 만들 수 있게 인터페이스 정의.
 */
public interface DistributedRateLimiter {
    boolean tryAcquire(String key);
}
