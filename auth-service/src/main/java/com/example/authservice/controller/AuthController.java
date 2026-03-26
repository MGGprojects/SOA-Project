package com.example.authservice.controller;

import com.example.authservice.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and token issuance")
public class AuthController {

    private final String DUMMY_UUID = "123e4567-e89b-12d3-a456-426614174000";
    private final String DUMMY_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjNlNDU2Ny1lODliLTEyZDMtYTQ1Ni00MjY2MTQxNzQwMDAiLCJuYW1lIjoiSm9obiBEb2UiLCJpYXQiOjE1MTYyMzkwMjJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    @Operation(
            summary = "Register a new user",
            description = "Registers a new user (business or customer). Returns a dummy UUID for now."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class)))
    })
    @PostMapping("/register")
    public RegisterResponse register(@RequestBody RegisterRequest request) {
        return new RegisterResponse(DUMMY_UUID, "User created");
    }

    @Operation(
            summary = "Login user",
            description = "Authenticates user and returns a dummy JWT token."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful login",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class)))
    })
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return new LoginResponse(DUMMY_TOKEN, DUMMY_UUID, "customer");
    }

    @Operation(
            summary = "Validate token",
            description = "Internal use. Validates token for other services. Returns true and dummy user info."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token validated",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ValidateTokenResponse.class)))
    })
    @PostMapping("/validate")
    public ValidateTokenResponse validate(@RequestBody ValidateTokenRequest request) {
        return new ValidateTokenResponse(true, DUMMY_UUID, "customer");
    }

    @Operation(
            summary = "Logout user",
            description = "Invalidates the token (expects JWT in Authorization header). Returns a dummy success message."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"message\":\"Logged out\"}")))
    })
    @PostMapping("/logout")
    public Map<String, String> logout(
            @Parameter(description = "JWT Token", required = true, example = "Bearer " + DUMMY_TOKEN)
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        return Map.of("message", "Logged out");
    }
}