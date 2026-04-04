package com.example.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
