package com.example.DATN_Fashion_Shop_BE.dto.response.revenue;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Top10Products {
    private Long productVariantId;
    private String productName;
    private String color;
    private String colorImage;
    private String size;
    private String imageUrl;
    private Long totalSold;
    private Double totalRevenue;
    private Long productId;
    private Long colorValueId;


    public Top10Products(Long productVariantId, String productName, String color, String colorImage,
                         String size, Long totalSold, Double totalRevenue, Long productId, Long colorValueId) {
        this.productVariantId = productVariantId;
        this.productName = productName;
        this.color = color;
        this.colorImage = colorImage;
        this.size = size;
        this.totalSold = totalSold;
        this.totalRevenue = totalRevenue;
        this.productId = productId;
        this.colorValueId = colorValueId;
    }
}
