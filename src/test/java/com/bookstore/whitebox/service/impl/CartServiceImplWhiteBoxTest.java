package com.bookstore.whitebox.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bookstore.common.Constants;
import com.bookstore.dto.CartAddDTO;
import com.bookstore.entity.Book;
import com.bookstore.entity.Cart;
import com.bookstore.exception.BusinessException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.mapper.CartMapper;
import com.bookstore.service.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-009 白盒测试：购物车同一 userId+bookId 组合执行 UPDATE 而非 INSERT
 *
 * 覆盖路径：
 *   CartServiceImpl.add() → CartMapper.selectOne() → cartMapper.updateById() / insert()
 */
@SpringBootTest
@Transactional
class CartServiceImplWhiteBoxTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private BookMapper bookMapper;

    private Long testUserId = 99999L;

    private Book createTestBook() {
        Book book = new Book();
        book.setTitle("T009测试书籍");
        book.setAuthor("测试作者");
        book.setStatus(Constants.BOOK_STATUS_ON);
        book.setPrice(new BigDecimal("50.00"));
        book.setStock(100);
        book.setSales(0);
        bookMapper.insert(book);
        return book;
    }

    @Test
    void add_sameUserSameBook_shouldUpdateQuantity() {
        Book book = createTestBook();

        CartAddDTO dto1 = new CartAddDTO();
        dto1.setBookId(book.getId().toString());
        dto1.setQuantity(3);
        cartService.add(testUserId, dto1);

        CartAddDTO dto2 = new CartAddDTO();
        dto2.setBookId(book.getId().toString());
        dto2.setQuantity(2);
        cartService.add(testUserId, dto2);

        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, testUserId).eq(Cart::getBookId, book.getId());
        Cart cart = cartMapper.selectOne(wrapper);

        assertNotNull(cart);
        assertEquals(5, cart.getQuantity());
    }

    @Test
    void add_differentUserSameBook_shouldInsertNewRecord() {
        Book book = createTestBook();

        CartAddDTO dto1 = new CartAddDTO();
        dto1.setBookId(book.getId().toString());
        dto1.setQuantity(1);
        cartService.add(testUserId, dto1);

        CartAddDTO dto2 = new CartAddDTO();
        dto2.setBookId(book.getId().toString());
        dto2.setQuantity(4);
        cartService.add(testUserId + 1, dto2);

        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getBookId, book.getId());
        long count = cartMapper.selectCount(wrapper);

        assertEquals(2, count);
    }

    @Test
    void add_bookNotExist_shouldThrowException() {
        CartAddDTO dto = new CartAddDTO();
        dto.setBookId("999999999");
        dto.setQuantity(1);

        assertThrows(BusinessException.class, () -> cartService.add(testUserId, dto));
    }

    @Test
    void add_bookOffSale_shouldThrowException() {
        Book book = createTestBook();
        book.setStatus(Constants.BOOK_STATUS_OFF);
        bookMapper.updateById(book);

        CartAddDTO dto = new CartAddDTO();
        dto.setBookId(book.getId().toString());
        dto.setQuantity(1);

        assertThrows(BusinessException.class, () -> cartService.add(testUserId, dto));
    }

    @Test
    void add_stockInsufficient_shouldThrowException() {
        Book book = createTestBook();
        book.setStock(1);
        bookMapper.updateById(book);

        CartAddDTO dto = new CartAddDTO();
        dto.setBookId(book.getId().toString());
        dto.setQuantity(10);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> cartService.add(testUserId, dto));
        assertEquals("库存不足", ex.getMessage());
    }
}