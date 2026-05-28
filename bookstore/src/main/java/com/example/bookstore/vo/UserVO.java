package com.example.bookstore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String username;

    private String email;

    private String phone;

    private String role;

    private Integer status;

    private LocalDateTime createTime;
}