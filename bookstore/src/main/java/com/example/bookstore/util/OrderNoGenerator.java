package com.example.bookstore.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class OrderNoGenerator {

    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNoGenerator() {}

    public static String generate() {
        long seq = SEQUENCE.incrementAndGet() % 1000;
        String date = LocalDateTime.now().format(FORMATTER);
        return date + String.format("%03d", seq);
    }
}