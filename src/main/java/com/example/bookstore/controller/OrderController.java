package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.dto.OrderCreateDTO;
import com.example.bookstore.dto.OrderQueryDTO;
import com.example.bookstore.service.OrderService;
import com.example.bookstore.util.AuthContext;
import com.example.bookstore.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        OrderVO order = orderService.create(AuthContext.getCurrentUserId(), orderCreateDTO);
        return Result.success(order);
    }

    @GetMapping("/list")
    public Result<List<OrderVO>> list(OrderQueryDTO queryDTO) {
        List<OrderVO> orders = orderService.getList(AuthContext.getCurrentUserId(), queryDTO);
        return Result.success(orders);
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        OrderVO order = orderService.getDetail(AuthContext.getCurrentUserId(), id);
        return Result.success(order);
    }

    @PutMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.pay(AuthContext.getCurrentUserId(), id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(AuthContext.getCurrentUserId(), id);
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id) {
        orderService.confirm(AuthContext.getCurrentUserId(), id);
        return Result.success();
    }
}