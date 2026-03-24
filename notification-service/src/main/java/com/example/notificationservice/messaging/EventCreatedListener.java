package com.example.notificationservice.messaging;

import com.example.notificationservice.config.RabbitConfig;
import com.example.notificationservice.dto.EventCreatedMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventCreatedListener {

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void handleEventCreated(EventCreatedMessage message) {
        System.out.println("Notification received:");
        System.out.println("Event created -> ID: " + message.getEventId()
                + ", Title: " + message.getTitle()
                + ", Venue: " + message.getVenue());
    }
}