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
public class ProductVariantResponse extends BaseResponse {
    private Long id;
    private Double basePrice;
    private Double salePrice;
    private Long colorId;
    private String colorName;
    private String size;
    private String productName;
    private Long productId;

    public static ProductVariantResponse fromProductVariant(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .basePrice(variant.getProduct().getBasePrice())
                .salePrice(variant.getAdjustedPrice())
                .colorId(variant.getColorValue().getId())
                .colorName(variant.getColorValue().getValueName())
                .size(variant.getSizeValue().getValueName())
                .productId(variant.getProduct().getId())
                .productName(variant.getProduct().getTranslationByLanguage("en").getName())
                .build();
    }
}
