package com.example.DATN_Fashion_Shop_BE.dto.response.product;

import com.example.DATN_Fashion_Shop_BE.dto.PromotionDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.store.StoreStockResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantsMediaResponse {
    private Long ProductId;
    private Long productVariantId;
    private String productImage;
    private String productName;
    private String colorName;
    private String sizeName;
    private String colorImage;

    public static ProductVariantsMediaResponse fromProductVariant(ProductVariant variant, String languageCode) {
        Product product = variant.getProduct();
        AttributeValue color = variant.getColorValue();
        String productImage = null;
        if (product.getMedias() != null && !product.getMedias().isEmpty()) {
            productImage = product.getMedias().stream()
                    .filter(media -> media.getColorValue() != null && color != null && media.getColorValue().getId().equals(color.getId())) // So sánh bằng ID thay vì equals()
                    .map(ProductMedia::getMediaUrl)
                    .findFirst()
                    .orElse(product.getMedias().getFirst().getMediaUrl()); // Nếu không có, lấy ảnh đầu tiên
        }

        ProductVariantsMediaResponse response = ProductVariantsMediaResponse.builder()
                .ProductId(variant.getProduct().getId())
                .productName(variant.getProduct().getTranslationByLanguage(languageCode).getName())
                .sizeName(variant.getSizeValue().getValueName())
                .productImage(productImage)
                .productVariantId(variant.getId())
                .colorName(variant.getColorValue().getValueName())
                .colorImage(variant.getColorValue().getValueImg())
                .build();

        return response;
    }
}
