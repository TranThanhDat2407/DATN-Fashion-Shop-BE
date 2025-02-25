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
    private Float discountValue;
    private Float minOrderValue;
    private String userLimit;
    private String expirationDate;
    private Boolean isActive = true;
    private String imageUrl;
    private String code;

    public static CouponResponse fromCoupon(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .expirationDate(coupon.getExpirationDate().toString())
                .isActive(coupon.getIsActive())
                .code(coupon.getCode())
                .build();
    }
}
