package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.config.CouponConfig;
import com.example.DATN_Fashion_Shop_BE.dto.CouponTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import com.example.DATN_Fashion_Shop_BE.model.Holiday;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.repository.CouponRepository;
import com.example.DATN_Fashion_Shop_BE.repository.HolidayRepository;
import com.example.DATN_Fashion_Shop_BE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledCouponService {
    private final CouponConfigService couponConfigService; ;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;
    private final EmailService emailService;
    private final FileStorageService fileStorageService;
    private  final HolidayRepository holidayRepository;
    private final HolidayCouponTranslationService holidayCouponTranslationService;
//
    @Scheduled(cron = "0 0 0 * * ?")
//   @Scheduled(cron = "0 * * * * ?")
    public void generateDailyCoupons() {
        LocalDate today = LocalDate.now();
        log.info("🔄 Kiểm tra và tạo mã giảm giá cho ngày {}", today);

        // 🎊 Kiểm tra và tạo mã giảm giá cho ngày lễ
        generateHolidayCoupons(today);

        // 🎂 Kiểm tra và tạo mã giảm giá sinh nhật cho khách hàng
        generateBirthdayCoupons(today);
    }

    private void generateHolidayCoupons(LocalDate today) {
        List<Holiday> holidaysToday = holidayRepository.findByDate(today);
        List<User> allUsers = userRepository.findAll();
        List<User> femaleUsers = userRepository.findByGender("FEMALE");

        for (Holiday holiday : holidaysToday) {
            String holidayKey = couponConfigService.normalizeString(holiday.getHolidayName());
            String couponCode = holidayKey + "_" + today;

            CouponConfig config = couponConfigService.getCouponConfig(holidayKey);
            if (config == null) {
                log.warn("⚠️ Không tìm thấy cấu hình mã giảm giá cho {}! Bỏ qua.", holiday.getHolidayName());
                continue;
            }

            // ✅ Lấy bản dịch từ bảng history_translation
            List<CouponTranslationDTO> translations = holidayCouponTranslationService.getTranslationsByType(holidayKey);
            if (translations.isEmpty()) {
                translations = holidayCouponTranslationService.getTranslationsByType("HOLIDAY"); // Lấy mặc định
            }

            // Nếu KHÔNG phải ngày lễ dành riêng cho phụ nữ => tạo mã giảm giá cho tất cả
            if (!isWomenHoliday(holiday.getHolidayName())) {
                if (!couponRepository.existsByCode(couponCode)) {
                    Coupon coupon = couponService.createCouponForAllUser(
                            couponCode,
                            config.getDiscountType(),
                            config.getDiscountValue(),
                            config.getMinOrderValue(),
                            config.getExpirationDays(),
                            true,
                            config.getImageUrl(),
                            translations // ✅ Sử dụng bản dịch từ DB
                    );
                    log.info("🎊 Đã tạo mã giảm giá ngày lễ: {}!", coupon.getCode());

                    for (User user : allUsers) {
                        emailService.sendCouponEmail(user.getEmail(), coupon.getCode(), config.getExpirationDays(), holidayKey,"vi");
                    }
                }
            }

            // Nếu là ngày lễ dành cho phụ nữ, chỉ tạo mã cho nữ
            if (isWomenHoliday(holiday.getHolidayName())) {
                String womenCouponCode = "WOMEN_" + holidayKey + "_" + today;
                if (!couponRepository.existsByCode(womenCouponCode)) {
                    for (User user : femaleUsers) {
                        Coupon coupon = couponService.createCouponForUser(
                                womenCouponCode,
                                config.getDiscountType(),
                                config.getDiscountValue(),
                                config.getMinOrderValue(),
                                config.getExpirationDays(),
                                user,
                                config.getImageUrl(),
                                translations // ✅ Sử dụng bản dịch từ DB
                        );
                        log.info("💖 Đã tạo mã giảm giá {} cho user: {}", coupon.getCode(), user.getEmail());

                        emailService.sendCouponEmail(user.getEmail(), coupon.getCode() , config.getExpirationDays(), holidayKey,"vi");
                    }
                }
            }
        }
    }



    public boolean isWomenHoliday(String holidayName) {
        List<String> womenHolidays = List.of("Ngày Quốc tế Phụ nữ", "Ngày Phụ nữ Việt Nam");
        return womenHolidays.contains(holidayName);
    }

    public void generateBirthdayCoupons(LocalDate today) {
        List<User> usersWithBirthday = userRepository.findByDateOfBirth(today);
        CouponConfig config = couponConfigService.getCouponConfig("sinhnhat");
        if (config == null) {
            log.warn("⚠️ Không tìm thấy cấu hình mã giảm giá sinh nhật! Bỏ qua việc tạo mã.");
            return;
        }

        // ✅ Lấy danh sách bản dịch từ bảng history_translation
        List<CouponTranslationDTO> translations = holidayCouponTranslationService.getTranslationsByType("sinhnhat");
        if (translations.isEmpty()) {
            translations = holidayCouponTranslationService.getTranslationsByType("sinhnhat"); // Lấy mặc định nếu không có
        }

        String imageUrl = config.getImageUrl() != null ? config.getImageUrl() : "/uploads/coupons/BdayCoupon.png";

        for (User user : usersWithBirthday) {
            String couponCode = "BDAY_" + user.getId() + "_" + today.getYear();
            if (!couponRepository.existsByCode(couponCode)) {
                Coupon coupon = couponService.createCouponForUser(
                        couponCode,
                        config.getDiscountType(),
                        config.getDiscountValue(),
                        config.getMinOrderValue(),
                        config.getExpirationDays(),
                        user,
                        imageUrl,
                        translations // ✅ Sử dụng bản dịch từ DB
                );

                log.info("🎂 Đã tạo mã giảm giá sinh nhật {} cho user {}!", coupon.getCode(), user.getEmail());
                emailService.sendCouponEmail(user.getEmail(), coupon.getCode(),  config.getExpirationDays(), "sinhnhat","vi");
            }
        }
    }





}


