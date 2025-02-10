package com.example.DATN_Fashion_Shop_BE.dto.response.product;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.ProductMedia;
import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import lombok.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductMediaResponse {
    private Long id;
    private String mediaUrl;
    private String mediaType;
    private Integer sortOrder;
    private Integer modelHeight;
    private Long colorValueId;
    private Long productId;
    private List<ProductVariantResponse> productVariants;

    public static ProductMediaResponse fromProductMedia(ProductMedia media) {
        return ProductMediaResponse.builder()
                .id(media.getId())
                .mediaUrl(media.getMediaUrl())
                .mediaType(media.getMediaType())
                .sortOrder(media.getSortOrder())
                .modelHeight(media.getModelHeight())
                .colorValueId(media.getColorValue() != null ? media.getColorValue().getId() : null)
                .productId(media.getProduct() != null ? media.getProduct().getId() : null)
                .productVariants(
                        media.getProductVariants() != null
                                ? media.getProductVariants().stream()
                                .map(ProductVariantResponse::fromProductVariant)
                                .collect(Collectors.toList())
                                : Collections.emptyList()
                )
                .build();
    }
}
