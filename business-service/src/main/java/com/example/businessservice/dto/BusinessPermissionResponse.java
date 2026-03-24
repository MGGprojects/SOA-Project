package com.example.businessservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BusinessPermissionResponse {
    private Long businessId;
    private Long userId;
    private boolean canCreateEvents;
}