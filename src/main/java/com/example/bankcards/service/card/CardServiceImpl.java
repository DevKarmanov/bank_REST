package com.example.bankcards.service.card;


import com.example.bankcards.dto.response.card.CardDtoResponse;
import com.example.bankcards.dto.response.card.pagination.card.CardDtoForSearchResponse;
import com.example.bankcards.dto.response.card.pagination.card.CardPageResponse;
import com.example.bankcards.dto.response.card.pagination.cardBlockRequest.BlockRequestPageResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardBlockRequest;
import com.example.bankcards.entity.card.State;
import com.example.bankcards.entity.user.MyUser;
import com.example.bankcards.exception.card.CardCreationException;
import com.example.bankcards.exception.card.CardNotFoundException;
import com.example.bankcards.exception.card.InsufficientFundsException;
import com.example.bankcards.exception.card.InvalidCardStateException;
import com.example.bankcards.repository.CardBlockRequestRepo;
import com.example.bankcards.repository.CardRepo;
import com.example.bankcards.service.user.UserService;
import com.example.bankcards.util.CardUtil;
import com.example.bankcards.util.encrypt.AESUtil;
import com.example.bankcards.util.encrypt.CardNumberGenerator;
import com.example.bankcards.util.encrypt.HashUtil;
import com.example.bankcards.util.mapper.BlockRequestMapper;
import com.example.bankcards.util.mapper.CardMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class CardServiceImpl implements CardService{
    private static final Logger log = LoggerFactory.getLogger(CardServiceImpl.class);
    private final CardRepo cardRepo;
    private final UserService userService;
    private final AESUtil aesUtil;
    private final CardMapper cardMapper;
    private final CardBlockRequestRepo blockRequestRepo;
    private final CardUtil cardUtil;
    private final BlockRequestMapper blockRequestMapper;

    public CardServiceImpl(CardRepo cardRepo, UserService userService, AESUtil aesUtil, CardMapper cardMapper, CardBlockRequestRepo blockRequestRepo, CardUtil cardUtil, BlockRequestMapper blockRequestMapper) {
        this.cardRepo = cardRepo;
        this.userService = userService;
        this.aesUtil = aesUtil;
        this.cardMapper = cardMapper;
        this.blockRequestRepo = blockRequestRepo;
        this.cardUtil = cardUtil;
        this.blockRequestMapper = blockRequestMapper;
    }

    @Override
    public Optional<Card> findById(Long id) {
        log.info("Finding card by id: {}", id);
        Optional<Card> optionalCard = cardRepo.findById(id);
        optionalCard.ifPresent(card -> {
            log.info("Card found with id: {}, populating masked card number", id);
            cardUtil.populateMaskedCardNumber(card);
        });
        if (optionalCard.isEmpty()) {
            log.warn("Card not found with id: {}", id);
        }
        return optionalCard;
    }

    @Override
    public Card findByCardNumber(String cardNumber) {
        String hash = HashUtil.hash(cardNumber);
        log.debug("Finding card by card number hash: {}", hash);

        Card card = cardRepo.findByCardNumberHash(hash)
                .orElseThrow(() -> new CardNotFoundException("Card with this numbers doesn't exist"));

        log.info("Card found, populating masked card number");
        cardUtil.populateMaskedCardNumber(card);
        return card;
    }
    @Transactional
    @Override
    public CardDtoResponse createCard(String ownerName) {
        MyUser owner = userService.getUserByName(ownerName);
        String cardNumber = CardNumberGenerator.generateCardNumber();

        try {
            String encryptedCardNumber = aesUtil.encrypt(cardNumber);

            Card card = new Card();
            card.setOwner(owner);
            card.setEncryptedCardNumber(encryptedCardNumber);
            card.setExpirationDate(LocalDate.now().plusYears(3));
            card.setState(State.ACTIVE);
            card.setBalance(BigDecimal.ZERO);
            card.setCardNumberHash(HashUtil.hash(cardNumber));

            cardRepo.save(card);

            String maskCardNumber = cardUtil.maskCardNumber(cardNumber);

            log.info("Card created with id: {}, masked number: {}", card.getId(), maskCardNumber);

            return new CardDtoResponse(
                    card.getId(),
                    cardNumber,
                    maskCardNumber,
                    card.getExpirationDate(),
                    card.getState(),
                    card.getBalance()
            );
        } catch (Exception e) {
            log.error("Failed to create card for user id {}: {}", owner.getId(), e.getMessage(), e);
            throw new CardCreationException("Error when creating a map for a user with id " + owner.getId(), e);
        }
    }

    @Transactional
    @Override
    public void addMoney(Long cardId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Attempt to add invalid amount: {}", amount);
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Card card = getCard(cardId);
        if (checkInvalidCard(card)){
            log.warn("Invalid card state detected: {}",
                    card.getState());
            throw new InvalidCardStateException("Account replenishment is impossible: card is inactive");
        }

        BigDecimal newBalance = card.getBalance().add(amount);
        card.setBalance(newBalance);

        cardRepo.save(card);

        log.info("Added {} to card {}. New balance: {}", amount, cardId, newBalance);
    }

    @Transactional
    @Override
    public void withdrawMoney(Long cardId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Attempt to withdraw invalid amount: {}", amount);
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Card card = getCard(cardId);
        checkPermission(card.getOwner());

        if (checkInvalidCard(card)){
            log.warn("Invalid card state detected: {}",
                    card.getState());
            throw new InvalidCardStateException("Withdrawal is not possible: card is inactive");
        }

        if (card.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds: trying to withdraw {}, but balance is {}", amount, card.getBalance());
            throw new IllegalStateException("Insufficient funds");
        }

        BigDecimal newBalance = card.getBalance().subtract(amount);
        card.setBalance(newBalance);

        cardRepo.save(card);

        log.info("Withdrew {} from card {}. New balance: {}", amount, cardId, newBalance);
    }


    @Transactional
    @Override
    public String toggleCardState(Long cardId) {
        log.info("Toggling state for card with id: {}", cardId);

        Card card = getCard(cardId);
        State oldState = card.getState();

        State newState = oldState.equals(State.ACTIVE) ? State.BLOCKED : State.ACTIVE;
        card.setState(newState);
        log.info("Card id {} changed state from {} to {}", cardId, oldState, newState);

        return newState == State.ACTIVE
                ? "You have successfully activated the card"
                : "You have successfully blocked the card";
    }

    @Transactional
    @Override
    public void delCard(Long cardId) {
        log.info("Deleting card with id: {}", cardId);
        Card card = getCard(cardId);
        blockRequestRepo.deleteAllByCard(card);
        cardRepo.delete(card);
        log.info("Card with id {} successfully deleted", cardId);
    }

    @Transactional(readOnly = true)
    @Override
    public String getBalance(Long cardId) {
        log.info("Getting balance for card id: {}", cardId);
        Card card = findById(cardId).orElseThrow(() -> {
            log.warn("Card not found for balance check: {}", cardId);
            return new CardNotFoundException("Card with this id doesn't exist");
        });
        checkPermission(card.getOwner());
        BigDecimal balance = card.getBalance();
        DecimalFormat df = new DecimalFormat("#,##0.00");
        String formattedBalance = df.format(balance);
        log.info("Balance for card id {} is {}", cardId, formattedBalance);
        return formattedBalance;
    }

    @Transactional
    @Override
    public void requestCardBlock(Long cardId, String reason) {
        log.info("Received request to block card with id: {}", cardId);

        if (reason == null || reason.trim().isEmpty()) {
            log.warn("Block request reason is empty or null for card id: {}", cardId);
            throw new IllegalArgumentException("Block reason cannot be empty");
        }
        if (reason.length() > 255) {
            log.warn("Block request reason is too long ({} chars) for card id: {}", reason.length(), cardId);
            throw new IllegalArgumentException("Block reason must not exceed 255 characters");
        }

        Card card = getCard(cardId);

        if (!card.getOwner().equals(userService.getCurrentUser())){
            throw new AccessDeniedException("Only the cardholder can fulfill this request");
        }

        CardBlockRequest blockRequest = new CardBlockRequest();
        blockRequest.setCard(card);
        blockRequest.setRequestedBy(userService.getCurrentUser());
        blockRequest.setRequestDate(LocalDateTime.now());
        blockRequest.setReason(reason.trim());

        blockRequestRepo.save(blockRequest);

        log.info("Block request for card id {} saved successfully with reason: {}", cardId, blockRequest.getReason());
    }



    @Transactional
    @Override
    public void transferMoney(Long fromCardId, Long toCardId, BigDecimal amount) {
        log.info("Transferring money: {} from card {} to card {}", amount, fromCardId, toCardId);
        Card fromCard = findById(fromCardId)
                .orElseThrow(() -> new CardNotFoundException("Card with this id doesn't exist"));
        Card toCard = findById(toCardId)
                .orElseThrow(() -> new CardNotFoundException("Card with this id doesn't exist"));

        if (!fromCard.getOwner().equals(toCard.getOwner())) {
            log.warn("Access denied: Transfer between cards with different owners. From card owner: {}, To card owner: {}",
                    fromCard.getOwner().getName(), toCard.getOwner().getName());
            throw new AccessDeniedException("You can only transfer between your own cards");
        }

        if (checkInvalidCard(fromCard) || checkInvalidCard(toCard)) {
            log.warn("Invalid card state detected during transfer. From card state: {}, To card state: {}",
                    fromCard.getState(), toCard.getState());
            throw new InvalidCardStateException("Transaction not possible: one or both cards are inactive");
        }

        BigDecimal fromCardAmount = fromCard.getBalance().subtract(amount);
        if (fromCardAmount.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Insufficient funds: Card id {}, balance: {}, requested amount: {}", fromCardId, fromCard.getBalance(), amount);
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        fromCard.setBalance(fromCardAmount);
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepo.save(fromCard);
        cardRepo.save(toCard);
        log.info("Transfer successful: {} transferred from card {} to card {}", amount, fromCardId, toCardId);
    }

    private boolean checkInvalidCard(Card card) {
        boolean isInactive = !card.getState().equals(State.ACTIVE);
        boolean isExpired = card.getExpirationDate().isBefore(LocalDate.now());
        return isInactive || isExpired;
    }



    @Transactional(readOnly = true)
    @Override
    public CardPageResponse getUserCards(String ownerName, int limit, int pageNumber) {
        log.info("Getting cards for user: {}, page: {}, limit: {}", ownerName, pageNumber, limit);
        MyUser owner = userService.getUserByName(ownerName);

        checkPermission(owner);

        PageRequest pageRequest = PageRequest.of(pageNumber, limit);
        Page<Card> page = cardRepo.findByOwner(owner, pageRequest);

        log.info("Found {} cards for user {}", page.getNumberOfElements(), ownerName);
        return toDto(page);
    }

    @Override
    public BlockRequestPageResponse getBlockRequests(int limit, int pageNumber) {
        PageRequest pageRequest = PageRequest.of(pageNumber, limit);

        Page<CardBlockRequest> page = blockRequestRepo.findAll(pageRequest);
        return new BlockRequestPageResponse(
                blockRequestMapper.toDtoList(page.getContent()),
                page.isLast(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.getNumberOfElements()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public CardPageResponse getAllCards(int limit, int pageNumber) {
        log.info("Getting all cards, page: {}, limit: {}", pageNumber, limit);
        PageRequest pageRequest = PageRequest.of(pageNumber, limit);

        Page<Card> page = cardRepo.findAll(pageRequest);

        log.info("Found {} cards total", page.getNumberOfElements());
        return toDto(page);
    }

    @Transactional(readOnly = true)
    @Override
    public CardDtoForSearchResponse getCardByNumber(String cardNumber) {
        log.info("Getting card by number");
        Card card = findByCardNumber(cardNumber);
        checkPermission(card.getOwner());
        return cardMapper.toDto(card);
    }

    @Transactional(readOnly = true)
    @Override
    public CardDtoForSearchResponse getCardById(Long cardId) {
        log.info("Getting card by id: {}", cardId);
        Card card = findById(cardId).orElseThrow(() -> new CardNotFoundException("Card with this id doesn't exist"));
        checkPermission(card.getOwner());
        return cardMapper.toDto(card);
    }

    private void checkPermission(MyUser owner){
        MyUser currentUser = userService.getCurrentUser();
        if (!currentUser.equals(owner) && currentUser.getRoles().stream().noneMatch(role -> role.equals("ADMIN"))) {
            log.warn("Access denied for user {} to resource owned by {}", currentUser.getName(), owner.getName());
            throw new AccessDeniedException("Access denied: you are neither the owner nor the admin");
        }
    }

    private CardPageResponse toDto(Page<Card> page){
        return new CardPageResponse(
                cardMapper.toDtoList(page.getContent()),
                page.isLast(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.isFirst(),
                page.getNumberOfElements()
        );
    }

    private Card getCard(Long cardId) {
        log.debug("Retrieving card with id: {}", cardId);
        return findById(cardId).orElseThrow(() -> {
            log.warn("Card not found with id: {}", cardId);
            return new CardNotFoundException("Card with this id doesn't exist");
        });
    }
}
