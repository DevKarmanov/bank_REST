package com.example.bankcards.controller.auth;

import com.example.bankcards.dto.request.AuthRequest;
import com.example.bankcards.dto.request.UserDtoRequest;
import com.example.bankcards.security.service.jwt.JwtServiceImpl;
import com.example.bankcards.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
public class AuthControllerImpl implements AuthController {
    private final UserService userService;
    private final JwtServiceImpl jwtService;

    public AuthControllerImpl(UserService userService, JwtServiceImpl jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public ResponseEntity<?> refreshAccessToken(Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token is missing");
        }

        String newAccessToken = jwtService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @Override
    public ResponseEntity<?> login(AuthRequest authRequest) {
        return ResponseEntity.ok(userService.login(authRequest));
    }

    @Override
    public ResponseEntity<?> register(UserDtoRequest userDtoRequest) {
        userService.registerUser(userDtoRequest);
        return ResponseEntity.ok("User registered successfully");
    }
}
