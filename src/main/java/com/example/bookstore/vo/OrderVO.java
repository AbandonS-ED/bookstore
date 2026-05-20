package com.example.bookstore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String orderNo;

    private BigDecimal totalAmount;

    private String status;

    private String payStatus;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String remark;

    private Long paymentId;

    private String payTime;

    private String expressNo;

    private String expireTime;

    private LocalDateTime createTime;

    private List<OrderItemVO> items;
}