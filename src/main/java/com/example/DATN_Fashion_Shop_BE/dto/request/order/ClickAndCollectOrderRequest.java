package com.example.DATN_Fashion_Shop_BE.dto.request.order;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClickAndCollectOrderRequest {
    @NotNull(message = "User ID không được để trống")
    private Long userId;

    @NotNull(message = "Store ID không được để trống")
    private Long storeId;

    private Long couponId; // Mã giảm giá (nếu có)

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private Long paymentMethodId;
}

