package com.example.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostCreateDTO {

    @NotBlank(message = "内容不能为空")
    @Size(max = 500, message = "内容不超过500字")
    private String content;

    private String image;

    private String bookId;
}
