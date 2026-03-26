package com.example.eventservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VenueAvailabilityResponse {
    private String venue;
    private List<TimeSlot> availableSlots;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TimeSlot {
        private String start;
        private String end;
    }
}
