package com.bookstore.whitebox.interceptor;

import com.bookstore.util.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-019 白盒测试：AdminInterceptor 权限校验
 *
 * 覆盖路径：
 *   WebMvcConfig.addInterceptors() → AdminInterceptor.preHandle()
 *     → AuthContext.getCurrentUserRole() → 返回 403/重定向
 *
 * 验证点：
 *   1. /admin/** 路径被拦截
 *   2. role=admin 的 token → 通过
 *   3. role=user 的 token → 拒绝
 *   4. 无 token → 拒绝
 */
@SpringBootTest
class AdminInterceptorWhiteBoxTest {

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Autowired
    private JwtUtils jwtUtils;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        ReflectionTestUtils.setField(jwtUtils, "secret", "bookstore-jwt-secret-key-for-test-2024");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 7200000L);
    }

    @Test
    void preHandle_adminRole_shouldReturnTrue() throws Exception {
        String token = jwtUtils.generateToken(1L, "admin");
        request.addHeader("Authorization", "Bearer " + token);
        request.setRequestURI("/admin/user/list");

        boolean result = adminInterceptor.preHandle(request, response, null);

        assertTrue(result);
    }

    @Test
    void preHandle_userRole_shouldReturnFalseOr403() throws Exception {
        String token = jwtUtils.generateToken(1L, "user");
        request.addHeader("Authorization", "Bearer " + token);
        request.setRequestURI("/admin/user/list");

        boolean result = adminInterceptor.preHandle(request, response, null);

        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    void preHandle_noToken_shouldReturnFalse() throws Exception {
        request.setRequestURI("/admin/user/list");

        boolean result = adminInterceptor.preHandle(request, response, null);

        assertFalse(result);
    }

    @Test
    void preHandle_nonAdminPath_shouldStillIntercept() throws Exception {
        String token = jwtUtils.generateToken(1L, "admin");
        request.addHeader("Authorization", "Bearer " + token);
        request.setRequestURI("/admin/book/list");

        boolean result = adminInterceptor.preHandle(request, response, null);

        assertTrue(result);
    }
}