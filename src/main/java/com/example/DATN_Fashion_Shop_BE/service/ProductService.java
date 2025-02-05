package com.example.DATN_Fashion_Shop_BE.service;


import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.*;
import com.example.DATN_Fashion_Shop_BE.dto.request.product.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.CreateProductResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductMediaResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.ProductVariantResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final ProductMediaRepository productMediaRepository;
    private final FileStorageService fileStorageService;
    private final LocalizationUtils localizationUtils;
    private final LanguageRepository languageRepository;

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

    public List<ProductMediaDTO> getProductImages(Long productId) {
        List<ProductMedia> productMediaList = productMediaRepository.findByProductId(productId);

        return productMediaList.stream()
                .filter(media -> "IMAGE".equals(media.getMediaType()))
                .map(ProductMediaDTO::fromProductMedia)
                .collect(Collectors.toList());
    }

    public List<ProductMediaDTO> getProductVideos(Long productId) {
        List<ProductMedia> productMediaList = productMediaRepository.findByProductId(productId);

        return productMediaList.stream()
                .filter(media -> "VIDEO".equals(media.getMediaType()))
                .map(ProductMediaDTO::fromProductMedia)
                .collect(Collectors.toList());
    }


    public List<ProductMediaDTO> getProductMediaWithColor(Long productId, Long colorId) {
        List<ProductMedia> productMediaList = productMediaRepository
                .findByProductIdAndColorValueId(productId, colorId);

        return productMediaList.stream()
                .map(ProductMediaDTO::fromProductMedia)
                .collect(Collectors.toList());
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

    public List<ProductVariant> getProductVariantsByMediaId(Long mediaId) {
        return productVariantRepository.findProductVariantsByMediaId(mediaId);
    }

    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest request) {
        // Tạo Product bằng Builder
        Product product = Product.builder()
                .status(request.getStatus())
                .basePrice(request.getBasePrice())
                .isActive(request.getIsActive())
                .build();

        // Xử lý danh sách bản dịch
        List<ProductsTranslation> translations = new ArrayList<>();
        if (request.getTranslations() != null) {
            for (CreateProductTranslationRequest transReq : request.getTranslations()) {
                Language language = languageRepository.findByCode(transReq.getLangCode())
                        .orElseThrow(() -> new IllegalArgumentException("Language not found with Code: " + transReq.getLangCode()));
                ProductsTranslation translation = ProductsTranslation.builder()
                        .name(transReq.getName())
                        .description(transReq.getDescription())
                        .material(transReq.getMaterial())
                        .care(transReq.getCare())
                        .language(language)
                        .product(product)
                        .build();
                translations.add(translation);
            }
        }
        product.setTranslations(translations);

        // Lưu Product cùng các bản dịch liên quan
        Product savedProduct = productRepository.save(product);

        // Trả về response sử dụng Builder
        return CreateProductResponse.fromProduct(savedProduct);
    }

    @Transactional
    public ProductVariantResponse createProductVariant(CreateProductVariantRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        AttributeValue colorValue = attributeValueRepository.findById(request.getColorValueId())
                .orElseThrow(() -> new IllegalArgumentException("Màu sắc không tồn tại"));

        AttributeValue sizeValue = attributeValueRepository.findById(request.getSizeValueId())
                .orElseThrow(() -> new IllegalArgumentException("Kích thước không tồn tại"));

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .colorValue(colorValue)
                .sizeValue(sizeValue)
                .salePrice(request.getSalePrice())
                .build();

        ProductVariant savedVariant = productVariantRepository.save(variant);

        return ProductVariantResponse.fromProductVariant(savedVariant);
    }

    @Transactional
    public List<ProductVariantResponse> createProductVariantsByPattern(CreateProductVariantsByPatternRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại"));

        AttributeValue colorValue = attributeValueRepository.findById(request.getColorValueId())
                .orElseThrow(() -> new IllegalArgumentException("Màu sắc không tồn tại"));

        // Lấy danh sách tất cả kích thước theo pattern
        List<AttributeValue> sizeValues = attributeValueRepository.findByPatternId(request.getPatternId());

        if (sizeValues.isEmpty()) {
            throw new IllegalArgumentException("Pattern không chứa kích thước nào!");
        }

        List<ProductVariantResponse> responseList = sizeValues.stream().map(sizeValue -> {
            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .colorValue(colorValue)
                    .sizeValue(sizeValue)
                    .salePrice(request.getSalePrice())
                    .build();

            ProductVariant savedVariant = productVariantRepository.save(variant);

            return ProductVariantResponse.fromProductVariant(savedVariant);
        }).collect(Collectors.toList());

        return responseList;
    }

    // Các phần mở rộng của hình ảnh và video
    private static final Set<String> IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4", "avi", "mov", "mkv");

    @Transactional
    public ProductMediaResponse uploadProductMedia(Long productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_FAILED)
                ));

        // Lưu file và lấy URL
        String fileUrl = fileStorageService.uploadFileAndGetName(file, "images/products");

        // Xác định loại media
        String mediaType = determineMediaType(file);

        // Tạo ProductMedia entity
        ProductMedia media = ProductMedia.builder()
                .mediaUrl(fileUrl)
                .mediaType(mediaType) // IMAGE hoặc VIDEO
                .product(product)
                .build();

        ProductMedia savedMedia = productMediaRepository.save(media);
        // Trả về response
        return ProductMediaResponse.fromProductMedia(savedMedia);
    }

    @Transactional
    public ProductMediaResponse updateProductMedia(Long id, MultipartFile mediaFile, UpdateProductMediaRequest request) {
        ProductMedia productMedia = productMediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ProductMedia với ID: " + id));

        // Nếu có file mới, thì xóa file cũ và upload file mới
        if (mediaFile != null) {
            fileStorageService.deleteFile(productMedia.getMediaUrl(),"products"); // Xóa ảnh cũ
            String newMediaUrl = fileStorageService.uploadFileAndGetName(mediaFile, "images/products");
            productMedia.setMediaUrl(newMediaUrl);
        }

        // Cập nhật metadata
        productMedia.setSortOrder(request.getSortOrder());
        productMedia.setModelHeight(request.getModelHeight());

        // Cập nhật màu sắc nếu có
        if (request.getColorValueId() != null) {
            AttributeValue colorValue = attributeValueRepository.findById(request.getColorValueId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy màu sắc với ID: " + request.getColorValueId()));
            productMedia.setColorValue(colorValue);
        }

        // Cập nhật danh sách `ProductVariant` nếu có
        if (request.getProductVariantIds() != null) {
            List<ProductVariant> variants = new ArrayList<>(productVariantRepository.findAllById(request.getProductVariantIds()));
            productMedia.getProductVariants().clear();
            productMedia.getProductVariants().addAll(variants);
        }

        ProductMedia updatedMedia = productMediaRepository.save(productMedia);
        return ProductMediaResponse.fromProductMedia(updatedMedia);
    }

    @Transactional
    public void deleteProductMedia(Long id) {
        ProductMedia productMedia = productMediaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NOT_FOUND)));

        fileStorageService.deleteFile(productMedia.getMediaUrl(),"products");

        productMediaRepository.delete(productMedia);
    }

    /**
     * Xác định loại media dựa vào phần mở rộng của file hoặc MIME type.
     */
    private String determineMediaType(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) return "UNKNOWN";
        // Lấy phần mở rộng của file
        String extension = getFileExtension(fileName);

        // Kiểm tra nếu là ảnh
        if (IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
            return "IMAGE";
        }

        // Kiểm tra nếu là video
        if (VIDEO_EXTENSIONS.contains(extension.toLowerCase())) {
            return "VIDEO";
        }

        return "UNKNOWN";
    }

    /**
     * Lấy phần mở rộng của file (ví dụ: "jpg" từ "image.jpg").
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex != -1 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }

}
