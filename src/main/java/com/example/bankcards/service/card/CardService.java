package com.example.bankcards.service.card;

import com.example.bankcards.dto.response.card.CardDtoResponse;
import com.example.bankcards.dto.response.card.pagination.card.CardDtoForSearchResponse;
import com.example.bankcards.dto.response.card.pagination.card.CardPageResponse;
import com.example.bankcards.dto.response.card.pagination.cardBlockRequest.BlockRequestPageResponse;
import com.example.bankcards.entity.card.Card;
import java.math.BigDecimal;
import java.util.Optional;

public interface CardService {
    Optional<Card> findById(Long id);

    Card findByCardNumber(String cardNumber);

    CardDtoResponse createCard(String ownerName);

    void addMoney(Long cardId,BigDecimal amount);
    void withdrawMoney(Long cardId, BigDecimal amount);

    String toggleCardState(Long cardId);

    void delCard(Long cardId);

    String getBalance(Long cardId);
    void requestCardBlock(Long cardId, String reason);

    void transferMoney(Long fromCardId, Long toCardId, BigDecimal amount);

    CardPageResponse getUserCards(String ownerName, int limit, int pageNumber);

    BlockRequestPageResponse getBlockRequests(int limit, int pageNumber);

    CardPageResponse getAllCards(int limit, int pageNumber);

    CardDtoForSearchResponse getCardByNumber(String cardNumber);
    CardDtoForSearchResponse getCardById(Long cardId);
}
