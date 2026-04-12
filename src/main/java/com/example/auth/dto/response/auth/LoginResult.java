package com.example.auth.dto.response.auth;

public record LoginResult(AuthResponse authResponse, String refreshToken) {
}
