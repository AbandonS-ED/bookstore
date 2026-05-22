package com.example.bookstore.controller;

import com.example.bookstore.common.PageResult;
import com.example.bookstore.common.Result;
import com.example.bookstore.dto.BookQueryDTO;
import com.example.bookstore.service.BookService;
import com.example.bookstore.util.AuthContext;
import com.example.bookstore.vo.BookDetailVO;
import com.example.bookstore.vo.BookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping("/list")
    public Result<PageResult<BookVO>> list(BookQueryDTO queryDTO) {
        PageResult<BookVO> result = bookService.pageQuery(queryDTO);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<BookDetailVO> detail(@PathVariable Long id) {
        Long userId = AuthContext.getCurrentUserId();
        BookDetailVO detail = bookService.getDetail(id, userId);
        return Result.success(detail);
    }

    @GetMapping("/ranking")
    public Result<List<BookVO>> ranking(
            @RequestParam(defaultValue = "sales") String type,
            @RequestParam(defaultValue = "all") String period) {
        List<BookVO> result = bookService.getRanking(type, period);
        return Result.success(result);
    }

    @GetMapping("/search")
    public Result<PageResult<BookVO>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<BookVO> result = bookService.search(keyword, pageNum, pageSize);
        return Result.success(result);
    }

    @GetMapping("/category/{categoryId}")
    public Result<PageResult<BookVO>> category(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) String timeRange) {
        PageResult<BookVO> result = bookService.findByCategory(categoryId, pageNum, pageSize, sortBy,
                minPrice, maxPrice, minRating, timeRange);
        return Result.success(result);
    }
}