package com.example.bookstore.service;

import com.example.bookstore.entity.BookQualityReview;

import java.util.List;

public interface BookQualityReviewService {

    List<BookQualityReview> getByBookId(Long bookId);

    List<BookQualityReview> getRandomByBookIds(List<Long> bookIds, int limit);
}
