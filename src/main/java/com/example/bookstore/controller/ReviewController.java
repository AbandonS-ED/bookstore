package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.entity.Review;
import com.example.bookstore.service.ReviewService;
import com.example.bookstore.util.AuthContext;
import com.example.bookstore.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public Result<Void> add(@RequestBody Review review) {
        reviewService.add(AuthContext.getCurrentUserId(), review);
        return Result.success();
    }

    @GetMapping("/book/{bookId}")
    public Result<List<ReviewVO>> bookReviews(@PathVariable Long bookId) {
        List<ReviewVO> reviews = reviewService.getByBookId(bookId);
        return Result.success(reviews);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(reviewService.getStats());
    }

    @GetMapping("/my")
    public Result<List<ReviewVO>> myReviews() {
        List<ReviewVO> reviews = reviewService.getByUserId(AuthContext.getCurrentUserId());
        return Result.success(reviews);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.delete(AuthContext.getCurrentUserId(), id);
        return Result.success();
    }
}