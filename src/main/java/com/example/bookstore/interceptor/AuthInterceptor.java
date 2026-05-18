package com.example.bookstore.interceptor;

import com.example.bookstore.common.Constants;
import com.example.bookstore.entity.User;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.util.AuthContext;
import com.example.bookstore.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            sendUnauthorized(response, "未登录");
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (JwtUtils.isTokenExpired(token)) {
            sendUnauthorized(response, "Token已过期");
            return false;
        }

        Long userId = JwtUtils.getUserId(token);
        String role = JwtUtils.getRole(token);

        User user = userMapper.selectById(userId);
        if (user == null) {
            sendUnauthorized(response, "用户不存在");
            return false;
        }

        if (user.getStatus().equals(Constants.STATUS_DISABLED)) {
            sendUnauthorized(response, "账号已被禁用");
            return false;
        }

        user.setPassword(null);
        AuthContext.setCurrentUser(user);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        AuthContext.remove();
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                "code", 401,
                "message", message
        )));
    }
}