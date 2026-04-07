package com.example.authservice.controller;

import com.example.authservice.dto.ValidationResponse;
import com.example.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Validation", description = "Endpoints for validating users")
public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Validate a user",
            description = "Temporary compatibility endpoint that checks whether a user exists in auth-service."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Validation result returned successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationResponse.class)
                    )
            )
    })
    @GetMapping("/{id}/validate")
    public ValidationResponse validateUser(
            @Parameter(description = "The ID of the user to validate", required = true, example = "1")
            @PathVariable Long id) {
        return authService.validateUser(id);
    }
}
