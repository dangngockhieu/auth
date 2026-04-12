package com.example.auth.service.impl;

import com.example.auth.dto.request.auth.LoginRequest;
import com.example.auth.dto.request.auth.RegisterRequest;
import com.example.auth.dto.response.auth.AuthResponse;
import com.example.auth.dto.response.auth.LoginResult;
import com.example.auth.entity.User;
import com.example.auth.entity.enums.UserStatus;
import com.example.auth.mapper.UserMapper;
import com.example.auth.repository.UserRepository;
import com.example.auth.security.JwtTokenProvider;
import com.example.auth.service.AuthService;
import com.example.auth.util.SecurityUtil;
import com.example.auth.util.exception.ConflictException;
import com.example.auth.util.exception.ResourceNotFoundException;
import com.example.auth.util.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

    private String normalizeEmail(String email) {
        if (email == null) {
            throw new UnauthorizedException("Authenticated user is missing.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private User getActiveUserByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return userRepository.findByEmailAndStatus(normalizedEmail, UserStatus.ACTIVE)
                .orElseThrow(() -> new UnauthorizedException("User account is not active."));
    }

    @Override
    @Transactional
    public AuthResponse.UserInfo register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email is already registered.");
        }
        User user = userMapper.toNewUser(request, normalizedEmail, passwordEncoder.encode(request.password()));
        User savedUser = userRepository.save(user);

        return userMapper.toUserProfile(savedUser);
    }

    @Override
    @Transactional
    public LoginResult login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.password()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = getActiveUserByEmail(normalizedEmail);

        return issueTokens(user);
    }

    @Override
    @Transactional
    public LoginResult refresh(String request) {
        Jwt decodedToken = this.jwtTokenProvider.checkValidToken(request);
        String email = decodedToken.getSubject();
        User user = getActiveUserByEmail(email);
        String rawToken = request;
        String hashed = SecurityUtil.hashWithSHA256(rawToken);

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(hashed)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        return issueTokens(user);
    }

    @Override
    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse.UserInfo getCurrentUserProfile(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toUserProfile(user);
    }

    private LoginResult issueTokens(User user) {
        AuthResponse.UserInfo userInfo = userMapper.toUserProfile(user);
        AuthResponse res = new AuthResponse();
        res.setUser(userInfo);

        String accessToken = jwtTokenProvider.createAccessToken(userInfo);
        String refreshToken = jwtTokenProvider.createRefreshToken(userInfo.getEmail());

        res.setAccessToken(accessToken);

        // Lưu hash của refresh token vào entity user
        user.setRefreshToken(SecurityUtil.hashWithSHA256(refreshToken));
        userRepository.save(user);

        return new LoginResult(res, refreshToken);
    }
}
