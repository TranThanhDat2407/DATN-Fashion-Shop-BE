package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.BannerDTO;
import com.example.DATN_Fashion_Shop_BE.dto.PaymentMethodDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.banner.BannerEditResponseDTO;
import com.example.DATN_Fashion_Shop_BE.service.PaymentMethodService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/payment")
@AllArgsConstructor
public class PaymentMethodController {
    private  PaymentMethodService paymentMethodService;
    private final LocalizationUtils localizationUtils;
    @GetMapping()
    public ResponseEntity<ApiResponse<List<PaymentMethodDTO>>> getPaymentMethodName() {
        List<PaymentMethodDTO> paymentMethodDTO = paymentMethodService.getAllPaymentMethod();
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PAYMENT_METHOD_RETRIEVED_SUCCESSFULLY),
                paymentMethodDTO
        ));
    }



}
