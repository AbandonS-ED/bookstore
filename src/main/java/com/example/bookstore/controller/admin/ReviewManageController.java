package com.example.bookstore.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookstore.common.Constants;
import com.example.bookstore.common.Result;
import com.example.bookstore.entity.Review;
import com.example.bookstore.mapper.ReviewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/review")
@RequiredArgsConstructor
public class ReviewManageController {

    private final ReviewMapper reviewMapper;

    @GetMapping("/list")
    public Result<Page<Review>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Review::getCreateTime);
        Page<Review> result = reviewMapper.selectPage(page, wrapper);
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