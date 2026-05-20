package com.example.bookstore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;

    private String orderNo;

    private String paymentNo;

    private String paymentMethod;

    private String amount;

    private String status;

    private String paidTime;

    private LocalDateTime createTime;
}