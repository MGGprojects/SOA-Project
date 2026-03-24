package com.example.eventservice.messaging;

import com.example.eventservice.config.RabbitConfig;
import com.example.eventservice.dto.EventCreatedMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishEventCreated(EventCreatedMessage message) {
        rabbitTemplate.convertAndSend(RabbitConfig.QUEUE_NAME, message);
    }
}