package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.entity.Review;
import com.example.bookstore.service.ReviewService;
import com.example.bookstore.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public Result<Void> add(@RequestParam Long userId, @RequestBody Review review) {
        reviewService.add(userId, review);
        return Result.success();
    }

    @GetMapping("/book/{bookId}")
    public Result<List<ReviewVO>> bookReviews(@PathVariable Long bookId) {
        List<ReviewVO> reviews = reviewService.getByBookId(bookId);
        return Result.success(reviews);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestParam Long userId, @PathVariable Long id) {
        reviewService.delete(userId, id);
        return Result.success();
    }
}