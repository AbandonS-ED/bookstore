package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookstore.entity.BookExcerpt;
import com.example.bookstore.mapper.BookExcerptMapper;
import com.example.bookstore.service.BookExcerptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookExcerptServiceImpl implements BookExcerptService {

    private final BookExcerptMapper bookExcerptMapper;

    @Override
    public List<BookExcerpt> getByBookId(Long bookId) {
        LambdaQueryWrapper<BookExcerpt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookExcerpt::getBookId, bookId)
                .orderByAsc(BookExcerpt::getSortOrder)
                .last("LIMIT 1");
        return bookExcerptMapper.selectList(wrapper);
    }

    @Override
    public Map<Long, BookExcerpt> getOnePerBookId(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<BookExcerpt> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(BookExcerpt::getBookId, bookIds)
                .orderByAsc(BookExcerpt::getSortOrder);
        List<BookExcerpt> all = bookExcerptMapper.selectList(wrapper);
        return all.stream()
                .collect(Collectors.toMap(BookExcerpt::getBookId, Function.identity(),
                        (a, b) -> a));
    }
}
