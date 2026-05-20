package com.example.bookstore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartAddDTO {

    @NotBlank(message = "书籍ID不能为空")
    private String bookId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量最少为1")
    private Integer quantity;
}