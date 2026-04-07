package com.example.businessservice.client;

import com.example.businessservice.dto.AuthValidateTokenRequest;
import com.example.businessservice.dto.AuthValidateTokenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "business-auth-token-client", url = "${auth-service.url:http://localhost:8081}")
public interface AuthClient {

    @PostMapping("/api/auth/validate")
    AuthValidateTokenResponse validateToken(@RequestBody AuthValidateTokenRequest request);
}
