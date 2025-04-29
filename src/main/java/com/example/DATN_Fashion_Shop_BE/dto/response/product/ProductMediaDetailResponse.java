package com.example.DATN_Fashion_Shop_BE.dto.response.product;
import com.example.DATN_Fashion_Shop_BE.model.ProductMedia;
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
public class ProductMediaDetailResponse {
    private Long id;
    private String mediaUrl;
    private Long colorId;
    private String mediaType;
    private Integer modelHeight;

    public static ProductMediaDetailResponse fromProductMedia(ProductMedia media) {
        return ProductMediaDetailResponse.builder()
                .id(media.getId())
                .mediaUrl(media.getMediaUrl())
                .colorId(media.getColorValue() != null ? media.getColorValue().getId() : null)
                .mediaType(media.getMediaType())
                .modelHeight(media.getModelHeight())
                .build();
    }
}
