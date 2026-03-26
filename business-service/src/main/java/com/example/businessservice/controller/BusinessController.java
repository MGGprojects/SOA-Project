package com.example.businessservice.controller;

import com.example.businessservice.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses")
@Tag(name = "Business Profile", description = "Endpoints for managing business profiles")
public class BusinessController {

    @GetMapping("/{businessId}/users/{userId}/can-create-events")
    @Operation(summary = "Check if a user can create events for a business")
    public BusinessPermissionResponse canCreateEvent(
            @PathVariable Long businessId,
            @PathVariable Long userId) {

        // Dummy
        boolean allowed = (businessId == 1 && userId == 1);

        return new BusinessPermissionResponse(businessId, userId, allowed);
    }

    @PostMapping
    @Operation(summary = "Create a business profile", description = "Creates a business profile. User must be authenticated with business role. Note: This endpoint currently returns dummy data.")
    @ApiResponse(responseCode = "200", description = "Profile created successfully")
    public BusinessProfileResponse createBusiness(@RequestBody CreateBusinessRequest request) {
        return new BusinessProfileResponse(
                UUID.randomUUID().toString(),
                request.getName() != null ? request.getName() : "Dummy Business",
                request.getAddress() != null ? request.getAddress() : "123 Main St",
                request.getCity() != null ? request.getCity() : "Dummy City",
                request.getContactEmail() != null ? request.getContactEmail() : "contact@dummy.com",
                request.getPhone() != null ? request.getPhone() : "555-1234",
                request.getDescription() != null ? request.getDescription() : "A dummy business description",
                UUID.randomUUID().toString()
        );
    }

    @GetMapping("/{businessId}")
    @Operation(summary = "Get a business profile", description = "Returns public business information. Note: This endpoint currently returns dummy data.")
    @ApiResponse(responseCode = "200", description = "Found business profile")
    public BusinessProfileResponse getBusiness(@PathVariable String businessId) {
        return new BusinessProfileResponse(
                businessId,
                "Dummy Business",
                "123 Main St",
                "Dummy City",
                "contact@dummy.com",
                "555-1234",
                "A dummy business description",
                UUID.randomUUID().toString()
        );
    }

    @PutMapping("/{businessId}")
    @Operation(summary = "Update a business profile", description = "Updates business profile. Only the owning user can update. Note: This endpoint currently returns dummy data.")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    public BusinessProfileResponse updateBusiness(
            @PathVariable String businessId,
            @RequestBody CreateBusinessRequest request) {
        return new BusinessProfileResponse(
                businessId,
                request.getName() != null ? request.getName() : "Updated Dummy Business",
                request.getAddress() != null ? request.getAddress() : "123 Main St Updated",
                request.getCity() != null ? request.getCity() : "Dummy City",
                request.getContactEmail() != null ? request.getContactEmail() : "updated@dummy.com",
                request.getPhone() != null ? request.getPhone() : "555-1234",
                request.getDescription() != null ? request.getDescription() : "Updated dummy description",
                UUID.randomUUID().toString()
        );
    }

    @GetMapping("/{businessId}/events")
    @Operation(summary = "Get all events for a business", description = "Returns all events for a specific business. Note: This endpoint currently returns dummy data.")
    @ApiResponse(responseCode = "200", description = "Events found")
    public List<EventResponse> getBusinessEvents(@PathVariable String businessId) {
        return Arrays.asList(
                new EventResponse(UUID.randomUUID().toString(), "Dummy Event 1", "2023-10-01T10:00:00Z", "2023-10-01T12:00:00Z"),
                new EventResponse(UUID.randomUUID().toString(), "Dummy Event 2", "2023-11-15T18:00:00Z", "2023-11-15T22:00:00Z")
        );
    }

    @GetMapping
    @Operation(summary = "List businesses", description = "Lists businesses, optionally filtered by city. Note: This endpoint currently returns dummy data.")
    @ApiResponse(responseCode = "200", description = "Businesses found")
    public BusinessListResponse listBusinesses(
            @RequestParam(required = false) String city,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "20") int limit) {
        
        List<BusinessProfileResponse> businesses = Arrays.asList(
                new BusinessProfileResponse(
                        UUID.randomUUID().toString(),
                        "Dummy Business 1",
                        "123 Main St",
                        city != null ? city : "Dummy City 1",
                        "contact1@dummy.com",
                        "555-1234",
                        "Description 1",
                        UUID.randomUUID().toString()
                ),
                new BusinessProfileResponse(
                        UUID.randomUUID().toString(),
                        "Dummy Business 2",
                        "456 Oak St",
                        city != null ? city : "Dummy City 2",
                        "contact2@dummy.com",
                        "555-5678",
                        "Description 2",
                        UUID.randomUUID().toString()
                )
        );

        return new BusinessListResponse(businesses, 2, page);
    }
}
