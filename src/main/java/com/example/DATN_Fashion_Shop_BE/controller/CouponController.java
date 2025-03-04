package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.AddressDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponLocalizedDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.coupon.CouponCreateRequestDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.coupon.CouponRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.coupon.CouponDetailResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.repository.UserRepository;
import com.example.DATN_Fashion_Shop_BE.service.CouponService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/coupons")
@AllArgsConstructor
public class CouponController {
    private final CouponService couponService;
    private final LocalizationUtils localizationUtils;
    private final UserRepository userRepository;
    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<Boolean>> applyCoupon(
            @RequestParam Long userId, @Valid @RequestBody CouponRequest request,
                                                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            bindingResult,
                            localizationUtils.getLocalizedMessage(MessageKeys.COUPON_CODE_REQUIRED),
                            localizationUtils
                    )
            );
        }
        boolean isApplied  = couponService.applyCoupon(userId, request.getCode());
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.COUPON_CODE_VALID),
                isApplied
        ));
    }
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(
            @RequestPart("request") @Valid CouponCreateRequestDTO request,
            BindingResult bindingResult,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {

        // 1️⃣ Kiểm tra lỗi validate dữ liệu đầu vào
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            bindingResult,
                            localizationUtils.getLocalizedMessage(MessageKeys.COUPON_CREATED_FAILED),
                            localizationUtils
                    )
            );
        }

        // 2️⃣ Gọi service để tạo coupon và upload ảnh
        CouponDTO createdCoupon = couponService.createCoupon(request, imageFile);

        // 3️⃣ Trả về response thành công
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.COUPON_CREATED_SUCCESS),
                createdCoupon
        ));
    }


    @PutMapping("update/{id}")
    public ResponseEntity<ApiResponse<CouponDTO>> updateCoupon(
            @PathVariable Long id,
            @RequestPart("request") @Valid CouponCreateRequestDTO request,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            bindingResult,
                            localizationUtils.getLocalizedMessage(MessageKeys.COUPON_UPDATE_FAILED),
                            localizationUtils
                    )
            );
        }

        CouponDTO updatedCoupon = couponService.updateCoupon(id, request, imageFile);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.COUPON_UPDATE_SUCCESS),
                updatedCoupon
        ));
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {

        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }



    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<CouponLocalizedDTO>>> getAllCoupons(@RequestParam String languageCode) {
        List<CouponLocalizedDTO> coupons = couponService.getAllCoupons(languageCode);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.COUPON_GETALL_SUCCESS),
                coupons
        ));
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CouponLocalizedDTO>>> getCouponsForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "en") String lang) {

        List<CouponLocalizedDTO> coupons = couponService.getCouponsForUser(userId, lang);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.COUPON_GETALL_SUCCESS),
                coupons
        ));
    }
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<CouponLocalizedDTO>>> searchCoupons(
            @RequestParam(required = false) String code,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expirationDate,
            @RequestParam(required = false) Float discountValue,
            @RequestParam(required = false) Float minOrderValue,
            @RequestParam(required = false) String languageCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy, // Trường để sắp xếp
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Page<CouponLocalizedDTO> result = couponService.searchCoupons(code, expirationDate, discountValue, minOrderValue, languageCode, page, size,sortBy, sortDirection);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.COUPON_GETALL_SUCCESS),
                result ));
    }
//    @PostMapping("/generate-birthday")
//    public ResponseEntity<Map<String, Object>> generateBirthdayCoupons() {
//        LocalDate today = LocalDate.now();
//        System.out.println("📅 Ngày hiện tại: " + today);
//
//        List<User> usersWithBirthday = userRepository.findByDateOfBirth(today);
//
//        if (usersWithBirthday.isEmpty()) {
//            return ResponseEntity.ok(Map.of("message", "🚫 Không có user nào có sinh nhật hôm nay.", "userCount", 0));
//        }
//
//        couponService.generateBirthdayCoupons(usersWithBirthday);
//        return ResponseEntity.ok(Map.of("message", "🎉 Đã tạo mã giảm giá sinh nhật thành công!", "userCount", usersWithBirthday.size()));
//    }

    @GetMapping("/{couponId}")
    public ResponseEntity<ApiResponse<CouponDetailResponse>> getCouponById(
            @PathVariable Long couponId
    ) throws DataNotFoundException {
        CouponDetailResponse response = couponService.getCouponById(couponId);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.USER_DETAILS_RETRIEVED_SUCCESSFULLY),
                response
        ));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<CouponDetailResponse>> getCouponById(
            @PathVariable String code
    ) throws DataNotFoundException {
        CouponDetailResponse response = couponService.getCouponByCode(code);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.USER_DETAILS_RETRIEVED_SUCCESSFULLY),
                response
        ));
    }

    @GetMapping("/validate/coupon-user")
    public ResponseEntity<ApiResponse<Boolean>> validateCouponUser(
            @RequestParam Long userId,
            @RequestParam Long couponId
    ){
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.USER_DETAILS_RETRIEVED_SUCCESSFULLY),
                couponService.canUserUseCoupon(userId, couponId)
        ));
    }

}
