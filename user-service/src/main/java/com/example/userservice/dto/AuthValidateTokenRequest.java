package com.example.userservice.dto;

public class AuthValidateTokenRequest {

    private String token;

    public AuthValidateTokenRequest() {
    }

    public AuthValidateTokenRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
