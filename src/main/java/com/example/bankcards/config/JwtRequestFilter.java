package com.example.bankcards.config;

import com.example.bankcards.security.service.MyUserDetailsService;
import com.example.bankcards.security.service.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    private final JwtService jwtService;
    private final MyUserDetailsService myUserDetailsService;

    public JwtRequestFilter(JwtService jwtService, MyUserDetailsService myUserDetailsService) {
        this.jwtService = jwtService;
        this.myUserDetailsService = myUserDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractJwtFromHeader(request);

        if (token != null) {
            logger.info("JWT token found in request header");
        } else {
            logger.debug("No JWT token found in request header");
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                authenticateUser(token, request);
                logger.info("User authenticated successfully: {}", jwtService.extractUsername(token));
            } catch (UsernameNotFoundException e) {
                logger.error("User not found: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
                return;
            } catch (DisabledException e) {
                logger.warn("User account disabled: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
                return;
            } catch (BadCredentialsException e) {
                logger.warn("Bad credentials: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
                return;
            } catch (Exception e) {
                logger.error("JWT token validation failed: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                return;
            }
        } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
            logger.debug("Security context already has authentication");
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            logger.debug("Authorization header found with Bearer token");
            return header.substring(7);
        }
        logger.debug("Authorization header missing or does not start with Bearer");
        return null;
    }

    private void authenticateUser(String token, HttpServletRequest request) {
        String username = jwtService.extractUsername(token);
        logger.debug("Extracted username from token: {}", username);

        UserDetails userDetails = myUserDetailsService.loadUserByUsername(username);
        logger.debug("Loaded UserDetails for username: {}", username);

        if (!jwtService.validateAccessToken(token, userDetails)) {
            logger.warn("JWT token validation failed for user: {}", username);
            throw new BadCredentialsException("Invalid JWT token");
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        logger.debug("Authentication set in SecurityContextHolder for user: {}", username);

        request.setAttribute("jwtToken", token);
    }
}

