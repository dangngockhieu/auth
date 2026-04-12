package com.example.auth.service;

import com.example.auth.dto.request.auth.LoginRequest;
import com.example.auth.dto.request.auth.RegisterRequest;
import com.example.auth.dto.response.auth.AuthResponse;
import com.example.auth.dto.response.auth.LoginResult;

public interface AuthService {

    AuthResponse.UserInfo register(RegisterRequest request);

    LoginResult login(LoginRequest request);

    LoginResult refresh(String request);

    void logout(String email);

    AuthResponse.UserInfo getCurrentUserProfile(String email);
}
