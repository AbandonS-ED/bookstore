package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookstore.common.Constants;
import com.example.bookstore.entity.Order;
import com.example.bookstore.entity.Payment;
import com.example.bookstore.exception.BusinessException;
import com.example.bookstore.mapper.OrderMapper;
import com.example.bookstore.mapper.PaymentMapper;
import com.example.bookstore.service.PaymentService;
import com.example.bookstore.util.OrderNoGenerator;
import com.example.bookstore.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public PaymentVO applyPay(Long userId, Long orderId, String paymentMethod) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(1, "订单不存在");
        }
        if (!Constants.ORDER_STATUS_CREATED.equals(order.getStatus()) && !Constants.ORDER_STATUS_PENDING.equals(order.getStatus())) {
            throw new BusinessException(1, "订单状态不允许支付");
        }

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentNo(OrderNoGenerator.generate());
        payment.setPaymentMethod(paymentMethod);
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(Constants.PAYMENT_STATUS_PENDING);
        paymentMapper.insert(payment);

        order.setStatus(Constants.ORDER_STATUS_PAYING);
        order.setPaymentId(payment.getId());
        orderMapper.updateById(order);

        return getPaymentVO(payment, order);
    }

    @Override
    @Transactional
    public void callback(String paymentNo, String status, String paidTime) {
        LambdaQueryWrapper<Payment> paymentWrapper = new LambdaQueryWrapper<>();
        paymentWrapper.eq(Payment::getPaymentNo, paymentNo);
        Payment payment = paymentMapper.selectOne(paymentWrapper);
        if (payment == null) {
            throw new BusinessException(1, "支付记录不存在");
        }

        payment.setStatus(status);
        if (Constants.PAYMENT_STATUS_SUCCESS.equals(status)) {
            payment.setPaidTime(paidTime);
        }
        paymentMapper.updateById(payment);

        Order order = orderMapper.selectById(payment.getOrderId());
        if (Constants.PAYMENT_STATUS_SUCCESS.equals(status)) {
            order.setStatus(Constants.ORDER_STATUS_PAID);
            order.setPayStatus(Constants.PAY_STATUS_PAID);
            order.setPayTime(paidTime);
        } else if (Constants.PAYMENT_STATUS_FAILED.equals(status)) {
            order.setStatus(Constants.ORDER_STATUS_CANCELLED);
        }
        orderMapper.updateById(order);
    }

    @Override
    public PaymentVO getPayment(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException(1, "订单不存在");
        }

        if (order.getPaymentId() == null) {
            throw new BusinessException(1, "支付记录不存在");
        }

        Payment payment = paymentMapper.selectById(order.getPaymentId());
        if (payment == null) {
            throw new BusinessException(1, "支付记录不存在");
        }

        return getPaymentVO(payment, order);
    }

    private PaymentVO getPaymentVO(Payment payment, Order order) {
        PaymentVO vo = new PaymentVO();
        vo.setId(payment.getId());
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setPaymentMethod(payment.getPaymentMethod());
        vo.setAmount(payment.getAmount().toString());
        vo.setStatus(payment.getStatus());
        vo.setPaidTime(payment.getPaidTime());
        vo.setCreateTime(payment.getCreateTime());
        return vo;
    }
}
