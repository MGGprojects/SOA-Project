package com.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class RegisterRequest {
    @Schema(description = "User's email", example = "dummy@example.com")
    private String email;

    @Schema(description = "User's password", example = "dummyPassword123")
    private String password;

    @Schema(description = "User's role", example = "customer")
    private String role;

    public RegisterRequest() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}