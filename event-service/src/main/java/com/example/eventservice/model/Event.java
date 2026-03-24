package com.example.eventservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    private Long id;
    private Long userId;
    private Long businessId;
    private String title;
    private String venue;
    private String startTime;
    private String endTime;
}