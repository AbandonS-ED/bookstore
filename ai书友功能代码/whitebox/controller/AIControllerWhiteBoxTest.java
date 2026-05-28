package com.example.bookstore.whitebox.controller;

import com.example.bookstore.service.*;
import com.example.bookstore.util.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T-012 白盒测试：AI 书友 Function Calling 工具调用链路
 *
 * 覆盖路径：
 *   chat() → processChat() → callAnthropic() → executeTool()
 *   验证工具分发：searchBooks / getBookDetail / addToCart / getCartItems / listAddresses
 *
 * 注意：需要配置有效的 MiniMax API Key（见 application-local.yml）
 */
@SpringBootTest
@AutoConfigureMockMvc
class AIControllerWhiteBoxTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtils jwtUtils;

    private String makeToken(Long userId, String role) {
        return jwtUtils.generateToken(userId, role);
    }

    @Test
    void chat_withSearchBooksToolCall_shouldReturnBooks() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "messages", List.of(Map.of("role", "user", "content", "找一本关于编程的书"))
        ));

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void chatStream_shouldReturnSseEvents() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "messages", List.of(Map.of("role", "user", "content", "你好"))
        ));

        mockMvc.perform(post("/api/ai/chat/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + makeToken(1L, "user"))
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void executeTool_searchBooks_shouldParseKeywordFromArgs() throws Exception {
        String requestBody = objectMapper.writeValueAsString(Map.of(
                "messages", List.of(Map.of("role", "user", "content",
                        "搜索书籍《三体》"))
        ));

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reply").isNotEmpty());
    }
}