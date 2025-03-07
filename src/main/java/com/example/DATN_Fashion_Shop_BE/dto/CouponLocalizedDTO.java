package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import com.example.DATN_Fashion_Shop_BE.model.CouponTranslation;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponLocalizedDTO {
    private Long id;
    private String code;
    private String discountType;
    private Float discountValue;
    private Float minOrderValue;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationDate;
    private Boolean isActive;
    private Boolean isGlobal;
    private String name;        // Tên theo ngôn ngữ
    private String description; // Mô tả theo ngôn ngữ
    private List<Long> userIds;
    private String imageUrl;
    // Danh sách người dùng được chỉ định
    public static CouponLocalizedDTO fromCoupons(Coupon coupon, CouponTranslation translation, List<Long> userIds) {
        return CouponLocalizedDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .expirationDate(coupon.getExpirationDate())
                .isActive(coupon.getIsActive())
                .minOrderValue(coupon.getMinOrderValue())
                .isGlobal(coupon.getIsGlobal())
                .name(translation != null ? translation.getName() : null)
                .userIds(userIds)
                .imageUrl(coupon.getImageUrl())
                .description(translation != null ? translation.getDescription() : null)
                .build();
    }
}