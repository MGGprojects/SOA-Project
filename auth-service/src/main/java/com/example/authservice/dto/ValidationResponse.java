package com.example.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Response object containing user validation result")
public class ValidationResponse {

    @Schema(description = "The ID of the validated user", example = "1")
    private Long userId;

    @Schema(description = "Whether the user is valid", example = "true")
    private boolean valid;
}
