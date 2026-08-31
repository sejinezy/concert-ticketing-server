package com.ticketing.payment;

public class PaymentNonRetryableException extends RuntimeException {

    public PaymentNonRetryableException(String message) {
        super(message);
    }
}
