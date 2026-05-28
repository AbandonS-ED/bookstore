package com.example.bookstore.service;

import com.example.bookstore.vo.FavoriteVO;

import java.util.List;
import java.util.Set;

public interface FavoriteService {
    void add(Long userId, Long bookId);
    void remove(Long userId, Long bookId);
    List<FavoriteVO> list(Long userId);
    boolean isFavorited(Long userId, Long bookId);
    Set<String> getFavoritedBookIds(Long userId);
}
