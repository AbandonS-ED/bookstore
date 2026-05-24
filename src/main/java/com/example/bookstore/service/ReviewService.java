package com.example.bookstore.service;

import com.example.bookstore.entity.Review;
import com.example.bookstore.vo.ReviewVO;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    void add(Long userId, Review review);

    List<ReviewVO> getByBookId(Long bookId);

    List<ReviewVO> getByUserId(Long userId);

    void delete(Long userId, Long reviewId);

    Map<String, Object> getStats();
}