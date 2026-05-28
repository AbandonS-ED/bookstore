package com.example.bookstore.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SecurityUtils {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private SecurityUtils() {}

    public static String encryptPassword(String password) {
        return encoder.encode(password);
    }

    public static boolean matchesPassword(String rawPassword, String encryptedPassword) {
        return encoder.matches(rawPassword, encryptedPassword);
    }
}