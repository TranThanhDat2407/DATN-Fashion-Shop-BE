package com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopProductsInStoreResponse {
    private Long productVariantId;
    private String productName;
    private String color;
    private String colorImage;
    private String size;
    private String imageUrl;
    private Long totalSold;
    private Double totalRevenue;
}
