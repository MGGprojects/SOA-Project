package com.example.notificationservice.messaging;

import com.example.notificationservice.config.RabbitConfig;
import com.example.notificationservice.dto.EventCreatedMessage;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens for event-creation messages on the RabbitMQ queue.
 * When a message is received, it persists a notification to the database
 * and logs the event details.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventCreatedListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void handleEventCreated(EventCreatedMessage message) {
        log.info("Received event-created message: eventId={}, title='{}', venue='{}', startTime={}, endTime={}, businessId={}",
                message.getEventId(),
                message.getTitle(),
                message.getVenue(),
                message.getStartTime(),
                message.getEndTime(),
                message.getBusinessId());

        try {
            // Persist the notification to the database
            notificationService.createFromEventCreated(message);
            log.info("Notification persisted successfully for eventId={}", message.getEventId());
        } catch (Exception e) {
            log.error("Failed to persist notification for eventId={}: {}", message.getEventId(), e.getMessage(), e);
        }
    }
}
