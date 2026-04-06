package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing user profiles and favorite events")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ──────────────────────────────────────────────────────────────────────
    // POST /api/users
    // ──────────────────────────────────────────────────────────────────────
    @Operation(
            summary = "Create a new user profile",
            description = "Creates a customer profile. Note: Authentication is handled by Auth Service separately."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User profile created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request – missing required fields or duplicate email")
    })
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            UserResponse response = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/users/{userId}
    // ──────────────────────────────────────────────────────────────────────
    @Operation(
            summary = "Get user profile",
            description = "Returns user profile by ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "The UUID of the user", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String userId,
            @Parameter(description = "JWT Token", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        return userService.getUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────────────
    // PUT /api/users/{userId}
    // ──────────────────────────────────────────────────────────────────────
    @Operation(
            summary = "Update user profile",
            description = "Updates user profile. Only provided (non-null) fields are updated."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "The UUID of the user", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String userId,
            @RequestBody UpdateUserRequest request,
            @Parameter(description = "JWT Token", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        return userService.updateUser(userId, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────────────
    // POST /api/users/{userId}/favorites/events/{eventId}
    // ──────────────────────────────────────────────────────────────────────
    @Operation(
            summary = "Add event to favorites",
            description = "Adds an event to user's favorites."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event added to favorites",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\":\"Event added to favorites\"}"))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/{userId}/favorites/events/{eventId}")
    public ResponseEntity<Map<String, String>> addFavoriteEvent(
            @Parameter(description = "The UUID of the user", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String userId,
            @Parameter(description = "The ID of the event to add to favorites", required = true, example = "evt-001")
            @PathVariable String eventId,
            @Parameter(description = "JWT Token", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        boolean success = userService.addFavoriteEvent(userId, eventId);
        if (!success) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Event added to favorites"));
    }

    // ──────────────────────────────────────────────────────────────────────
    // DELETE /api/users/{userId}/favorites/events/{eventId}
    // ──────────────────────────────────────────────────────────────────────
    @Operation(
            summary = "Remove event from favorites",
            description = "Removes an event from user's favorites."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event removed from favorites",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\":\"Event removed from favorites\"}"))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{userId}/favorites/events/{eventId}")
    public ResponseEntity<Map<String, String>> removeFavoriteEvent(
            @Parameter(description = "The UUID of the user", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String userId,
            @Parameter(description = "The ID of the event to remove from favorites", required = true, example = "evt-001")
            @PathVariable String eventId,
            @Parameter(description = "JWT Token", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        boolean success = userService.removeFavoriteEvent(userId, eventId);
        if (!success) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Event removed from favorites"));
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/users/{userId}/favorites/events
    // ──────────────────────────────────────────────────────────────────────
    @Operation(
            summary = "Get user's favorite events",
            description = "Returns user's favorite events with pagination."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorite events returned successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = FavoriteEventsResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}/favorites/events")
    public ResponseEntity<FavoriteEventsResponse> getFavoriteEvents(
            @Parameter(description = "The UUID of the user", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String userId,
            @Parameter(description = "Page number", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @Parameter(description = "JWT Token", example = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        return userService.getFavoriteEvents(userId, page, limit)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
