package com.example.bankcards.controller.auth;

import com.example.bankcards.dto.request.AuthRequest;
import com.example.bankcards.dto.request.UserDtoRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping("/auth")
@CrossOrigin
public interface AuthController {

    @PostMapping("/refresh-token")
    ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request);

    @PostMapping("/login")
    ResponseEntity<?> login(@RequestBody AuthRequest authRequest);

    @PostMapping(value = "/register")
    ResponseEntity<?> register(@RequestBody UserDtoRequest userDtoRequest);
}
