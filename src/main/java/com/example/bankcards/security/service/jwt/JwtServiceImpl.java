package com.example.bankcards.security.service.jwt;

import com.example.bankcards.exception.jwt.InvalidJwtTokenException;
import com.example.bankcards.exception.jwt.InvalidRefreshTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService{
    private final static Logger logger = LoggerFactory.getLogger(JwtServiceImpl.class);

    @Value("${jwt.secret-key}")
    private String secret_key;

    private final UserDetailsService userDetailsService;

    public JwtServiceImpl(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }


    @Override
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "access");
        return createToken(claims, userDetails.getUsername(), 1000 * 60 * 15);
    }

    @Override
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "refresh");
        return createToken(claims, userDetails.getUsername(), 1000 * 60 * 60 * 24 * 7);
    }


    private String createToken(Map<String, Object> claims, String subject, long expirationTimeMillis) {
        JwtBuilder builder = Jwts.builder()
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTimeMillis));

        claims.forEach(builder::claim);

        return builder
                .signWith(getSecretKey())
                .compact();
    }

    @Override
    public Boolean validateAccessToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        try {
            return (isAccessToken(token) && username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        }catch (Exception e){
            return false;
        }
    }


    private boolean isAccessToken(String token) {
        Claims claims = extractAllClaims(token);
        return "access".equals(claims.get("token_type"));
    }

    @Override
    public Boolean validateRefreshToken(String token) {
        if (!isRefreshToken(token)) {
            throw new IllegalArgumentException("Provided token is not a refresh token");
        }
        return !isTokenExpired(token);
    }

    private boolean isRefreshToken(String token) {
        Claims claims = extractAllClaims(token);
        return "refresh".equals(claims.get("token_type"));
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        if (validateRefreshToken(refreshToken)) {
            String username = extractUsername(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return generateAccessToken(userDetails);
        } else {
            throw new InvalidRefreshTokenException("Refresh token is invalid or expired");
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            logger.error("JWT parsing failed: {}", e.getMessage());
            throw new InvalidJwtTokenException("Token parsing failed", e);
        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret_key.getBytes(StandardCharsets.UTF_8));
    }


}

