package com.example.bankcards.controller;

import com.example.bankcards.controller.auth.AuthControllerImpl;
import com.example.bankcards.dto.request.AuthRequest;
import com.example.bankcards.dto.request.UserDtoRequest;
import com.example.bankcards.dto.response.auth.AuthResponse;
import com.example.bankcards.security.service.MyUserDetailsService;
import com.example.bankcards.security.service.jwt.JwtServiceImpl;
import com.example.bankcards.service.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtServiceImpl jwtService;

    @MockBean
    private MyUserDetailsService myUserDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void refreshAccessToken_ShouldReturnBadRequest_IfNoToken() throws Exception {
        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Refresh token is missing"));
    }

    @Test
    void refreshAccessToken_ShouldReturnNewToken() throws Exception {
        String refreshToken = "valid-refresh-token";
        String newAccessToken = "new-access-token";

        when(jwtService.refreshAccessToken(refreshToken)).thenReturn(newAccessToken);

        Map<String, String> request = Map.of("refreshToken", refreshToken);

        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(newAccessToken));
    }

    @Test
    void login_ShouldReturnTokens() throws Exception {
        AuthRequest authRequest = new AuthRequest("user", "password");
        AuthResponse authResponse = new AuthResponse("access-token", "refresh-token");

        when(userService.login(any(AuthRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

    }

    @Test
    void register_ShouldReturnOk_WhenUserRegistered() throws Exception {
        UserDtoRequest userDtoRequest = new UserDtoRequest("newuser","password", List.of("ROLE_USER"));

        doNothing().when(userService).registerUser(any(UserDtoRequest.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenUserExists() throws Exception {
        UserDtoRequest userDtoRequest = new UserDtoRequest("existinguser","password",List.of("ROLE_USER"));

        doThrow(new IllegalArgumentException("User already exists"))
                .when(userService).registerUser(any(UserDtoRequest.class));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDtoRequest)))
                .andExpect(status().isBadRequest());
    }
}
