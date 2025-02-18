package com.example.DATN_Fashion_Shop_BE.dto.request.coupon;

import com.example.DATN_Fashion_Shop_BE.dto.CategoryTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponCreateRequestDTO {
    @NotBlank(message = MessageKeys.COUPON_CODE_REQUIRED)
    private String code;

    @NotBlank(message = MessageKeys.DISCOUNT_TYPE_REQUIRED)
    private String discountType;

    @Min(value = 0, message = MessageKeys.DISCOUNT_VALUE_INVALID)
    private Float discountValue;

    @Min(value = 0, message = MessageKeys.MIN_ORDER_VALUE_INVALID)
    private Float minOrderValue;

    @FutureOrPresent(message = MessageKeys.EXPIRATION_DATE_INVALID)
    private LocalDateTime expirationDate;
    private Boolean isGlobal; // Nếu true, không cần userIds
    private List<Long> userIds; // Danh sách người dùng được chỉ định

    private List<CouponTranslationDTO> translations; // Danh sách bản dịch
}
