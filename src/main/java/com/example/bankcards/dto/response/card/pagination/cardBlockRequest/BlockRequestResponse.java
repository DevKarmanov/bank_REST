package com.example.bankcards.dto.response.card.pagination.cardBlockRequest;

import com.example.bankcards.dto.response.card.pagination.card.CardDtoForSearchResponse;

import java.time.LocalDateTime;

public record BlockRequestResponse(CardDtoForSearchResponse cardDto, String ownerName, LocalDateTime requestDate, String reason) {
}
