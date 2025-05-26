package com.example.bankcards.controller.handler;

import com.example.bankcards.exception.card.CardCreationException;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.card.InvalidCardStateException;
import com.example.bankcards.exception.jwt.InvalidRefreshTokenException;
import com.example.bankcards.exception.user.UserAlreadyExist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<?> handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        logger.warn("Invalid refresh token: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "invalid_refresh_token", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Bad credentials: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "bad_credentials", "Invalid username or password");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<?> handleDisabledException(DisabledException ex) {
        logger.warn("Disabled account: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "account_disabled", "User account is disabled");
    }

    @ExceptionHandler(UserAlreadyExist.class)
    public ResponseEntity<?> handleUserAlreadyExist(UserAlreadyExist ex) {
        logger.warn("User already exists: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, "user_already_exists", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        logger.warn("Illegal argument: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "illegal_argument", ex.getMessage());
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCardNotFound(CardNotFoundException ex) {
        logger.warn("Card not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "card_not_found", ex.getMessage());
    }

    @ExceptionHandler(CardCreationException.class)
    public ResponseEntity<Map<String, String>> handleCardCreation(CardCreationException ex) {
        logger.error("Error creating card: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "card_creation_error", ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN, "access_denied", ex.getMessage());
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientFunds(InsufficientFundsException ex) {
        logger.warn("Insufficient funds: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "insufficient_funds", ex.getMessage());
    }

    @ExceptionHandler(InvalidCardStateException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCardState(InvalidCardStateException ex) {
        logger.warn("Invalid card state: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "invalid_card_state", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "internal_server_error", "Internal server error");
    }

    private ResponseEntity<Map<String, String>> buildResponse(HttpStatus status, String errorCode, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "error", errorCode,
                "message", message
        ));
    }
}
