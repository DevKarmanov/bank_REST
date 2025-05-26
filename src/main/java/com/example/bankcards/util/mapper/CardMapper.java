package com.example.bankcards.util.mapper;


import com.example.bankcards.dto.response.card.pagination.card.CardDtoForSearchResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.util.CardUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CardMapper {
    private final CardUtil cardUtil;

    public CardMapper(CardUtil cardUtil) {
        this.cardUtil = cardUtil;
    }

    public CardDtoForSearchResponse toDto(Card card){
        return new CardDtoForSearchResponse(
                card.getId(),
                cardUtil.maskCardNumber(card.getMaskedCardNumber()),
                card.getExpirationDate(),
                card.getState(),
                card.getBalance());
    }

    public List<CardDtoForSearchResponse> toDtoList(List<Card> cards){
        return cards.stream().map(card->{
            if (card.getMaskedCardNumber()==null){
                cardUtil.populateMaskedCardNumber(card);
            }
            return toDto(card);
        }).toList();
    }
}
