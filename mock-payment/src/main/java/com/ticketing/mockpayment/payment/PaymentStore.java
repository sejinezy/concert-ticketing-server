package com.ticketing.mockpayment.payment;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class PaymentStore {

    private final Map<String, PaymentResponse> payments = new ConcurrentHashMap<>();

    private final Counter paymentCreationCounter;

    public PaymentStore(MeterRegistry meterRegistry) {

        this.paymentCreationCounter =
                Counter.builder("mock.payment.creation")
                        .description("Number of newly created mock payments")
                        .register(meterRegistry);
    }

    public PaymentResponse getOrCreate(
            String idempotencyKey,
            Supplier<PaymentResponse> paymentSupplier
    ) {

        return payments.computeIfAbsent(
                idempotencyKey,
                key -> {
                    PaymentResponse payment = paymentSupplier.get();
                    paymentCreationCounter.increment();
                    return payment;
                }
        );
    }

    public List<PaymentResponse> findAll() {
        return List.copyOf(payments.values());
    }

    public void clear() {
        payments.clear();
    }
}
