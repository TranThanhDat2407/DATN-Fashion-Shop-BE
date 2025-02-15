package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.AddressDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CouponLocalizedDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.coupon.CouponCreateRequestDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.coupon.CouponRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.service.CouponService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/coupons")
@AllArgsConstructor
public class CouponController {
    private final CouponService couponService;
    private final LocalizationUtils localizationUtils;
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
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CouponDTO>> createCoupon(@Valid @RequestBody CouponCreateRequestDTO request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            bindingResult,
                            localizationUtils.getLocalizedMessage(MessageKeys.COUPON_CREATED_FAILED),
                            localizationUtils
                    )
            );
        }
        CouponDTO createdCoupon = couponService.createCoupon(request);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.COUPON_CREATED_SUCCESS),
                createdCoupon
        ));
    }

    @PutMapping("update/{id}")
    public ResponseEntity<ApiResponse<CouponDTO>> updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponCreateRequestDTO request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            bindingResult,
                            localizationUtils.getLocalizedMessage(MessageKeys.COUPON_UPDATE_FAILED),
                            localizationUtils
                    )
            );
        }

        CouponDTO updatedCoupon = couponService.updateCoupon(id, request);
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
    public ResponseEntity<List<CouponLocalizedDTO>> getCouponsForUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "en") String lang) {

        List<CouponLocalizedDTO> coupons = couponService.getCouponsForUser(userId, lang);
        return ResponseEntity.ok(coupons);
    }



}
