package com.example.bookstore.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    @Test
    void encryptPassword_encodesRawPassword() {
        String rawPassword = "123456";
        String encrypted = SecurityUtils.encryptPassword(rawPassword);

        assertNotNull(encrypted);
        assertNotEquals(rawPassword, encrypted);
    }

    @Test
    void matchesPassword_withCorrectPassword_returnsTrue() {
        String rawPassword = "123456";
        String encrypted = SecurityUtils.encryptPassword(rawPassword);

        assertTrue(SecurityUtils.matchesPassword(rawPassword, encrypted));
    }

    @Test
    void matchesPassword_withWrongPassword_returnsFalse() {
        String encrypted = SecurityUtils.encryptPassword("123456");

        assertFalse(SecurityUtils.matchesPassword("wrongpassword", encrypted));
    }

    @Test
    void encryptPassword_differentHashesForSamePassword() {
        String hash1 = SecurityUtils.encryptPassword("123456");
        String hash2 = SecurityUtils.encryptPassword("123456");

        // BCrypt generates different hashes due to salt
        assertNotEquals(hash1, hash2);
        // But both should match the original password
        assertTrue(SecurityUtils.matchesPassword("123456", hash1));
        assertTrue(SecurityUtils.matchesPassword("123456", hash2));
    }
}