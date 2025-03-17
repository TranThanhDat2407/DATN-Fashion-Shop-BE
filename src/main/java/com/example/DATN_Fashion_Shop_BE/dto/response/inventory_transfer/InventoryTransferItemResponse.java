package com.example.DATN_Fashion_Shop_BE.dto.response.inventory_transfer;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryTransferItemResponse extends BaseResponse {
    private Long productVariantId;
    private String productName;
    private String productImage;
    private String colorImage;
    private String colorName;
    private String size;
    private Integer quantity;

    public static InventoryTransferItemResponse fromInventoryTransferItem(InventoryTransferItem item, String langCode) {
        ProductVariant variant = item.getProductVariant();
        Product product = variant.getProduct();
        AttributeValue color = variant.getColorValue();

        // Chọn ảnh sản phẩm phù hợp với màu sắc
        String productImage = product.getMedias().stream()
                .filter(media -> media.getColorValue() != null && color != null && media.getColorValue().getId().equals(color.getId()))
                .map(ProductMedia::getMediaUrl)
                .findFirst()
                .orElse(product.getMedias().isEmpty() ? null : product.getMedias().get(0).getMediaUrl());

        return InventoryTransferItemResponse.builder()
                .productVariantId(variant.getId())
                .productName(product.getTranslationByLanguage(langCode).getName())
                .productImage(productImage)
                .colorName(color.getValueName())
                .colorImage(color.getValueImg())
                .size(variant.getSizeValue().getValueName())
                .quantity(item.getQuantity())
                .build();
    }
}
