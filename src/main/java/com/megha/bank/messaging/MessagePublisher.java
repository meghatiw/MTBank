package com.megha.bank.messaging;

public interface MessagePublisher {
    void publish(String routingKey, String message);
}
