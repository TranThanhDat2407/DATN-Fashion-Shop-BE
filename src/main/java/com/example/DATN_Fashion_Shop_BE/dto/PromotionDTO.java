package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PromotionDTO {
    private Long id;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;

    public static PromotionDTO fromPromotion(Promotion promotion){
        return PromotionDTO.builder()
                .id(promotion.getId())
                .description(promotion.getDescriptions())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .build();
    }
}
