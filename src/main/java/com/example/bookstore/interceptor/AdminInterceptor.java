package com.example.bookstore.interceptor;

import com.example.bookstore.common.Constants;
import com.example.bookstore.util.AuthContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper;

    public AdminInterceptor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String role = AuthContext.getCurrentUserRole();

        if (!Constants.ROLE_ADMIN.equals(role)) {
            sendForbidden(response, "无权限访问");
            return false;
        }

        return true;
    }

    private void sendForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                "code", 403,
                "message", message
        )));
    }
}