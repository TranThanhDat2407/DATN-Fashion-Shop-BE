package com.example.DATN_Fashion_Shop_BE.service;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledCouponService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final CouponService couponService;
//    @Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void generateBirthdayCouponsForUsers() {
        LocalDate today = LocalDate.now();
        List<User> usersWithBirthday = userRepository.findByDateOfBirth(today);
        System.out.println("🔍 Users with birthday today: " + usersWithBirthday.size());
        if (!usersWithBirthday.isEmpty()) {
            log.info("🎉 Tạo mã giảm giá cho {} user có sinh nhật hôm nay!", usersWithBirthday.size());
            couponService.generateBirthdayCoupons(usersWithBirthday);
        } else {
            log.info("❌ Hôm nay không có user nào sinh nhật.");
        }
    }
}


