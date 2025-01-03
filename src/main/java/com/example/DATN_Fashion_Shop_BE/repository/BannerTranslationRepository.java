package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.BannersTranslation;
import com.example.DATN_Fashion_Shop_BE.model.CategoriesTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BannerTranslationRepository extends JpaRepository<BannersTranslation, Long> {
    List<BannersTranslation> findByBannerIdInAndLanguageCode(List<Long> bannerIds, String languageCode);
    Optional<BannersTranslation> findByBannerIdAndLanguageCode(Long categoryId, String languageCode);
    List<BannersTranslation> findByBannerId(Long id);
}
