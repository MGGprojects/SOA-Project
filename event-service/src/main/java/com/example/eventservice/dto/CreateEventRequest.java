package com.example.eventservice.dto;

import lombok.Data;

@Data
public class CreateEventRequest {
    private Long userId;
    private Long businessId;
    private String title;
    private String venue;
    private String startTime;
    private String endTime;
}