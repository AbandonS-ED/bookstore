package com.example.bookstore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal origPrice;

    private BigDecimal discountPrice;

    private LocalDateTime discountEndTime;

    private Boolean onDiscount;

    private Integer stock;

    private Integer sales;

    private Integer favoritedCount;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long categoryId;

    private String categoryName;

    private String coverUrl;

    private String quote;

    private Integer status;

    private LocalDate expectedShelfDate;

    private Double avgRating;

    private Integer reviewCount;
}