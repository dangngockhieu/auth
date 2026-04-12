package com.example.auth.mapper;

import com.example.auth.dto.request.auth.RegisterRequest;
import com.example.auth.dto.response.auth.AuthResponse;
import com.example.auth.entity.User;
import com.example.auth.entity.enums.Role;
import com.example.auth.entity.enums.UserStatus;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toNewUser(RegisterRequest request, String normalizedEmail, String encodedPassword) {
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(encodedPassword);
        user.setRole(Role.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    public AuthResponse.UserInfo toUserProfile(User user) {
        return new AuthResponse.UserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name());
    }
}
