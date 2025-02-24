package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import com.example.DATN_Fashion_Shop_BE.model.ProductMedia;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductMediaDTO {
    private Long id;
    private String mediaUrl;
    private String mediaType;
    private Long colorId;
    private boolean hasVariants;

    public static ProductMediaDTO fromProductMedia(ProductMedia productMedia) {
        return ProductMediaDTO.builder()
                .id(productMedia.getId())
                .mediaUrl(productMedia.getMediaUrl())
                .mediaType(productMedia.getMediaType())
                .colorId(productMedia.getColorValue() != null
                        ? productMedia.getColorValue().getId() : null)
                .hasVariants(productMedia.getProductVariants() != null
                        && !productMedia.getProductVariants().isEmpty()) // Kiểm tra có variant hay không
                .build();
    }
}
