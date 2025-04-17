package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.config.CouponConfig;
import com.example.DATN_Fashion_Shop_BE.model.CouponConfigEntity;
import com.example.DATN_Fashion_Shop_BE.model.Holiday;
import com.example.DATN_Fashion_Shop_BE.repository.CouponConfigRepository;
import com.example.DATN_Fashion_Shop_BE.repository.HolidayRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;


@Slf4j
@Service
@RequiredArgsConstructor
public class CouponConfigService {
    private final HolidayRepository holidayRepository;
    private final Map<String, CouponConfig> couponConfigMap = new HashMap<>();
    private final CouponConfigRepository couponConfigRepository;

    @PostConstruct
    public void init() {
        // Load từ DB
        List<CouponConfigEntity> savedConfigs = couponConfigRepository.findAll();
        for (CouponConfigEntity config : savedConfigs) {
            couponConfigMap.put(config.getType(), config);
        }
        log.info("✅ Đã load {} cấu hình từ bảng coupon_config.", savedConfigs.size());

        // Load ngày lễ
        List<Holiday> holidays = holidayRepository.findAll();
        for (Holiday holiday : holidays) {
            String holidayKey = normalizeString(holiday.getHolidayName());

            if (!couponConfigMap.containsKey(holidayKey)) {
                CouponConfigEntity newConfig = CouponConfigEntity.builder()
                        .type(holidayKey)
                        .build();
                couponConfigRepository.save(newConfig);
                couponConfigMap.put(holidayKey, newConfig);
                log.info("🆕 Đã thêm cấu hình mới cho ngày lễ: {}", holidayKey);
            }
        }

        // Kiểm tra mã chao thanh vien moi
        if (!couponConfigMap.containsKey("chaomungthanhvienmoi")) {
            CouponConfigEntity welcomeConfig = CouponConfigEntity.builder()
                    .type("chaomungthanhvienmoi")
                    .build();
            couponConfigRepository.save(welcomeConfig);
            couponConfigMap.put("chaomungthanhvienmoi", welcomeConfig);
            log.info("🆕 Đã thêm cấu hình mặc định cho chaomungthanhvienmoi: {}.");
        }
        // Kiểm tra mã sinh nhật
        if (!couponConfigMap.containsKey("sinhnhat")) {
            CouponConfigEntity birthdayConfig = CouponConfigEntity.builder()
                    .type("sinhnhat")
                    .build();
            couponConfigRepository.save(birthdayConfig);
            couponConfigMap.put("sinhnhat", birthdayConfig);
            log.info("🆕 Đã thêm cấu hình mặc định cho mã sinh nhật.");
        }
    }
    public Map<String, CouponConfig> getAllCouponConfigs() {
        return couponConfigMap;
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

            // Cập nhật vào Map
            couponConfigMap.put(normalizedType, oldConfig);

            // Lưu vào database
            CouponConfigEntity entity = couponConfigRepository.findByType(normalizedType)
                    .orElse(new CouponConfigEntity());

            entity.setType(normalizedType);
            entity.setDiscountType(oldConfig.getDiscountType());
            entity.setDiscountValue(oldConfig.getDiscountValue());
            entity.setMinOrderValue(oldConfig.getMinOrderValue());
            entity.setExpirationDays(oldConfig.getExpirationDays());
            entity.setImageUrl(oldConfig.getImageUrl());

            couponConfigRepository.save(entity); // Lưu vào DB

            log.info("✅ Sau: {}", oldConfig);
        } else {
            log.warn("⚠️ Không tìm thấy loại mã: '{}' trong couponConfigMap! Không thể cập nhật.", normalizedType);
        }

        log.info("📌 Danh sách couponConfigMap: {}", couponConfigMap.keySet());
    }

    public String normalizeString(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return Pattern.compile("\\p{M}").matcher(normalized).replaceAll("") // Xóa dấu
                .toLowerCase()
                .replace("đ", "d") // Xử lý riêng chữ "đ"
                .replaceAll("[^a-z0-9]", ""); // Xóa ký tự đặc biệt và khoảng trắng
    }

    @Transactional
    public boolean resetCouponConfig(String type) {
        Optional<CouponConfigEntity> couponOptional = couponConfigRepository.findByType(type);
        if (couponOptional.isPresent()) {
            CouponConfigEntity coupon = couponOptional.get();
            coupon.setDiscountType(null);
            coupon.setDiscountValue(null);
            coupon.setMinOrderValue(null);
            coupon.setExpirationDays(0);
            coupon.setImageUrl(null);
            couponConfigRepository.save(coupon);

            // Gọi lại phương thức để làm mới couponConfigMap
            loadCouponConfigsFromDatabase();
            return true;
        }
        return false;
    }

    public void loadCouponConfigsFromDatabase() {
        List<CouponConfigEntity> savedConfigs = couponConfigRepository.findAll();
        couponConfigMap.clear(); // Xóa dữ liệu cũ
        for (CouponConfigEntity config : savedConfigs) {
            CouponConfig couponConfig = new CouponConfig(config);
            couponConfigMap.put(config.getType(), couponConfig);
        }
        log.info("✅ Đã làm mới couponConfigMap từ cơ sở dữ liệu.");
    }

    public Map<String, CouponConfig> getValidCouponConfigs() {
        loadCouponConfigsFromDatabase(); // Đảm bảo dữ liệu mới được load từ DB
        Map<String, CouponConfig> validCouponConfigs = new HashMap<>();
        for (Map.Entry<String, CouponConfig> entry : couponConfigMap.entrySet()) {
            CouponConfig config = entry.getValue();
            if (config.getDiscountType() != null ||
                    config.getDiscountValue() != null ||
                    config.getMinOrderValue() != null ||
                    config.getImageUrl() != null ||
                    (config.getExpirationDays() != 0 && config.getExpirationDays() > 0)) {
                validCouponConfigs.put(entry.getKey(), config);
            }
        }
        return validCouponConfigs;
    }



}
