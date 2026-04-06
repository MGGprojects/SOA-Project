package com.example.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents a notification generated when an event is created.
 * Persisted to PostgreSQL for later retrieval via REST API.
 */
@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The type of notification (e.g., "EVENT_CREATED") */
    @Column(nullable = false)
    private String type;

    /** Human-readable notification message */
    @Column(nullable = false, length = 2000)
    private String message;

    /** The event ID that triggered this notification */
    @Column(nullable = false)
    private String eventId;

    /** The title of the event */
    private String eventTitle;

    /** The venue of the event */
    private String eventVenue;

    /** The start time of the event */
    private String eventStartTime;

    /** The end time of the event */
    private String eventEndTime;

    /** The business ID that owns the event */
    private Long businessId;

    /** Whether this notification has been read */
    @Column(nullable = false)
    private boolean read = false;

    /** Timestamp when the notification was created */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
