package com.example.DATN_Fashion_Shop_BE.dto.request.coupon;

import com.example.DATN_Fashion_Shop_BE.dto.CategoryTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CouponCreateRequestDTO {

    private String code;

    @NotBlank(message = MessageKeys.DISCOUNT_TYPE_REQUIRED)
    private String discountType;

    @PositiveOrZero(message = MessageKeys.DISCOUNT_VALUE_INVALID)
    private Float discountValue;

    @PositiveOrZero(message = MessageKeys.DISCOUNT_VALUE_INVALID)
    private Float minOrderValue;

    private String imageUrl;




//    @NotNull(message = MessageKeys.EXPIRATION_DATE_INVALID)  // ✅ Thêm kiểm tra null
//    @FutureOrPresent(message = MessageKeys.EXPIRATION_DATE_INVALID)
    private LocalDateTime expirationDate;

    @NotNull(message = MessageKeys.EXPIRATION_DATE_INVALID)
    @Min(value = 1, message = MessageKeys.EXPIRATION_DATE_INVALID)
    private Integer expirationDays; // Số ngày hết hạn, phải >= 1


    private Boolean isGlobal; // Nếu true, không cần userIds
    private List<Long> userIds; // Danh sách người dùng được chỉ định

    private List<CouponTranslationDTO> translations; // Danh sách bản dịch
}
