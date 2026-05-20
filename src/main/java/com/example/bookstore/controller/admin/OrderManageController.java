package com.example.bookstore.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.bookstore.common.Constants;
import com.example.bookstore.common.Result;
import com.example.bookstore.dto.ShipDTO;
import com.example.bookstore.entity.Order;
import com.example.bookstore.mapper.OrderMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class OrderManageController {

    private final OrderMapper orderMapper;

    @GetMapping("/list")
    public Result<Page<Order>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Page<Order> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        Page<Order> result = orderMapper.selectPage(page, wrapper);
        return Result.success(result);
    }

    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id, @Valid @RequestBody ShipDTO shipDTO) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error(1, "订单不存在");
        }
        if (!Constants.ORDER_STATUS_PAID.equals(order.getStatus())) {
            return Result.error(1, "订单状态不允许发货");
        }
        order.setStatus(Constants.ORDER_STATUS_SHIPPED);
        order.setExpressNo(shipDTO.getExpressNo());
        orderMapper.updateById(order);
        return Result.success();
    }

    @PutMapping("/{id}/deliver")
    public Result<Void> deliver(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error(1, "订单不存在");
        }
        if (!Constants.ORDER_STATUS_SHIPPED.equals(order.getStatus())) {
            return Result.error(1, "订单状态不允许收货");
        }
        order.setStatus(Constants.ORDER_STATUS_DELIVERED);
        orderMapper.updateById(order);
        return Result.success();
    }

    @PutMapping("/{id}/refund")
    public Result<Void> refund(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error(1, "订单不存在");
        }
        if (!Constants.ORDER_STATUS_PAID.equals(order.getStatus()) && !Constants.ORDER_STATUS_SHIPPED.equals(order.getStatus())) {
            return Result.error(1, "订单状态不允许退款");
        }
        order.setStatus(Constants.ORDER_STATUS_REFUNDED);
        orderMapper.updateById(order);
        return Result.success();
    }
}