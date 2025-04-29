package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.Category;
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
public class ProductCategoryDTO {
    private Long id;
    private String name;

    public static ProductCategoryDTO fromCategory(Category category, String translatedName) {
        return ProductCategoryDTO.builder()
                .id(category.getId()) // Map the category ID
                .name(translatedName != null ? translatedName : "") // Use the translated name or an empty string
                .build();
    }
}
