package com.example.DATN_Fashion_Shop_BE.dto.response.promotion;
import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PromotionResponse extends BaseResponse {
    private Long id;
    private String description;
    private Double discountRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private List<Long> productIds;

    public static PromotionResponse fromPromotion(Promotion promotion) {
        PromotionResponse response = PromotionResponse.builder()
                .id(promotion.getId())
                .description(promotion.getDescriptions())
                .discountRate(promotion.getDiscountPercentage())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .isActive(promotion.getIsActive())
                .productIds(promotion.getProducts().stream()
                        .map(Product::getId)
                        .collect(Collectors.toList()))
                .build();
        response.setCreatedAt(promotion.getCreatedAt());
        response.setUpdatedBy(promotion.getUpdatedBy());
        response.setCreatedBy(promotion.getCreatedBy());
        response.setUpdatedBy(promotion.getUpdatedBy());
        return response;
    }
}
