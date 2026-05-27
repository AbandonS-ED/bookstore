package com.bookstore.whitebox.service.impl;

import com.bookstore.common.Constants;
import com.bookstore.dto.OrderCreateDTO;
import com.bookstore.entity.*;
import com.bookstore.exception.BusinessException;
import com.bookstore.mapper.*;
import com.bookstore.service.OrderService;
import com.bookstore.vo.OrderVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T-010 白盒测试：订单创建时库存原子扣减
 * T-021 白盒测试：订单状态机流转
 *
 * 覆盖路径：
 *   OrderService.create() → BookMapper.decreaseStock() [WHERE stock >= quantity]
 *   OrderService.pay() → BookMapper.increaseSales()
 *   OrderService.cancel() → 库存回滚（不回滚 sales）
 *   OrderService.confirm()/applyRefund()/applyAfterSale() → 状态校验
 */
@SpringBootTest
@Transactional
class OrderServiceImplWhiteBoxTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private CartMapper cartMapper;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private AddressMapper addressMapper;

    private Long testUserId = 99998L;

    private Book createTestBook(int stock) {
        Book book = new Book();
        book.setTitle("T010测试书籍");
        book.setAuthor("测试作者");
        book.setStatus(Constants.BOOK_STATUS_ON);
        book.setPrice(new BigDecimal("39.90"));
        book.setStock(stock);
        book.setSales(0);
        bookMapper.insert(book);
        return book;
    }

    private Address createTestAddress() {
        Address addr = new Address();
        addr.setUserId(testUserId);
        addr.setReceiverName("测试收货人");
        addr.setPhone("13800138000");
        addr.setProvince("广东省");
        addr.setCity("深圳市");
        addr.setDistrict("南山区");
        addr.setDetailAddress("科技路1号");
        addr.setIsDefault(1);
        addressMapper.insert(addr);
        return addr;
    }

    private Cart createCartWithBook(Long userId, Book book, int quantity) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setBookId(book.getId());
        cart.setQuantity(quantity);
        cartMapper.insert(cart);
        return cart;
    }

    @Test
    void create_shouldDecreaseStockAtomically() {
        Book book = createTestBook(10);
        Address addr = createTestAddress();
        Cart cart = createCartWithBook(testUserId, book, 3);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addr.getId().toString());
        dto.setCartItemIds(List.of(cart.getId().toString()));

        OrderVO order = orderService.create(testUserId, dto);

        assertNotNull(order);
        Book updated = bookMapper.selectById(book.getId());
        assertEquals(7, updated.getStock());
    }

    @Test
    void create_insufficientStock_shouldThrowExceptionAndNotDecrement() {
        Book book = createTestBook(2);
        Address addr = createTestAddress();
        Cart cart = createCartWithBook(testUserId, book, 5);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addr.getId().toString());
        dto.setCartItemIds(List.of(cart.getId().toString()));

        assertThrows(BusinessException.class, () -> orderService.create(testUserId, dto));

        Book unchanged = bookMapper.selectById(book.getId());
        assertEquals(2, unchanged.getStock());
    }

    @Test
    void cancel_shouldRollbackStock() {
        Book book = createTestBook(10);
        Address addr = createTestAddress();
        Cart cart = createCartWithBook(testUserId, book, 3);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addr.getId().toString());
        dto.setCartItemIds(List.of(cart.getId().toString()));
        OrderVO order = orderService.create(testUserId, dto);
        assertEquals(7, bookMapper.selectById(book.getId()).getStock());

        orderService.cancel(testUserId, order.getId());

        Book restored = bookMapper.selectById(book.getId());
        assertEquals(10, restored.getStock());
        assertEquals(Constants.ORDER_STATUS_CANCELLED,
                orderMapper.selectById(order.getId()).getStatus());
    }

    @Test
    void pay_shouldIncreaseSales() {
        Book book = createTestBook(10);
        book.setSales(0);
        bookMapper.updateById(book);
        Address addr = createTestAddress();
        Cart cart = createCartWithBook(testUserId, book, 2);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addr.getId().toString());
        dto.setCartItemIds(List.of(cart.getId().toString()));
        OrderVO order = orderService.create(testUserId, dto);

        Order orderEntity = orderMapper.selectById(order.getId());
        orderEntity.setStatus(Constants.ORDER_STATUS_PAYING);
        orderMapper.updateById(orderEntity);

        orderService.pay(testUserId, order.getId());

        Book updated = bookMapper.selectById(book.getId());
        assertEquals(2, updated.getSales());
    }

    @Test
    void confirm_shouldChangeStatusToCompleted() {
        Book book = createTestBook(10);
        Address addr = createTestAddress();
        Cart cart = createCartWithBook(testUserId, book, 1);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addr.getId().toString());
        dto.setCartItemIds(List.of(cart.getId().toString()));
        OrderVO order = orderService.create(testUserId, dto);

        Order o = orderMapper.selectById(order.getId());
        o.setStatus(Constants.ORDER_STATUS_SHIPPED);
        orderMapper.updateById(o);

        orderService.confirm(testUserId, order.getId());

        assertEquals(Constants.ORDER_STATUS_COMPLETED,
                orderMapper.selectById(order.getId()).getStatus());
    }

    @Test
    void applyRefund_shouldOnlyAllowPaidOrShipped() {
        Book book = createTestBook(10);
        Address addr = createTestAddress();
        Cart cart = createCartWithBook(testUserId, book, 1);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addr.getId().toString());
        dto.setCartItemIds(List.of(cart.getId().toString()));
        OrderVO order = orderService.create(testUserId, dto);

        assertThrows(BusinessException.class,
                () -> orderService.applyRefund(testUserId, order.getId()));
    }

    @Test
    void applyAfterSale_shouldOnlyAllowDeliveredOrCompleted() {
        Book book = createTestBook(10);
        Address addr = createTestAddress();
        Cart cart = createCartWithBook(testUserId, book, 1);

        OrderCreateDTO dto = new OrderCreateDTO();
        dto.setAddressId(addr.getId().toString());
        dto.setCartItemIds(List.of(cart.getId().toString()));
        OrderVO order = orderService.create(testUserId, dto);

        assertThrows(BusinessException.class,
                () -> orderService.applyAfterSale(testUserId, order.getId()));
    }
}