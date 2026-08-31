package com.ticketing.mockpayment.payment;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentStore paymentStore;
    private final MockPaymentScenario mockPaymentScenario;

    public PaymentController(PaymentStore paymentStore, MockPaymentScenario mockPaymentScenario) {
        this.paymentStore = paymentStore;
        this.mockPaymentScenario = mockPaymentScenario;
    }

    @PostMapping
    public PaymentResponse pay(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody PaymentRequest request
    ) throws InterruptedException {

        return switch (mockPaymentScenario.get()) {

            case SUCCESS ->
                    paymentStore.getOrCreate(
                            idempotencyKey,
                            this::createPayment
                    );

            case DELAY -> {
                PaymentResponse response =
                        paymentStore.getOrCreate(
                                idempotencyKey,
                                this::createPayment
                        );

                Thread.sleep(5_000);

                yield response;
            }

            case ERROR ->
                    throw new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Mock payment failure"
                    );

            case BAD_REQUEST -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Mock invaild payment request"
            );
        };
    }

    @GetMapping
    public List<PaymentResponse> payments() {
        return paymentStore.findAll();
    }

    @DeleteMapping
    public void clearPayments() {
        paymentStore.clear();
    }

    private PaymentResponse createPayment() {
        return new PaymentResponse(
                UUID.randomUUID().toString(),
                "SUCCESS"
        );
    }
}
