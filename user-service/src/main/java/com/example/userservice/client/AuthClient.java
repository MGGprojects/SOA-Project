package com.example.userservice.client;

import com.example.userservice.dto.AuthValidateTokenRequest;
import com.example.userservice.dto.AuthValidateTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-auth-token-client", url = "${auth-service.url:http://localhost:8081}")
public interface AuthClient {

    @PostMapping("/api/auth/validate")
    AuthValidateTokenResponse validateToken(@RequestBody AuthValidateTokenRequest request);
}
