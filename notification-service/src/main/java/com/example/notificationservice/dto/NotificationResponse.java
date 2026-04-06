package com.example.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private String type;
    private String message;
    private String eventId;
    private String eventTitle;
    private String eventVenue;
    private String eventStartTime;
    private String eventEndTime;
    private Long businessId;
    private boolean read;
    private String createdAt;
}
