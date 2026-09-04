package com.ticketing.payment;

import com.ticketing.payment.ratelimit.DistributedRateLimiter;
import com.ticketing.payment.ratelimit.exception.PaymentRateLimitExceededException;
import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.bulkhead.annotation.Bulkhead.Type;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentClient {

    private static final String PAYMENT_RATE_LIMIT_KEY = "payment";

    private final RestClient restClient;
    private final DistributedRateLimiter distributedRateLimiter;

    public PaymentClient(RestClient restClient, DistributedRateLimiter distributedRateLimiter) {
        this.restClient = restClient;
        this.distributedRateLimiter = distributedRateLimiter;
    }


    @Retry(name = "payment")
    @Bulkhead(name = "payment", type = Type.SEMAPHORE)
    @CircuitBreaker(name = "payment")
    public PaymentResponse pay(
            Long reservationId,
            Long amount,
            String idempotencyKey
    ) {

        if (!distributedRateLimiter.tryAcquire(PAYMENT_RATE_LIMIT_KEY)) {
            throw new PaymentRateLimitExceededException();
        }

        PaymentRequest request = new PaymentRequest(reservationId, amount);

        return restClient.post()
                .uri("/payments")
                .header("Idempotency-Key", idempotencyKey)
                .body(request)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        (request1, response)->{
                            throw new PaymentNonRetryableException(
                                    "Payment request rejected: "
                                            + response.getStatusCode()
                            );
                        }
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        (request1, response) ->{
                            throw new PaymentRetryableException(
                                    "Payment server error: "
                                            + response.getStatusCode()
                            );
                        }
                )
                .body(PaymentResponse.class);
    }
}
