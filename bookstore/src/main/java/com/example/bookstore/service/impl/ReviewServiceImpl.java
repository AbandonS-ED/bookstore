package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookstore.common.Constants;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.OrderItem;
import com.example.bookstore.entity.Review;
import com.example.bookstore.entity.User;
import com.example.bookstore.exception.BusinessException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.mapper.OrderItemMapper;
import com.example.bookstore.mapper.OrderMapper;
import com.example.bookstore.mapper.ReviewMapper;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.service.ReviewService;
import com.example.bookstore.util.AuthContext;
import com.example.bookstore.vo.ReviewVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final BookMapper bookMapper;
    private final UserMapper userMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderMapper orderMapper;

    @Override
    public void add(Long userId, Review review) {
        Book book = bookMapper.selectById(review.getBookId());
        if (book == null) {
            throw new BusinessException(1, "书籍不存在");
        }

        if (review.getOrderItemId() != null) {
            OrderItem orderItem = orderItemMapper.selectById(review.getOrderItemId());
            if (orderItem == null) {
                throw new BusinessException(1, "订单商品不存在");
            }
            Order order = orderMapper.selectById(orderItem.getOrderId());
            if (order == null || !order.getUserId().equals(userId)) {
                throw new BusinessException(1, "订单不存在");
            }
            if (!"delivered".equals(order.getStatus()) && !"completed".equals(order.getStatus())) {
                throw new BusinessException(1, "订单未完成，无法评价");
            }
            LambdaQueryWrapper<Review> dupWrapper = new LambdaQueryWrapper<>();
            dupWrapper.eq(Review::getOrderItemId, review.getOrderItemId());
            if (reviewMapper.selectCount(dupWrapper) > 0) {
                throw new BusinessException(1, "该商品已评价");
            }
        }

        review.setUserId(userId);
        review.setStatus(Constants.REVIEW_SHOW);
        reviewMapper.insert(review);
    }

    @Override
    public List<ReviewVO> getByBookId(Long bookId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getBookId, bookId)
                .eq(Review::getStatus, Constants.REVIEW_SHOW)
                .orderByDesc(Review::getCreateTime);
        List<Review> reviews = reviewMapper.selectList(wrapper);

        List<Long> userIds = reviews.stream().map(Review::getUserId).distinct().collect(Collectors.toList());
        Map<Long, String> usernames;
        if (!userIds.isEmpty()) {
            usernames = userMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(User::getId, User::getUsername));
        } else {
            usernames = Collections.emptyMap();
        }

        return reviews.stream().map(review -> {
            ReviewVO vo = new ReviewVO();
            vo.setId(review.getId());
            vo.setUserId(review.getUserId());
            vo.setBookId(review.getBookId());
            vo.setRating(review.getRating());
            vo.setContent(review.getContent());
            vo.setCreateTime(review.getCreateTime());

            vo.setUsername(usernames.get(review.getUserId()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ReviewVO> getByUserId(Long userId) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreateTime);
        List<Review> reviews = reviewMapper.selectList(wrapper);

        List<Long> bookIds = reviews.stream().map(Review::getBookId).distinct().collect(Collectors.toList());
        Map<Long, Book> bookMap;
        if (!bookIds.isEmpty()) {
            bookMap = bookMapper.selectBatchIds(bookIds).stream()
                    .collect(Collectors.toMap(Book::getId, b -> b));
        } else {
            bookMap = Collections.emptyMap();
        }

        return reviews.stream().map(review -> {
            ReviewVO vo = new ReviewVO();
            vo.setId(review.getId());
            vo.setUserId(review.getUserId());
            vo.setBookId(review.getBookId());
            vo.setRating(review.getRating());
            vo.setContent(review.getContent());
            vo.setCreateTime(review.getCreateTime());

            Book book = bookMap.get(review.getBookId());
            if (book != null) {
                vo.setBookTitle(book.getTitle());
                vo.setBookCoverUrl(book.getCoverUrl());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId, Long reviewId) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(1, "评论不存在");
        }
        if (!review.getUserId().equals(userId) && !Constants.ROLE_ADMIN.equals(AuthContext.getCurrentUserRole())) {
            throw new BusinessException(1, "无权删除此评论");
        }
        reviewMapper.deleteById(reviewId);
    }

    @Override
    public Map<String, Object> getStats() {
        LambdaQueryWrapper<Review> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(Review::getStatus, Constants.REVIEW_SHOW);
        long totalReviews = reviewMapper.selectCount(totalWrapper);

        LambdaQueryWrapper<Review> goodWrapper = new LambdaQueryWrapper<>();
        goodWrapper.eq(Review::getStatus, Constants.REVIEW_SHOW)
                .ge(Review::getRating, 4);
        long goodReviews = reviewMapper.selectCount(goodWrapper);

        double goodRate = totalReviews > 0 ? (double) goodReviews / totalReviews : 0.0;

        LambdaQueryWrapper<Book> bookWrapper = new LambdaQueryWrapper<>();
        bookWrapper.eq(Book::getStatus, Constants.BOOK_STATUS_ON);
        long totalBooks = bookMapper.selectCount(bookWrapper);

        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getRole, Constants.ROLE_USER)
                .eq(User::getStatus, Constants.STATUS_NORMAL);
        long totalUsers = userMapper.selectCount(userWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalReviews", totalReviews);
        stats.put("goodReviews", goodReviews);
        stats.put("goodRate", goodRate);
        stats.put("totalBooks", totalBooks);
        stats.put("totalUsers", totalUsers);
        return stats;
    }
}