package com.example.bankcards.util.encrypt;

import java.util.Random;

public class CardNumberGenerator {
    private static final Random random = new Random();

    private static final String BIN = "400000";

    public static String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder(BIN);

        for (int i = 0; i < 9; i++) {
            cardNumber.append(random.nextInt(10));
        }

        int checkDigit = getCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    private static int getCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) n -= 9;
            }
            sum += n;
            alternate = !alternate;
        }
        int mod = sum % 10;
        return (mod == 0) ? 0 : 10 - mod;
    }

}
