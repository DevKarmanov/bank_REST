package com.example.bankcards.security.service.jwt;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

public interface JwtService {
    String generateAccessToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails);
    Boolean validateAccessToken(String token, UserDetails userDetails);
    Boolean validateRefreshToken(String token);
    String refreshAccessToken(String refreshToken);
    String extractUsername(String token);
    Date extractExpiration(String token);
}
