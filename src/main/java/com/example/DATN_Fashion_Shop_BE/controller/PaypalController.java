package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.service.PaypalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/paypal")
@RequiredArgsConstructor
public class PaypalController {

    private final PaypalService paypalService;

    @PostMapping("/create-order")
    public ResponseEntity<String> createOrder(@RequestParam Double amount) {
        String approveUrl = paypalService.createOrder(
                amount,
                "http://localhost:4200/client/usd/en/paypal-success",
                "http://localhost:4200/client/usd/en/paypal-cancel"
        );
        return ResponseEntity.ok(approveUrl);
    }

    @PostMapping("/capture-order")
    public ResponseEntity<?> captureOrder(@RequestParam String token) {
        try {
            Map result = paypalService.captureOrder(token);

            String status = (String) result.get("status");

            // Chỉ trả về thông tin nếu đã capture thành công
            if ("COMPLETED".equals(status)) {
                return ResponseEntity.ok(Map.of(
                        "status", status,
                        "paypalResponse", result
                ));
            } else {
                // Nếu không thành công, trả về thông báo
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Transaction not completed yet.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("CAPTURE FAILED");
        }
    }


    @PostMapping("/verify-order")
    public ResponseEntity<?> verifyOrder(@RequestParam String token) {
        try {
            Map orderInfo = paypalService.getOrderStatus(token);
            String status = (String) orderInfo.get("status");


            return ResponseEntity.ok(Map.of(
                    "status", status,
                    "order", orderInfo
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("VERIFY FAILED");
        }
    }

}

