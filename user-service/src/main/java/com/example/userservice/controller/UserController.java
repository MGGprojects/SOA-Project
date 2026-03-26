package com.example.userservice.controller;

import com.example.userservice.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing user profiles and favorite events")
public class UserController {

    private final String DUMMY_UUID = "123e4567-e89b-12d3-a456-426614174000";
    private final String DUMMY_EMAIL = "john.doe@example.com";
    private final String DUMMY_FIRST_NAME = "John";
    private final String DUMMY_LAST_NAME = "Doe";
    private final String DUMMY_CREATED_AT = "2025-03-26T14:30:00Z";

    @Operation(
            summary = "Create a new user profile",
            description = "Creates a customer profile. Note: Authentication is handled by Auth Service separately."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)))
    })
    @PostMapping
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        return new UserResponse(
                DUMMY_UUID,
                request.getEmail() != null ? request.getEmail() : DUMMY_EMAIL,
                request.getFirstName() != null ? request.getFirstName() : DUMMY_FIRST_NAME,
                request.getLastName() != null ? request.getLastName() : DUMMY_LAST_NAME,
                Instant.now().toString()
        );
    }

    @Operation(
            summary = "Get user profile",
            description = "Returns user profile. Only the user themselves can access (auth required)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)))
    })
    @GetMapping("/{userId}")
    public UserResponse getUser(
            @Parameter(description = "The UUID of the user", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String userId,
            @Parameter(description = "JWT Token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return new UserResponse(userId, DUMMY_EMAIL, DUMMY_FIRST_NAME, DUMMY_LAST_NAME, DUMMY_CREATED_AT);
    }

    @Operation(
            summary = "Update user profile",
            description = "Updates user profile. Only the user themselves can update (auth required)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)))
    })
    @PutMapping("/{userId}")
    public UserResponse updateUser(
            @Parameter(description = "The UUID of the user", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String userId,
            @RequestBody UpdateUserRequest request,
            @Parameter(description = "JWT Token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return new UserResponse(
                userId,
                DUMMY_EMAIL,
                request.getFirstName() != null ? request.getFirstName() : DUMMY_FIRST_NAME,
                request.getLastName() != null ? request.getLastName() : DUMMY_LAST_NAME,
                DUMMY_CREATED_AT
        );
    }

    @Operation(
            summary = "Add event to favorites",
            description = "Adds an event to user's favorites. Only the user themselves can add (auth required)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event added to favorites",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\":\"Event added to favorites\"}")))
    })
    @PostMapping("/{userId}/favorites/events/{eventId}")
    public Map<String, String> addFavoriteEvent(
            @Parameter(description = "The UUID of the user", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String userId,
            @Parameter(description = "The ID of the event to add to favorites", required = true, example = "evt-001")
            @PathVariable String eventId,
            @Parameter(description = "JWT Token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return Map.of("message", "Event added to favorites");
    }

    @Operation(
            summary = "Remove event from favorites",
            description = "Removes an event from user's favorites. Only the user themselves can remove (auth required)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event removed from favorites",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\":\"Event removed from favorites\"}")))
    })
    @DeleteMapping("/{userId}/favorites/events/{eventId}")
    public Map<String, String> removeFavoriteEvent(
            @Parameter(description = "The UUID of the user", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String userId,
            @Parameter(description = "The ID of the event to remove from favorites", required = true, example = "evt-001")
            @PathVariable String eventId,
            @Parameter(description = "JWT Token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return Map.of("message", "Event removed from favorites");
    }

    @Operation(
            summary = "Get user's favorite events",
            description = "Returns user's favorite events with pagination. Only the user themselves can access (auth required)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorite events returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FavoriteEventsResponse.class)))
    })
    @GetMapping("/{userId}/favorites/events")
    public FavoriteEventsResponse getFavoriteEvents(
            @Parameter(description = "The UUID of the user", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String userId,
            @Parameter(description = "Page number", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @Parameter(description = "JWT Token", required = true, example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        List<String> dummyEvents = List.of("evt-001", "evt-002", "evt-003");
        return new FavoriteEventsResponse(dummyEvents, dummyEvents.size());
    }
}
