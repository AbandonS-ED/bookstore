package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.bookstore.entity.ChatHistory;
import com.example.bookstore.mapper.ChatHistoryMapper;
import com.example.bookstore.service.ChatHistoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
    implements ChatHistoryService {

    @Override
    public List<ChatHistory> getRecentMessages(Long userId, int limit) {
        LambdaQueryWrapper<ChatHistory> qw = new LambdaQueryWrapper<>();
        qw.eq(ChatHistory::getUserId, userId)
          .orderByAsc(ChatHistory::getCreateTime)
          .last("LIMIT " + limit);
        return baseMapper.selectList(qw);
    }

    @Override
    public void clearHistory(Long userId) {
        LambdaQueryWrapper<ChatHistory> qw = new LambdaQueryWrapper<>();
        qw.eq(ChatHistory::getUserId, userId);
        baseMapper.delete(qw);
    }
}
