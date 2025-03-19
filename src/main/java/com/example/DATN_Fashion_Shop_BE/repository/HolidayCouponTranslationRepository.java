package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.model.HolidayCouponTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HolidayCouponTranslationRepository extends JpaRepository<HolidayCouponTranslation, Long> {
    List<HolidayCouponTranslation> findByCouponType(String couponType);
    Optional<HolidayCouponTranslation> findByCouponTypeAndLanguageCode(String couponType, String languageCode);
}