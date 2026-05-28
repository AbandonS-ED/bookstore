package com.example.bookstore.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.bookstore.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("category")
public class Category extends BaseEntity {

    private String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentId;

    private Integer sort;

    private Integer status;
}