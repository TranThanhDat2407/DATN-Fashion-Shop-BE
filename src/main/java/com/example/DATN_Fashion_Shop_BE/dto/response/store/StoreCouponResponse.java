package com.example.DATN_Fashion_Shop_BE.dto.response.store;
import com.example.DATN_Fashion_Shop_BE.dto.PromotionDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.coupon.CouponResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreCouponResponse extends BaseResponse {
   private Long id;
   private String discountType;
   private Float discountValue;
   private String code;

   public static StoreCouponResponse fromCoupon(Coupon coupon) {
      return StoreCouponResponse.builder()
              .id(coupon.getId())
              .discountType(coupon.getDiscountType())
              .discountValue(coupon.getDiscountValue())
              .code(coupon.getCode())
              .build();
   }
}
