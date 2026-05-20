package com.example.bookstore.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentVO {

    private Long id;

    private Long orderId;

    private String orderNo;

    private String paymentNo;

    private String paymentMethod;

    private String amount;

    private String status;

    private String paidTime;

    private LocalDateTime createTime;
}