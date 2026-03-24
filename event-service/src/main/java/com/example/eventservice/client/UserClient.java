package com.example.eventservice.client;

import com.example.eventservice.dto.UserValidationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "http://localhost:8081")
public interface UserClient {

    @GetMapping("/api/users/{id}/validate")
    UserValidationResponse validateUser(@PathVariable("id") Long id);
}