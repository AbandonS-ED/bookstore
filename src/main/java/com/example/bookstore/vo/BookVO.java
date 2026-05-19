package com.example.bookstore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private BigDecimal price;

    private Integer stock;

    private Long categoryId;

    private String categoryName;

    private String coverUrl;

    private Integer status;
}