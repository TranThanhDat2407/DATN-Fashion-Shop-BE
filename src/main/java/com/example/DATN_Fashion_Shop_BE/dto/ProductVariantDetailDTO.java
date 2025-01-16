package com.example.DATN_Fashion_Shop_BE.dto;

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
public class ProductVariantDetailDTO {
    private Long id;
    private String name;
    private String color;
    private String size;
    private Double basePrice;
    private Double salePrice;

    public static ProductVariantDetailDTO fromProductVariant(ProductVariant productVariant, String langCode){
        Product product = productVariant.getProduct();
        Double salePrice = productVariant.getSalePrice(); // Lấy giá salePrice từ ProductVariant

        // Tính toán khuyến mãi dựa trên salePrice
        if (product.getPromotion() != null && product.getPromotion().getIsActive()) {
            Promotion promotion = product.getPromotion();

            if (promotion.getDiscountPercentage() != null) {
                salePrice *= (1 - promotion.getDiscountPercentage() / 100); // Áp dụng giảm giá theo %
            }
            // Đảm bảo salePrice không âm
            salePrice = Math.max(salePrice, 0);
        }

        return ProductVariantDetailDTO.builder()
                .id(productVariant.getId())
                .name(product.getTranslationByLanguage(langCode).getName())
                .color(productVariant.getColorValue().getValueName())
                .size(productVariant.getSizeValue().getValueName())
                .basePrice(product.getBasePrice())
                .salePrice(salePrice)
                .build();
    }
}
