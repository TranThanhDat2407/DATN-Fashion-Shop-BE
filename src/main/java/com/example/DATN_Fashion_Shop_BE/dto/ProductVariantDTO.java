package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import com.example.DATN_Fashion_Shop_BE.model.Promotion;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDTO {
    private Long id;
    private String color;
    private String size;
    private Double salePrice;

    public static ProductVariantDTO fromProductVariant(ProductVariant productVariant){
        Product product = productVariant.getProduct();
        Double salePrice = productVariant.getAdjustedPrice(); // Lấy giá salePrice từ ProductVariant

        // Tính toán khuyến mãi dựa trên salePrice
        if (product.getPromotion() != null && product.getPromotion().getIsActive()) {
            Promotion promotion = product.getPromotion();

            if (promotion.getDiscountPercentage() != null) {
                salePrice *= (1 - promotion.getDiscountPercentage() / 100); // Áp dụng giảm giá theo %
            }
            // Đảm bảo salePrice không âm
            salePrice = Math.max(salePrice, 0);
        }

        return ProductVariantDTO.builder()
                .id(productVariant.getId())
                .color(productVariant.getColorValue().getValueName())
                .size(productVariant.getSizeValue().getValueName())
                .salePrice(salePrice)
                .build();
    }
}
