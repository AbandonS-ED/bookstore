package com.example.bookstore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private LocalDate publishDate;

    private BigDecimal price;

    private Integer stock;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;

    private String categoryName;

    private String description;

    private String coverUrl;

    private String quote;

    private Integer status;

    private Double avgRating;

    private Integer reviewCount;
}