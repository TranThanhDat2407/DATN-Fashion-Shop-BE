package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.BannersTranslation;
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
public class BannerTranslationDTO {
    @NotNull(message = MessageKeys.CATEGORY_TRANSLATION_LANGUAGE_REQUIRED)
    private String languageCode; // Mã ngôn ngữ (vd: vi, en, jp)
    @NotEmpty(message = MessageKeys.CATEGORY_TRANSLATION_NAME_REQUIRED)
    private String title;
    @NotEmpty(message = MessageKeys.CATEGORY_TRANSLATION_NAME_REQUIRED)
    private String subtitle;

    public static BannerTranslationDTO fromBannersTranslation(BannersTranslation translation) {
        return BannerTranslationDTO.builder()
                .languageCode(translation.getLanguage().getCode())
                .title(translation.getTitle())
                .subtitle(translation.getSubtitle())
                .build();
    }
}
