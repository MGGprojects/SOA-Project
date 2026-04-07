package com.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class RegisterResponse {
    @Schema(description = "Generated ID for the new user", example = "1")
    private Long userId;

    @Schema(description = "Registered email", example = "dummy@example.com")
    private String email;

    @Schema(description = "User role", example = "CUSTOMER")
    private String role;

    @Schema(description = "Status message", example = "User created")
    private String message;

    public RegisterResponse(Long userId, String email, String role, String message) {
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
