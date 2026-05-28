package com.example.bookstore.controller;

import com.example.bookstore.common.Result;
import com.example.bookstore.dto.PayCallbackDTO;
import com.example.bookstore.service.PaymentService;
import com.example.bookstore.util.AuthContext;
import com.example.bookstore.vo.PaymentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${callback.secret}")
    private String callbackSecret;

    @GetMapping("/{orderId}")
    public Result<PaymentVO> getPayment(@PathVariable Long orderId) {
        PaymentVO payment = paymentService.getPayment(AuthContext.getCurrentUserId(), orderId);
        return Result.success(payment);
    }

    @PostMapping("/callback")
    public Result<Void> callback(@RequestHeader("X-Callback-Secret") String secret,
                                 @RequestBody PayCallbackDTO callbackDTO) {
        if (!callbackSecret.equals(secret)) {
            return Result.error(401, "回调鉴权失败");
        }
        paymentService.callback(callbackDTO.getPaymentNo(), callbackDTO.getStatus(), callbackDTO.getPaidTime());
        return Result.success();
    }
}
