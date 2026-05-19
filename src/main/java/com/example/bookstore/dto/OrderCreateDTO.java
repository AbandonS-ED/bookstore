package com.example.bookstore.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {

    @NotNull(message = "地址ID不能为空")
    private String addressId;

    @NotEmpty(message = "请选择要购买的商品")
    private List<String> cartItemIds;

    private String remark;
}