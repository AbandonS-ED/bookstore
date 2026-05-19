package com.example.bookstore.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.bookstore.common.Constants;
import com.example.bookstore.dto.CartAddDTO;
import com.example.bookstore.dto.CartUpdateDTO;
import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Cart;
import com.example.bookstore.exception.BusinessException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.mapper.CartMapper;
import com.example.bookstore.service.CartService;
import com.example.bookstore.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final BookMapper bookMapper;

    @Override
    public void add(Long userId, CartAddDTO cartAddDTO) {
        Book book = bookMapper.selectById(Long.parseLong(cartAddDTO.getBookId()));
        if (book == null) {
            throw new BusinessException(1, "书籍不存在");
        }
        if (book.getStatus().equals(Constants.BOOK_STATUS_OFF)) {
            throw new BusinessException(1, "书籍已下架");
        }
        if (book.getStock() < cartAddDTO.getQuantity()) {
            throw new BusinessException(1, "库存不足");
        }

        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId).eq(Cart::getBookId, Long.parseLong(cartAddDTO.getBookId()));
        Cart existingCart = cartMapper.selectOne(wrapper);

        if (existingCart != null) {
            existingCart.setQuantity(existingCart.getQuantity() + cartAddDTO.getQuantity());
            cartMapper.updateById(existingCart);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setBookId(Long.parseLong(cartAddDTO.getBookId()));
            cart.setQuantity(cartAddDTO.getQuantity());
            cartMapper.insert(cart);
        }
    }

    @Override
    public void updateQuantity(Long userId, CartUpdateDTO cartUpdateDTO) {
        Cart cart = cartMapper.selectById(Long.parseLong(cartUpdateDTO.getId()));
        if (cart == null) {
            throw new BusinessException(1, "购物车记录不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(1, "无权操作此购物车");
        }

        Book book = bookMapper.selectById(cart.getBookId());
        if (book != null && book.getStock() < cartUpdateDTO.getQuantity()) {
            throw new BusinessException(1, "库存不足");
        }

        cart.setQuantity(cartUpdateDTO.getQuantity());
        cartMapper.updateById(cart);
    }

    @Override
    public void delete(Long userId, Long cartId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new BusinessException(1, "购物车记录不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException(1, "无权操作此购物车");
        }
        cartMapper.deleteById(cartId);
    }

    @Override
    public List<CartVO> getList(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        List<Cart> carts = cartMapper.selectList(wrapper);

        return carts.stream().map(cart -> {
            CartVO vo = new CartVO();
            vo.setId(cart.getId());
            vo.setBookId(cart.getBookId());
            vo.setQuantity(cart.getQuantity());

            Book book = bookMapper.selectById(cart.getBookId());
            if (book != null) {
                vo.setBookTitle(book.getTitle());
                vo.setBookAuthor(book.getAuthor());
                vo.setCoverUrl(book.getCoverUrl());
                vo.setPrice(book.getPrice());
                vo.setSubtotal(book.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void clear(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        cartMapper.delete(wrapper);
    }
}