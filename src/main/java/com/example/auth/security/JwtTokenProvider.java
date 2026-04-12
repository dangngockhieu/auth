package com.example.auth.security;

import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import com.example.auth.dto.response.auth.AuthResponse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class JwtTokenProvider {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    // Tiêm các Bean cần thiết vào qua Constructor
    public JwtTokenProvider(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public String createAccessToken(AuthResponse.UserInfo dto) {
        Instant now = Instant.now();
        Instant validity = now.plus(jwtProperties.accessTokenExpiration(), ChronoUnit.SECONDS);

        List<String> listAuthority = new ArrayList<>();
        listAuthority.add(dto.getRole());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(dto.getEmail())
                .claim("user", dto)
                .claim("permissions", listAuthority)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JwtProperties.JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String createRefreshToken(String email) {
        Instant now = Instant.now();
        Instant validity = now.plus(jwtProperties.refreshTokenExpiration(), ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JwtProperties.JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public Jwt checkValidToken(String token) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                jwtProperties.getSecretKey()).macAlgorithm(JwtProperties.JWT_ALGORITHM).build();
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            System.out.println(">>> RefreshToken error: " + e.getMessage());
            throw e;
        }
    }
}