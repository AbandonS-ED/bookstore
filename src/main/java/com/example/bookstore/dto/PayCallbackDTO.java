package com.example.bookstore.dto;

import lombok.Data;

@Data
public class PayCallbackDTO {

    private String paymentNo;

    private String status;

    private String paidTime;
}