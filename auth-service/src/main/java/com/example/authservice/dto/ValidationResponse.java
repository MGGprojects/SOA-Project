package com.example.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationResponse {
    private Long userId;
    private boolean valid;
}