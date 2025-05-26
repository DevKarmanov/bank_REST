package com.example.bankcards.service.user;


import com.example.bankcards.dto.request.AuthRequest;
import com.example.bankcards.dto.request.UserDtoRequest;
import com.example.bankcards.dto.request.UserPatchRequest;
import com.example.bankcards.dto.response.auth.AuthResponse;
import com.example.bankcards.entity.user.MyUser;
import com.example.bankcards.exception.user.UserAlreadyExist;
import com.example.bankcards.exception.user.UserDeletionException;
import com.example.bankcards.repository.CardBlockRequestRepo;
import com.example.bankcards.repository.MyUserRepo;
import com.example.bankcards.security.service.jwt.JwtService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService{
    private final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final MyUserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final CardBlockRequestRepo cardBlockRequestRepo;

    public UserServiceImpl(MyUserRepo userRepo, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtService jwtService, CardBlockRequestRepo cardBlockRequestRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.cardBlockRequestRepo = cardBlockRequestRepo;
    }

    @Transactional
    @Override
    public void registerUser(UserDtoRequest userDtoRequest) {
        String name = userDtoRequest.name().trim();
        String password = userDtoRequest.password().trim();
        List<String> userRoles = userDtoRequest.role();

        if (name.isEmpty() || password.isEmpty() || userRoles == null || userRoles.isEmpty()) {
            logger.warn("Registration failed: empty name or password or roles");
            throw new IllegalArgumentException("Name, password and roles must not be empty");
        }

        if (userRepo.existsByNameIgnoreCase(name)) {
            logger.warn("Registration failed: user with name '{}' already exists", name);
            throw new UserAlreadyExist("A user with this name already exists");
        }

        MyUser user = new MyUser(
                name,
                userRoles,
                passwordEncoder.encode(password),
                true,
                LocalDateTime.now()
        );

        userRepo.save(user);

        logger.info("User '{}' registered successfully", name);
    }

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password())
            );
        } catch (DisabledException e) {
            logger.warn("Authentication failed: user is disabled - {}", authRequest.username());
            throw new DisabledException("User account is disabled", e);
        } catch (BadCredentialsException e) {
            logger.warn("Authentication failed: bad credentials for username - {}", authRequest.username());
            throw new BadCredentialsException("Incorrect username or password", e);
        } catch (AuthenticationException e) {
            logger.error("Authentication failed due to unexpected error for username {}: {}", authRequest.username(), e.getMessage(), e);
            throw new RuntimeException("Unexpected authentication error", e);
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.username());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        logger.info("User '{}' successfully authenticated. Tokens issued.", authRequest.username());

        return new AuthResponse(accessToken, refreshToken);
    }



    @Transactional
    @Override
    public void delUser(String name){
        MyUser user = getUserByName(name);
        cardBlockRequestRepo.deleteAllByRequestedBy(user);
        try {
            userRepo.delete(user);
        }catch (Exception e){
            logger.error("User deletion error");
            throw new UserDeletionException("Failed to delete user", e);
        }
    }

    @Transactional
    @Override
    public void updateUser(UserPatchRequest userPatchRequest) {
        MyUser user = getCurrentUser();

        userPatchRequest.name().ifPresent(name -> {
            if (name.trim().isEmpty()) {
                throw new IllegalArgumentException("Name cannot be blank");
            }
            if(getUserByName(name)!=null){
                throw new UserAlreadyExist("User with this name already exist");
            }
            user.setName(name);
        });

        userRepo.save(user);
    }


    @Transactional
    @Override
    public void blockUser(String userName,
                          LocalDateTime unlockAt,
                          String reason) {

        MyUser user = getCurrentUser();

        user.setEnable(false);
        user.setUnlockAt(unlockAt);
        user.setBlockReason(reason);
        userRepo.save(user);
    }

    @Transactional
    @Override
    public String toggleUserAuthorities(String userName) {
        MyUser user = getUserByName(userName);
        logger.info("Toggling ADMIN role for user '{}'", userName);

        List<String> roles = new ArrayList<>(user.getRoles());
        logger.debug("Current roles for user '{}': {}", userName, roles);

        boolean roleRemoved = false;

        if (roles.contains("ADMIN")) {
            roles.remove("ADMIN");
            logger.info("ADMIN role removed from user '{}'", userName);
            roleRemoved = true;

            if (roles.isEmpty()) {
                roles.add("USER");
                logger.info("No other roles found, assigning default role 'USER' to user '{}'", userName);
            }

        } else {
            if (roles.size() == 1) {
                logger.info("Single role '{}' detected, replacing with ADMIN", roles.get(0));
                roles.clear();
            }
            roles.add("ADMIN");
            logger.info("ADMIN role added to user '{}'", userName);
        }

        user.setRoles(roles);
        userRepo.save(user);
        logger.debug("Updated roles for user '{}': {}", userName, roles);

        if (roleRemoved) {
            logger.info("User '{}' downgraded from ADMIN", userName);
            return "User downgraded";
        } else {
            logger.info("User '{}' promoted to ADMIN", userName);
            return "User promoted to admin";
        }
    }


    @Transactional
    @Override
    public void unblockUser(String userName) {
        MyUser user = getCurrentUser();

        user.setEnable(true);
        user.setUnlockAt(LocalDateTime.now());
        userRepo.save(user);
    }

    @Override
    public MyUser getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getUserByName(authentication.getName());
    }

    @Override
    public MyUser getUserByName(String userName) {
        return userRepo.findByName(userName)
                .orElseThrow(() -> new UsernameNotFoundException("User with this name doesn't exist"));
    }

}
