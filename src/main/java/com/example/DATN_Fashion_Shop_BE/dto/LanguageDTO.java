package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.Language;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LanguageDTO {
    private String code;
    private String name;

    public static LanguageDTO fromLanguage(Language language) {
        return LanguageDTO.builder()
                .name(language.getName())
                .code(language.getCode())
                .build();
    }
}
