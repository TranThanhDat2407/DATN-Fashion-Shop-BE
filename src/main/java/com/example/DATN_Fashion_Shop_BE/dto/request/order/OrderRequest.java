package com.example.DATN_Fashion_Shop_BE.dto.request.order;

import jakarta.validation.constraints.NotNull;
import lombok.*;
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    @NotNull(message = "User ID không được để trống.")
    private Long userId;

    private Long couponId;

    @NotNull(message = "Shipping Method ID không được để trống.")
    private Long shippingMethodId;

    private Long shippingAddress;

    @NotNull(message = "Payment Method ID không được để trống.")
    private Long paymentMethodId;

    private Long storeId;
    private String receiverName;
    private String receiverPhone;
}

