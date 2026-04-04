package com.example.eventservice.controller;

import com.example.eventservice.dto.*;
import com.example.eventservice.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Event Management", description = "APIs for managing events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    // ──────────────────────────────────────────────────────────────────────
    // POST /api/events
    // ──────────────────────────────────────────────────────────────────────
    @PostMapping
    @Operation(
        summary = "Create a new event",
        description = "Creates a new event for a business. Validates business permission via Business Service, "
                + "checks venue conflicts, persists to database, and publishes an event-created message to RabbitMQ."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Event created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad request – invalid input"),
        @ApiResponse(responseCode = "403", description = "Forbidden – user does not own the business"),
        @ApiResponse(responseCode = "409", description = "Conflict – venue scheduling conflict")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> createEvent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Event creation details",
                required = true,
                content = @Content(
                    schema = @Schema(implementation = CreateEventRequest.class),
                    examples = @ExampleObject(
                        value = "{\"title\":\"Tech Conference 2026\",\"description\":\"Annual technology conference\",\"startTime\":\"2026-06-15T09:00:00Z\",\"endTime\":\"2026-06-15T17:00:00Z\",\"venue\":\"Convention Center\",\"city\":\"Amsterdam\",\"businessId\":\"1\"}"
                    )
                )
            )
            @RequestBody CreateEventRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        try {
            EventResponse response = eventService.createEvent(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("not allowed")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/events
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping
    @Operation(
        summary = "Search and list events",
        description = "Public endpoint to search events with optional filters (city, date, businessId) and pagination."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Events retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventListResponse.class)))
    })
    public ResponseEntity<EventListResponse> getEvents(
        @Parameter(description = "Filter by city") @RequestParam(required = false) String city,
        @Parameter(description = "Filter by date (YYYY-MM-DD)") @RequestParam(required = false) String date,
        @Parameter(description = "Filter by business ID") @RequestParam(required = false) String businessId,
        @Parameter(description = "Page number (default: 1)") @RequestParam(defaultValue = "1") int page,
        @Parameter(description = "Items per page (default: 20)") @RequestParam(defaultValue = "20") int limit
    ) {
        EventListResponse response = eventService.getEvents(city, date, businessId, page, limit);
        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/events/{eventId}
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping("/{eventId}")
    @Operation(
        summary = "Get event details",
        description = "Returns detailed information about a specific event, including business info fetched from Business Service."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event details retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventDetailResponse.class))),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventDetailResponse> getEventById(
        @Parameter(description = "Event ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String eventId
    ) {
        return eventService.getEventById(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────────────
    // PUT /api/events/{eventId}
    // ──────────────────────────────────────────────────────────────────────
    @PutMapping("/{eventId}")
    @Operation(
        summary = "Update an event",
        description = "Updates an existing event. Checks venue conflicts if time/venue changes."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EventResponse.class))),
        @ApiResponse(responseCode = "404", description = "Event not found"),
        @ApiResponse(responseCode = "409", description = "Conflict – venue scheduling conflict")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> updateEvent(
        @Parameter(description = "Event ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
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
        @RequestBody UpdateEventRequest request,
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        try {
            return eventService.updateEvent(eventId, request, userId)
                    .map(response -> ResponseEntity.ok((Object) response))
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // DELETE /api/events/{eventId}
    // ──────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{eventId}")
    @Operation(
        summary = "Cancel/delete an event",
        description = "Cancels or removes an event from the database."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Event cancelled successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DeleteEventResponse.class))),
        @ApiResponse(responseCode = "404", description = "Event not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<DeleteEventResponse> deleteEvent(
        @Parameter(description = "Event ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable String eventId,
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        return eventService.deleteEvent(eventId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/events/venues/{venue}/availability
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping("/venues/{venue}/availability")
    @Operation(
        summary = "Get venue availability",
        description = "Returns available time slots for a venue on a given date. "
                + "Calculates free slots by subtracting booked events from the venue's operating hours (08:00–23:00 UTC)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Venue availability retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = VenueAvailabilityResponse.class)))
    })
    public ResponseEntity<VenueAvailabilityResponse> getVenueAvailability(
        @Parameter(description = "Venue name", example = "Convention Center")
        @PathVariable String venue,
        @Parameter(description = "Date to check availability (YYYY-MM-DD)", example = "2026-06-15")
        @RequestParam(required = false) String date
    ) {
        VenueAvailabilityResponse response = eventService.getVenueAvailability(venue, date);
        return ResponseEntity.ok(response);
    }
}
