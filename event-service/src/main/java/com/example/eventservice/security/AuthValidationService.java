package com.example.eventservice.security;

import com.example.eventservice.client.AuthClient;
import com.example.eventservice.dto.AuthValidateTokenRequest;
import com.example.eventservice.dto.AuthValidateTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthValidationService {

    private final AuthClient authClient;

    public AuthenticatedUser requireBusinessUser(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        AuthValidateTokenResponse response;

        try {
            response = authClient.validateToken(new AuthValidateTokenRequest(token));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token validation failed");
        }

        if (response == null || !response.isValid() || response.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(response.getUserId(), response.getRole());
        if (!authenticatedUser.hasRole("BUSINESS")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Business role required");
        }

        return authenticatedUser;
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank() || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token is required");
        }

        return authorizationHeader.substring(7);
    }
}
