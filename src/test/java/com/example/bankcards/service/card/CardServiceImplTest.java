package com.example.bankcards.service.card;

import com.example.bankcards.dto.response.card.CardDtoResponse;
import com.example.bankcards.dto.response.card.pagination.card.CardDtoForSearchResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardBlockRequest;
import com.example.bankcards.entity.card.State;
import com.example.bankcards.entity.user.MyUser;
import com.example.bankcards.repository.CardBlockRequestRepo;
import com.example.bankcards.repository.CardRepo;
import com.example.bankcards.service.user.UserService;
import com.example.bankcards.util.CardUtil;
import com.example.bankcards.util.encrypt.AESUtil;
import com.example.bankcards.util.encrypt.CardNumberGenerator;
import com.example.bankcards.util.encrypt.HashUtil;
import com.example.bankcards.util.mapper.CardMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepo cardRepo;
    @Mock private UserService userService;
    @Mock private AESUtil aesUtil;
    @Mock private CardMapper cardMapper;
    @Mock private CardBlockRequestRepo blockRequestRepo;
    @Mock private CardUtil cardUtil;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    void findById_shouldReturnCard() {
        Card card = new Card();
        card.setId(1L);
        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));

        Optional<Card> result = cardService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(cardUtil).populateMaskedCardNumber(card);
    }

    @Test
    void findByCardNumber_shouldReturnCard() {
        String number = "1234123412341234";
        String hash = HashUtil.hash(number);
        Card card = new Card();
        when(cardRepo.findByCardNumberHash(hash)).thenReturn(Optional.of(card));

        Card result = cardService.findByCardNumber(number);

        assertEquals(card, result);
    }

    @Test
    void createCard_shouldReturnCardDtoResponse() throws Exception {
        MyUser user = new MyUser();
        user.setId(1L);
        String cardNumber = "1234567890123456";
        String encrypted = "enc";
        String masked = "**** **** **** 3456";

        when(userService.getUserByName("ivan")).thenReturn(user);
        when(aesUtil.encrypt(cardNumber)).thenReturn(encrypted);
        when(cardUtil.maskCardNumber(cardNumber)).thenReturn(masked);

        try (MockedStatic<CardNumberGenerator> generatorMockedStatic = mockStatic(CardNumberGenerator.class)) {
            generatorMockedStatic.when(CardNumberGenerator::generateCardNumber).thenReturn(cardNumber);

            CardDtoResponse response = cardService.createCard("ivan");

            assertEquals(masked, response.maskCardNumber());
            assertEquals(BigDecimal.ZERO, response.balance());
            assertEquals(State.ACTIVE, response.state());
        }
    }


    @Test
    void toggleCardState_shouldToggleActiveToBlocked() {
        Card card = new Card();
        card.setId(1L);
        card.setState(State.ACTIVE);
        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));

        String result = cardService.toggleCardState(1L);

        assertEquals("You have successfully blocked the card", result);
        assertEquals(State.BLOCKED, card.getState());
    }

    @Test
    void delCard_shouldDeleteCard() {
        Card card = new Card();
        card.setId(1L);
        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));

        cardService.delCard(1L);

        verify(blockRequestRepo).deleteAllByCard(card);
        verify(cardRepo).delete(card);
    }

    @Test
    void getBalance_shouldReturnFormattedBalance() {
        Card card = new Card();
        card.setId(1L);
        card.setBalance(BigDecimal.valueOf(1234.56));
        MyUser owner = new MyUser();
        card.setOwner(owner);

        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));
        when(userService.getCurrentUser()).thenReturn(owner);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(owner, null)
        );

        String balance = cardService.getBalance(1L);

        assertTrue(balance.contains("1") && balance.contains("234") && balance.contains(",56"));

    }

    @Test
    void requestCardBlock_shouldSaveBlockRequest() {
        Card card = new Card();
        MyUser user = new MyUser();
        card.setOwner(user);

        when(cardRepo.findById(1L)).thenReturn(Optional.of(card));
        when(userService.getCurrentUser()).thenReturn(user);

        cardService.requestCardBlock(1L, "stolen");

        verify(blockRequestRepo).save(any(CardBlockRequest.class));
    }

    @Test
    void transferMoney_shouldTransferCorrectly() {
        Card from = new Card();
        from.setId(1L);
        from.setBalance(new BigDecimal("100.00"));
        from.setState(State.ACTIVE);

        Card to = new Card();
        to.setId(2L);
        to.setBalance(new BigDecimal("50.00"));
        to.setState(State.ACTIVE);

        MyUser user = new MyUser();
        from.setOwner(user);
        to.setOwner(user);

        when(cardRepo.findById(1L)).thenReturn(Optional.of(from));
        when(cardRepo.findById(2L)).thenReturn(Optional.of(to));

        cardService.transferMoney(1L, 2L, new BigDecimal("25.00"));

        assertEquals(new BigDecimal("75.00"), from.getBalance());
        assertEquals(new BigDecimal("75.00"), to.getBalance());
    }

    @Test
    void getCardByNumber_shouldReturnCardDto() throws Exception {
        MyUser user = new MyUser();
        Card card = new Card();
        card.setId(1L);
        card.setEncryptedCardNumber(aesUtil.encrypt("1234123412341234"));
        card.setExpirationDate(LocalDate.of(2030, 1, 1));
        card.setState(State.ACTIVE);
        card.setBalance(BigDecimal.valueOf(500));
        card.setOwner(user);

        when(cardRepo.findByCardNumberHash(any())).thenReturn(Optional.of(card));
        when(userService.getCurrentUser()).thenReturn(user);
        when(cardMapper.toDto(card)).thenReturn(
                new CardDtoForSearchResponse(
                        1L,
                        "**** **** **** 1234",
                        LocalDate.of(2030, 1, 1),
                        State.ACTIVE,
                        BigDecimal.valueOf(500)
                )
        );

        CardDtoForSearchResponse dto = cardService.getCardByNumber("1234123412341234");

        assertEquals(1L, dto.id());
        assertEquals("**** **** **** 1234", dto.maskCardNumber());
        assertEquals(LocalDate.of(2030, 1, 1), dto.expirationDate());
        assertEquals(State.ACTIVE, dto.state());
        assertEquals(BigDecimal.valueOf(500), dto.balance());
    }

}

