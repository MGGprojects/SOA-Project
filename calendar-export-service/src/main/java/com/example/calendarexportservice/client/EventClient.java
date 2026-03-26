package com.example.calendarexportservice.client;

import com.example.calendarexportservice.dto.EventDetailResponse;
import org.springframework.stereotype.Component;

@Component
public class EventClient {

    // Dummy data constants (matching Event Service dummy data)
    private static final String DUMMY_EVENT_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String DUMMY_BUSINESS_ID = "123e4567-e89b-12d3-a456-426614174000";
    private static final String DUMMY_BUSINESS_NAME = "Tech Corp";

    /**
     * Fetches event details from the Event Service.
     * Currently returns dummy data matching the Event Service's dummy responses.
     * In a real scenario, this would call GET /api/events/{eventId} on the Event Service.
     */
    public EventDetailResponse getEventById(String eventId) {
        // Dummy implementation - in real scenario would call Event Service:
        // GET http://localhost:8082/api/events/{eventId}

        EventDetailResponse.BusinessInfo businessInfo = new EventDetailResponse.BusinessInfo(
            DUMMY_BUSINESS_ID,
            DUMMY_BUSINESS_NAME
        );

        return new EventDetailResponse(
            eventId != null ? eventId : DUMMY_EVENT_ID,
            "Tech Conference 2026",
            "Annual technology conference featuring the latest innovations in software development, AI, and cloud computing",
            "2026-06-15T09:00:00Z",
            "2026-06-15T17:00:00Z",
            "Convention Center",
            businessInfo
        );
    }
}
