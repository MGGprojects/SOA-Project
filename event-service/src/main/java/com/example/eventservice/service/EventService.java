package com.example.eventservice.service;

import com.example.eventservice.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    // Dummy data - no database connection
    private static final String DUMMY_EVENT_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String DUMMY_BUSINESS_ID = "123e4567-e89b-12d3-a456-426614174000";
    private static final String DUMMY_BUSINESS_NAME = "Tech Corp";

    /**
     * Creates a new event with dummy data
     * Note: Authorization and venue conflict validation are simulated
     */
    public EventResponse createEvent(CreateEventRequest request) {
        // In a real implementation, this would:
        // 1. Validate user authorization (business owner)
        // 2. Check venue conflicts
        // 3. Save to database
        
        return new EventResponse(
            DUMMY_EVENT_ID,
            request.getTitle(),
            request.getDescription(),
            request.getStartTime(),
            request.getEndTime(),
            request.getVenue(),
            request.getBusinessId() != null ? request.getBusinessId() : DUMMY_BUSINESS_ID
        );
    }

    /**
     * Returns a list of events with dummy data
     * Note: Filters are accepted but not applied
     */
    public EventListResponse getEvents(String city, String date, String businessId, int page, int limit) {
        // In a real implementation, this would:
        // 1. Apply filters (city, date, businessId)
        // 2. Apply pagination
        // 3. Query database
        
        List<EventResponse> dummyEvents = Arrays.asList(
            new EventResponse(
                "550e8400-e29b-41d4-a716-446655440000",
                "Tech Conference 2026",
                "Annual technology conference",
                "2026-06-15T09:00:00Z",
                "2026-06-15T17:00:00Z",
                "Convention Center",
                "123e4567-e89b-12d3-a456-426614174000"
            ),
            new EventResponse(
                "550e8400-e29b-41d4-a716-446655440001",
                "Music Festival",
                "Summer music festival featuring local artists",
                "2026-07-20T14:00:00Z",
                "2026-07-20T23:00:00Z",
                "City Park",
                "123e4567-e89b-12d3-a456-426614174001"
            ),
            new EventResponse(
                "550e8400-e29b-41d4-a716-446655440002",
                "Food Fair",
                "International food and culture fair",
                "2026-08-10T11:00:00Z",
                "2026-08-10T20:00:00Z",
                "Downtown Square",
                "123e4567-e89b-12d3-a456-426614174002"
            )
        );

        return new EventListResponse(dummyEvents, dummyEvents.size(), page);
    }

    /**
     * Returns detailed event information with dummy data
     */
    public EventDetailResponse getEventById(String eventId) {
        // In a real implementation, this would:
        // 1. Query database by eventId
        // 2. Return 404 if not found
        
        EventDetailResponse.BusinessInfo businessInfo = new EventDetailResponse.BusinessInfo(
            DUMMY_BUSINESS_ID,
            DUMMY_BUSINESS_NAME
        );

        return new EventDetailResponse(
            eventId,
            "Tech Conference 2026",
            "Annual technology conference featuring the latest innovations in software development, AI, and cloud computing",
            "2026-06-15T09:00:00Z",
            "2026-06-15T17:00:00Z",
            "Convention Center",
            businessInfo
        );
    }

    /**
     * Updates an event with dummy data
     * Note: Authorization and venue conflict validation are simulated
     */
    public EventResponse updateEvent(String eventId, UpdateEventRequest request) {
        // In a real implementation, this would:
        // 1. Validate user authorization (event owner)
        // 2. Check venue conflicts if time/venue changes
        // 3. Update database
        
        return new EventResponse(
            eventId,
            request.getTitle(),
            request.getDescription(),
            request.getStartTime(),
            request.getEndTime(),
            request.getVenue(),
            DUMMY_BUSINESS_ID
        );
    }

    /**
     * Deletes/cancels an event with dummy data
     * Note: Authorization is simulated
     */
    public DeleteEventResponse deleteEvent(String eventId) {
        // In a real implementation, this would:
        // 1. Validate user authorization (event owner)
        // 2. Delete from database or mark as cancelled
        
        return new DeleteEventResponse("Event cancelled");
    }

    /**
     * Returns venue availability with dummy data
     * Note: Date parameter is accepted but not applied
     */
    public VenueAvailabilityResponse getVenueAvailability(String venue, String date) {
        // In a real implementation, this would:
        // 1. Query all events for the venue on the given date
        // 2. Calculate available time slots
        // 3. Return actual availability
        
        String baseDate = date != null ? date : "2026-06-15";
        
        List<VenueAvailabilityResponse.TimeSlot> dummySlots = Arrays.asList(
            new VenueAvailabilityResponse.TimeSlot(
                baseDate + "T09:00:00Z",
                baseDate + "T12:00:00Z"
            ),
            new VenueAvailabilityResponse.TimeSlot(
                baseDate + "T13:00:00Z",
                baseDate + "T17:00:00Z"
            ),
            new VenueAvailabilityResponse.TimeSlot(
                baseDate + "T18:00:00Z",
                baseDate + "T22:00:00Z"
            )
        );

        return new VenueAvailabilityResponse(venue, dummySlots);
    }
}
