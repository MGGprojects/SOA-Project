package com.example.authservice.service;

import com.example.authservice.dto.LoginRequest;
import com.example.authservice.dto.LoginResponse;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.RegisterResponse;
import com.example.authservice.dto.ValidateTokenRequest;
import com.example.authservice.dto.ValidateTokenResponse;
import com.example.authservice.dto.ValidationResponse;
import com.example.authservice.model.AuthUser;
import com.example.authservice.model.UserRole;
import com.example.authservice.repository.AuthUserRepository;
import com.example.authservice.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            AuthUserRepository authUserRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.authUserRepository = authUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (authUserRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }

        UserRole role = parseRole(request.getRole());

        AuthUser authUser = new AuthUser();
        authUser.setEmail(normalizedEmail);
        authUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        authUser.setRole(role);

        AuthUser savedUser = authUserRepository.save(authUser);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "User created"
        );
    }

    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase(Locale.ROOT);

        AuthUser authUser = authUserRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), authUser.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtService.generateToken(authUser);
        return new LoginResponse(token, authUser.getId(), authUser.getRole().name());
    }

    public ValidateTokenResponse validateToken(ValidateTokenRequest request) {
        String token = sanitizeToken(request.getToken());
        boolean valid = jwtService.isTokenValid(token);

        if (!valid) {
            return new ValidateTokenResponse(false, null, null, null);
        }

        return new ValidateTokenResponse(
                true,
                jwtService.extractUserId(token),
                jwtService.extractRole(token),
                jwtService.extractEmail(token)
        );
    }

    public ValidationResponse validateUser(Long userId) {
        return new ValidationResponse(userId, authUserRepository.existsById(userId));
    }

    private UserRole parseRole(String rawRole) {
        try {
            return UserRole.valueOf(rawRole.trim().toUpperCase(Locale.ROOT));
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be CUSTOMER or BUSINESS");
        }
    }

    private String sanitizeToken(String token) {
        if (token == null) {
            return null;
        }

        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }

        return token;
    }
}
