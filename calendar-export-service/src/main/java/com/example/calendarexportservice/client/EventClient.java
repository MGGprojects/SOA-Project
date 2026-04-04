package com.example.calendarexportservice.client;

import com.example.calendarexportservice.dto.EventDetailResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client that calls the Event Service to fetch event details.
 * Replaces the previous dummy/stub implementation with a real HTTP call.
 */
@FeignClient(name = "event-service", url = "${event-service.url:http://localhost:8082}")
public interface EventClient {

    /**
     * Fetches event details from the Event Service.
     * Calls GET /api/events/{eventId} on the Event Service.
     */
    @GetMapping("/api/events/{eventId}")
    EventDetailResponse getEventById(@PathVariable("eventId") String eventId);
}
