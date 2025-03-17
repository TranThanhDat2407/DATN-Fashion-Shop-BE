package com.example.DATN_Fashion_Shop_BE.dto.request.order;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStorePaymentMethodRequest {
    private String paymentMethodName;
}

