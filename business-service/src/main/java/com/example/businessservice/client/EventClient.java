package com.example.businessservice.client;

import com.example.businessservice.dto.EventListWrapper;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for communicating with the Event Service.
 * Used to retrieve events belonging to a specific business.
 */
@FeignClient(name = "event-service", url = "${event-service.url:http://localhost:8082}")
public interface EventClient {

    @GetMapping("/api/events")
    EventListWrapper getEvents(@RequestParam("businessId") String businessId,
                               @RequestParam(value = "page", defaultValue = "1") int page,
                               @RequestParam(value = "limit", defaultValue = "100") int limit);
}
