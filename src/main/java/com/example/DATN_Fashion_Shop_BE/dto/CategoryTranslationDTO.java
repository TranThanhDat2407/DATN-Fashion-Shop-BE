package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.CategoriesTranslation;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryTranslationDTO {
    @NotNull(message = MessageKeys.CATEGORY_TRANSLATION_LANGUAGE_REQUIRED)
    private String languageCode; // Mã ngôn ngữ (vd: vi, en, jp)
    @NotEmpty(message = MessageKeys.CATEGORY_TRANSLATION_NAME_REQUIRED)
    private String name; // Tên bản dịch

    public static CategoryTranslationDTO fromCategoryTranslation(CategoriesTranslation categoryTranslation) {
        return CategoryTranslationDTO.builder()
                .name(categoryTranslation.getName())
                .languageCode(categoryTranslation.getLanguage().getCode())
                .build();
    }
}
