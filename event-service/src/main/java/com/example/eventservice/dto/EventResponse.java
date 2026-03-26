package com.example.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private String eventId;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private String venue;
    private String businessId;
}
