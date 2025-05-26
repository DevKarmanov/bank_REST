package com.example.bankcards.service.user;

import com.example.bankcards.dto.request.AuthRequest;
import com.example.bankcards.dto.request.UserDtoRequest;
import com.example.bankcards.dto.request.UserPatchRequest;
import com.example.bankcards.dto.response.auth.AuthResponse;
import com.example.bankcards.entity.user.MyUser;
import com.example.bankcards.repository.MyUserRepo;
import com.example.bankcards.security.service.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private MyUserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    private MyUser testUser;
    private UserDtoRequest testUserDtoRequest;
    private AuthRequest testAuthRequest;

    @BeforeEach
    void setUp() {
        testUser = new MyUser();
        testUser.setId(1L);
        testUser.setName("testUser");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(new ArrayList<>(List.of("ROLE_USER")));

        testUserDtoRequest = new UserDtoRequest("testUser","password", List.of("ROLE_USER"));

        testAuthRequest = new AuthRequest("testUser","password");
    }

    @Test
    void registerUser_Success() {
        when(userRepo.existsByNameIgnoreCase(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepo.save(any(MyUser.class))).thenReturn(testUser);

        userService.registerUser(testUserDtoRequest);

        verify(userRepo).save(any(MyUser.class));
    }

    @Test
    void registerUser_UserAlreadyExists() {
        when(userRepo.existsByNameIgnoreCase(anyString())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.registerUser(testUserDtoRequest));
    }

    @Test
    void login_Success() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(mock(UserDetails.class));
        when(jwtService.generateAccessToken(any(UserDetails.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(UserDetails.class))).thenReturn("refreshToken");

        AuthResponse result = userService.login(testAuthRequest);

        assertNotNull(result);
        assertEquals("accessToken", result.jwtToken());
        assertEquals("refreshToken", result.refreshToken());
    }

    @Test
    void updateUser_Success() {
        UserPatchRequest patchRequest = new UserPatchRequest(Optional.of("newName"));

        when(userRepo.findByName(anyString())).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(MyUser.class))).thenReturn(testUser);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        userService.updateUser(patchRequest);

        verify(userRepo).save(any(MyUser.class));
        assertEquals("newName", testUser.getName());

        SecurityContextHolder.clearContext();
    }

    @Test
    void blockUser_Success() {
        String userName = "testUser";
        LocalDateTime unlockAt = LocalDateTime.now().plusHours(1);
        String reason = "Test block reason";

        when(userRepo.findByName(userName)).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(MyUser.class))).thenReturn(testUser);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        userService.blockUser(userName, unlockAt, reason);

        assertFalse(testUser.isEnable());
        assertEquals(unlockAt, testUser.getUnlockAt());
        assertEquals(reason, testUser.getBlockReason());
        verify(userRepo).save(testUser);

        SecurityContextHolder.clearContext();
    }

    @Test
    void toggleUserAuthorities_Success() {
        when(userRepo.findByName(anyString())).thenReturn(Optional.of(testUser));
        when(userRepo.save(any(MyUser.class))).thenReturn(testUser);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("testUser");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String result = userService.toggleUserAuthorities("testUser");

        assertNotNull(result);
        verify(userRepo).save(testUser);

        assertTrue(testUser.getRoles().contains("ROLE_ADMIN") || testUser.getRoles().contains("ROLE_USER"));

        SecurityContextHolder.clearContext();
    }
} 