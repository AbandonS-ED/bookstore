package com.example.bookstore.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.bookstore.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("review")
public class Review extends BaseEntity {

    private Long userId;

    private Long bookId;

    private Long orderItemId;

    private Integer rating;

    private String content;

    private Integer status;

    @TableField(exist = false)
    private String username;

    @TableField(exist = false)
    private String bookTitle;
}