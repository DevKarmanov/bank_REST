package com.example.bankcards.security.service;

import com.example.bankcards.entity.user.MyUser;
import com.example.bankcards.repository.MyUserRepo;
import com.example.bankcards.security.model.MyUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MyUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(MyUserDetailsService.class);
    private final MyUserRepo myUserRepo;

    public MyUserDetailsService(MyUserRepo myUserRepo) {
        this.myUserRepo = myUserRepo;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DisabledException {
        try {
            log.debug("Attempting to load user by username: {}", username);

            MyUser user = myUserRepo.findByName(username)
                    .orElseThrow(() -> {
                        log.warn("User not found: {}", username);
                        return new UsernameNotFoundException("User not found");
                    });

            LocalDateTime now = LocalDateTime.now();

            if (!user.isEnable()) {
                if (user.getUnlockAt() != null && user.getUnlockAt().isAfter(now)) {
                    log.warn("User {} is disabled until {}", username, user.getUnlockAt());
                    throw new DisabledException("User is disabled: " + user.getBlockReason());
                } else {
                    log.info("User {} unlock time passed or not set, enabling user", username);
                    user.setEnable(true);
                    myUserRepo.save(user);
                }
            }

            log.info("User {} loaded successfully", username);
            return new MyUserDetails(user);

        } catch (DisabledException e) {
            log.error("Disabled user access attempt: {}", username);
            throw new DisabledException("User " + username + " is currently disabled: " + e.getMessage());
        } catch (UsernameNotFoundException e) {
            log.error("User not found exception for username: {}", username);
            throw new UsernameNotFoundException("User with username '" + username + "' not found");
        } catch (Exception e) {
            log.error("Unexpected error during user retrieval for username {}: {}", username, e.getMessage());
            throw new InternalAuthenticationServiceException("Internal error during authentication for user '" + username + "'", e);
        }

    }
}

