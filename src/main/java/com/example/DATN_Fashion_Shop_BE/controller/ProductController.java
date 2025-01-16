package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.StaffResponse;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.Staff;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.service.*;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final LocalizationUtils localizationUtils;

//    @GetMapping("{languageCode}")
//    public ResponseEntity<ApiResponse<PageResponse<ProductListDTO>>> getProductsWithTranslations(
//            @PathVariable String languageCode,
//            @RequestParam(value = "isActive", required = false) Boolean isActive,
//            @RequestParam(value = "name", required = false) String name,
//            @RequestParam(value = "page", defaultValue = "0") int page,
//            @RequestParam(value = "size", defaultValue = "10") int size,
//            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
//            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
//
//        Page<ProductListDTO> productsPage = productService.getProductsWithTranslations(
//                languageCode, isActive, name, page, size, sortBy, sortDir);
//
//        return ResponseEntity.ok(ApiResponseUtils.successResponse(
//                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
//                PageResponse.fromPage(productsPage)
//        ));
//    }

//    @GetMapping("promotions/{languageCode}")
//    public ResponseEntity<ApiResponse<PageResponse<ProductListDTO>>> getProductsPromotionWithTranslations(
//            @PathVariable String languageCode,
//            @RequestParam(value = "isActive", required = false) Boolean isActive,
//            @RequestParam(value = "name", required = false) String name,
//            @RequestParam(value = "page", defaultValue = "0") int page,
//            @RequestParam(value = "size", defaultValue = "10") int size,
//            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
//            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
//
//        Page<ProductListDTO> productsPage = productService.getProductsPromotionWithTranslations(
//                languageCode, isActive, name, page, size, sortBy, sortDir);
//
//        return ResponseEntity.ok(ApiResponseUtils.successResponse(
//                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
//                PageResponse.fromPage(productsPage)
//        ));
//    }

    @GetMapping("color/{productId}")
    public ResponseEntity<ApiResponse<List<ColorDTO>>> getColorsByProductId(
            @PathVariable(value = "productId") Long productId) {

        List<ColorDTO> colors = productService.getColorsByProductId(productId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                colors
        ));
    }

    @GetMapping("size/{productId}")
    public ResponseEntity<ApiResponse<List<SizeDTO>>> getSizesByProductId(
            @PathVariable(value = "productId") Long productId) {

        List<SizeDTO> sizes = productService.getSizesByProductId(productId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                sizes
        ));
    }

    @GetMapping("variants/{productId}")
    public ResponseEntity<ApiResponse<ProductVariantDTO>> getProductVariants(
            @PathVariable(value = "productId") Long productId,
            @RequestParam(value = "colorId", required = false) Long colorId,
            @RequestParam(value = "sizeId", required = false) Long sizeId
    ) {

        ProductVariantDTO variant = productService.getProductVariant(productId,colorId,sizeId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                variant
        ));
    }

    @GetMapping("variants/{languageCode}/{productVariantId}")
    public ResponseEntity<ApiResponse<ProductVariantDetailDTO>> getProductVariants(
            @PathVariable(value = "productVariantId") Long productVariantId,
            @PathVariable(value = "languageCode") String languageCode
    ) {
        ProductVariantDetailDTO variant = productService
                .getProductVariantDetail(productVariantId,languageCode);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                variant
        ));
    }

    @GetMapping("detail/{languageCode}/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailDTO>> getProductDetail(
            @PathVariable Long productId,
            @PathVariable String languageCode) {

        ProductDetailDTO productDetail = productService
                .getProductDetail(productId, languageCode);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                productDetail
        ));
    }

    @GetMapping("/{languageCode}/{productId}/categories")
    public ResponseEntity<ApiResponse<List<ProductCategoryDTO>>> getCategories(
            @PathVariable Long productId,
            @PathVariable String languageCode) {

        List<ProductCategoryDTO> categories = productService
                .getCategoriesByProductIdAndLangCode(productId, languageCode);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                categories
        ));
    }

    @GetMapping("/{languageCode}/{productId}/categories/root")
    public ResponseEntity<ApiResponse<List<ProductCategoryDTO>>> getCategoriesRoot(
            @PathVariable Long productId,
            @PathVariable String languageCode) {

        List<ProductCategoryDTO> categories = productService
                .getRootCategoriesByProductIdAndLangCode(productId, languageCode);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                categories
        ));
    }

    @GetMapping("media/{productId}")
    public ResponseEntity<ApiResponse<List<ProductMediaDTO>>> getProductImages(
            @PathVariable Long productId) {

        List<ProductMediaDTO> productMedia = productService.getProductMedia(productId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                productMedia
        ));
    }

    @GetMapping("media/{productId}/{colorId}")
    public ResponseEntity<ApiResponse<List<ProductMediaDTO>>> getProductImagesWithColor(
            @PathVariable Long productId,
            @PathVariable Long colorId
    ) {

        List<ProductMediaDTO> productMedia = productService.getProductMediaWithColor(productId,colorId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                productMedia
        ));
    }

    @GetMapping("/{languageCode}")
    public ResponseEntity<ApiResponse<PageResponse<ProductListDTO>>> getFilteredProducts(
            @PathVariable String languageCode,
            @RequestParam Long categoryId,
            @RequestParam(required = false, defaultValue = "true") Boolean isActive,
            @RequestParam(required = false ) String name,
            @RequestParam(required = false ) Double minPrice,
            @RequestParam(required = false ) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

       Page<ProductListDTO> products = productService.getFilteredProducts(
                languageCode,name, categoryId, isActive, minPrice, maxPrice,
                page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                PageResponse.fromPage(products)
        ));
    }
}
