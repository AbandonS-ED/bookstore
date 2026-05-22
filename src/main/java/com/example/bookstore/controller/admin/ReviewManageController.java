package com.example.bookstore.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookstore.common.Constants;
import com.example.bookstore.common.Result;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Review;
import com.example.bookstore.entity.User;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.mapper.ReviewMapper;
import com.example.bookstore.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/review")
@RequiredArgsConstructor
public class ReviewManageController {

    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;

    @GetMapping("/list")
    public Result<Page<Review>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Review::getCreateTime);
        Page<Review> result = reviewMapper.selectPage(page, wrapper);

        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            Map<Long, String> userMap = userMapper.selectList(null).stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
            Map<Long, String> bookMap = bookMapper.selectList(null).stream()
                .collect(Collectors.toMap(Book::getId, Book::getTitle));
            result.getRecords().forEach(review -> {
                if (review.getUserId() != null) {
                    review.setUsername(userMap.get(review.getUserId()));
                }
                if (review.getBookId() != null) {
                    review.setBookTitle(bookMap.get(review.getBookId()));
                }
            });
        }

        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/{id}/hide")
    public Result<Void> hide(@PathVariable Long id) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            return Result.error(1, "评论不存在");
        }
        review.setStatus(Constants.REVIEW_HIDE);
        reviewMapper.updateById(review);
        return Result.success();
    }
}