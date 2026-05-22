package com.example.bookstore.service;

import com.example.bookstore.dto.BookQueryDTO;
import com.example.bookstore.vo.BookDetailVO;
import com.example.bookstore.vo.BookVO;
import com.example.bookstore.common.PageResult;

import java.math.BigDecimal;
import java.util.List;

public interface BookService {

    PageResult<BookVO> pageQuery(BookQueryDTO queryDTO);

    PageResult<BookVO> findByCategory(Long categoryId, Integer pageNum, Integer pageSize, String sortBy,
                                      BigDecimal minPrice, BigDecimal maxPrice, Integer minRating, String timeRange);

    PageResult<BookVO> search(String keyword, Integer pageNum, Integer pageSize);

    List<BookVO> getRanking(String type, String period);

    BookDetailVO getDetail(Long id, Long userId);

    BookDetailVO getDetail(Long id);
}