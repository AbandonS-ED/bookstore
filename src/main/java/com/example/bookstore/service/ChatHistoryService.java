package com.example.bookstore.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.bookstore.entity.ChatHistory;

import java.util.List;

public interface ChatHistoryService extends IService<ChatHistory> {
    List<ChatHistory> getRecentMessages(Long userId, int limit);
    void clearHistory(Long userId);
}
