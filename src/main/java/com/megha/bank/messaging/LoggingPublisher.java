package com.megha.bank.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingPublisher implements MessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingPublisher.class);

    @Override
    public void publish(String routingKey, String message) {
        log.info("[LoggingPublisher] publish to {}: {}", routingKey, message);
    }
}
