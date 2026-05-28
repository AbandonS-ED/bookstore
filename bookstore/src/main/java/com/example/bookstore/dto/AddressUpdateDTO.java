package com.example.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressUpdateDTO {

    @NotBlank(message = "地址ID不能为空")
    private String id;

    @NotBlank(message = "收货人不能为空")
    @Size(max = 50, message = "收货人姓名不超过50字")
    private String receiverName;

    @NotBlank(message = "联系电话不能为空")
    @Size(max = 20, message = "联系电话不超过20位")
    private String phone;

    @NotBlank(message = "省份不能为空")
    @Size(max = 50, message = "省份不超过50字")
    private String province;

    @NotBlank(message = "城市不能为空")
    @Size(max = 50, message = "城市不超过50字")
    private String city;

    @Size(max = 50, message = "区县不超过50字")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    @Size(max = 200, message = "详细地址不超过200字")
    private String detailAddress;

    private Integer isDefault;
}