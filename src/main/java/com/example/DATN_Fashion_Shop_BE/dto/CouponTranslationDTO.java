package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.CategoriesTranslation;
import com.example.DATN_Fashion_Shop_BE.model.CouponTranslation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponTranslationDTO {
    @NotBlank(message = MessageKeys.COUPON_NAME_REQUIRED)
    private String name;

    @NotBlank(message = MessageKeys.COUPON_DESCRIPTION_REQUIRED)
    private String description;

    @NotBlank(message = MessageKeys.LANGUAGE_CODE_REQUIRED)
    @Pattern(regexp = "^(vi|en|jp)$", message = MessageKeys.LANGUAGE_CODE_INVALID)
    private String languageCode; // Mã ngôn ngữ (vd: vi, en, jp)

    public static CouponTranslationDTO fromCouponTranslation(CouponTranslation couponTranslation) {
        return CouponTranslationDTO.builder()
                .name(couponTranslation.getName())
                .languageCode(couponTranslation.getLanguage().getCode())
                .description(couponTranslation.getDescription())
                .build();
    }
}
