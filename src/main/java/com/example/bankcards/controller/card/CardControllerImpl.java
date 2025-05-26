package com.example.bankcards.controller.card;


import com.example.bankcards.dto.response.card.CardDtoResponse;
import com.example.bankcards.dto.response.card.pagination.card.CardDtoForSearchResponse;
import com.example.bankcards.dto.response.card.pagination.card.CardPageResponse;
import com.example.bankcards.service.card.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
public class CardControllerImpl implements CardController {

    private final CardService cardService;

    public CardControllerImpl(CardService cardService) {
        this.cardService = cardService;
    }

    @Override
    public ResponseEntity<?> getMyCards(int limit, int pageNumber, @AuthenticationPrincipal UserDetails userDetails) {
        CardPageResponse response = cardService.getUserCards(userDetails.getUsername(), limit, pageNumber);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getBlockRequests(int limit, int pageNumber) {
        return ResponseEntity.ok(cardService.getBlockRequests(limit, pageNumber));
    }

    @Override
    public ResponseEntity<?> getBalance(Long id) {
        String balance = cardService.getBalance(id);
        return ResponseEntity.ok(balance);
    }

    @Override
    public ResponseEntity<?> toggleCardState(Long id) {
        String result = cardService.toggleCardState(id);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity<?> requestCardBlock(Long cardId, String reason) {
        cardService.requestCardBlock(cardId, reason);
        return ResponseEntity.ok("Application left successfully");
    }

    @Override
    public ResponseEntity<?> transferMoney(Long fromCardId, Long toCardId, BigDecimal amount) {
        cardService.transferMoney(fromCardId, toCardId, amount);
        return ResponseEntity.ok("Transfer completed successfully");
    }

    @Override
    public ResponseEntity<?> getCardByNumber(String cardNumber) {
        CardDtoForSearchResponse response = cardService.getCardByNumber(cardNumber);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> getCardById(Long cardId) {
        CardDtoForSearchResponse response = cardService.getCardById(cardId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> createCard(String ownerName) {
        CardDtoResponse response = cardService.createCard(ownerName);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    public ResponseEntity<?> deleteCard(Long id) {
        cardService.delCard(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<?> getAllCards(int limit, int pageNumber) {
        CardPageResponse response = cardService.getAllCards(limit, pageNumber);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<?> addMoney(Long cardId, BigDecimal amount) {
        cardService.addMoney(cardId, amount);
        return ResponseEntity.ok("Money added successfully");
    }

    @Override
    public ResponseEntity<?> withdrawMoney(Long cardId, BigDecimal amount) {
        cardService.withdrawMoney(cardId, amount);
        return ResponseEntity.ok("Money withdrawn successfully");
    }
}
