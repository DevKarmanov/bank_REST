package com.example.bankcards.exception.jwt;

public class InvalidJwtTokenException extends RuntimeException {
    public InvalidJwtTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}