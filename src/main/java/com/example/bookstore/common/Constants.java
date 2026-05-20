package com.example.bookstore.common;

public interface Constants {

    // ========== 角色 ==========
    String ROLE_USER = "user";
    String ROLE_ADMIN = "admin";

    // ========== 用户状态 ==========
    Integer STATUS_NORMAL = 1;
    Integer STATUS_DISABLED = 0;

    // ========== 书籍状态 ==========
    Integer BOOK_STATUS_ON = 1;   // 上架
    Integer BOOK_STATUS_OFF = 0;  // 下架

    // ========== 地址默认状态 ==========
    Integer ADDRESS_DEFAULT = 1;    // 默认地址
    Integer ADDRESS_NOT_DEFAULT = 0;

    // ========== 评论状态 ==========
    Integer REVIEW_SHOW = 1;   // 显示
    Integer REVIEW_HIDE = 0;   // 隐藏

    // ========== 订单状态 ==========
    String ORDER_STATUS_CREATED = "created";       // 已创建（待支付）
    String ORDER_STATUS_PAYING = "paying";         // 支付中（待支付）
    String ORDER_STATUS_PENDING = "pending";       // 待支付（兼容旧状态）
    String ORDER_STATUS_PAID = "paid";             // 已支付
    String ORDER_STATUS_SHIPPED = "shipped";       // 已发货
    String ORDER_STATUS_DELIVERED = "delivered";   // 已收货
    String ORDER_STATUS_COMPLETED = "completed";   // 已完成
    String ORDER_STATUS_CANCELLED = "cancelled";   // 已取消
    String ORDER_STATUS_EXPIRED = "expired";       // 已过期
    String ORDER_STATUS_REFUNDED = "refunded";     // 已退款

    // ========== 支付状态 ==========
    String PAY_STATUS_UNPAID = "unpaid";    // 未支付
    String PAY_STATUS_PAID = "paid";       // 已支付
    String PAY_STATUS_REFUNDED = "refunded"; // 已退款
    String PAYMENT_STATUS_PENDING = "pending";   // 待支付
    String PAYMENT_STATUS_SUCCESS = "success";   // 支付成功
    String PAYMENT_STATUS_FAILED = "failed";     // 支付失败
    String PAYMENT_STATUS_REFUNDED = "refunded"; // 已退款

    // ========== 支付方式 ==========
    String PAYMENT_METHOD_ALIPAY = "alipay";   // 支付宝
    String PAYMENT_METHOD_WECHAT = "wechat";  // 微信支付
    String PAYMENT_METHOD_CARD = "card";      // 银行卡

    // ========== 订单超时时间（分钟）==========
    int ORDER_EXPIRE_MINUTES = 30;

    // ========== 订单状态流转 ==========
    // created → paying → paid → shipped → delivered → completed
    //    ↓           ↓
    //  expired   cancelled（从 created/paying 可取消）
    // shipped → delivered → completed（确认收货）
    // paid/shipped → refunded（退款）
}