package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.config.CouponConfig;
import com.example.DATN_Fashion_Shop_BE.model.Holiday;
import com.example.DATN_Fashion_Shop_BE.repository.HolidayRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class CouponConfigService {
    private final HolidayRepository holidayRepository;
    private final Map<String, CouponConfig> couponConfigMap = new HashMap<>();

    @PostConstruct
    public void init() {
        List<Holiday> holidays = holidayRepository.findAll();
        for (Holiday holiday : holidays) {
            String holidayKey = normalizeString(holiday.getHolidayName());
            couponConfigMap.put(holidayKey, new CouponConfig(
                    null,
                    null,
                    null,
                    0,
                    null
            ));
        }
        log.info("✅ Đã load {} cấu hình giảm giá từ bảng holiday.", holidays.size());

        // Thêm cấu hình mặc định cho sinh nhật
        couponConfigMap.put("birthday", new CouponConfig(
                null,
                null,
                null,
                0,
                null
        ));


    }

    // Lấy cấu hình theo loại mã
    public CouponConfig getCouponConfig(String type) {
        String normalizedType = normalizeString(type);
        return couponConfigMap.getOrDefault(normalizedType, new CouponConfig());
    }
    public void updateCouponConfig(String type, CouponConfig newConfig) {
        String normalizedType = normalizeString(type);
        if (couponConfigMap.containsKey(normalizedType)) {
            CouponConfig oldConfig = couponConfigMap.get(normalizedType);

            log.info("🔄 Cập nhật mã giảm giá cho loại: {}", normalizedType);
            log.info("📝 Trước: {}", oldConfig);

            // Cập nhật dữ liệu
            if (newConfig.getDiscountType() != null) oldConfig.setDiscountType(newConfig.getDiscountType());
            if (newConfig.getDiscountValue() != null) oldConfig.setDiscountValue(newConfig.getDiscountValue());
            if (newConfig.getMinOrderValue() != null) oldConfig.setMinOrderValue(newConfig.getMinOrderValue());
            if (newConfig.getExpirationDays() != 0) oldConfig.setExpirationDays(newConfig.getExpirationDays());
            if (newConfig.getImageUrl() != null) oldConfig.setImageUrl(newConfig.getImageUrl());

            log.info("✅ Sau: {}", oldConfig);
        } else {
            log.warn("⚠️ Không tìm thấy loại mã: {} trong couponConfigMap! Không thể cập nhật.", normalizedType);
        }
        log.info("📌 Danh sách couponConfigMap: {}", couponConfigMap);
    }

    public String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return Pattern.compile("\\p{M}").matcher(normalized).replaceAll("") // Xóa dấu
                .toLowerCase()
                .replace("đ", "d") // Xử lý riêng chữ "đ"
                .replaceAll("[^a-z0-9]", ""); // Xóa ký tự đặc biệt và khoảng trắng
    }




}
