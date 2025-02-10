package com.example.DATN_Fashion_Shop_BE.dto.response.product;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditProductResponse {
    private Long id; // ID của sản phẩm
    private String status; // Trạng thái của sản phẩm
    private Double basePrice; // Giá cơ bản
    private Boolean isActive; // Trạng thái hoạt động
    private List<ProductTranslationResponse> translations; // Danh sách bản dịch

    public static EditProductResponse fromProduct(Product product) {
        List<ProductTranslationResponse> translationResponses = product.getTranslations().stream()
                .map(ProductTranslationResponse::fromProductsTranslation)
                .collect(Collectors.toList());
        return EditProductResponse.builder()
                .id(product.getId())
                .status(product.getStatus())
                .basePrice(product.getBasePrice())
                .isActive(product.getIsActive())
                .translations(translationResponses)
                .build();
    }
}
