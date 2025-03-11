package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.CouponDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponLocalizedDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.Notification.NotificationTranslationRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.coupon.CouponCreateRequestDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.coupon.CouponDetailResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
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
    private final NotificationService notificationService;

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

        // Danh sách user sẽ nhận thông báo
        List<User> targetUsers;

        // 2️⃣ Nếu là Global, lấy tất cả customer
        if (request.getIsGlobal()) {
            targetUsers = userRepository.findByRoleCustomer();
        } else if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            // Nếu không phải global, tạo ràng buộc với user
            targetUsers = userRepository.findAllById(request.getUserIds());

            Coupon finalCoupon = coupon;
            List<CouponUserRestriction> restrictions = targetUsers.stream()
                    .map(user -> CouponUserRestriction.builder()
                            .user(user)
                            .coupon(finalCoupon)
                            .build())
                    .collect(Collectors.toList());
            couponUserRestrictionRepository.saveAll(restrictions);
        } else {
            targetUsers = List.of(); // Nếu không có user nào, tránh lỗi null
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

        // 4️⃣ Gửi Notification cho danh sách user đã lấy
        sendCouponNotification(coupon, targetUsers);

        // 5️⃣ Trả về CouponDTO đã có ảnh
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

    private void sendCouponNotification(Coupon coupon, List<User> users) {
        List<NotificationTranslationRequest> translations = notificationService.createCouponTranslations(coupon);

        users.forEach(user -> {
            notificationService.createNotification(
                    user.getId(),
                    "COUPON",
                    null, // redirectUrl không cần backend xử lý
                    coupon.getImageUrl(),
                    translations
            );
        });
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
        // 1️ Kiểm tra coupon có tồn tại không
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));
        userCouponUsageRepository.deleteByCouponId(id);
        String imageUrl = coupon.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            fileStorageService.backupAndDeleteFile(imageUrl, "coupons");
        }

        // 3️ Xóa bản dịch trước
        couponTranslationRepository.deleteByCouponId(id);

        // 4⃣ Xóa coupon
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
    public Page<CouponLocalizedDTO> searchCoupons(String keyword, LocalDateTime expirationDate,
                                                  Float discountValue, Float minOrderValue,
                                                  String languageCode, Long userId, int page, int size,
                                                  String sortBy, String sortDirection) {
        // Xác định trường cần sắp xếp
        String sortField = "createdAt"; // Mặc định sắp xếp theo ngày tạo
        if ("expirationDate".equalsIgnoreCase(sortBy)) {
            sortField = "expirationDate";
        }

        // Xác định chiều sắp xếp (tăng dần hoặc giảm dần)
        Sort sort = "desc".equalsIgnoreCase(sortDirection) ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        //  Sử dụng `keyword` để tìm kiếm trên nhiều trường
        Specification<Coupon> spec = CouponSpecification.filterCoupons(keyword, expirationDate, discountValue, minOrderValue, languageCode,userId);

        // Truy vấn danh sách coupon
        Page<Coupon> couponPage = couponRepository.findAll(spec, pageable);

        // Mapping kết quả sang DTO mà không cần userIds
        return couponPage.map(coupon -> {
            CouponTranslation translation = coupon.getCouponTranslationByLanguage(languageCode);
            //  Đảm bảo lấy name & description từ CouponTranslation
            List<Long> allowedUserIds = couponUserRestrictionRepository.findUserIdsByCouponId(coupon.getId());

            return CouponLocalizedDTO.fromCoupons(coupon, translation, allowedUserIds);
        });
    }





    public CouponDetailResponse getCouponById(Long couponId) throws DataNotFoundException {
        Coupon coupon = couponRepository.findById(couponId).orElseThrow(
                () -> new DataNotFoundException("Coupon not found")
        );
        return CouponDetailResponse.fromCoupon(coupon);
    }

    public CouponDetailResponse getCouponByCode(String code) throws DataNotFoundException {
        Coupon coupon = couponRepository.findFirstByCode(code).orElseThrow(
                () -> new DataNotFoundException("Coupon not found")
        );
        return CouponDetailResponse.fromCoupon(coupon);
    }


    private String generateRandomCode(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    @Transactional
    public Coupon createCouponForUser(String prefix, String discountType, Float discountValue,
                                      Float minOrderValue, int expirationDays, User user,
                                      String imageUrl, List<CouponTranslationDTO> translationDTOs) {
        try {
            // ✅ Sinh mã giảm giá ngẫu nhiên
            String code = generateRandomCode(prefix);

            // ✅ Tạo đối tượng Coupon
            Coupon coupon = Coupon.builder()
                    .code(code)
                    .discountType(discountType)
                    .discountValue(discountValue)
                    .minOrderValue(minOrderValue)
                    .expirationDate(LocalDateTime.now().plusDays(expirationDays))
                    .imageUrl(imageUrl)
                    .isGlobal(false)
                    .isActive(true)
                    .build();

            // ✅ Lưu coupon vào database
            coupon = couponRepository.save(coupon);

            // ✅ Áp dụng coupon cho user
            CouponUserRestriction restriction = CouponUserRestriction.builder()
                    .user(user)
                    .coupon(coupon)
                    .build();
            couponUserRestrictionRepository.save(restriction);

            // ✅ Thêm bản dịch
            saveCouponTranslations(coupon, translationDTOs);

            return coupon;

        } catch (Exception e) {
            throw new RuntimeException("❌ Error creating coupon: " + e.getMessage(), e);
        }
    }


    @Transactional
    public Coupon createCouponForAllUser(String prefix, String discountType, Float discountValue,
                                         Float minOrderValue, int expirationDays, boolean isGlobal,
                                         String imageUrl, List<CouponTranslationDTO> translationDTOs) {
        // ✅ Sinh mã giảm giá ngẫu nhiên
        String code = generateRandomCode(prefix);

        // ✅ Tạo đối tượng Coupon
        Coupon coupon = Coupon.builder()
                .discountType(discountType)
                .discountValue(discountValue)
                .minOrderValue(minOrderValue)
                .expirationDate(LocalDateTime.now().plusDays(expirationDays))
                .code(code)
                .imageUrl(imageUrl)
                .isGlobal(true)
                .userRestrictions(new ArrayList<>()) // Tránh lỗi null list
                .build();

        // ✅ Lưu coupon vào database
        coupon = couponRepository.save(coupon);

        // ✅ Thêm bản dịch
        saveCouponTranslations(coupon, translationDTOs);

        return coupon;
    }


    private void saveCouponTranslations(Coupon coupon, List<CouponTranslationDTO> translationDTOs) {
        if (translationDTOs != null && !translationDTOs.isEmpty()) {
            List<CouponTranslation> translations = translationDTOs.stream()
                    .map(dto -> {
                        Language language = languageRepository.findByCode(dto.getLanguageCode())
                                .orElseThrow(() -> new RuntimeException("Language not found for code: " + dto.getLanguageCode()));

                        return CouponTranslation.builder()
                                .name(dto.getName())
                                .description(dto.getDescription())
                                .coupon(coupon)
                                .language(language)
                                .build();
                    })
                    .collect(Collectors.toList());

            couponTranslationRepository.saveAll(translations);
        }
    }


    public Boolean canUserUseCoupon (Long userId, Long couponId){
        Optional<Coupon> couponOpt = couponRepository.findById(couponId);
        if (couponOpt.isEmpty()) {
            return false;
        }

        Coupon coupon = couponOpt.get();

        if (!coupon.getIsActive() || coupon.getExpirationDate().isBefore(LocalDateTime.now())) {
            return false;
        }

        return couponUserRestrictionRepository.findByUserIdAndCouponId(userId, couponId).isPresent();
    }

}
