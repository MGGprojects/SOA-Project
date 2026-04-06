package com.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ValidateTokenResponse {
    @Schema(description = "Is token valid", example = "true")
    private boolean valid;

    @Schema(description = "User ID", example = "1")
    private Long userId;

    @Schema(description = "User role", example = "CUSTOMER")
    private String role;

    @Schema(description = "User email", example = "dummy@example.com")
    private String email;

    public ValidateTokenResponse(boolean valid, Long userId, String role, String email) {
        this.valid = valid;
        this.userId = userId;
        this.role = role;
        this.email = email;
    }

    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
