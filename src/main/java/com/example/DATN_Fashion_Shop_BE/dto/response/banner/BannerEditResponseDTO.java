package com.example.DATN_Fashion_Shop_BE.dto.response.banner;

import com.example.DATN_Fashion_Shop_BE.dto.BannerTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.model.Banner;
import com.example.DATN_Fashion_Shop_BE.model.BannersTranslation;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BannerEditResponseDTO {
    private Long id;
    private String mediaURL;
    private String redirectURL;
    private String logoURL;
    private Boolean isActive;
    private List<BannerTranslationDTO> translations;

    public static BannerEditResponseDTO fromBanner(Banner banner, List<BannersTranslation> translations) {
        List<BannerTranslationDTO> translationDTOs = translations.stream()
                .map(BannerTranslationDTO::fromBannersTranslation)
                .collect(Collectors.toList());

        return BannerEditResponseDTO.builder()
                .id(banner.getId())
                .mediaURL(banner.getMediaUrl())
                .redirectURL(banner.getRedirectUrl())
                .logoURL(banner.getLogoUrl())
                .isActive(banner.getIsActive())
                .translations(translationDTOs)
                .build();
    }
}
