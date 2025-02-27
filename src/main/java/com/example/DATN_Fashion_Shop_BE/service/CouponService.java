package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.CouponDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponLocalizedDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.coupon.CouponCreateRequestDTO;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.specification.CouponSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
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
    private final EmailService emailService;
    private final FileStorageService fileStorageService;


    public boolean applyCoupon(Long userId, String couponCode) {
        Optional<Coupon> couponOpt = couponRepository.findFirstByCode(couponCode);
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
    public CouponDTO createCoupon(CouponCreateRequestDTO request, MultipartFile imageFile) {
        String imageUrl = null;

        // Kiểm tra nếu có ảnh thì upload
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = fileStorageService.uploadFileAndGetName(imageFile, "coupons");
        }

        // 1️⃣ Tạo Coupon
        Coupon coupon = Coupon.builder()
                .code(request.getCode())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderValue(request.getMinOrderValue())
                .expirationDate(request.getExpirationDate())
                .isActive(true)
                .isGlobal(request.getIsGlobal()) // Set isGlobal
                .imageUrl(imageUrl) // Lưu đường dẫn ảnh vào DB
                .build();

        coupon = couponRepository.save(coupon);

        // 2️⃣ Nếu không phải global, tạo ràng buộc với user
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

        // 3️⃣ Lưu bản dịch coupon
        Coupon finalCoupon1 = coupon;
        List<CouponTranslation> translations = request.getTranslations().stream()
                .map(translationDTO -> {
                    Language language = languageRepository.findByCode(translationDTO.getLanguageCode())
                            .orElseThrow(() -> new RuntimeException("Language not found for code: " + translationDTO.getLanguageCode()));

                    return CouponTranslation.builder()
                            .name(translationDTO.getName())
                            .description(translationDTO.getDescription())
                            .coupon(finalCoupon1)
                            .language(language)
                            .build();
                })
                .collect(Collectors.toList());

        couponTranslationRepository.saveAll(translations);

        // 4️⃣ Trả về CouponDTO đã có ảnh
        return CouponDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .expirationDate(coupon.getExpirationDate())
                .isActive(coupon.getIsActive())
                .isGlobal(coupon.getIsGlobal())
                .imageUrl(coupon.getImageUrl()) // Trả về đường dẫn ảnh
                .build();
    }

    @Transactional
    public CouponDTO updateCoupon(Long id, CouponCreateRequestDTO request, MultipartFile imageFile) {
        // 1️⃣ Lấy coupon cần cập nhật
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        // 2️⃣ Nếu có ảnh mới thì xử lý upload
        if (imageFile != null && !imageFile.isEmpty()) {
            String oldImageUrl = coupon.getImageUrl();
            if (oldImageUrl != null) {
                fileStorageService.backupAndDeleteFile(oldImageUrl, "coupons"); // Xóa ảnh cũ nếu có
            }
            String newImageUrl = fileStorageService.uploadFileAndGetName(imageFile, "/images/coupons");
            coupon.setImageUrl(newImageUrl);
        }

        // 3️⃣ Cập nhật thông tin coupon
        coupon.setCode(request.getCode());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderValue(request.getMinOrderValue());
        coupon.setExpirationDate(request.getExpirationDate());



        coupon = couponRepository.save(coupon);

        // 5️⃣ Xóa bản dịch cũ và thêm bản dịch mới
        couponTranslationRepository.deleteByCouponId(id);

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

        // 6️⃣ Trả về CouponDTO đã có thông tin ảnh
        return CouponDTO.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderValue(coupon.getMinOrderValue())
                .expirationDate(coupon.getExpirationDate())
                .isActive(coupon.getIsActive())
                .isGlobal(coupon.getIsGlobal())
                .imageUrl(coupon.getImageUrl()) // Trả về đường dẫn ảnh mới
                .build();
    }

    @Transactional
    public void deleteCoupon(Long id) {
        // 1️⃣ Kiểm tra coupon có tồn tại không
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        // 2️⃣ Xóa ảnh nếu có
        String imageUrl = coupon.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            fileStorageService.backupAndDeleteFile(imageUrl, "coupons");
        }

        // 3️⃣ Xóa bản dịch trước
        couponTranslationRepository.deleteByCouponId(id);

        // 4️⃣ Xóa coupon
        couponRepository.deleteById(id);
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
    public Page<CouponLocalizedDTO> searchCoupons(String code, LocalDateTime expirationDate,
                                                  Float discountValue, Float minOrderValue,
                                                  String languageCode, int page, int size,
                                                  String sortBy, String sortDirection) {
        Sort sort;

        // Xác định trường cần sắp xếp
        if ("expirationDate".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("expirationDate");
        } else {
            sort = Sort.by("createdAt"); // Mặc định sắp xếp theo ngày tạo
        }

        // Xác định chiều sắp xếp (tăng dần hoặc giảm dần)
        if ("desc".equalsIgnoreCase(sortDirection)) {
            sort = sort.descending();
        } else {
            sort = sort.ascending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Specification<Coupon> spec = CouponSpecification.filterCoupons(code, expirationDate, discountValue, minOrderValue, languageCode);

        Page<Coupon> couponPage = couponRepository.findAll(spec, pageable);

        return couponPage.map(coupon -> {
            CouponTranslation translation = coupon.getCouponTranslationByLanguage(languageCode);
            List<Long> userIds = couponUserRestrictionRepository.findUserIdsByCouponId(coupon.getId());
            return CouponLocalizedDTO.fromCoupons(coupon, translation, userIds);
        });
    }
    public void generateBirthdayCoupons(List<User> usersWithBirthday) {
        LocalDateTime today = LocalDateTime.now();
        String birthdayImageUrl = "/images/coupons/5625ad39-d0cb-4b36-a582-3bcf288260a2_pc_1720432249113_2117241469.jpg";
        for (User user : usersWithBirthday) {
            String couponCode = "BDAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Coupon coupon = Coupon.builder()
                    .code(couponCode)
                    .discountType("PERCENTAGE")
                    .discountValue(10.0f)
                    .minOrderValue(100.0f)
                    .expirationDate(today.plusDays(7))
                    .imageUrl(birthdayImageUrl)
                    .isActive(true)
                    .isGlobal(false)
                    .build();
            // ✅ Lưu coupon trước
            coupon = couponRepository.save(coupon);

            CouponUserRestriction restriction = CouponUserRestriction.builder()
                    .user(user)
                    .coupon(coupon)
                    .build();

            // ✅ Lưu restriction vào DB
            couponUserRestrictionRepository.save(restriction);

            // 📨 Gửi email thông báo cho user
            emailService.sendBirthdayCoupon(user.getEmail(), couponCode);
        }
    }


}
