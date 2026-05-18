package com.example.bookstore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {

    @NotNull(message = "地址ID不能为空")
    private Long addressId;

    private List<Long> cartItemIds;

    private String remark;
}