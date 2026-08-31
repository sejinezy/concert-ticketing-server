package com.ticketing.payment;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/payments")
public class PaymentTestController {

    private final PaymentClient paymentClient;

    public PaymentTestController(PaymentClient paymentClient) {
        this.paymentClient = paymentClient;
    }

    @PostMapping("/{reservationId}")
    public PaymentResponse pay(
            @PathVariable Long reservationId
    ) {
        String idempotencyKey = UUID.randomUUID().toString();

        return paymentClient.pay(
                reservationId,
                50_000L,
                idempotencyKey
        );
    }

}
