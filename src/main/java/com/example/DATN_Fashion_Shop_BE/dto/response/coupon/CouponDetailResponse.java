package com.example.DATN_Fashion_Shop_BE.dto.response.coupon;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponDetailResponse extends BaseResponse {
    private Long id;
    private Float discountValue;
    private Float minOrderValue;
    private String discountType;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime expirationDate;
    private String code;

    public static CouponDetailResponse fromCoupon(Coupon coupon) {
        CouponDetailResponse response = CouponDetailResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .minOrderValue(coupon.getMinOrderValue())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .expirationDate(coupon.getExpirationDate())
                .build();
        response.setCreatedAt(coupon.getCreatedAt());
        response.setUpdatedAt(coupon.getUpdatedAt());
        response.setCreatedBy(coupon.getCreatedBy());
        response.setUpdatedBy(coupon.getUpdatedBy());

        return response;
    }

}
