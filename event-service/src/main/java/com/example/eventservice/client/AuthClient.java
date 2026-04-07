package com.example.eventservice.client;

import com.example.eventservice.dto.AuthValidateTokenRequest;
import com.example.eventservice.dto.AuthValidateTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-token-client", url = "${auth-service.url:http://localhost:8081}")
public interface AuthClient {

    @PostMapping("/api/auth/validate")
    AuthValidateTokenResponse validateToken(@RequestBody AuthValidateTokenRequest request);
}
