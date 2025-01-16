package com.example.DATN_Fashion_Shop_BE.service;


import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final ProductMediaRepository productMediaRepository;
    private final LocalizationUtils localizationUtils;

    public List<ProductCategoryDTO> getCategoriesByProductIdAndLangCode(Long productId, String langCode) {
        // Lấy Product dựa trên ID
        Product product = productRepository.findByIdWithCategories(productId)
                .orElseThrow(() -> new RuntimeException(localizationUtils
                        .getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_FAILED)));

        // Lấy danh sách Category và chuyển đổi sang DTO
        return product.getCategories().stream()
                .map(category -> {
                    // Lấy bản dịch theo mã ngôn ngữ
                    String translatedName = category.getTranslationByLanguage(langCode);
                    return ProductCategoryDTO.fromCategory(category, translatedName);
                })
                .collect(Collectors.toList());
    }

    public List<ProductCategoryDTO> getRootCategoriesByProductIdAndLangCode(Long productId, String langCode) {
        // Lấy Product dựa trên ID
        Product product = productRepository.findByIdWithCategories(productId)
                .orElseThrow(() -> new RuntimeException(localizationUtils
                        .getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_FAILED)));

        // Tìm tất cả danh mục gốc liên quan đến các danh mục của sản phẩm
        List<Category> rootCategories = product.getCategories().stream()
                .flatMap(category -> findAllRootCategories(category).stream())
                .collect(Collectors.toList()); // Không dùng Set

        // Chuyển đổi danh mục gốc thành DTO
        return rootCategories.stream()
                .map(rootCategory -> {
                    // Lấy bản dịch theo mã ngôn ngữ
                    String translatedName = rootCategory.getTranslationByLanguage(langCode);
                    System.out.println("Category ID: " + rootCategory.getId() + ", Translated Name: " + translatedName);
                    return ProductCategoryDTO.fromCategory(rootCategory, translatedName);
                })
                .collect(Collectors.toList());
    }

    private Set<Category> findAllRootCategories(Category category) {
        Set<Category> rootCategories = new HashSet<>();
        while (category != null) {
            if (category.getParentCategory() == null) {
                rootCategories.add(category); // Chỉ thêm danh mục gốc
            }
            category = category.getParentCategory(); // Đi lên danh mục cha
        }
        return rootCategories;
    }

    public List<ColorDTO> getColorsByProductId(Long productId) {
        List<AttributeValue> colors = attributeValueRepository.findColorsByProductId(productId);

        return colors.stream()
                .map(ColorDTO::fromColor)
                .collect(Collectors.toList());
    }

    public List<SizeDTO> getSizesByProductId(Long productId) {
        List<AttributeValue> sizes = attributeValueRepository.findSizesByProductId(productId);

        return sizes.stream()
                .map(SizeDTO::fromSize)
                .collect(Collectors.toList());
    }

    public ProductVariantDTO getProductVariant(Long productId, Long colorId, Long sizeId) {
        // Tìm ProductVariant dựa trên productId, colorId, và sizeId
        ProductVariant productVariant = productVariantRepository
                .findByProductIdAndColorValueIdAndSizeValueId(productId, colorId, sizeId)
                .orElseThrow(() -> new RuntimeException(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_FAILED))
                );

        // Chuyển đổi ProductVariant sang DTO
        return ProductVariantDTO.fromProductVariant(productVariant);
    }

    public ProductVariantDetailDTO getProductVariantDetail(Long id, String langCode) {
        ProductVariant productVariant = productVariantRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_FAILED))
                );
        return ProductVariantDetailDTO.fromProductVariant(productVariant, langCode);
    }

    public ProductDetailDTO getProductDetail(Long productId, String langCode) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(localizationUtils
                        .getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_FAILED)));

        return ProductDetailDTO.fromProduct(product, langCode);
    }

    public List<ProductMediaDTO> getProductMedia(Long productId) {
        List<ProductMedia> productMediaList = productMediaRepository.findByProductId(productId);

        return productMediaList.stream()
                .map(ProductMediaDTO::fromProductMedia)
                .collect(Collectors.toList());
    }

    public List<ProductMediaDTO> getProductMediaWithColor(Long productId,Long colorId) {
        List<ProductMedia> productMediaList = productMediaRepository
                .findByProductIdAndColorValueId(productId,colorId);

        return productMediaList.stream()
                .map(ProductMediaDTO::fromProductMedia)
                .collect(Collectors.toList());
    }

    public Page<ProductListDTO> getProductsWithFilters(
            String languageCode,
            String name,
            Long categoryId,
            Boolean isActive,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        Page<Product> products = productRepository
                .findProductsByCategoryAndName(categoryId, isActive,name,pageable);

        return products.map(product -> ProductListDTO.fromProduct(product,
                product.getTranslationByLanguage(languageCode)));
    }

    // lấy product theo khoảng giá nhưng lọc theo
    public Page<ProductListDTO> getProductsWithLowestVariantPrice(
            String languageCode,
            Long categoryId,
            Boolean isActive,
            String nameKeyword,
            Double minPrice,
            Double maxPrice,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        Page<Product> products = productRepository.findProductsByCategoryAndLowestPrice(
                categoryId, isActive, nameKeyword, minPrice, maxPrice, pageable);

        return products.map(product -> ProductListDTO.fromProduct(product,
                product.getTranslationByLanguage(languageCode)));
    }


    public Page<ProductListDTO> getFilteredProducts(
            String languageCode,
            String name,
            Long categoryId,
            Boolean isActive,
            Double minPrice,
            Double maxPrice,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        // Tạo Pageable object với phân trang và sắp xếp
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        // Kiểm tra nếu có minPrice và maxPrice, gọi phương thức tìm sản phẩm với giá lọc
        if (minPrice != null || maxPrice != null) {
            // Tìm sản phẩm theo giá với các điều kiện đã cho
            Page<Product> products = productRepository.findProductsByCategoryAndLowestPrice(
                    categoryId, isActive, name, minPrice, maxPrice, pageable);

            return products.map(product -> ProductListDTO.fromProduct(product,
                    product.getTranslationByLanguage(languageCode)));
        } else {
            // Nếu không có điều kiện về giá, tìm sản phẩm theo các điều kiện khác
            Page<Product> products = productRepository.findProductsByCategoryAndName(
                    categoryId, isActive, name, pageable);

            return products.map(product -> ProductListDTO.fromProduct(product,
                    product.getTranslationByLanguage(languageCode)));
        }
    }
}
