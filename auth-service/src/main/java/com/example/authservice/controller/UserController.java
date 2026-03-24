package com.example.authservice.controller;

import com.example.authservice.dto.ValidationResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}/validate")
    public ValidationResponse validateUser(@PathVariable Long id) {
        boolean isValid = (id == 1); // Dummy
        return new ValidationResponse(id, isValid);
    }
}