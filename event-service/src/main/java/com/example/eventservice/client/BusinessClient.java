package com.example.eventservice.client;

import com.example.eventservice.dto.BusinessPermissionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "business-service", url = "http://localhost:8083")
public interface BusinessClient {

    @GetMapping("/api/businesses/{businessId}/users/{userId}/can-create-events")
    BusinessPermissionResponse canCreateEvents(
            @PathVariable("businessId") Long businessId,
            @PathVariable("userId") Long userId
    );
}