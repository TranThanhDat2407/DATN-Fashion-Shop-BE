package com.example.DATN_Fashion_Shop_BE.dto.request.order;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private Long userId;
    private Long couponId;
    private Long shippingMethodId;
    private String shippingAddress;
    private Long paymentMethodId;

}
