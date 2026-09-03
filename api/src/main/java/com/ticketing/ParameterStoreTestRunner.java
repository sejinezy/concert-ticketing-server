package com.ticketing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ParameterStoreTestRunner implements ApplicationRunner {

    @Value("${test-message}")
    private String testMessage;

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("Parameter Store value = " + testMessage);
    }
}