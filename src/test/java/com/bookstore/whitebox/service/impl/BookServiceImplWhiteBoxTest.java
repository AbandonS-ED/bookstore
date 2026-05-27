package com.bookstore.whitebox.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bookstore.common.Constants;
import com.bookstore.dto.BookQueryDTO;
import com.bookstore.entity.Book;
import com.bookstore.mapper.BookMapper;
import com.bookstore.service.BookService;
import com.bookstore.vo.BookVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-007 白盒测试：BookServiceImpl.pageQuery() SQL 等价转换验证
 *
 * 覆盖路径：
 *   BookController.page() → BookService.pageQuery() → BookMapper.selectPage()
 *
 * 验证点：
 *   1. status=1（上线）筛选条件正确
 *   2. keyword 模糊匹配 title OR author
 *   3. 分页参数 pageSize/current 正确传递
 *   4. sortBy 排序分支覆盖
 */
@SpringBootTest
@Transactional
class BookServiceImplWhiteBoxTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookMapper bookMapper;

    @Test
    void pageQuery_defaultStatus_shouldFilterByStatusOn() {
        BookQueryDTO query = new BookQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);

        var result = bookService.pageQuery(query);

        assertNotNull(result);
        assertNotNull(result.getRecords());
        assertTrue(result.getTotal() >= 0);

        for (BookVO vo : result.getRecords()) {
            assertEquals(Constants.BOOK_STATUS_ON, vo.getStatus());
        }
    }

    @Test
    void pageQuery_withKeyword_shouldSearchTitleAndAuthor() {
        Book book = new Book();
        book.setTitle("测试书籍_白盒_T007");
        book.setAuthor("测试作者");
        book.setStatus(Constants.BOOK_STATUS_ON);
        book.setPrice(new BigDecimal("99.00"));
        book.setStock(100);
        book.setSales(0);
        bookMapper.insert(book);

        BookQueryDTO query = new BookQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setKeyword("测试书籍_白盒_T007");

        var result = bookService.pageQuery(query);

        assertTrue(result.getTotal() >= 1);
        assertTrue(result.getRecords().stream()
                .anyMatch(b -> b.getTitle().contains("测试书籍_白盒_T007")));
    }

    @Test
    void pageQuery_withCategoryId_shouldFilterCorrectly() {
        BookQueryDTO query = new BookQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setCategoryId("1");

        var result = bookService.pageQuery(query);

        for (BookVO vo : result.getRecords()) {
            assertEquals(Long.parseLong("1"), vo.getCategoryId());
        }
    }

    @Test
    void pageQuery_pagination_shouldRespectPageSize() {
        BookQueryDTO query1 = new BookQueryDTO();
        query1.setPageNum(1);
        query1.setPageSize(5);

        BookQueryDTO query2 = new BookQueryDTO();
        query2.setPageNum(2);
        query2.setPageSize(5);

        var result1 = bookService.pageQuery(query1);
        var result2 = bookService.pageQuery(query2);

        assertEquals(5, result1.getRecords().size());
        assertEquals(5, result2.getRecords().size());
        assertNotEquals(result1.getRecords().get(0).getId(), result2.getRecords().get(0).getId());
    }

    @Test
    void sortBy_salesDesc_shouldOrderBySales() {
        BookQueryDTO query = new BookQueryDTO();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setSortBy("sales_desc");

        var result = bookService.pageQuery(query);

        if (result.getRecords().size() >= 2) {
            for (int i = 0; i < result.getRecords().size() - 1; i++) {
                assertTrue(result.getRecords().get(i).getSales()
                        >= result.getRecords().get(i + 1).getSales());
            }
        }
    }
}