package com.ticketing.payment;

public record PaymentRequest(
        Long reservationId,
        Long amount
) {
}
