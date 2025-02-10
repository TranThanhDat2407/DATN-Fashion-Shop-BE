package com.example.DATN_Fashion_Shop_BE.dto.response.banner;

import com.example.DATN_Fashion_Shop_BE.model.Banner;
import com.example.DATN_Fashion_Shop_BE.model.BannersTranslation;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BannerAdminResponseDTO {
    private Long id;
    private String logoUrl;
    private String mediaUrl;
    private Boolean isActive;
    private String title;
    private String subtitle;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime activationDate; // Ngày giờ kích hoạt
    private LocalDateTime endDate; // Ngày giờ kết thúc

    public static BannerAdminResponseDTO fromBanner(Banner banner, BannersTranslation bannerTranslation) {
        return BannerAdminResponseDTO.builder()
                .id(banner.getId())
                .logoUrl(banner.getLogoUrl())
                .mediaUrl(banner.getMediaUrl())
                .isActive(banner.getIsActive())
                .title(bannerTranslation.getTitle()) // Dùng title từ BannerTranslation
                .subtitle(bannerTranslation.getSubtitle()) // Dùng subtitle từ BannerTranslation
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .activationDate(banner.getActivationDate())
                .endDate(banner.getEndDate())
                .build();
    }
}
