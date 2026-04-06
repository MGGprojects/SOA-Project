package com.example.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO matching the EventCreatedMessage published by the Event Service.
 * Fields must match the event-service's EventCreatedMessage exactly.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventCreatedMessage {
    private String eventId;
    private String title;
    private String venue;
    private String startTime;
    private String endTime;
    private Long businessId;
}
