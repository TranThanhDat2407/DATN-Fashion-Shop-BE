package com.example.DATN_Fashion_Shop_BE.dto.response.payment;

import com.example.DATN_Fashion_Shop_BE.dto.PaymentMethodDTO;
import com.example.DATN_Fashion_Shop_BE.model.PaymentMethod;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodResponse {
    private Long id;
    private String methodName;
    public static PaymentMethodResponse fromPaymentMethod(PaymentMethod paymentMethod) {
        return PaymentMethodResponse.builder()
                .id(paymentMethod.getId())
                .methodName(paymentMethod.getMethodName())
                .build();
    }
}
