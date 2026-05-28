package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookstore.entity.BookQualityReview;
import com.example.bookstore.mapper.BookQualityReviewMapper;
import com.example.bookstore.service.BookQualityReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookQualityReviewServiceImpl implements BookQualityReviewService {

    private final BookQualityReviewMapper bookQualityReviewMapper;

    @Override
    public List<BookQualityReview> getByBookId(Long bookId) {
        LambdaQueryWrapper<BookQualityReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookQualityReview::getBookId, bookId)
                .orderByDesc(BookQualityReview::getRating)
                .last("LIMIT 3");
        return bookQualityReviewMapper.selectList(wrapper);
    }

    @Override
    public List<BookQualityReview> getRandomByBookIds(List<Long> bookIds, int limit) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<BookQualityReview> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BookQualityReview::getBookId, bookIds)
                .orderByDesc(BookQualityReview::getRating);
        List<BookQualityReview> all = bookQualityReviewMapper.selectList(wrapper);
        Collections.shuffle(all);
        return all.stream().limit(limit).collect(Collectors.toList());
    }
}
