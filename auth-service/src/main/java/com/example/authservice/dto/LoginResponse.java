package com.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class LoginResponse {
    @Schema(description = "JWT Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
    private String token;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "User role", example = "CUSTOMER")
    private String role;

    public LoginResponse(String token, Long userId, String role) {
        this.token = token;
        this.userId = userId;
        this.role = role;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
