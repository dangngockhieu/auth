package com.example.auth.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String jwtSecret,
        long accessTokenExpiration,
        long refreshTokenExpiration) {
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    public SecretKey getSecretKey() {
        return new SecretKeySpec(jwtSecret.getBytes(), JWT_ALGORITHM.getName());
    }
}
