package com.example.bookstore.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderNoGeneratorTest {

    @Test
    void generate_returnsUniqueNumbers() {
        String orderNo1 = OrderNoGenerator.generate();
        String orderNo2 = OrderNoGenerator.generate();

        assertNotNull(orderNo1);
        assertNotNull(orderNo2);
        assertNotEquals(orderNo1, orderNo2);
    }

    @Test
    void generate_hasCorrectLength() {
        String orderNo = OrderNoGenerator.generate();

        // Format: yyyyMMddHHmmss (14 digits) + sequence (3 digits) = 17 digits
        assertEquals(17, orderNo.length());
    }

    @Test
    void generate_containsOnlyDigits() {
        String orderNo = OrderNoGenerator.generate();

        assertTrue(orderNo.matches("\\d+"));
    }

    @Test
    void generate_startsWithCurrentDateTime() {
        String orderNo = OrderNoGenerator.generate();

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String expectedPrefix = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        assertTrue(orderNo.startsWith(expectedPrefix),
            "Expected to start with " + expectedPrefix + " but got " + orderNo);
    }
}