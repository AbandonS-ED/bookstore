package com.example.bookstore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String isbn;

    private String title;

    private String author;

    private String publisher;

    private LocalDate publishDate;

    private BigDecimal price;

    private Integer stock;

    private Integer sales;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;

    private String categoryName;

    private String coverUrl;

    private String quote;

    private Integer status;

    private Double avgRating;

    private Integer reviewCount;
}