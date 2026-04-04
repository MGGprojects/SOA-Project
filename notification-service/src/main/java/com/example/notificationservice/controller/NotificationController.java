package com.example.notificationservice.controller;

import com.example.notificationservice.dto.NotificationListResponse;
import com.example.notificationservice.dto.NotificationResponse;
import com.example.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "APIs for retrieving and managing notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/notifications
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping
    @Operation(
        summary = "List all notifications",
        description = "Returns all notifications with pagination, ordered by creation time (newest first)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = NotificationListResponse.class)))
    })
    public ResponseEntity<NotificationListResponse> getNotifications(
        @Parameter(description = "Page number (1-based, default 1)")
        @RequestParam(defaultValue = "1") int page,
        @Parameter(description = "Items per page (default 20)")
        @RequestParam(defaultValue = "20") int limit
    ) {
        NotificationListResponse response = notificationService.getNotifications(page, limit);
        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/notifications/unread
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping("/unread")
    @Operation(
        summary = "List unread notifications",
        description = "Returns only unread notifications with pagination, ordered by creation time (newest first)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = NotificationListResponse.class)))
    })
    public ResponseEntity<NotificationListResponse> getUnreadNotifications(
        @Parameter(description = "Page number (1-based, default 1)")
        @RequestParam(defaultValue = "1") int page,
        @Parameter(description = "Items per page (default 20)")
        @RequestParam(defaultValue = "20") int limit
    ) {
        NotificationListResponse response = notificationService.getUnreadNotifications(page, limit);
        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/notifications/unread/count
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping("/unread/count")
    @Operation(
        summary = "Get unread notification count",
        description = "Returns the total number of unread notifications."
    )
    @ApiResponse(responseCode = "200", description = "Unread count retrieved successfully")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    // ──────────────────────────────────────────────────────────────────────
    // GET /api/notifications/{id}
    // ──────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(
        summary = "Get a notification by ID",
        description = "Returns a single notification by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification found",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = NotificationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<NotificationResponse> getNotificationById(
        @Parameter(description = "Notification ID", example = "1")
        @PathVariable Long id
    ) {
        return notificationService.getNotificationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────────────
    // PUT /api/notifications/{id}/read
    // ──────────────────────────────────────────────────────────────────────
    @PutMapping("/{id}/read")
    @Operation(
        summary = "Mark a notification as read",
        description = "Marks a specific notification as read."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification marked as read",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = NotificationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    public ResponseEntity<NotificationResponse> markAsRead(
        @Parameter(description = "Notification ID", example = "1")
        @PathVariable Long id
    ) {
        return notificationService.markAsRead(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
