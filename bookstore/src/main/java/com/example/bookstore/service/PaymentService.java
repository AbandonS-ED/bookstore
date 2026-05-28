package com.example.bookstore.service;

import com.example.bookstore.vo.PaymentVO;

public interface PaymentService {

    PaymentVO applyPay(Long userId, Long orderId, String paymentMethod);

    void callback(String paymentNo, String status, String paidTime);

    PaymentVO getPayment(Long userId, Long orderId);
}