package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.dto.CartAddDTO;
import com.example.bookstore.dto.CartUpdateDTO;
import com.example.bookstore.service.CartService;
import com.example.bookstore.util.AuthContext;
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
    public Result<List<CartVO>> list() {
        List<CartVO> carts = cartService.getList(AuthContext.getCurrentUserId());
        return Result.success(carts);
    }

    @PostMapping("/add")
    public Result<Void> add(@Valid @RequestBody CartAddDTO cartAddDTO) {
        cartService.add(AuthContext.getCurrentUserId(), cartAddDTO);
        return Result.success();
    }

    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody CartUpdateDTO cartUpdateDTO) {
        cartService.updateQuantity(AuthContext.getCurrentUserId(), cartUpdateDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.delete(AuthContext.getCurrentUserId(), id);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear() {
        cartService.clear(AuthContext.getCurrentUserId());
        return Result.success();
    }
}