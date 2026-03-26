package com.example.eventservice.dto;

import lombok.Data;

@Data
public class UpdateEventRequest {
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private String venue;
}
