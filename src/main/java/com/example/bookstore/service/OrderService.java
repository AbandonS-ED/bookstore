package com.example.bookstore.service;

import com.example.bookstore.dto.OrderCreateDTO;
import com.example.bookstore.dto.OrderQueryDTO;
import com.example.bookstore.vo.OrderVO;

import java.util.List;

public interface OrderService {

    OrderVO create(Long userId, OrderCreateDTO orderCreateDTO);

    void pay(Long userId, Long orderId);

    void cancel(Long userId, Long orderId);

    void confirm(Long userId, Long orderId);

    void applyRefund(Long userId, Long orderId);

    void applyAfterSale(Long userId, Long orderId);

    List<OrderVO> getList(Long userId, OrderQueryDTO queryDTO);

    OrderVO getDetail(Long userId, Long orderId);
}