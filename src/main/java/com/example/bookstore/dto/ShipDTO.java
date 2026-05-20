package com.example.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShipDTO {

    @NotBlank(message = "物流单号不能为空")
    private String expressNo;
}