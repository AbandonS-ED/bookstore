package com.example.bookstore.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookDetailVO {

    private Long id;

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private LocalDate publishDate;

    private BigDecimal price;

    private Integer stock;

    private Long categoryId;

    private String categoryName;

    private String description;

    private String coverUrl;

    private Integer status;

    private Double avgRating;

    private Integer reviewCount;
}