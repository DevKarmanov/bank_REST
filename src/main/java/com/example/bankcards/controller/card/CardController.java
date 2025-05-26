package com.example.bankcards.controller.card;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@CrossOrigin
@RequestMapping("/api/v1/card")
public interface CardController {

    @GetMapping("/my")
    ResponseEntity<?> getMyCards(
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "0") int pageNumber,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @GetMapping("/block-requests")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<?> getBlockRequests(@RequestParam(required = false, defaultValue = "10") int limit,
                                       @RequestParam(required = false, defaultValue = "0") int pageNumber);

    @GetMapping("/balance/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    ResponseEntity<?> getBalance(@PathVariable Long id);

    @PatchMapping("/toggle/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<?> toggleCardState(@PathVariable Long id);

    @PostMapping("/block-request/{cardId}")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<?> requestCardBlock(
            @PathVariable Long cardId,
            @RequestParam String reason
    );

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    ResponseEntity<?> transferMoney(
            @RequestParam Long fromCardId,
            @RequestParam Long toCardId,
            @RequestParam BigDecimal amount
    );

    @GetMapping("/search/by-number/{cardNumber}")
    ResponseEntity<?> getCardByNumber(@PathVariable String cardNumber);

    @GetMapping("/search/by-id/{cardId}")
    ResponseEntity<?> getCardById(@PathVariable Long cardId);

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<?> createCard(@RequestParam String ownerName);

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<?> deleteCard(@PathVariable Long id);

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<?> getAllCards(
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "0") int pageNumber
    );
}



