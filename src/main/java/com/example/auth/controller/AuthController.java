package com.example.auth.controller;

import com.example.auth.dto.request.auth.LoginRequest;
import com.example.auth.dto.request.auth.RegisterRequest;
import com.example.auth.dto.response.auth.AuthResponse;
import com.example.auth.dto.response.auth.LoginResult;
import com.example.auth.security.JwtProperties;
import com.example.auth.service.AuthService;
import com.example.auth.util.SecurityUtil;
import com.example.auth.util.annotation.ApiMessage;
import com.example.auth.util.exception.UnauthorizedException;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    public AuthController(AuthService authService, JwtProperties jwtProperties) {
        this.authService = authService;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/register")
    @ApiMessage("Register successfully")
    public ResponseEntity<AuthResponse.UserInfo> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse.UserInfo response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    @ApiMessage("Login successfully")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult response = authService.login(request);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", response.refreshToken())
                .httpOnly(true)
                // .secure(true)
                .path("/")
                .maxAge(jwtProperties.refreshTokenExpiration())
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response.authResponse());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty())
            throw new UnauthorizedException("Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại");

        LoginResult newTokens = authService.refresh(refreshToken);
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", newTokens.refreshToken())
                .httpOnly(true)
                // .secure(true)
                .path("/")
                .maxAge(jwtProperties.refreshTokenExpiration())
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(newTokens.authResponse());
    }

    @PostMapping("/logout")
    @ApiMessage("Logout successfully")
    public ResponseEntity<Void> logout() {
        String email = SecurityUtil.getCurrentUserLogin().orElse(null);
        authService.logout(email);
        ResponseCookie deleteCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                // .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    @GetMapping("/account")
    @ApiMessage("Get current user profile successfully")
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser() {
        String email = SecurityUtil.getCurrentUserLogin().orElse(null);
        AuthResponse.UserInfo response = authService.getCurrentUserProfile(email);
        return ResponseEntity.ok(response);
    }
}
