package com.example.DATN_Fashion_Shop_BE.dto.response.promotion;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionSimpleResponse extends BaseResponse {
    private Long id;
    private String description;
    private Double discountRate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime endDate;
    private Boolean isActive;

    public static PromotionSimpleResponse fromPromotion(Promotion promotion) {
        PromotionSimpleResponse response = PromotionSimpleResponse.builder()
                .id(promotion.getId())
                .description(promotion.getDescriptions())
                .discountRate(promotion.getDiscountPercentage())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .isActive(promotion.getIsActive())
                .build();
        response.setCreatedAt(promotion.getCreatedAt());
        response.setUpdatedAt(promotion.getUpdatedAt());
        response.setCreatedBy(promotion.getCreatedBy());
        response.setUpdatedBy(promotion.getUpdatedBy());
        return response;
    }
}
