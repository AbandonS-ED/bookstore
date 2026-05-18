package com.example.bookstore.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartVO {

    private Long id;

    private Long bookId;

    private String bookTitle;

    private String bookAuthor;

    private String coverUrl;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;
}