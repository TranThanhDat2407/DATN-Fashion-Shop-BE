package com.example.DATN_Fashion_Shop_BE.config;

import com.example.DATN_Fashion_Shop_BE.model.CouponConfigEntity;
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
    public CouponConfig(CouponConfigEntity entity) {
        this.discountType = entity.getDiscountType();
        this.discountValue = entity.getDiscountValue();
        this.minOrderValue = entity.getMinOrderValue();
        this.expirationDays = entity.getExpirationDays();
        this.imageUrl = entity.getImageUrl();
    }
}
