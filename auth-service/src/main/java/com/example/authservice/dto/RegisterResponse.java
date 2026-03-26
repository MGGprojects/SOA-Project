package com.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class RegisterResponse {
    @Schema(description = "Generated UUID for the new user", example = "123e4567-e89b-12d3-a456-426614174000")
    private String userId;

    @Schema(description = "Status message", example = "User created")
    private String message;

    public RegisterResponse(String userId, String message) {
        this.userId = userId;
        this.message = message;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}