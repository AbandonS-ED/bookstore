package com.example.bookstore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Long userId;

    private String username;

    private Long bookId;

    private Integer rating;

    private String content;

    private LocalDateTime createTime;

    private String bookTitle;

    private String bookCoverUrl;
}