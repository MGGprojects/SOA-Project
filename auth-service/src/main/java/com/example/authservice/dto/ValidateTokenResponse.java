package com.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ValidateTokenResponse {
    @Schema(description = "Is token valid", example = "true")
    private boolean valid;

    @Schema(description = "User UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String userId;

    @Schema(description = "User role", example = "customer")
    private String role;

    public ValidateTokenResponse(boolean valid, String userId, String role) {
        this.valid = valid;
        this.userId = userId;
        this.role = role;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}