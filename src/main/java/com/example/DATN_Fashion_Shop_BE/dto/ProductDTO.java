package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.Product;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private Double basePrice;
    private Boolean isActive;
    private String promotionName;

    public static ProductDTO fromProduct(Product product, String translationName) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(translationName != null ? translationName : "") // Sử dụng tên bản dịch
                .basePrice(product.getBasePrice())
                .isActive(product.getIsActive())
                .promotionName(product.getPromotion() != null ? product.getPromotion().getDescriptions() : null)
                .build();
    }
}
