package com.example.bookstore.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.bookstore.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("author")
public class Author extends BaseEntity {

    private String name;

    private String avatarUrl;

    private String bio;

    private String country;

    private String birthYear;

    private String awards;
}
