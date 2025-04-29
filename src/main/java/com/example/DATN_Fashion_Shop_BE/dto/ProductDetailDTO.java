package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.promotion.PromotionResponse;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.ProductsTranslation;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailDTO extends BaseResponse {
    private Long id;
    private String name;
    private String description;
    private String material;
    private String care;
    private Double basePrice;
    private PromotionDTO promotion;
    private Boolean isActive;

    public static ProductDetailDTO fromProduct(Product product, String langCode) {
        ProductsTranslation translation = product.getTranslationByLanguage(langCode);
        return ProductDetailDTO.builder()
                .id(product.getId())
                .name(translation.getName())
                .description(translation.getDescription())
                .material(translation.getMaterial())
                .care(translation.getCare())
                .basePrice(product.getBasePrice())
                .isActive(product.getIsActive())
                .promotion(product.getPromotion() != null ?
                        PromotionDTO.fromPromotion(product.getPromotion()) : null)
                .build();
    }
}
