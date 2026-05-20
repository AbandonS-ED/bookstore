package com.example.bookstore.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.bookstore.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("`order`")
public class Order extends BaseEntity {

    private String orderNo;

    private Long userId;

    private BigDecimal totalAmount;

    private String status;

    private String payStatus;

    private Long addressId;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private String remark;

    private Long paymentId;

    private String payTime;

    private String expressNo;

    private String expireTime;
}