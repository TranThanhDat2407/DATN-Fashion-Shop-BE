package com.example.DATN_Fashion_Shop_BE.dto.request.promotion;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductsPromotionRequest {
    private List<Long> productIds;
    private boolean activate;
}
