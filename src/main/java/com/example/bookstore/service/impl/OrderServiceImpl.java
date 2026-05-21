package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookstore.common.Constants;
import com.example.bookstore.dto.OrderCreateDTO;
import com.example.bookstore.dto.OrderQueryDTO;
import com.example.bookstore.entity.*;
import com.example.bookstore.exception.BusinessException;
import com.example.bookstore.mapper.*;
import com.example.bookstore.service.OrderService;
import com.example.bookstore.util.OrderNoGenerator;
import com.example.bookstore.vo.OrderItemVO;
import com.example.bookstore.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final BookMapper bookMapper;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public OrderVO create(Long userId, OrderCreateDTO orderCreateDTO) {
        Long addressId = Long.parseLong(orderCreateDTO.getAddressId());
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(1, "收货地址不存在");
        }

        List<Cart> cartItems = new ArrayList<>();
        Map<Long, Book> bookCache = new HashMap<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        if (orderCreateDTO.getCartItemIds() != null && !orderCreateDTO.getCartItemIds().isEmpty()) {
            for (String cartIdStr : orderCreateDTO.getCartItemIds()) {
                Long cartId = Long.parseLong(cartIdStr);
                Cart cart = cartMapper.selectById(cartId);
                if (cart == null || !cart.getUserId().equals(userId)) {
                    throw new BusinessException(1, "购物车记录不存在");
                }
                Book book = bookCache.computeIfAbsent(cart.getBookId(), bookMapper::selectById);
                if (book == null) {
                    throw new BusinessException(1, "书籍不存在");
                }
                if (book.getStatus().equals(Constants.BOOK_STATUS_OFF)) {
                    throw new BusinessException(1, "书籍[" + book.getTitle() + "]已下架");
                }
                if (book.getStock() < cart.getQuantity()) {
                    throw new BusinessException(1, "书籍[" + book.getTitle() + "]库存不足");
                }
                cartItems.add(cart);
            }
        } else {
            throw new BusinessException(1, "请选择要购买的商品");
        }

        Order order = new Order();
        order.setOrderNo(OrderNoGenerator.generate());
        order.setUserId(userId);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setStatus(Constants.ORDER_STATUS_CREATED);
        order.setPayStatus(Constants.PAY_STATUS_UNPAID);
        order.setExpireTime(LocalDateTime.now().plusMinutes(Constants.ORDER_EXPIRE_MINUTES).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        order.setAddressId(address.getId());
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetailAddress());
        order.setRemark(orderCreateDTO.getRemark());
        orderMapper.insert(order);

        for (Cart cart : cartItems) {
            Book book = bookCache.get(cart.getBookId());

            int affected = bookMapper.decreaseStock(book.getId(), cart.getQuantity());
            if (affected == 0) {
                throw new BusinessException(1, "书籍[" + book.getTitle() + "]库存不足");
            }

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setBookId(book.getId());
            item.setBookTitle(book.getTitle());
            item.setBookAuthor(book.getAuthor());
            item.setCoverUrl(book.getCoverUrl());
            item.setPrice(book.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setSubtotal(book.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            orderItemMapper.insert(item);

            cartMapper.deleteById(cart.getId());

            totalAmount = totalAmount.add(item.getSubtotal());
        }

        order.setTotalAmount(totalAmount);
        orderMapper.updateById(order);

        return getOrderVO(order);
    }

    @Override
    @Transactional
    public void pay(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(1, "订单不存在");
        }
        if (!Constants.ORDER_STATUS_PAYING.equals(order.getStatus())) {
            throw new BusinessException(1, "订单状态不允许支付");
        }

        order.setStatus(Constants.ORDER_STATUS_PAID);
        order.setPayStatus(Constants.PAY_STATUS_PAID);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancel(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(1, "订单不存在");
        }
        if (!Constants.ORDER_STATUS_CREATED.equals(order.getStatus()) && !Constants.ORDER_STATUS_PAYING.equals(order.getStatus())) {
            throw new BusinessException(1, "订单状态不允许取消");
        }

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        for (OrderItem item : items) {
            Book book = bookMapper.selectById(item.getBookId());
            if (book != null) {
                book.setStock(book.getStock() + item.getQuantity());
                bookMapper.updateById(book);
            }
        }

        order.setStatus(Constants.ORDER_STATUS_CANCELLED);
        orderMapper.updateById(order);
    }

    @Override
    public void confirm(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(1, "订单不存在");
        }
        if (!Constants.ORDER_STATUS_DELIVERED.equals(order.getStatus())) {
            throw new BusinessException(1, "订单状态不允许确认收货");
        }

        order.setStatus(Constants.ORDER_STATUS_COMPLETED);
        orderMapper.updateById(order);
    }

    @Override
    public List<OrderVO> getList(Long userId, OrderQueryDTO queryDTO) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);

        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(Order::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<Order> result = orderMapper.selectPage(page, wrapper);

        return result.getRecords().stream().map(this::getOrderVO).collect(Collectors.toList());
    }

    @Override
    public OrderVO getDetail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(1, "订单不存在");
        }
        return getOrderVO(order);
    }

    private OrderVO getOrderVO(Order order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setRemark(order.getRemark());
        vo.setPaymentId(order.getPaymentId());
        vo.setPayTime(order.getPayTime());
        vo.setExpressNo(order.getExpressNo());
        vo.setExpireTime(order.getExpireTime());
        vo.setCreateTime(order.getCreateTime());

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, order.getId());
        List<OrderItem> items = orderItemMapper.selectList(itemWrapper);

        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setBookId(item.getBookId());
            itemVO.setBookTitle(item.getBookTitle());
            itemVO.setBookAuthor(item.getBookAuthor());
            itemVO.setCoverUrl(item.getCoverUrl());
            itemVO.setPrice(item.getPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setSubtotal(item.getSubtotal());
            return itemVO;
        }).collect(Collectors.toList());

        vo.setItems(itemVOs);
        return vo;
    }
}