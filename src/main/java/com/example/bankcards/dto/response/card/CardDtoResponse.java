package com.example.bankcards.dto.response.card;


import com.example.bankcards.entity.card.State;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardDtoResponse(Long id, String cardNumber, String maskCardNumber, LocalDate expirationDate, State state,
                              BigDecimal balance) {
}
