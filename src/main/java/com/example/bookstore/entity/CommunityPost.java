package com.example.bookstore.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.bookstore.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("community_post")
public class CommunityPost extends BaseEntity {

    private Long userId;

    private String username;

    private String content;

    private String imageUrl;

    private Integer likes;

    private Integer liked;

    private Long bookId;

    @TableField(exist = false)
    private String bookTitle;
}