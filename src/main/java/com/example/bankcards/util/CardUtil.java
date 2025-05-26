package com.example.bankcards.util;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.util.encrypt.AESUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CardUtil {
    private final static Logger logger = LoggerFactory.getLogger(CardUtil.class);
    private final AESUtil aesUtil;

    public CardUtil(AESUtil aesUtil) {
        this.aesUtil = aesUtil;
    }

    public void populateMaskedCardNumber(Card card) {
        String masked = getMaskedCardNumberOrDefault(card.getEncryptedCardNumber());
        card.setMaskedCardNumber(masked);
    }

    private String getMaskedCardNumberOrDefault(String encryptedCardNumber) {
        if (encryptedCardNumber == null) {
            return "**** **** **** ****";
        }
        try {
            logger.debug("Trying to decrypt: {}", encryptedCardNumber);
            String decrypted = aesUtil.decrypt(encryptedCardNumber);
            logger.debug("Decrypted card number: {}", decrypted);
            return maskCardNumber(decrypted);
        } catch (Exception e) {
            logger.error("Failed to decrypt card number: {}", e.getMessage(), e);
            return "**** **** **** ****";
        }
    }

    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            logger.warn("Card number is null or too short to mask: {}", cardNumber);
            return cardNumber;
        }
        String masked = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        logger.debug("Card number masked as: {}", masked);
        return masked;
    }
}
