package com.example.userservice.security;

public class AuthenticatedUser {

    private final Long authUserId;
    private final String role;
    private final String email;

    public AuthenticatedUser(Long authUserId, String role, String email) {
        this.authUserId = authUserId;
        this.role = role;
        this.email = email;
    }

    public Long getAuthUserId() {
        return authUserId;
    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }
}
