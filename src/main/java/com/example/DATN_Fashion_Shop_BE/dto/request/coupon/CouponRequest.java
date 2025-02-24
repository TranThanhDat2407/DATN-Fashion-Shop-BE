package com.example.DATN_Fashion_Shop_BE.dto.request.coupon;

import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponRequest {
    @NotBlank(message = MessageKeys.COUPON_CODE_REQUIRED)
    private String code;
    private String imageUrl;
    private String discountType;
    private Float discountValue;
    private Float minOrderValue;
    private LocalDateTime expirationDate;

}
