package com.example.bookstore.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class StockAdjustDTO {

    @NotNull(message = "库存数量不能为空")
    @PositiveOrZero(message = "库存数量必须大于等于0")
    private Integer stock;
}
