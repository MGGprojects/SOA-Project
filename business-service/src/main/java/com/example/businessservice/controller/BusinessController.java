package com.example.businessservice.controller;

import com.example.businessservice.dto.*;
import com.example.businessservice.security.AuthValidationService;
import com.example.businessservice.security.AuthenticatedUser;
import com.example.businessservice.service.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Business Profile", description = "Endpoints for managing business profiles")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;
    private final AuthValidationService authValidationService;

    // ──────────────────────────────────────────────────────────────────────
    // POST /api/businesses
    // ──────────────────────────────────────────────────────────────────────
    @PostMapping
    @Operation(
        summary = "Create a business profile",
        description = "Creates a business profile. The ownerId must be provided as a request header (X-User-Id)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Profile created successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request – missing required fields")
    })
    public ResponseEntity<BusinessProfileResponse> createBusiness(
            @RequestBody CreateBusinessRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        AuthenticatedUser authenticatedUser = authValidationService.requireBusinessUser(authHeader);
        BusinessProfileResponse response = businessService.createBusiness(request, authenticatedUser.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/businesses/{businessId}
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping("/{businessId}")
    @Operation(
        summary = "Get a business profile",
        description = "Returns public business information by ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found business profile"),
        @ApiResponse(responseCode = "404", description = "Business not found")
    })
    public ResponseEntity<BusinessProfileResponse> getBusiness(
            @Parameter(description = "Business ID") @PathVariable Long businessId) {

        return businessService.getBusiness(businessId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────────────
    // PUT /api/businesses/{businessId}
    // ──────────────────────────────────────────────────────────────────────
    @PutMapping("/{businessId}")
    @Operation(
        summary = "Update a business profile",
        description = "Updates business profile fields. Only provided (non-null) fields are updated."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "404", description = "Business not found")
    })
    public ResponseEntity<BusinessProfileResponse> updateBusiness(
            @Parameter(description = "Business ID") @PathVariable Long businessId,
            @RequestBody CreateBusinessRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            AuthenticatedUser authenticatedUser = authValidationService.requireBusinessUser(authHeader);

            return businessService.updateBusiness(businessId, request, authenticatedUser.getUserId())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/businesses
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping
    @Operation(
        summary = "List businesses",
        description = "Lists businesses, optionally filtered by city, with pagination."
    )
    @ApiResponse(responseCode = "200", description = "Businesses found")
    public ResponseEntity<BusinessListResponse> listBusinesses(
            @Parameter(description = "Filter by city") @RequestParam(required = false) String city,
            @Parameter(description = "Page number (1-based, default 1)") @RequestParam(required = false, defaultValue = "1") int page,
            @Parameter(description = "Items per page (default 20)") @RequestParam(required = false, defaultValue = "20") int limit) {

        BusinessListResponse response = businessService.listBusinesses(city, page, limit);
        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/businesses/{businessId}/events
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping("/{businessId}/events")
    @Operation(
        summary = "Get all events for a business",
        description = "Returns all events for a specific business by calling the Event Service."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Events found"),
        @ApiResponse(responseCode = "404", description = "Business not found")
    })
    public ResponseEntity<List<EventResponse>> getBusinessEvents(
            @Parameter(description = "Business ID") @PathVariable Long businessId) {

        List<EventResponse> events = businessService.getBusinessEvents(businessId);
        if (events == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(events);
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/businesses/{businessId}/users/{userId}/can-create-events
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping("/{businessId}/users/{userId}/can-create-events")
    @Operation(
        summary = "Check if a user can create events for a business",
        description = "Returns whether the given user is the owner of the business and thus allowed to create events."
    )
    @ApiResponse(responseCode = "200", description = "Permission check result")
    public ResponseEntity<BusinessPermissionResponse> canCreateEvent(
            @Parameter(description = "Business ID") @PathVariable Long businessId,
            @Parameter(description = "User ID") @PathVariable Long userId) {

        BusinessPermissionResponse response = businessService.canCreateEvents(businessId, userId);
        return ResponseEntity.ok(response);
    }
}
