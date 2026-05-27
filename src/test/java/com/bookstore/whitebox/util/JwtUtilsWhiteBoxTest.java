package com.bookstore.whitebox.util;

import com.bookstore.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-004 白盒测试：JWT Token 生成、过期判断、解析
 *
 * 覆盖路径：
 *   JwtUtils.generateToken() → JwtUtils.isTokenExpired() → JwtUtils.getUserId() / getRole()
 */
@SpringBootTest
class JwtUtilsWhiteBoxTest {

    @Autowired
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtils, "secret", "bookstore-jwt-secret-key-for-test-2024");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 7200000L);
    }

    @Test
    void generateToken_shouldContainUserIdAndRole() {
        Long userId = 12345L;
        String role = "user";

        String token = jwtUtils.generateToken(userId, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(userId, jwtUtils.getUserId(token));
        assertEquals(role, jwtUtils.getRole(token));
    }

    @Test
    void isTokenExpired_shouldReturnFalse_forFreshToken() {
        String token = jwtUtils.generateToken(1L, "user");
        assertFalse(jwtUtils.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_shouldReturnTrue_forTamperedToken() {
        String token = jwtUtils.generateToken(1L, "user");
        String tampered = token.substring(0, Math.max(0, token.length() - 5)) + "xxxxx";
        assertTrue(jwtUtils.isTokenExpired(tampered));
    }

    @Test
    void getUserId_shouldParseCorrectly() {
        Long expectedId = 99999L;
        String token = jwtUtils.generateToken(expectedId, "admin");
        assertEquals(expectedId, jwtUtils.getUserId(token));
    }

    @Test
    void getRole_shouldParseCorrectly_adminRole() {
        String token = jwtUtils.generateToken(1L, "admin");
        assertEquals("admin", jwtUtils.getRole(token));
    }

    @Test
    void getRole_shouldParseCorrectly_userRole() {
        String token = jwtUtils.generateToken(1L, "user");
        assertEquals("user", jwtUtils.getRole(token));
    }
}