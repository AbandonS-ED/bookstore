package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.dto.OrderCreateDTO;
import com.example.bookstore.dto.OrderQueryDTO;
import com.example.bookstore.service.OrderService;
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
    public Result<OrderVO> create(@RequestParam Long userId, @Valid @RequestBody OrderCreateDTO orderCreateDTO) {
        OrderVO order = orderService.create(userId, orderCreateDTO);
        return Result.success(order);
    }

    @GetMapping("/list")
    public Result<List<OrderVO>> list(@RequestParam Long userId, OrderQueryDTO queryDTO) {
        List<OrderVO> orders = orderService.getList(userId, queryDTO);
        return Result.success(orders);
    }

    @GetMapping("/{id}")
    public Result<OrderVO> detail(@RequestParam Long userId, @PathVariable Long id) {
        OrderVO order = orderService.getDetail(userId, id);
        return Result.success(order);
    }

    @PutMapping("/{id}/pay")
    public Result<Void> pay(@RequestParam Long userId, @PathVariable Long id) {
        orderService.pay(userId, id);
        return Result.success();
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@RequestParam Long userId, @PathVariable Long id) {
        orderService.cancel(userId, id);
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@RequestParam Long userId, @PathVariable Long id) {
        orderService.confirm(userId, id);
        return Result.success();
    }
}