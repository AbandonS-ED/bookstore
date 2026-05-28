package com.example.bookstore.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.bookstore.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book")
public class Book extends BaseEntity {

    private String isbn;

    @NotBlank(message = "书籍标题不能为空")
    private String title;

    private String author;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long authorId;

    private String publisher;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    private BigDecimal origPrice;

    private BigDecimal discountPrice;

    private LocalDateTime discountEndTime;

    @NotNull(message = "库存不能为空")
    @Positive(message = "库存必须大于0")
    private Integer stock;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long categoryId;

    private String description;

    private String coverUrl;

    private String quote;

    private Integer status;

    @TableField(exist = false)
    private String categoryName;

    private LocalDate expectedShelfDate;

    private Integer sales;

    private Integer favoritedCount;
}