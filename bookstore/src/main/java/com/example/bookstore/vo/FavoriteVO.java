package com.example.bookstore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long bookId;

    private String title;

    private String author;

    private BigDecimal price;

    private BigDecimal origPrice;

    private BigDecimal favoritedPrice;

    private String coverUrl;

    private Integer stock;

    private Double avgRating;

    private String categoryName;

    private Integer sales;

    private LocalDateTime createTime;
}
