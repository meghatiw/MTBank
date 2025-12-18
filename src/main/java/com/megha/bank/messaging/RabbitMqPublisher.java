package com.megha.bank.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.beans.factory.ObjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RabbitMqPublisher will only be created when a RabbitTemplate bean is present (i.e., RabbitMQ configured).
 * It is marked @Primary so that, when present, it is the preferred MessagePublisher implementation.
 *
 * This version uses ObjectProvider<RabbitTemplate> to avoid IDE/autowire warnings when RabbitTemplate is absent.
 */
@Component
@Primary
@ConditionalOnBean(RabbitTemplate.class)
public class RabbitMqPublisher implements MessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMqPublisher(ObjectProvider<RabbitTemplate> rabbitTemplateProvider) {
        this.rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
    }

    @Override
    public void publish(String routingKey, String message) {
        if (rabbitTemplate == null) {
            log.warn("RabbitTemplate not available, falling back to logging. Message to {}: {}", routingKey, message);
            return;
        }
        // Publish to default exchange with routing key
        rabbitTemplate.convertAndSend(routingKey, message);
        log.info("Published message to rabbit with routingKey={}", routingKey);
    }
}
