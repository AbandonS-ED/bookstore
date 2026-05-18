package com.example.bookstore.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewVO {

    private Long id;

    private Long userId;

    private String username;

    private Long bookId;

    private Integer rating;

    private String content;

    private LocalDateTime createTime;
}