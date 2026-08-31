package com.ticketing.mockpayment.payment;

import org.springframework.stereotype.Component;

@Component
public class MockPaymentScenario {

    private volatile Scenario scenario = Scenario.SUCCESS;

    public Scenario get() {
        return scenario;
    }

    public void change(Scenario scenario) {
        this.scenario = scenario;
    }

    public enum Scenario {
        SUCCESS,
        DELAY,
        ERROR,
        BAD_REQUEST
    }
}
