package com.ticketing.payment.ratelimit.exception;

import com.ticketing.support.error.CoreException;
import com.ticketing.support.error.ErrorType;

public class PaymentRateLimitExceededException extends CoreException {

    public PaymentRateLimitExceededException() {
        super(ErrorType.PAYMENT_RATE_LIMIT_EXCEEDED);
    }
}
