package com.ticketing.mockpayment.payment;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mock/scenario")
public class MockScenarioController {

    private final MockPaymentScenario mockPaymentScenario;

    public MockScenarioController(
            MockPaymentScenario mockPaymentScenario
    ) {
        this.mockPaymentScenario = mockPaymentScenario;
    }

    @PostMapping("/{scenario}")
    public void changeScenario(
            @PathVariable MockPaymentScenario.Scenario scenario
    ) {
        mockPaymentScenario.change(scenario);
    }

    @GetMapping
    public MockPaymentScenario.Scenario currentScenario() {
        return mockPaymentScenario.get();
    }
}
