package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.CouponDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponLocalizedDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.coupon.CouponCreateRequestDTO;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final UserCouponUsageRepository userCouponUsageRepository;
    private final LanguageRepository languageRepository;
    private final CouponTranslationRepository couponTranslationRepository;
    private final UserRepository userRepository;
    private final CouponUserRestrictionRepository couponUserRestrictionRepository;
    public boolean applyCoupon(Long userId, String couponCode) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(couponCode);
        if (couponOpt.isEmpty()) {
            throw new RuntimeException("Mã giảm giá không tồn tại.");
        }

        Coupon coupon = couponOpt.get();
        if (!coupon.getIsActive()) {
            throw new RuntimeException("Mã giảm giá không còn hiệu lực.");
        }

        // Kiểm tra xem user đã sử dụng mã này chưa
        boolean hasUsedCoupon = userCouponUsageRepository.existsByUserIdAndCouponId(userId, coupon.getId());
        if (hasUsedCoupon) {
            throw new RuntimeException("Bạn đã sử dụng mã giảm giá này rồi.");
        }

        // Lưu lịch sử sử dụng mã giảm giá
        UserCouponUsage usage = UserCouponUsage.builder()
                .user(User.builder().id(userId).build())
                .coupon(coupon)
                .used(true)
                .build();
        userCouponUsageRepository.save(usage);

        return true;
    }
    @Transactional
    public CouponDTO createCoupon(CouponCreateRequestDTO request) {
        // 1️⃣ Tạo Coupon
        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .expirationDate(request.getExpirationDate())
                .isActive(true)
                .isGlobal(request.getIsGlobal()) // Set isGlobal
                .build();

        coupon = couponRepository.save(coupon);


        if (!request.getIsGlobal() && request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            List<User> users = userRepository.findAllById(request.getUserIds());
            Coupon finalCoupon = coupon;
            List<CouponUserRestriction> restrictions = users.stream()
                    .map(user -> CouponUserRestriction.builder()
                            .user(user)
                            .coupon(finalCoupon)
                            .build())
                    .collect(Collectors.toList());
            couponUserRestrictionRepository.saveAll(restrictions);
        }
        Coupon finalCoupon = coupon;
        List<CouponTranslation> translations = request.getTranslations().stream()
                .map(translationDTO -> {
                    Language language = languageRepository.findByCode(translationDTO.getLanguageCode())
                            .orElseThrow(() -> new RuntimeException("Language not found for code: " + translationDTO.getLanguageCode()));

                    return CouponTranslation.builder()
                            .name(translationDTO.getName())
                            .description(translationDTO.getDescription())
                            .coupon(finalCoupon)
                            .language(language)
                            .build();
                })
                .collect(Collectors.toList());

        couponTranslationRepository.saveAll(translations);
        return CouponDTO.fromCoupon(coupon);
    }

    @Transactional
    public CouponDTO updateCoupon(Long id, CouponCreateRequestDTO request) {
        // 1️. Lấy coupon cần cập nhật
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        // 2️. Cập nhật thông tin coupon
        coupon.setCode(request.getCode());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderValue(request.getMinOrderValue());
        coupon.setExpirationDate(request.getExpirationDate());

        coupon = couponRepository.save(coupon);

        // 3️. Xóa bản dịch cũ và thêm bản dịch mới
        couponTranslationRepository.deleteByCouponId(id); // Xóa bản dịch cũ

        Coupon finalCoupon = coupon;
        List<CouponTranslation> translations = request.getTranslations().stream()
                .map(translationDTO -> {
                    Language language = languageRepository.findByCode(translationDTO.getLanguageCode())
                            .orElseThrow(() -> new RuntimeException("Language not found for code: " + translationDTO.getLanguageCode()));

                    return CouponTranslation.builder()
                            .name(translationDTO.getName())
                            .description(translationDTO.getDescription())
                            .coupon(finalCoupon) // Gán lại coupon sau khi cập nhật
                            .language(language)
                            .build();
                })
                .collect(Collectors.toList());

        couponTranslationRepository.saveAll(translations);

        return CouponDTO.fromCoupon(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        if (!couponRepository.existsById(id)) {
            throw new RuntimeException("Coupon not found");
        }

        couponTranslationRepository.deleteByCouponId(id); // Xóa bản dịch trước
        couponRepository.deleteById(id); // Xóa coupon
    }



    public List<CouponLocalizedDTO> getAllCoupons(String languageCode) {
        List<Coupon> coupons = couponRepository.findAll();

        return coupons.stream().map(coupon -> {
            // Lấy danh sách userId được quyền sử dụng coupon
            List<Long> allowedUserIds = couponUserRestrictionRepository.findUserIdsByCouponId(coupon.getId());

            // Lấy bản dịch của coupon theo ngôn ngữ
            CouponTranslation translation = coupon.getCouponTranslationByLanguage(languageCode);

            return CouponLocalizedDTO.fromCoupons(coupon, translation, allowedUserIds);
        }).collect(Collectors.toList());
    }


    public List<CouponLocalizedDTO> getCouponsForUser(Long userId, String languageCode) {
        // Lấy danh sách mã giảm giá áp dụng cho tất cả user
        List<Coupon> globalCoupons = couponRepository.findByIsGlobalTrueAndIsActiveTrue();

        // Lấy danh sách mã giảm giá dành riêng cho user
        List<Coupon> userSpecificCoupons = couponRepository.findCouponsByUserId(userId);

        // Hợp nhất 2 danh sách
        Set<Coupon> availableCoupons = new HashSet<>();
        availableCoupons.addAll(globalCoupons);
        availableCoupons.addAll(userSpecificCoupons);

        // Lấy danh sách mã user đã sử dụng
        List<Long> usedCouponIds = userCouponUsageRepository.findUsedCouponIdsByUserId(userId);

        return availableCoupons.stream()
                .filter(coupon -> !usedCouponIds.contains(coupon.getId())) // Lọc mã đã dùng
                .map(coupon -> {
                    CouponTranslation translation = coupon.getCouponTranslationByLanguage(languageCode);

                    // Lấy danh sách user được phép sử dụng mã
                    List<Long> allowedUserIds = couponUserRestrictionRepository.findUserIdsByCouponId(coupon.getId());

                    return CouponLocalizedDTO.fromCoupons(coupon, translation, allowedUserIds);
                })
                .collect(Collectors.toList());
    }


}
