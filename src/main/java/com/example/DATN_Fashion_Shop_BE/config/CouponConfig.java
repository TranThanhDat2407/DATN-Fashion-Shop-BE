package com.example.DATN_Fashion_Shop_BE.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponConfig {
    private String discountType;
    private Float discountValue;
    private Float minOrderValue;
    private int expirationDays;
    private String imageUrl;
}
