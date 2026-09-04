package com.ticketing.payment.ratelimit.exception;

public class RateLimiterUnavailableException extends RuntimeException {

    public RateLimiterUnavailableException(Throwable cause) {
        super("Distributed rate limiter is unavailable.", cause);
    }
}
