package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.ProductDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.StaffResponse;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.Staff;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.service.ProductService;
import com.example.DATN_Fashion_Shop_BE.service.StaffService;
import com.example.DATN_Fashion_Shop_BE.service.TokenService;
import com.example.DATN_Fashion_Shop_BE.service.UserService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final LocalizationUtils localizationUtils;
    private final TokenService tokenService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getProducts(
            @RequestParam(value = "languageCode", defaultValue = "en") String languageCode,
            @RequestParam(value = "isActive", required = false) Boolean isActive,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        List<ProductDTO> productDTOs = productService.getProductsWithTranslations(languageCode, isActive, name, page, size);

        return ResponseEntity.ok(productDTOs);
    }

}
