package com.example.bankcards.dto.response.card.pagination.card;

import com.example.bankcards.entity.card.State;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardDtoForSearchResponse(Long id, String maskCardNumber, LocalDate expirationDate, State state,
                                       BigDecimal balance) {
}
