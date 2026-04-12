package com.example.auth.security;

import com.example.auth.util.format_response.RestResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    public CustomAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        String message = "Bạn cần đăng nhập (hoặc đính kèm Token) để truy cập tài nguyên này.";

        // Bắt lỗi động: Bóc tách lỗi chi tiết từ máy quét Token của Spring
        if (authException instanceof OAuth2AuthenticationException oauth2Exception) {
            message = oauth2Exception.getError().getDescription();
        }

        RestResponse<?> res = RestResponse.error(message, null);
        mapper.writeValue(response.getWriter(), res);
    }
}
