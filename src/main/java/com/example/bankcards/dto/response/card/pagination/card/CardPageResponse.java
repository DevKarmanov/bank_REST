package com.example.bankcards.dto.response.card.pagination.card;

import java.util.List;


public record CardPageResponse(
        List<CardDtoForSearchResponse> cards,
        boolean last,
        int totalPages,
        long totalElements,
        boolean first,
        int numberOfElements
) {}
