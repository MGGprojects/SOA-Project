package com.example.calendarexportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDetailResponse {
    private String eventId;
    private String title;
    private String description;
    private String startTime;
    private String endTime;
    private String venue;
    private BusinessInfo business;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BusinessInfo {
        private String businessId;
        private String name;
    }
}
