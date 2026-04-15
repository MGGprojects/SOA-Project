package com.example.eventservice.dto;

import lombok.Data;

@Data
public class CreateEventRequest {
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private String venue;
    private String city;
    private String businessId;
    private Boolean forceCreation;
}
