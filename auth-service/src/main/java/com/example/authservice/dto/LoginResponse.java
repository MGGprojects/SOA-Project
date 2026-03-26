package com.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class LoginResponse {
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
    private String token;

    @Schema(description = "User UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String userId;

    @Schema(description = "User role", example = "customer")
    private String role;

    public LoginResponse(String token, String userId, String role) {
        this.token = token;
        this.userId = userId;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}