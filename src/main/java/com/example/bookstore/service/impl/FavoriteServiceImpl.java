package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookstore.common.Constants;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.entity.Favorite;
import com.example.bookstore.entity.Review;
import com.example.bookstore.exception.BusinessException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.mapper.CategoryMapper;
import com.example.bookstore.mapper.FavoriteMapper;
import com.example.bookstore.mapper.ReviewMapper;
import com.example.bookstore.service.FavoriteService;
import com.example.bookstore.vo.FavoriteVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public void add(Long userId, Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BusinessException(1, "书籍不存在");
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getBookId, bookId);
        if (favoriteMapper.selectCount(wrapper) > 0) {
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setBookId(bookId);
        favorite.setFavoritedPrice(book.getPrice());
        favoriteMapper.insert(favorite);
        bookMapper.increaseFavoritedCount(bookId);
    }

    @Override
    @Transactional
    public void remove(Long userId, Long bookId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getBookId, bookId);
        int deleted = favoriteMapper.delete(wrapper);
        if (deleted > 0) {
            bookMapper.decreaseFavoritedCount(bookId);
        }
    }

    @Override
    public List<FavoriteVO> list(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).orderByDesc(Favorite::getCreateTime);
        List<Favorite> favorites = favoriteMapper.selectList(wrapper);

        if (favorites.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> bookIds = favorites.stream().map(Favorite::getBookId).collect(Collectors.toSet());
        Map<Long, Book> bookMap = bookMapper.selectBatchIds(bookIds).stream()
                .collect(Collectors.toMap(Book::getId, b -> b));

        Map<Long, double[]> reviewStats = reviewMapper.selectList(
                new LambdaQueryWrapper<Review>()
                        .in(Review::getBookId, bookIds)
                        .eq(Review::getStatus, Constants.REVIEW_SHOW)
        ).stream().collect(Collectors.groupingBy(
                Review::getBookId,
                Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            double avg = list.stream().mapToInt(Review::getRating).average().orElse(0.0);
                            return new double[]{avg, list.size()};
                        }
                )
        ));

        Set<Long> categoryIds = bookMap.values().stream()
                .map(Book::getCategoryId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNames;
        if (!categoryIds.isEmpty()) {
            categoryNames = categoryMapper.selectBatchIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
        } else {
            categoryNames = Collections.emptyMap();
        }

        return favorites.stream().map(fav -> {
            Book book = bookMap.get(fav.getBookId());
            if (book == null) return null;

            FavoriteVO vo = new FavoriteVO();
            vo.setId(fav.getId());
            vo.setBookId(book.getId());
            vo.setTitle(book.getTitle());
            vo.setAuthor(book.getAuthor());
            vo.setPrice(book.getPrice());
            vo.setOrigPrice(book.getOrigPrice());
            vo.setFavoritedPrice(fav.getFavoritedPrice());
            vo.setCoverUrl(book.getCoverUrl());
            vo.setStock(book.getStock());
            vo.setSales(book.getSales());
            vo.setCategoryName(categoryNames.get(book.getCategoryId()));
            vo.setCreateTime(fav.getCreateTime());

            double[] stats = reviewStats.get(book.getId());
            vo.setAvgRating(stats != null ? stats[0] : 0.0);

            return vo;
        }).filter(vo -> vo != null).collect(Collectors.toList());
    }

    @Override
    public boolean isFavorited(Long userId, Long bookId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId).eq(Favorite::getBookId, bookId);
        return favoriteMapper.selectCount(wrapper) > 0;
    }

    @Override
    public Set<String> getFavoritedBookIds(Long userId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId);
        return favoriteMapper.selectList(wrapper).stream()
                .map(Favorite::getBookId)
                .map(String::valueOf)
                .collect(Collectors.toSet());
    }
}
