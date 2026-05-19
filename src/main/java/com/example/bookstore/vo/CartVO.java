package com.example.bookstore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long bookId;

    private String bookTitle;

    private String bookAuthor;

    private String coverUrl;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal subtotal;
}