package com.archmanager_back.util;

import java.security.SecureRandom;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class RandomStringUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "abcdefghijklmnopqrstuvwxyz"
            + "0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String randomAlphanumeric(int length) {
        if (length < 1) {
            throw new IllegalArgumentException("Length must be >= 1");
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(idx));
        }
        return sb.toString();
    }
}
