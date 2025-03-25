package com.example.DATN_Fashion_Shop_BE.repository;

import com.example.DATN_Fashion_Shop_BE.config.CouponConfig;
import com.example.DATN_Fashion_Shop_BE.model.CouponConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponConfigRepository extends JpaRepository<CouponConfigEntity, Long> {
    Optional<CouponConfigEntity> findByType(String type);


}
