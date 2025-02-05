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
    private String color;
    private String size;
    private String productName;

    public static ProductVariantResponse fromProductVariant(ProductVariant variant) {
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .basePrice(variant.getProduct().getBasePrice())
                .salePrice(variant.getSalePrice())
                .color(variant.getColorValue().getValueName())
                .size(variant.getSizeValue().getValueName())
                .productName(variant.getProduct().getTranslationByLanguage("en").getName())
                .build();
    }
}
