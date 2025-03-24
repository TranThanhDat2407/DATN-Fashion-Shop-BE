package com.example.DATN_Fashion_Shop_BE.dto.response.revenue;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStatistics {
    private Long productVariantId;
    private String productName;
    private String color;
    private String colorImage;
    private String size;
    private String imageUrl;
    private Long totalQuantity;
}
