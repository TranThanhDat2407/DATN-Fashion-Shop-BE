package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.Address;
import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import com.example.DATN_Fashion_Shop_BE.model.CouponTranslation;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponDTO {
    private Long id;
    private String code;
    private String discountType;
    private Float discountValue;
    private Float minOrderValue;
    private LocalDateTime expirationDate;
    private Boolean isActive;
    private String imageUrl;
    private Boolean isGlobal;



    public static CouponDTO fromCoupon(Coupon coupon) {
        return CouponDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .expirationDate(coupon.getExpirationDate())
                .isActive(coupon.getIsActive())
                .imageUrl(coupon.getImageUrl())
                .isGlobal(coupon.getIsGlobal())
                // Gán bản dịch (nếu có)

                .build();
    }
}
