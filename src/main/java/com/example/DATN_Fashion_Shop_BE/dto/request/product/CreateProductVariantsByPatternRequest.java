package com.example.DATN_Fashion_Shop_BE.dto.request.product;

import lombok.*;


@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductVariantsByPatternRequest {
    private Long productId;
    private Long colorValueId;  // Chọn màu cụ thể
    private Long patternId;     // Chọn Pattern (XXS-XXL, S-L, v.v.)
    private Double salePrice;
}
