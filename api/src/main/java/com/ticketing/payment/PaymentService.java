package com.ticketing.payment;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final PaymentClient paymentClient;

    public PaymentService(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    public PaymentResponse pay(
            Long reservationId,
            Long amount,
            String idempotencyKey
    ) {
        return paymentClient.pay(
                reservationId,
                amount,
                idempotencyKey
        );
    }
}
