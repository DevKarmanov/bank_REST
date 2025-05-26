package com.example.bankcards.exception.card;

public class InvalidCardStateException extends RuntimeException {
    public InvalidCardStateException(String message) {
        super(message);
    }
}
