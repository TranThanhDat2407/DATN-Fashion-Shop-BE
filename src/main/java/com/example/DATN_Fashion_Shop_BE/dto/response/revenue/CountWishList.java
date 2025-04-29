package com.example.DATN_Fashion_Shop_BE.dto.response.revenue;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountWishList {
    private Long productVariantId;
    private String productName;
    private String color;
    private String colorImage;
    private String imageUrl;
    private Double totalPrice;
    private Long totalWishList;
}
