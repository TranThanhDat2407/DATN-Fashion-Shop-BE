package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.CurrencyDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.service.CurrencyService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/currencies")
@AllArgsConstructor
public class CurrencyController {
    private final LocalizationUtils localizationUtils;
    private CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CurrencyDTO>>> getAllCurrencies() {

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                currencyService.getAllCurrencies()
        ));

    }


}
