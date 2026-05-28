package com.example.bookstore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommunityPostVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String username;

    private String content;

    private String image;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long bookId;

    private String bookTitle;

    private String bookAuthor;

    private Integer likes;

    private Boolean liked;

    private Integer status;

    private LocalDateTime createTime;
}
