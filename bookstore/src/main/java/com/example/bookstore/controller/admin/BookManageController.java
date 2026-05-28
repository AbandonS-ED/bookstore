package com.example.bookstore.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookstore.common.Result;
import com.example.bookstore.dto.StockAdjustDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.entity.OrderItem;
import com.example.bookstore.entity.Review;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.mapper.CategoryMapper;
import com.example.bookstore.mapper.OrderItemMapper;
import com.example.bookstore.mapper.ReviewMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/book")
@RequiredArgsConstructor
public class BookManageController {

    private final BookMapper bookMapper;
    private final ReviewMapper reviewMapper;
    private final OrderItemMapper orderItemMapper;
    private final CategoryMapper categoryMapper;

    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody Book book) {
        book.setStatus(1);
        bookMapper.insert(book);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody Book book) {
        bookMapper.updateById(book);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
        reviewWrapper.eq(Review::getBookId, id);
        if (reviewMapper.selectCount(reviewWrapper) > 0) {
            return Result.error(1, "该书籍下存在评论，无法删除");
        }

        LambdaQueryWrapper<OrderItem> orderItemWrapper = new LambdaQueryWrapper<>();
        orderItemWrapper.eq(OrderItem::getBookId, id);
        if (orderItemMapper.selectCount(orderItemWrapper) > 0) {
            return Result.error(1, "该书籍存在订单记录，无法删除");
        }

        bookMapper.deleteById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Book book = new Book();
        book.setId(id);
        book.setStatus(status);
        bookMapper.updateById(book);
        return Result.success();
    }

    @PutMapping("/{id}/stock")
    public Result<Void> updateStock(@PathVariable Long id, @Valid @RequestBody StockAdjustDTO dto) {
        Book book = bookMapper.selectById(id);
        if (book == null) {
            return Result.error(1, "书籍不存在");
        }
        bookMapper.updateStock(id, dto.getStock());
        return Result.success();
    }

    @GetMapping("/list")
    public Result<Page<Book>> list(@RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) Long categoryId,
                                    @RequestParam(required = false) Integer status,
                                    @RequestParam(required = false) Boolean discount,
                                    @RequestParam(required = false) String sortBy,
                                    @RequestParam(defaultValue = "1") Integer pageNum,
                                    @RequestParam(defaultValue = "10") Integer pageSize) {
        LambdaQueryWrapper<Book> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Book::getTitle, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Book::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Book::getStatus, status);
        }
        if (Boolean.TRUE.equals(discount)) {
            wrapper.isNotNull(Book::getDiscountPrice)
                    .isNotNull(Book::getDiscountEndTime)
                    .gt(Book::getDiscountEndTime, LocalDateTime.now());
        }
        if ("sales".equals(sortBy)) {
            wrapper.orderByDesc(Book::getSales);
        } else if ("price".equals(sortBy)) {
            wrapper.orderByDesc(Book::getPrice);
        } else if ("stock".equals(sortBy)) {
            wrapper.orderByDesc(Book::getStock);
        } else {
            wrapper.orderByDesc(Book::getCreateTime);
        }
        Page<Book> page = new Page<>(pageNum, pageSize);
        Page<Book> result = bookMapper.selectPage(page, wrapper);

        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            Map<Long, String> categoryMap = categoryMapper.selectList(null).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
            result.getRecords().forEach(book -> {
                if (book.getCategoryId() != null) {
                    book.setCategoryName(categoryMap.get(book.getCategoryId()));
                }
            });
        }

        return Result.success(result);
    }
}