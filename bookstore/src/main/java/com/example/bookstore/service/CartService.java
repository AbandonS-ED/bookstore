package com.example.bookstore.service;

import com.example.bookstore.dto.CartAddDTO;
import com.example.bookstore.dto.CartUpdateDTO;
import com.example.bookstore.vo.CartVO;

import java.util.List;

public interface CartService {

    void add(Long userId, CartAddDTO cartAddDTO);

    void updateQuantity(Long userId, CartUpdateDTO cartUpdateDTO);

    void delete(Long userId, Long cartId);

    List<CartVO> getList(Long userId);

    void clear(Long userId);
}