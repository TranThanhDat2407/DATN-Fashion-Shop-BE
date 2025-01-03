package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.LanguageDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.service.LanguageService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/languages")
@AllArgsConstructor
public class LanguageController {
    private final LanguageService languageService;
    private final LocalizationUtils localizationUtils;

    @Operation(
            summary = "Lấy danh sách ngôn ngữ",
            description = "Endpoint để lấy danh sách tất cả các ngôn ngữ hiện có trong hệ thống.",
            tags = {"Languages"}
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<LanguageDTO>>> getLanguages() {
        List<LanguageDTO> languages = languageService.getLanguages();

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.LANGUAGE_RETRIEVED_SUCCESSFULLY),
                languages
        ));
    }
}
