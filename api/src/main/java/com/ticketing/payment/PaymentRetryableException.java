package com.ticketing.payment;

public class PaymentRetryableException extends RuntimeException {

    public PaymentRetryableException(String message) {
        super(message);
    }
}
