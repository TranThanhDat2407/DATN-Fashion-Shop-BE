package com.example.DATN_Fashion_Shop_BE.dto;

import com.example.DATN_Fashion_Shop_BE.model.Banner;
import com.example.DATN_Fashion_Shop_BE.model.BannersTranslation;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BannerDTO {
    private Long id;
    private String logoUrl;
    private String mediaUrl;
    private String redirectUrl;
    private Boolean isActive;
    private String title;
    private String subtitle;

    public static BannerDTO fromBanner(Banner banner, BannersTranslation translation) {
        return BannerDTO.builder()
                .id(banner.getId())
                .logoUrl(banner.getLogoUrl())
                .mediaUrl(banner.getMediaUrl())
                .redirectUrl(banner.getRedirectUrl())
                .isActive(banner.getIsActive())
                .title(translation != null ? translation.getTitle() : "")
                .subtitle(translation != null ? translation.getSubtitle() : "")
                .build();
    }
}
