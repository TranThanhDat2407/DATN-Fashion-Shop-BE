package com.example.DATN_Fashion_Shop_BE.dto.response.category;

import com.example.DATN_Fashion_Shop_BE.dto.CategoryTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.Category;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryEditResponseDTO extends BaseResponse {
    private Long id;
    private String imageUrl;
    private Long parentId;
    private Boolean isActive;
    private List<CategoryTranslationDTO> translations;

    public static CategoryEditResponseDTO fromCategory(Category category) {
        CategoryEditResponseDTO response = CategoryEditResponseDTO.builder()
                .id(category.getId())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                .isActive(category.getIsActive())
                .translations(category.getTranslations() != null
                        ? category.getTranslations().stream()
                        .map(CategoryTranslationDTO::fromCategoryTranslation)
                        .collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
        response.setCreatedAt(response.getCreatedAt());
        response.setUpdatedAt(response.getUpdatedAt());
        response.setUpdatedBy(response.getUpdatedBy());
        response.setCreatedBy(response.getCreatedBy());
        return response;
    }
}
