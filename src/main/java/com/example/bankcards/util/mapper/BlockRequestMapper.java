package com.example.bankcards.util.mapper;


import com.example.bankcards.dto.response.card.pagination.cardBlockRequest.BlockRequestResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardBlockRequest;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class BlockRequestMapper {
    private final CardMapper cardMapper;

    public BlockRequestMapper(CardMapper cardMapper) {
        this.cardMapper = cardMapper;
    }

    public BlockRequestResponse toDto(CardBlockRequest blockRequest){
        Card card = blockRequest.getCard();
        return new BlockRequestResponse(cardMapper.toDto(card),blockRequest.getRequestedBy().getName(),blockRequest.getRequestDate(),blockRequest.getReason());
    }

    public List<BlockRequestResponse> toDtoList(List<CardBlockRequest> blockRequests){
        return blockRequests.stream().map(this::toDto).toList();
    }
}
