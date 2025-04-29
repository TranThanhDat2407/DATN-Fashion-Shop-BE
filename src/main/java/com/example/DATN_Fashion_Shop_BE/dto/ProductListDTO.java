package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.ProductsTranslation;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductListDTO extends BaseResponse{
    private Long id;
    private String name;
    private Double basePrice;
    private Boolean isActive;
    private PromotionDTO promotion;

    public static ProductListDTO fromProduct(Product product,
                                             ProductsTranslation translation) {

        ProductListDTO builder = ProductListDTO.builder()
                .id(product.getId())
                .name(translation.getName() != null ? translation.getName()  : "") // Product name translation
                .basePrice(product.getBasePrice())
                .isActive(product.getIsActive())
                .promotion(product.getPromotion() != null ? PromotionDTO.fromPromotion(product.getPromotion()) : null)
                .build();
        builder.setCreatedAt(product.getCreatedAt());
        builder.setUpdatedAt(product.getUpdatedAt());
        return builder;

    }
}
