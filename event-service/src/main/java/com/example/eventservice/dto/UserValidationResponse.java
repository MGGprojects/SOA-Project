package com.example.eventservice.dto;

import lombok.Data;

@Data
public class UserValidationResponse {
    private Long userId;
    private boolean valid;
}