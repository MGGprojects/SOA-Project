package com.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ValidateTokenRequest {
    @Schema(description = "JWT Token to validate", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
    private String token;

    public ValidateTokenRequest() {}

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}