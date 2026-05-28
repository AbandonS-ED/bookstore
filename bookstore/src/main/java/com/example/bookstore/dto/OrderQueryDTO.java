package com.example.bookstore.dto;

import lombok.Data;

@Data
public class OrderQueryDTO {

    private String status;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}