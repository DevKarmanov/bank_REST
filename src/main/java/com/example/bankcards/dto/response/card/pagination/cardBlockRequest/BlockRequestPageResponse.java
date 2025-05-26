package com.example.bankcards.dto.response.card.pagination.cardBlockRequest;

import java.util.List;


public record BlockRequestPageResponse(
        List<BlockRequestResponse> cards,
        boolean last,
        int totalPages,
        long totalElements,
        boolean first,
        int numberOfElements
) {}
