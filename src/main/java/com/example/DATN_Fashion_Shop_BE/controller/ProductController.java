package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.*;
import com.example.DATN_Fashion_Shop_BE.dto.request.product.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.StaffResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.category.CategoryCreateResponseDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.product.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.wishlist.BooleanWishlistResponse;
import com.example.DATN_Fashion_Shop_BE.model.Product;
import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import com.example.DATN_Fashion_Shop_BE.model.Staff;
import com.example.DATN_Fashion_Shop_BE.model.User;
import com.example.DATN_Fashion_Shop_BE.service.*;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    private final LocalizationUtils localizationUtils;

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

        ProductVariantDTO variant = productService.getProductVariant(productId, colorId, sizeId);

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
                .getProductVariantDetail(productVariantId, languageCode);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                variant
        ));
    }

    @GetMapping("lowest-price-variant/{languageCode}/{productId}")
    public ResponseEntity<ApiResponse<ProductVariantDetailDTO>> getLowestPriceProductVariant(
            @PathVariable(value = "productId") Long productId,
            @PathVariable(value = "languageCode") String languageCode,
            @RequestParam(required = false) Long UserId
    ) {
        ProductVariantDetailDTO variant = productService
                .getLowestPriceVariant(productId, languageCode,UserId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                variant
        ));
    }

    @GetMapping("/wishlist/check")
    public ResponseEntity<ApiResponse<BooleanWishlistResponse>> checkProductInWishlist(
            @RequestParam(required = false) Long userId,
            @RequestParam Long productId,
            @RequestParam Long colorId) {

        boolean exists = productService.isProductInWishlist(userId, productId, colorId);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                BooleanWishlistResponse.builder()
                        .isInWishList(exists)
                        .build()
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

    @GetMapping("images/{productId}")
    public ResponseEntity<ApiResponse<List<ProductMediaDTO>>> getProductImages(
            @PathVariable Long productId) {

        List<ProductMediaDTO> productMedia = productService.getProductImages(productId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                productMedia
        ));
    }

    @GetMapping("suggest/{langCode}")
    public ResponseEntity<ApiResponse<List<ProductSearchResponse>>> getProductImages(
            @PathVariable String langCode,
            @RequestParam (required = false) String productName
            ) {

        List<ProductSearchResponse> products = productService.searchProducts(productName, langCode);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                products
        ));
    }


    @GetMapping("videos/{productId}")
    public ResponseEntity<ApiResponse<List<ProductMediaDTO>>> getProductVideos(
            @PathVariable Long productId) {

        List<ProductMediaDTO> productMedia = productService.getProductVideos(productId);

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

        List<ProductMediaDTO> productMedia = productService.getProductMediaWithColor(productId, colorId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                productMedia
        ));
    }

    @GetMapping("/media/detail/{mediaId}")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getProductVariantsByMediaId(@PathVariable Long mediaId) {
        List<ProductVariant> productVariants = productService.getProductVariantsByMediaId(mediaId);

        List<ProductVariantResponse> productVariantDTOs = productVariants.stream()
                .map(ProductVariantResponse::fromProductVariant)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_VARIANTS_RETRIEVED_SUCCESSFULLY),
                productVariantDTOs));
    }

    @GetMapping("/{languageCode}")
    public ResponseEntity<ApiResponse<PageResponse<ProductListDTO>>> getFilteredProducts(
            @PathVariable String languageCode,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Long promotionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<ProductListDTO> products = productService.getFilteredProducts(
                languageCode, name, categoryId, isActive, minPrice, maxPrice,
                page, size, promotionId ,sortBy, sortDir);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                PageResponse.fromPage(products)
        ));
    }

    @GetMapping("/media/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            // Construct the file path
            Path filePath = Paths.get("uploads/images/products/" + filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // Check if file exists
            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Get file extension to determine media type
            String fileExtension = getFileExtension(filename).toLowerCase();
            MediaType mediaType = determineMediaType(fileExtension);

            // Return the file as binary stream
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreateProductResponse>> createProductWithMedia(
            @RequestPart("product") @Valid @NotNull CreateProductRequest productRequest,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles) {

        // 1. Tạo sản phẩm
        CreateProductResponse productResponse = productService.createProduct(productRequest);

        // 2. Nếu có media, thêm vào sản phẩm
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            List<ProductMediaResponse> mediaResponses = mediaFiles.stream()
                    .map(file -> productService.uploadProductMedia(productResponse.getId(), file))
                    .collect(Collectors.toList());
            productResponse.setMedia(mediaResponses);
        }

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_VARIANTS_RETRIEVED_SUCCESSFULLY),
                productResponse));
    }

    /**
     * Endpoint cập nhật sản phẩm kèm media.
     * Nhận form-data với:
     *  - "product": JSON của UpdateProductRequest.
     *  - "mediaFiles": danh sách MultipartFile (optional).
     */
    @PutMapping(path = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreateProductResponse>> updateProductWithMedia(
            @PathVariable Long productId,
            @RequestPart("product") @Valid @NotNull UpdateProductRequest updateRequest,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles) {

        // 1. Cập nhật sản phẩm
        CreateProductResponse productResponse = productService.updateProduct(productId, updateRequest);

        // 2. Nếu có media mới, thêm vào sản phẩm (có thể upload thêm mới hoặc cập nhật lại media cũ)
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            List<ProductMediaResponse> mediaResponses = mediaFiles.stream()
                    .map(file -> productService.uploadProductMedia(productId, file))
                    .collect(Collectors.toList());
            productResponse.setMedia(mediaResponses);
        }

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                productResponse));
    }

    /**
     * API xóa sản phẩm
     * Endpoint: DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                null));
    }

    /**
     * API lấy thông tin chi tiết của sản phẩm để sửa
     * Endpoint: GET /api/products/edit/{productId}
     */
    @GetMapping("/edit/{productId}")
    public ResponseEntity<ApiResponse<EditProductResponse>> getProductForEdit(@PathVariable Long productId) {
        EditProductResponse productDetail = productService.getProductForEdit(productId);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                productDetail));
    }


    @PostMapping(path = "/insert-variant/{productId}",consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> createProductVariant(
            @ModelAttribute CreateProductVariantRequest request) {

        ProductVariantResponse variantResponse = productService.createProductVariant(request);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_VARIANTS_RETRIEVED_SUCCESSFULLY),
                variantResponse));
    }

    @PostMapping(path = "/insert-variant/pattern/{productId}",consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> createProductVariantsByPattern(
            @ModelAttribute CreateProductVariantsByPatternRequest request) {

        List<ProductVariantResponse> variantResponses = productService.createProductVariantsByPattern(request);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_VARIANTS_RETRIEVED_SUCCESSFULLY),
                variantResponses));
    }

    /**
     * API cập nhật biến thể sản phẩm
     * @param id ID của ProductVariant được cập nhật
     * @param request Dữ liệu cập nhật
     * @return ProductVariantResponse sau khi cập nhật
     */
    @PutMapping("product-variant/{id}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateProductVariant(
            @PathVariable Long id,
            @RequestBody UpdateProductVariantRequest request) {

        ProductVariantResponse response = productService.updateProductVariant(id, request);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_VARIANTS_RETRIEVED_SUCCESSFULLY),
                response));
    }

    @PutMapping("product-variant/{productId}/{colorId}")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> updateProductVariantsPrice(
            @PathVariable Long productId,
            @PathVariable Long colorId,
            @RequestParam Double salePrice) {

        List<ProductVariantResponse> responses = productService.
                updateSalePriceForVariantsByProductAndColor(productId, colorId,salePrice);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_VARIANTS_RETRIEVED_SUCCESSFULLY),
                responses));
    }

    /**
     * API xóa biến thể sản phẩm
     * @param id ID của ProductVariant cần xóa
     * @return Thông báo xóa thành công
     */
    @DeleteMapping("product-variant/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProductVariant(@PathVariable Long id) {
        productService.deleteProductVariant(id);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_VARIANTS_RETRIEVED_SUCCESSFULLY),
                null));
    }


    @DeleteMapping("/product-variants")
    public ResponseEntity<ApiResponse<String>> deleteProductVariants(@RequestBody List<Long> variantIds) {
        productService.deleteProductVariants(variantIds);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_VARIANTS_RETRIEVED_SUCCESSFULLY),
                null));
    }

    /**
     * Endpoint để upload media cho sản phẩm (chỉ nhận file).
     * Dữ liệu: chỉ có file upload, không có phần JSON data.
     */
    @PostMapping(path = "/upload-media/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<ProductMediaResponse>>> uploadProductMedia(
            @PathVariable Long productId,
            @RequestPart(value = "mediaFiles", required = false) List<MultipartFile> mediaFiles) {

        if (mediaFiles == null || mediaFiles.isEmpty()) {
            throw new IllegalArgumentException("No media files provided");
        }

        // Gọi service upload media, duyệt qua từng file
        List<ProductMediaResponse> mediaResponses = mediaFiles.stream()
                .map(file -> productService.uploadProductMedia(productId, file))
                .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        mediaResponses));
    }

    @PutMapping(value = "product-media/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ApiResponse<ProductMediaResponse>> updateProductMedia(
            @PathVariable Long id,
            @RequestPart(value = "mediaFile", required = false) MultipartFile mediaFile,
            @RequestPart("request") UpdateProductMediaRequest request) {

        ProductMediaResponse updatedMedia = productService.updateProductMedia(id, mediaFile, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        updatedMedia));
    }

    @DeleteMapping("/delete-media/{mediaId}")
    public ResponseEntity<ApiResponse<String>> deleteProductMedia(@PathVariable Long mediaId) {
        productService.deleteProductMedia(mediaId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        null));

    }

    @GetMapping("/media/info/{mediaId}")
    public ResponseEntity<ApiResponse<ProductMediaDetailResponse>> getMediaInfo(
            @PathVariable Long mediaId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        productService.getMedia(mediaId)));

    }

    @GetMapping("/{productId}/inventory")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getInventoryByProductAndColor(@PathVariable Long productId, @RequestParam Long colorId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        productService.getInventoryByProductAndColor(productId, colorId)));

    }

    @GetMapping("/{productId}/{colorId}/{sizeId}/inventory")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByProductAndColorAndSize(
            @PathVariable Long productId, @PathVariable Long colorId, @PathVariable Long sizeId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        productService.getInventoryByProductAndColorAndSize(productId, colorId,sizeId)));
    }

    @GetMapping("/variants/by-product-name")
    public ResponseEntity<ApiResponse<PageResponse<ProductVariantsMediaResponse>>> searchVariants(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String languageCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        PageResponse.fromPage(productService.searchVariantsByProductName(productName,
                                languageCode, pageable)))
                );
    }


    @GetMapping("/image/{filename}")
    public ResponseEntity<Resource> showImage(@PathVariable String filename) {
        try {
            // Construct the file path
            Path filePath = Paths.get("uploads/images/products/" + filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            // Check if file exists
            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Get file extension to determine media type
            String fileExtension = getFileExtension(filename).toLowerCase();
            MediaType mediaType = determineMediaType(fileExtension);

            // Return the file as binary stream
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    private String getFileExtension(String filename) {
        int lastIndexOfDot = filename.lastIndexOf(".");
        if (lastIndexOfDot == -1) {
            return ""; // Không có phần mở rộng
        }
        return filename.substring(lastIndexOfDot + 1);
    }

    private MediaType determineMediaType(String fileExtension) {
        switch (fileExtension.toLowerCase()) {
            case "png":
                return MediaType.IMAGE_PNG;
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "svg":
                return MediaType.valueOf("image/svg+xml");
            case "pdf":
                return MediaType.APPLICATION_PDF;

            // Hỗ trợ video
            case "mp4":
                return MediaType.valueOf("video/mp4");
            case "avi":
                return MediaType.valueOf("video/x-msvideo");
            case "mov":
                return MediaType.valueOf("video/quicktime");
            case "mkv":
                return MediaType.valueOf("video/x-matroska");
            case "webm":
                return MediaType.valueOf("video/webm");

            default:
                return MediaType.APPLICATION_OCTET_STREAM; // Dùng loại này nếu không nhận diện được
        }
    }

    @PostMapping("/set-categories")
    public ResponseEntity<ApiResponse<String>> setCategoriesForProduct(@RequestBody SetCategoryProductRequest request) {
        productService.setCategoriesForProduct(request);
        ApiResponse<String> response = ApiResponse.<String>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.PRODUCT_VARIANTS_RETRIEVED_SUCCESSFULLY))
                .status(HttpStatus.OK.value())
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/remove-category")
    public ResponseEntity<ApiResponse<String>> removeCategoryFromProduct(
            @RequestParam Long productId,
            @RequestParam Long categoryId) {

        productService.removeCategoryFromProduct(productId, categoryId);

        ApiResponse<String> response = ApiResponse.<String>builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY))
                .status(HttpStatus.OK.value())
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }
}
