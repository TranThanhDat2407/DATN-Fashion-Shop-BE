package com.example.DATN_Fashion_Shop_BE.dto.request.store;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Getter
@Setter
public class StorePaymentRequest {
    private Long userId;
    private Long storeId;
    private Long couponId;
    private Long paymentMethodId;
    private String transactionCode;
    private Double totalAmount;
    private Double totalPrice;
    private Double taxAmount;
}
