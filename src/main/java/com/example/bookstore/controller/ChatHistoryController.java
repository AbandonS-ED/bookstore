package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.entity.ChatHistory;
import com.example.bookstore.service.ChatHistoryService;
import com.example.bookstore.util.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    @GetMapping("/history")
    public Result<List<ChatHistory>> getHistory() {
        Long userId = AuthContext.getCurrentUserId();
        if (userId == null) {
            return Result.success(List.of());
        }
        return Result.success(chatHistoryService.getRecentMessages(userId, 50));
    }

    @DeleteMapping("/clear")
    public Result<Void> clearHistory() {
        Long userId = AuthContext.getCurrentUserId();
        if (userId != null) {
            chatHistoryService.clearHistory(userId);
        }
        return Result.success();
    }
}
