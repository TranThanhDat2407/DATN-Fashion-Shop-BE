package com.example.DATN_Fashion_Shop_BE.dto.response.product;
import com.example.DATN_Fashion_Shop_BE.dto.response.FieldErrorDetails;
import com.example.DATN_Fashion_Shop_BE.model.ProductsTranslation;
import lombok.*;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductTranslationResponse{
    private String name; // Tên sản phẩm
    private String description; // Mô tả sản phẩm
    private String material; // Chất liệu
    private String care; // Hướng dẫn bảo quản
    private String languageCode; // Mã ngôn ngữ

    public static ProductTranslationResponse fromProductsTranslation(ProductsTranslation translation) {
        return ProductTranslationResponse.builder()
                .name(translation.getName())
                .description(translation.getDescription())
                .material(translation.getMaterial())
                .care(translation.getCare())
                .languageCode(translation.getLanguage().getCode())
                .build();
    }
}
