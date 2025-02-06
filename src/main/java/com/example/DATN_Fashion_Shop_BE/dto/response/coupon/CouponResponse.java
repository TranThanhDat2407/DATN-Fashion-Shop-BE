package com.example.DATN_Fashion_Shop_BE.dto.response.coupon;

import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import com.example.DATN_Fashion_Shop_BE.model.User;
import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponResponse {

    private Long id;
    private User user;
    private String discountType;
    private String discountValue;
    private String minOrderValue;
    private String userLimit;
    private String expirationDate;
    private Boolean isActive = true;
    private String code;

    public static CouponResponse fromCoupon(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .user(coupon.getUser())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .userLimit(coupon.getUserLimit())
                .expirationDate(coupon.getExpirationDate())
                .isActive(coupon.getIsActive())
                .code(coupon.getCode())
                .build();
    }
}
