package com.example.bookstore.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookstore.common.PageResult;
import com.example.bookstore.dto.BookQueryDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Category;
import com.example.bookstore.entity.Review;
import com.example.bookstore.mapper.AuthorMapper;
import com.example.bookstore.mapper.BookChapterMapper;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.mapper.CategoryMapper;
import com.example.bookstore.mapper.ReviewMapper;
import com.example.bookstore.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookMapper bookMapper;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private AuthorMapper authorMapper;

    @Mock
    private BookChapterMapper bookChapterMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("测试书籍");
        testBook.setAuthor("测试作者");
        testBook.setPrice(new BigDecimal("99.00"));
        testBook.setStock(100);
        testBook.setStatus(1);
    }

    @Test
    void pageQuery_returnsBookList() {
        Page<Book> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testBook));
        page.setTotal(1);

        when(bookMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        BookQueryDTO queryDTO = new BookQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);

        PageResult<?> result = bookService.pageQuery(queryDTO);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    void findByCategory_filtersByCategoryId() {
        testBook.setCategoryId(2L);
        Page<Book> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testBook));
        page.setTotal(1);

        when(bookMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<?> result = bookService.findByCategory(2L, 1, 10, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    void getDetail_whenBookExists_returnsBookDetail() {
        testBook.setCategoryId(1L);
        when(bookMapper.selectById(1L)).thenReturn(testBook);

        Category category = new Category();
        category.setId(1L);
        category.setName("文学");
        when(categoryMapper.selectById(1L)).thenReturn(category);

        when(reviewMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        when(bookChapterMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        var result = bookService.getDetail(1L);

        assertNotNull(result);
        assertEquals("测试书籍", result.getTitle());
        assertEquals("文学", result.getCategoryName());
    }

    @Test
    void getDetail_whenBookNotExists_throwsException() {
        when(bookMapper.selectById(99L)).thenReturn(null);

        assertThrows(Exception.class, () -> bookService.getDetail(99L));
    }

    @Test
    void search_withKeyword_returnsMatchingBooks() {
        Page<Book> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testBook));
        page.setTotal(1);

        when(bookMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        PageResult<?> result = bookService.search("测试", 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }
}