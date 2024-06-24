package com.example.demo.flowable;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CallExternalSystemDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CallExternalSystemDelegate.class);

    @Override
    public void execute(DelegateExecution delegateExecution) {
        LOGGER.info("Calling the external system for employee {}", delegateExecution.getVariable("employee"));
    }
}
