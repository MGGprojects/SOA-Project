package com.example.eventservice.controller;

import com.example.eventservice.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Management", description = "APIs for managing events")
public class EventController {

    // Dummy data constants
    private static final String DUMMY_EVENT_ID = "550e8400-e29b-41d4-a716-446655440000";
    private static final String DUMMY_BUSINESS_ID = "123e4567-e89b-12d3-a456-426614174000";
    private static final Map<String, String> DUMMY_BUSINESS_NAMES = Map.of(
            "123e4567-e89b-12d3-a456-426614174000", "Tech Corp",
            "123e4567-e89b-12d3-a456-426614174001", "City Sounds",
            "123e4567-e89b-12d3-a456-426614174002", "Global Bites"
    );

    @PostMapping
    @Operation(
        summary = "Create a new event",
        description = "Creates a new event for a business. **Authorization Required: Business owner**\n\n" +
                "**Note:** This endpoint currently uses dummy data and does not connect to a database.\n\n" +
                "**Dummy Values:**\n" +
                "- eventId: \"550e8400-e29b-41d4-a716-446655440000\"\n" +
                "- businessId: \"123e4567-e89b-12d3-a456-426614174000\" (from request)\n" +
                "- Authorization is simulated (not enforced)\n" +
                "- Venue conflict validation is simulated (always passes)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Event created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EventResponse.class),
                examples = @ExampleObject(
                    value = "{\"eventId\":\"550e8400-e29b-41d4-a716-446655440000\",\"title\":\"Tech Conference 2026\",\"description\":\"Annual technology conference\",\"startTime\":\"2026-06-15T09:00:00Z\",\"endTime\":\"2026-06-15T17:00:00Z\",\"venue\":\"Convention Center\",\"businessId\":\"123e4567-e89b-12d3-a456-426614174000\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not own the business")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<EventResponse> createEvent(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Event creation details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = CreateEventRequest.class),
                examples = @ExampleObject(
                    value = "{\"title\":\"Tech Conference 2026\",\"description\":\"Annual technology conference\",\"startTime\":\"2026-06-15T09:00:00Z\",\"endTime\":\"2026-06-15T17:00:00Z\",\"venue\":\"Convention Center\",\"businessId\":\"123e4567-e89b-12d3-a456-426614174000\"}"
                )
            )
        )
        @RequestBody CreateEventRequest request
    ) {
        // Dummy implementation - in real scenario would:
        // 1. Validate user authorization (business owner)
        // 2. Check venue conflicts
        // 3. Save to database
        
        EventResponse response = new EventResponse(
            DUMMY_EVENT_ID,
            request.getTitle(),
            request.getDescription(),
            request.getStartTime(),
            request.getEndTime(),
            request.getVenue(),
            request.getBusinessId() != null ? request.getBusinessId() : DUMMY_BUSINESS_ID
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
        summary = "Search and list events",
        description = "Public endpoint to search events with optional filters.\n\n" +
                "**Note:** This endpoint currently uses dummy data and does not connect to a database.\n\n" +
                "**Dummy Values:**\n" +
                "- Returns 3 sample events\n" +
                "- Filters (city, date, businessId) are accepted but not applied\n" +
                "- Pagination parameters are accepted but return fixed results\n" +
                "- Sample events include: Tech Conference, Music Festival, Food Fair"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Events retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EventListResponse.class),
                examples = @ExampleObject(
                    value = "{\"events\":[{\"eventId\":\"550e8400-e29b-41d4-a716-446655440000\",\"title\":\"Tech Conference 2026\",\"description\":\"Annual technology conference\",\"startTime\":\"2026-06-15T09:00:00Z\",\"endTime\":\"2026-06-15T17:00:00Z\",\"venue\":\"Convention Center\",\"businessId\":\"123e4567-e89b-12d3-a456-426614174000\"}],\"total\":3,\"page\":1}"
                )
            )
        )
    })
    public ResponseEntity<EventListResponse> getEvents(
        @Parameter(description = "Filter by city") @RequestParam(required = false) String city,
        @Parameter(description = "Filter by date (YYYY-MM-DD)") @RequestParam(required = false) String date,
        @Parameter(description = "Filter by business ID") @RequestParam(required = false) String businessId,
        @Parameter(description = "Page number (default: 1)") @RequestParam(defaultValue = "1") int page,
        @Parameter(description = "Items per page (default: 20)") @RequestParam(defaultValue = "20") int limit
    ) {
        // Dummy implementation - in real scenario would:
        // 1. Apply filters (city, date, businessId)
        // 2. Apply pagination
        // 3. Query database
        
        List<EventResponse> dummyEvents = getDummyEvents();

        EventListResponse response = new EventListResponse(dummyEvents, dummyEvents.size(), page);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}")
    @Operation(
        summary = "Get event details",
        description = "Returns detailed information about a specific event.\n\n" +
                "**Note:** This endpoint currently uses dummy data and does not connect to a database.\n\n" +
                "**Dummy Values:**\n" +
                "- eventId: \"550e8400-e29b-41d4-a716-446655440000\"\n" +
                "- Returns a sample Tech Conference event\n" +
                "- Business info: { businessId: \"123e4567-e89b-12d3-a456-426614174000\", name: \"Tech Corp\" }"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Event details retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EventDetailResponse.class),
                examples = @ExampleObject(
                    value = "{\"eventId\":\"550e8400-e29b-41d4-a716-446655440000\",\"title\":\"Tech Conference 2026\",\"description\":\"Annual technology conference\",\"startTime\":\"2026-06-15T09:00:00Z\",\"endTime\":\"2026-06-15T17:00:00Z\",\"venue\":\"Convention Center\",\"business\":{\"businessId\":\"123e4567-e89b-12d3-a456-426614174000\",\"name\":\"Tech Corp\"}}"
                )
            )
        ),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventDetailResponse> getEventById(
        @Parameter(description = "Event ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String eventId
    ) {
        // Dummy implementation - in real scenario would:
        // 1. Query database by eventId
        // 2. Return 404 if not found
        
        return getDummyEvents().stream()
                .filter(event -> event.getEventId().equals(eventId))
                .findFirst()
                .map(this::toEventDetailResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{eventId}")
    @Operation(
        summary = "Update an event",
        description = "Updates an existing event. **Authorization Required: Event owner**\n\n" +
                "**Note:** This endpoint currently uses dummy data and does not connect to a database.\n\n" +
                "**Dummy Values:**\n" +
                "- eventId: \"550e8400-e29b-41d4-a716-446655440000\"\n" +
                "- Authorization is simulated (not enforced)\n" +
                "- Venue conflict validation is simulated (always passes)\n" +
                "- Returns updated event with provided values"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Event updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = EventResponse.class),
                examples = @ExampleObject(
                    value = "{\"eventId\":\"550e8400-e29b-41d4-a716-446655440000\",\"title\":\"Updated Tech Conference 2026\",\"description\":\"Updated description\",\"startTime\":\"2026-06-15T10:00:00Z\",\"endTime\":\"2026-06-15T18:00:00Z\",\"venue\":\"Grand Convention Center\",\"businessId\":\"123e4567-e89b-12d3-a456-426614174000\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not own the event"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<EventResponse> updateEvent(
        @Parameter(description = "Event ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String eventId,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Updated event details",
            required = true,
            content = @Content(
                schema = @Schema(implementation = UpdateEventRequest.class),
                examples = @ExampleObject(
                    value = "{\"title\":\"Updated Tech Conference 2026\",\"description\":\"Updated description\",\"startTime\":\"2026-06-15T10:00:00Z\",\"endTime\":\"2026-06-15T18:00:00Z\",\"venue\":\"Grand Convention Center\"}"
                )
            )
        )
        @RequestBody UpdateEventRequest request
    ) {
        // Dummy implementation - in real scenario would:
        // 1. Validate user authorization (event owner)
        // 2. Check venue conflicts if time/venue changes
        // 3. Update database
        
        EventResponse response = new EventResponse(
            eventId,
            request.getTitle(),
            request.getDescription(),
            request.getStartTime(),
            request.getEndTime(),
            request.getVenue(),
            DUMMY_BUSINESS_ID
        );
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{eventId}")
    @Operation(
        summary = "Cancel/delete an event",
        description = "Cancels or removes an event. **Authorization Required: Event owner**\n\n" +
                "**Note:** This endpoint currently uses dummy data and does not connect to a database.\n\n" +
                "**Dummy Values:**\n" +
                "- eventId: \"550e8400-e29b-41d4-a716-446655440000\"\n" +
                "- Authorization is simulated (not enforced)\n" +
                "- Always returns success message"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Event cancelled successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DeleteEventResponse.class),
                examples = @ExampleObject(
                    value = "{\"message\":\"Event cancelled\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User does not own the event"),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DeleteEventResponse> deleteEvent(
        @Parameter(description = "Event ID", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String eventId
    ) {
        // Dummy implementation - in real scenario would:
        // 1. Validate user authorization (event owner)
        // 2. Delete from database or mark as cancelled
        
        DeleteEventResponse response = new DeleteEventResponse("Event cancelled");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/venues/{venue}/availability")
    @Operation(
        summary = "Get venue availability",
        description = "Returns available time slots for a venue on a given date.\n\n" +
                "**Note:** This endpoint currently uses dummy data and does not connect to a database.\n\n" +
                "**Dummy Values:**\n" +
                "- venue: Any venue name (e.g., \"Convention Center\")\n" +
                "- Returns 3 sample available time slots\n" +
                "- Slots: 09:00-12:00, 13:00-17:00, 18:00-22:00\n" +
                "- Date parameter is accepted but not applied to results"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Venue availability retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = VenueAvailabilityResponse.class),
                examples = @ExampleObject(
                    value = "{\"venue\":\"Convention Center\",\"availableSlots\":[{\"start\":\"2026-06-15T09:00:00Z\",\"end\":\"2026-06-15T12:00:00Z\"},{\"start\":\"2026-06-15T13:00:00Z\",\"end\":\"2026-06-15T17:00:00Z\"},{\"start\":\"2026-06-15T18:00:00Z\",\"end\":\"2026-06-15T22:00:00Z\"}]}"
                )
            )
        )
    })
    public ResponseEntity<VenueAvailabilityResponse> getVenueAvailability(
        @Parameter(description = "Venue name", example = "Convention Center")
        @PathVariable String venue,
        @Parameter(description = "Date to check availability (YYYY-MM-DD)", example = "2026-06-15")
        @RequestParam(required = false) String date
    ) {
        // Dummy implementation - in real scenario would:
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

        VenueAvailabilityResponse response = new VenueAvailabilityResponse(venue, dummySlots);
        return ResponseEntity.ok(response);
    }

    private List<EventResponse> getDummyEvents() {
        return Arrays.asList(
                new EventResponse(
                        "550e8400-e29b-41d4-a716-446655440000",
                        "Tech Conference 2026",
                        "Annual technology conference featuring the latest innovations in software development, AI, and cloud computing",
                        "2026-06-15T09:00:00Z",
                        "2026-06-15T17:00:00Z",
                        "Convention Center",
                        DUMMY_BUSINESS_ID
                ),
                new EventResponse(
                        "550e8400-e29b-41d4-a716-446655440001",
                        "Music Festival",
                        "Summer music festival featuring local artists, food trucks, and outdoor performances.",
                        "2026-07-20T14:00:00Z",
                        "2026-07-20T23:00:00Z",
                        "City Park",
                        "123e4567-e89b-12d3-a456-426614174001"
                ),
                new EventResponse(
                        "550e8400-e29b-41d4-a716-446655440002",
                        "Food Fair",
                        "International food and culture fair with tastings, workshops, and family activities.",
                        "2026-08-10T11:00:00Z",
                        "2026-08-10T20:00:00Z",
                        "Downtown Square",
                        "123e4567-e89b-12d3-a456-426614174002"
                )
        );
    }

    private EventDetailResponse toEventDetailResponse(EventResponse event) {
        EventDetailResponse.BusinessInfo businessInfo = new EventDetailResponse.BusinessInfo(
                event.getBusinessId(),
                DUMMY_BUSINESS_NAMES.getOrDefault(event.getBusinessId(), "Local Organizer")
        );

        return new EventDetailResponse(
                event.getEventId(),
                event.getTitle(),
                event.getDescription(),
                event.getStartTime(),
                event.getEndTime(),
                event.getVenue(),
                businessInfo
        );
    }
}
