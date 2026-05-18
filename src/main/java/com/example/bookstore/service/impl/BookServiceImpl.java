package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookstore.common.Constants;
import com.example.bookstore.common.PageResult;
import com.example.bookstore.dto.BookQueryDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.entity.Review;
import com.example.bookstore.exception.BusinessException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.mapper.CategoryMapper;
import com.example.bookstore.mapper.ReviewMapper;
import com.example.bookstore.service.BookService;
import com.example.bookstore.vo.BookDetailVO;
import com.example.bookstore.vo.BookVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;
    private final ReviewMapper reviewMapper;

    @Override
    public PageResult<BookVO> pageQuery(BookQueryDTO queryDTO) {
        Page<Book> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, Constants.BOOK_STATUS_ON);

        Page<Book> result = bookMapper.selectPage(page, wrapper);
        List<BookVO> voList = result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

        PageResult<BookVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(voList);
        return pageResult;
    }

    @Override
    public PageResult<BookVO> findByCategory(Long categoryId, Integer pageNum, Integer pageSize) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, Constants.BOOK_STATUS_ON)
                .eq(Book::getCategoryId, categoryId);

        Page<Book> result = bookMapper.selectPage(page, wrapper);
        List<BookVO> voList = result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

        PageResult<BookVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(voList);
        return pageResult;
    }

    @Override
    public PageResult<BookVO> search(String keyword, Integer pageNum, Integer pageSize) {
        Page<Book> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Book::getStatus, Constants.BOOK_STATUS_ON)
                .and(w -> w.like(Book::getTitle, keyword)
                        .or()
                        .like(Book::getAuthor, keyword)
                        .or()
                        .like(Book::getIsbn, keyword));

        Page<Book> result = bookMapper.selectPage(page, wrapper);
        List<BookVO> voList = result.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());

        PageResult<BookVO> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setRecords(voList);
        return pageResult;
    }

    @Override
    public BookDetailVO getDetail(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException(1, "书籍不存在");
        }

        BookDetailVO vo = new BookDetailVO();
        vo.setId(book.getId());
        vo.setIsbn(book.getIsbn());
        vo.setTitle(book.getTitle());
        vo.setAuthor(book.getAuthor());
        vo.setPublisher(book.getPublisher());
        vo.setPublishDate(book.getPublishDate());
        vo.setPrice(book.getPrice());
        vo.setStock(book.getStock());
        vo.setCategoryId(book.getCategoryId());
        vo.setDescription(book.getDescription());
        vo.setCoverUrl(book.getCoverUrl());
        vo.setStatus(book.getStatus());

        if (book.getCategoryId() != null) {
            Category category = categoryMapper.selectById(book.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(Review::getBookId, id).eq(Review::getStatus, Constants.REVIEW_SHOW);
        List<Review> reviews = reviewMapper.selectList(reviewWrapper);

        if (!reviews.isEmpty()) {
            double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            vo.setAvgRating(avgRating);
            vo.setReviewCount(reviews.size());
        } else {
            vo.setAvgRating(0.0);
            vo.setReviewCount(0);
        }

        return vo;
    }

    private BookVO convertToVO(Book book) {
        BookVO vo = new BookVO();
        vo.setId(book.getId());
        vo.setIsbn(book.getIsbn());
        vo.setTitle(book.getTitle());
        vo.setAuthor(book.getAuthor());
        vo.setPublisher(book.getPublisher());
        vo.setPrice(book.getPrice());
        vo.setStock(book.getStock());
        vo.setCategoryId(book.getCategoryId());
        vo.setCoverUrl(book.getCoverUrl());
        vo.setStatus(book.getStatus());

        if (book.getCategoryId() != null) {
            Category category = categoryMapper.selectById(book.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        return vo;
    }
}