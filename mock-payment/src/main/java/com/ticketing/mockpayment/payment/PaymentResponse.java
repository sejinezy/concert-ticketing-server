package com.ticketing.mockpayment.payment;

public record PaymentResponse(
        String paymentId,
        String status
) {
}
