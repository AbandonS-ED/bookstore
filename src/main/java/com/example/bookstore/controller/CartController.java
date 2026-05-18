package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.dto.CartAddDTO;
import com.example.bookstore.dto.CartUpdateDTO;
import com.example.bookstore.service.CartService;
import com.example.bookstore.vo.CartVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/list")
    public Result<List<CartVO>> list(@RequestParam Long userId) {
        List<CartVO> carts = cartService.getList(userId);
        return Result.success(carts);
    }

    @PostMapping("/add")
    public Result<Void> add(@RequestParam Long userId, @Valid @RequestBody CartAddDTO cartAddDTO) {
        cartService.add(userId, cartAddDTO);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestParam Long userId, @Valid @RequestBody CartUpdateDTO cartUpdateDTO) {
        cartService.updateQuantity(userId, cartUpdateDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@RequestParam Long userId, @PathVariable Long id) {
        cartService.delete(userId, id);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear(@RequestParam Long userId) {
        cartService.clear(userId);
        return Result.success();
    }
}