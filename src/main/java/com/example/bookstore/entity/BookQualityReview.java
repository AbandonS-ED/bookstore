package com.example.bookstore.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.bookstore.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("book_quality_review")
public class BookQualityReview extends BaseEntity {

    private Long bookId;

    private String reviewerName;

    private String content;

    private String source;

    private BigDecimal rating;
}
