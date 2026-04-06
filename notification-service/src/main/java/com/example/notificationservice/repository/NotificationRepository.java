package com.example.notificationservice.repository;

import com.example.notificationservice.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Find notifications by event ID */
    List<Notification> findByEventId(String eventId);

    /** Find notifications by business ID, ordered by creation time descending */
    Page<Notification> findByBusinessIdOrderByCreatedAtDesc(Long businessId, Pageable pageable);

    /** Find all notifications ordered by creation time descending */
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Find unread notifications ordered by creation time descending */
    Page<Notification> findByReadFalseOrderByCreatedAtDesc(Pageable pageable);

    /** Count unread notifications */
    long countByReadFalse();
}
