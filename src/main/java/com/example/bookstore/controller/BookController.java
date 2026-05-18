package com.example.bookstore.controller;

import com.example.bookstore.common.PageResult;
import com.example.bookstore.common.Result;
import com.example.bookstore.dto.BookQueryDTO;
import com.example.bookstore.service.BookService;
import com.example.bookstore.vo.BookDetailVO;
import com.example.bookstore.vo.BookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
        BookDetailVO detail = bookService.getDetail(id);
        return Result.success(detail);
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
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResult<BookVO> result = bookService.findByCategory(categoryId, pageNum, pageSize);
        return Result.success(result);
    }
}