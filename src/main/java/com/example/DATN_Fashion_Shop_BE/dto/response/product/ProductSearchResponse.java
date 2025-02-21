package com.example.DATN_Fashion_Shop_BE.dto.response.product;
import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import com.example.DATN_Fashion_Shop_BE.model.ProductsTranslation;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchResponse{
    private Long id;
    private String name;
    private String imageUrl;

    public static ProductSearchResponse fromTranslation(ProductsTranslation trans) {
        return ProductSearchResponse.builder()
                .id(trans.getProduct().getId())
                .name(trans.getName())
                .imageUrl( trans.getProduct().getMedias().isEmpty() ? null :
                        trans.getProduct().getMedias().getFirst().getMediaUrl())
                .build();
    }
}
