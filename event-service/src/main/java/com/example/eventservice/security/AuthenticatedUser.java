package com.example.eventservice.security;

public class AuthenticatedUser {

    private final Long userId;
    private final String role;

    public AuthenticatedUser(Long userId, String role) {
        this.userId = userId;
        this.role = role;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean hasRole(String expectedRole) {
        return role != null && role.equalsIgnoreCase(expectedRole);
    }
}
