package com.example.auth.service;

import com.example.auth.entity.User;

public interface UserService {
    User getUserByEmail(String email);
}
