package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.dto.PayCallbackDTO;
import com.example.bookstore.service.PaymentService;
import com.example.bookstore.util.AuthContext;
import com.example.bookstore.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{orderId}")
    public Result<PaymentVO> getPayment(@PathVariable Long orderId) {
        PaymentVO payment = paymentService.getPayment(AuthContext.getCurrentUserId(), orderId);
        return Result.success(payment);
    }

    @PostMapping("/callback")
    public Result<Void> callback(@RequestBody PayCallbackDTO callbackDTO) {
        paymentService.callback(callbackDTO.getPaymentNo(), callbackDTO.getStatus(), callbackDTO.getPaidTime());
        return Result.success();
    }
}
