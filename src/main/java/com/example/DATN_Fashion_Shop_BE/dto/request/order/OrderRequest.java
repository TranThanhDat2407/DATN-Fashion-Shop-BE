package com.example.DATN_Fashion_Shop_BE.dto.request.order;

import lombok.Data;

@Data
public class OrderRequest {
    private Long userId;
    private Long couponId;
    private Long shippingMethodId;
    private String shippingAddress;
    private Long paymentMethodId;

}
