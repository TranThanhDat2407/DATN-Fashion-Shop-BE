package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import com.example.DATN_Fashion_Shop_BE.model.CouponTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponTranslationRepository extends JpaRepository<CouponTranslation, Long> {

    // Lấy danh sách bản dịch của một coupon theo couponId
    List<CouponTranslation> findByCouponId(Long couponId);

    // Lấy bản dịch của coupon theo mã ngôn ngữ
    Optional<CouponTranslation> findByCouponIdAndLanguageCode(Long couponId, String languageCode);

    void deleteByCouponId(Long id);

    Optional<CouponTranslation> findByCoupon(Coupon coupon);
}

