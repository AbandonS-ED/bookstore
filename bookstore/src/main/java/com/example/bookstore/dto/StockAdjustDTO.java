package com.example.bookstore.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustDTO {

    @NotNull(message = "库存不能为空")
    private Integer stock;
}
