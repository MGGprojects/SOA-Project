package com.example.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing user profile information")
public class UserResponse {

    @Schema(description = "User UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private String userId;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's first name", example = "John")
    private String firstName;

    @Schema(description = "User's last name", example = "Doe")
    private String lastName;

    @Schema(description = "Account creation timestamp", example = "2025-03-26T14:30:00Z")
    private String createdAt;

    public UserResponse() {}

    public UserResponse(String userId, String email, String firstName, String lastName, String createdAt) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
