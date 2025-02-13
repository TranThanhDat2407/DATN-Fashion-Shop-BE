package com.example.DATN_Fashion_Shop_BE.dto.request.payment;

import com.example.DATN_Fashion_Shop_BE.dto.BannerTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.dto.PaymentMethodDTO;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodRequestDTO {
    private String methodName;
}
