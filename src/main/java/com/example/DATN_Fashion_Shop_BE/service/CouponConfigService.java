package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.config.CouponConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class CouponConfigService {
    private final Map<String, CouponConfig> couponConfigMap = new HashMap<>();

    @PostConstruct
    public void init() {
        couponConfigMap.put("birthday", new CouponConfig("PERCENTAGE", 20f, 150000f, 5, "/uploads/coupons/birthday.png"));
        couponConfigMap.put("holiday", new CouponConfig("PERCENTAGE", 15f, 200000f, 3, "/uploads/coupons/holiday.png"));
        couponConfigMap.put("women_day", new CouponConfig("PERCENTAGE", 20f, 150000f, 5, "/uploads/coupons/women_day.png"));
    }
    // Lấy cấu hình theo loại mã
    public CouponConfig getCouponConfig(String type) {
        return couponConfigMap.getOrDefault(type, new CouponConfig());
    }
    public void updateCouponConfig(String type, CouponConfig newConfig) {
        if (couponConfigMap.containsKey(type)) {
            CouponConfig oldConfig = couponConfigMap.get(type);

            // Chỉ cập nhật nếu có dữ liệu mới
            if (newConfig.getDiscountType() != null) oldConfig.setDiscountType(newConfig.getDiscountType());
            if (newConfig.getDiscountValue() != null) oldConfig.setDiscountValue(newConfig.getDiscountValue());
            if (newConfig.getMinOrderValue() != null) oldConfig.setMinOrderValue(newConfig.getMinOrderValue());
            if (newConfig.getExpirationDays() != 0) oldConfig.setExpirationDays(newConfig.getExpirationDays());
            if (newConfig.getImageUrl() != null) oldConfig.setImageUrl(newConfig.getImageUrl());


        }
    }

}
