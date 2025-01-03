package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Banner;
import com.example.DATN_Fashion_Shop_BE.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByIsActive(Boolean isActive);
    @Query("SELECT DISTINCT b FROM Banner b " +
            "JOIN BannersTranslation bt ON bt.banner.id = b.id " +
            "WHERE " +
            "(COALESCE(:title, '') = '' OR LOWER(bt.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:isActive IS NULL OR b.isActive = :isActive)")
    Page<Banner> findBannersByFillers(
            @Param("title") String name,
            @Param("isActive") Boolean isActive,
            Pageable pageable);
}
