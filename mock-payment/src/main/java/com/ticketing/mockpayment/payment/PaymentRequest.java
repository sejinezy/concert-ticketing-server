package com.ticketing.mockpayment.payment;

public record PaymentRequest(
        Long reservationId,
        Long amount
) {
}
