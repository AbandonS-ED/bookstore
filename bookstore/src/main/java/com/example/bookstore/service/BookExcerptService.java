package com.example.bookstore.service;

import com.example.bookstore.entity.BookExcerpt;

import java.util.List;
import java.util.Map;

public interface BookExcerptService {

    List<BookExcerpt> getByBookId(Long bookId);

    Map<Long, BookExcerpt> getOnePerBookId(List<Long> bookIds);
}
