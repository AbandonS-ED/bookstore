package com.example.bookstore.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ProfileUpdateDTO {

    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
}
