package com.example.bookstore.entity;

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

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book")
public class Book extends BaseEntity {

    private String isbn;

    @NotBlank(message = "书籍标题不能为空")
    private String title;

    private String author;

    private String publisher;

    private LocalDate publishDate;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "库存不能为空")
    @Positive(message = "库存必须大于0")
    private Integer stock;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long categoryId;

    private String description;

    private String coverUrl;

    private Integer status;

    private Integer sales;
}