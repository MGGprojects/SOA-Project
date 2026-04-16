package com.example.notificationservice.service;

import com.example.notificationservice.dto.EventCreatedMessage;
import com.example.notificationservice.dto.NotificationListResponse;
import com.example.notificationservice.dto.NotificationResponse;
import com.example.notificationservice.model.Notification;
import com.example.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Creates and persists a notification from an event-created message received via RabbitMQ.
     */
    @Transactional
    public NotificationResponse createFromEventCreated(EventCreatedMessage message) {
        String notificationMessage = String.format(
                "New event created: '%s' at %s, from %s to %s",
                message.getTitle(),
                message.getVenue(),
                message.getStartTime(),
                message.getEndTime()
        );

        Notification notification = new Notification();
        notification.setType("EVENT_CREATED");
        notification.setMessage(notificationMessage);
        notification.setEventId(message.getEventId());
        notification.setEventTitle(message.getTitle());
        notification.setEventVenue(message.getVenue());
        notification.setEventStartTime(message.getStartTime());
        notification.setEventEndTime(message.getEndTime());
        notification.setBusinessId(message.getBusinessId());
        notification.setRead(false);

        Notification saved = notificationRepository.save(notification);
        log.info("Persisted notification id={} for eventId={}", saved.getId(), saved.getEventId());

        return toResponse(saved);
    }

    /**
     * Retrieves all notifications with pagination, ordered by creation time descending.
     */
    @Transactional(readOnly = true)
    public NotificationListResponse getNotifications(int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), limit);
        Page<Notification> notificationPage = notificationRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<NotificationResponse> notifications = notificationPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new NotificationListResponse(notifications, (int) notificationPage.getTotalElements(), page);
    }

    /**
     * Retrieves unread notifications with pagination.
     */
    @Transactional(readOnly = true)
    public NotificationListResponse getUnreadNotifications(int page, int limit) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), limit);
        Page<Notification> notificationPage = notificationRepository.findByReadFalseOrderByCreatedAtDesc(pageable);

        List<NotificationResponse> notifications = notificationPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new NotificationListResponse(notifications, (int) notificationPage.getTotalElements(), page);
    }

    /**
     * Retrieves a single notification by ID.
     */
    @Transactional(readOnly = true)
    public Optional<NotificationResponse> getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .map(this::toResponse);
    }

    /**
     * Marks a notification as read.
     */
    @Transactional
    public Optional<NotificationResponse> markAsRead(Long id) {
        return notificationRepository.findById(id)
                .map(notification -> {
                    notification.setRead(true);
                    Notification updated = notificationRepository.save(notification);
                    log.info("Marked notification id={} as read", updated.getId());
                    return toResponse(updated);
                });
    }

    /**
     * Returns the count of unread notifications.
     */
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByReadFalse();
    }

    /**
     * Converts a Notification entity to a NotificationResponse DTO.
     */
    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.getEventId(),
                notification.getEventTitle(),
                notification.getEventVenue(),
                notification.getEventStartTime(),
                notification.getEventEndTime(),
                notification.getBusinessId(),
                notification.isRead(),
                notification.getCreatedAt() != null ? notification.getCreatedAt().toString() : null
        );
    }
}
