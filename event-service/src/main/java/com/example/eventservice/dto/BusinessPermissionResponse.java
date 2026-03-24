package com.example.eventservice.dto;

import lombok.Data;

@Data
public class BusinessPermissionResponse {
    private Long businessId;
    private Long userId;
    private boolean canCreateEvents;
}