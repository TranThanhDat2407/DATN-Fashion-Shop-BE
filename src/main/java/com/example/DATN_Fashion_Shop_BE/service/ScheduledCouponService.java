package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.CouponTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.model.Coupon;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.repository.CouponRepository;
import com.example.DATN_Fashion_Shop_BE.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;
    private final EmailService emailService;


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
        List<LocalDate> holidaysForAllUsers = List.of(
                LocalDate.of(today.getYear(), 1, 1),  // Tết Dương Lịch
                LocalDate.of(today.getYear(), 4, 30), // Giải phóng miền Nam
                LocalDate.of(today.getYear(), 9, 2)   // Quốc khánh
        );
        List<LocalDate> holidaysForWomenOnly = List.of(
                LocalDate.of(today.getYear(), 3, 8),  // Ngày Quốc tế Phụ nữ
                LocalDate.of(today.getYear(), 10, 20) // Ngày Nhà giáo Việt Nam
        );
        if (holidaysForAllUsers.contains(today)) {
            // 🎉 Mã giảm giá cho tất cả người dùng
            String couponCode = "HOLIDAY_" + today;
            if (!couponRepository.existsByCode(couponCode)) {
                Coupon coupon = couponService.createCouponForAllUser(
                        couponCode, "PERCENTAGE", 15f, 200000f, 3, true,
                        "/uploads/coupons/holidayCoupon.png",COUPON_TRANSLATIONS.get("HOLIDAY")

                );
                log.info("🎊 Đã tạo mã giảm giá ngày lễ: {}!", coupon.getCode());

                // Lấy danh sách tất cả người dùng
                List<User> allUsers = userRepository.findAll();
                for (User user : allUsers) {
                    emailService.sendCouponEmail(user.getEmail(), coupon.getCode(),
                            "/uploads/coupons/holidayCoupon.png", 3, "HOLIDAY");
                }
            }
        }
        if (holidaysForWomenOnly.contains(today)) {
            // 🎀 Mã giảm giá chỉ cho người dùng nữ
            String couponCode = "WOMEN_DAY_" + today;
            if (!couponRepository.existsByCode(couponCode)) {
                List<User> femaleUsers = userRepository.findByGender("FEMALE"); // Lấy danh sách nữ
                for (User user : femaleUsers) {
                    Coupon coupon = couponService.createCouponForUser(
                            couponCode, "PERCENTAGE", 20f, 150000f, 5, user,
                            "/uploads/coupons/wonmendayCoupon.png",COUPON_TRANSLATIONS.get("HOLIDAY")
                            );
                    log.info("💖 Đã tạo mã giảm giá {} cho user: {}", coupon.getCode(), user.getEmail());
                    emailService.sendCouponEmail(user.getEmail(), coupon.getCode(),
                            "/uploads/coupons/wonmendayCoupon.png", 5, "WOMEN_DAY");
                }
            }
        }
    }
    private void generateBirthdayCoupons(LocalDate today) {
        List<User> usersWithBirthday = userRepository.findByDateOfBirth(today);
        for (User user : usersWithBirthday) {
            String couponCode = "BDAY_" + user.getId();
            if (!couponRepository.existsByCode(couponCode)) {
                Coupon coupon = couponService.createCouponForUser(
                        couponCode, "FIXED", 100000f, 300000f, 7, user,
                        "/uploads/coupons/BdayCoupon.png",COUPON_TRANSLATIONS.get("BIRTHDAY")
                        );
                log.info("🎂 Đã tạo mã giảm giá sinh nhật {} cho user {}!", coupon.getCode(), user.getId());
                emailService.sendCouponEmail(user.getEmail(), coupon.getCode(),
                        "BdayCoupon.png", 7, "BIRTHDAY");

            }
        }
    }
    static final Map<String, List<CouponTranslationDTO>> COUPON_TRANSLATIONS = Map.of(
            "BIRTHDAY", List.of(
                    new CouponTranslationDTO("Mã giảm giá sinh nhật", "Giảm giá nhân dịp sinh nhật", "vi"),
                    new CouponTranslationDTO("Birthday Discount", "Discount for your birthday", "en"),
                    new CouponTranslationDTO("誕生日割引", "誕生日の割引", "jp")
            ),
            "HOLIDAY", List.of(
                    new CouponTranslationDTO("Mã giảm giá ngày lễ", "Ưu đãi đặc biệt cho ngày lễ", "vi"),
                    new CouponTranslationDTO("Holiday Discount", "Special offer for the holiday", "en"),
                    new CouponTranslationDTO("祝日割引", "祝日の特別オファー", "jp")
            ),
            "WELCOME", List.of(
                    new CouponTranslationDTO("Mã giảm giá chào mừng", "Ưu đãi cho khách hàng mới", "vi"),
                    new CouponTranslationDTO("Welcome Discount", "Special offer for new users", "en"),
                    new CouponTranslationDTO("ウェルカム割引", "新規ユーザー向けの特別オファー", "jp")
            )
    );

}


