package com.example.businessservice.controller;

import com.example.businessservice.dto.BusinessPermissionResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    @GetMapping("/{businessId}/users/{userId}/can-create-events")
    public BusinessPermissionResponse canCreateEvent(
            @PathVariable Long businessId,
            @PathVariable Long userId) {

        // Dummy
        boolean allowed = (businessId == 1 && userId == 1);

        return new BusinessPermissionResponse(businessId, userId, allowed);
    }
}