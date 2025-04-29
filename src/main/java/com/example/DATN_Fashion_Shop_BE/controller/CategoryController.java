package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.CategoryDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.category.CategoryAdminResponseDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.category.CategoryCreateRequestDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.category.CategoryCreateResponseDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.category.CategoryEditResponseDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.FieldErrorDetails;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.model.CategoriesTranslation;
import com.example.DATN_Fashion_Shop_BE.model.Category;
import com.example.DATN_Fashion_Shop_BE.repository.CategoryRepository;
import com.example.DATN_Fashion_Shop_BE.repository.CategoryTranslationRepository;
import com.example.DATN_Fashion_Shop_BE.service.CategoryService;

import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix}/categories")
@AllArgsConstructor
public class CategoryController {
    private final LocalizationUtils localizationUtils;
    private final CategoryTranslationRepository categoryTranslationRepository;
    private final CategoryRepository categoryRepository;
    private CategoryService categoryService;
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg", "avif", "gif");
    @Operation(
            summary = "Lấy danh sách category cho Admin",
            description = "API này được sử dụng để lấy danh sách category cho quản trị viên với các tham số phân trang và lọc theo nhiều tiêu chí." +
                    "\n\nCác tham số đầu vào bao gồm:" +
                    "\n- `page`: Chỉ định trang hiện tại cần lấy. Giá trị mặc định là `0` nếu không được cung cấp." +
                    "\n- `size`: Số lượng category trên mỗi trang. Giá trị mặc định là `10` nếu không được cung cấp." +
                    "\n- `name`: Lọc theo tên category. Nếu không cung cấp, tất cả các category sẽ được trả về." +
                    "\n- `parentId`: Lọc theo ID của category cha." +
                    "\n  - Nếu không có tham số này, API sẽ trả về tất cả các category." +
                    "\n  - Nếu tham số này có giá trị `0`, API sẽ chỉ trả về các category không có category cha." +
                    "\n  - Nếu cung cấp `parentId` là một giá trị cụ thể, API sẽ trả về các category con của category cha đó." +
                    "\n- `isActive`: Lọc các category theo trạng thái hoạt động (`true` hoặc `false`). Nếu không cung cấp, tất cả các category sẽ được lấy." +
                    "\n- `sortBy`: Chỉ định trường để sắp xếp category. Giá trị mặc định là `createdAt`." +
                    "\n- `sortDir`: Chỉ định hướng sắp xếp (tăng dần hoặc giảm dần). Giá trị mặc định là `asc` (tăng dần).",
            tags = {"Categories"},
            security = { @SecurityRequirement(name = "bearer-key") }
    )
    @GetMapping("{languageCode}/admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<CategoryAdminResponseDTO>>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @PathVariable String languageCode) {

        Page<CategoryAdminResponseDTO> pageResult = categoryService
                .getCategoriesPage(page, size, name, parentId, isActive, languageCode, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                PageResponse.fromPage(pageResult)
        ));
    }

    @Operation(
            summary = "Lấy thông tin category theo ID",
            description = "API này trả về thông tin chi tiết của category với ID đã cho. " +
                    "Tham số `id` là ID của category cần lấy thông tin. " +
                    "Các tham số yêu cầu: \n" +
                    "- `languageCode`: Mã ngôn ngữ (ví dụ: `en`, `vi`).\n" +
                    "- `id`: ID của category cần truy vấn.\n",
            tags = {"Categories"}
    )
    @GetMapping("{languageCode}/category/{id}")
    public ResponseEntity<ApiResponse<Object>> getCategory(
            @PathVariable String languageCode,
            @PathVariable Long id) {

        if (id == null || id <= 0) {
            logger.error("Invalid category ID: {}", id);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_FAILED),
                            "id",
                            id
                            ,
                            MessageKeys.CATEGORY_NOT_FOUND
                    )
            );
        }

        CategoryDTO categoryDTO = categoryService.getCategory(id, languageCode);

        if (categoryDTO == null || !categoryDTO.getIsActive()) {
            logger.error("Category ID {} is inactive or not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.NOT_FOUND,
                            localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NOT_FOUND),
                            "id",
                            id
                            ,
                            MessageKeys.CATEGORY_NOT_FOUND
                    )
            );
        }

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                categoryDTO
        ));
    }
    /**
     * Lấy danh sách category cha với bản dịch theo ngôn ngữ.
     *
     * @param languageCode Mã ngôn ngữ (vd: en, vi, jp).
     * @return Danh sách category cha với tên đã dịch.
     */
    @Operation(
            summary = "Lấy danh sách category cha với thông tin dịch ngôn ngữ",
            description = "API này trả về danh sách các category cha (có `parentId` là null) với thông tin dịch ngôn ngữ dựa trên `languageCode`. " +
                    "Nếu tham số `isActive` được truyền vào, API sẽ chỉ trả về các category có trạng thái hoạt động (active) tương ứng. " +
                    "Tham số `isActive` là tùy chọn. Nếu không có, tất cả các category cha sẽ được trả về mà không phân biệt trạng thái hoạt động. ",
            tags = "Categories"
    )
    @GetMapping("{languageCode}/parent")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getParentCategoriesWithTranslations(
            @PathVariable String languageCode,
            @RequestParam(required = false) Boolean isActive) {

        List<CategoryDTO> parentCategories = categoryService.getParentCategoriesWithTranslations(languageCode, isActive);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                parentCategories
        ));
    }

    @Operation(
            summary = "Lấy danh sách category con của category cha với thông tin dịch ngôn ngữ",
            description = "API này trả về danh sách các category con của một category cha cụ thể (được xác định bởi `parentId`) với thông tin dịch ngôn ngữ theo `languageCode`. " +
                    "Nếu tham số `isActive` được truyền vào, API sẽ chỉ trả về các category con có trạng thái hoạt động (active) tương ứng. " +
                    "Tham số `isActive` là tùy chọn. Nếu không có, tất cả các category con sẽ được trả về mà không phân biệt trạng thái hoạt động. ",
            tags = "Categories"
    )
    @GetMapping("{languageCode}/category/parent/{parentId}")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getChildCategoriesWithTranslations(
            @PathVariable String languageCode,
            @PathVariable Long parentId,
            @RequestParam(required = false) Boolean isActive) {

        List<CategoryDTO> childCategories = categoryService.getChildCategoriesWithTranslations(languageCode, parentId, isActive);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                childCategories
        ));
    }

    @Operation(
            summary = "Lấy category cha bằng id của category con",
            description = "api đảo ngược. ",
            tags = "Categories"
    )
    @GetMapping("{languageCode}/category/parent/reverse/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getParent(
            @PathVariable String languageCode,
            @PathVariable Long categoryId,
            @RequestParam(required = false) Boolean isActive) {

        CategoryDTO childCategories = categoryService.getParentCategoriesWithTranslations(languageCode,
                categoryId, isActive);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                childCategories
        ));
    }
    public CategoryDTO getParentCategoriesWithTranslations(String languageCode, Long categoryId, Boolean isActive) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));

        // Tìm danh mục cha gần nhất thỏa mãn điều kiện isActive (nếu có)
        Category parent = category.getParentCategory();
        while (parent != null && isActive != null && !parent.getIsActive().equals(isActive)) {
            parent = parent.getParentCategory();
        }

        // Nếu không có danh mục cha nào thỏa mãn, trả về null
        if (parent == null) {
            return null;
        }

        // Lấy bản dịch của danh mục cha gần nhất
        CategoriesTranslation translation = categoryTranslationRepository
                .findByCategoryIdAndLanguageCode(parent.getId(), languageCode)
                .orElse(null);

        // Chuyển sang DTO
        return CategoryDTO.builder()
                .id(parent.getId())
                .imageUrl(parent.getImageUrl())
                .name(translation != null ? translation.getName() : "")
                .isActive(parent.getIsActive())
                .build();
    }

    @Operation(
            summary = "Lấy thông tin category để admin chỉnh sửa",
            description = "API này trả về thông tin chi tiết của category với ID xác định để người dùng có thể chỉnh sửa.",
            tags = {"Categories"}
    )
    @GetMapping("edit/{id}")
    public ResponseEntity<ApiResponse<CategoryEditResponseDTO>> getCategoryForEdit(@PathVariable Long id) {
        CategoryEditResponseDTO responseDTO = categoryService.getCategoryForEdit(id);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_RETRIEVED_SUCCESSFULLY),
                responseDTO
        ));
    }
    /**
     * API thêm category mới với hình ảnh và các bản dịch.
     *
     * @param request   DTO chứa thông tin category cần tạo
     * @param imageFile Tệp hình ảnh được tải lên
     * @return Thông tin category đã tạo
     */
    @Operation(
            summary = "Tạo Category mới",
            description = "API này tạo một category mới với thông tin từ đối tượng `CategoryCreateRequestDTO` và ảnh được tải lên qua `imageFile`. " +
                    "Sau khi tạo thành công, trả về thông tin chi tiết của category mới được tạo, bao gồm URL của ảnh đã tải lên.",
            tags = "Categories"
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CategoryCreateResponseDTO>> createCategoryWithImage(
            @RequestPart("request") @Valid CategoryCreateRequestDTO request,
            BindingResult bindingResult,
            @RequestPart("imageFile") MultipartFile imageFile) {

        if (bindingResult.hasErrors()) {
            List<FieldErrorDetails> fieldErrors = bindingResult.getFieldErrors().stream()
                    .map(error -> new FieldErrorDetails(
                            error.getField(),
                            request,
                            localizationUtils.getLocalizedMessage(error.getDefaultMessage())
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_FAILED),
                            fieldErrors
                    )
            );
        }

        if (imageFile != null && !imageFile.isEmpty() &&  !isValidFile(imageFile, ALLOWED_IMAGE_EXTENSIONS)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.VALIDATION_IMAGE),
                            "logoFile",
                            imageFile.getOriginalFilename(),
                            null
                    )
            );
        }

        CategoryDTO createdCategory = categoryService.createCategoryWithImage(request, imageFile);
        request.setImageUrl(createdCategory.getImageUrl());

        CategoryCreateResponseDTO responseData = CategoryCreateResponseDTO.builder()
                .id(createdCategory.getId())
                .requestCategory(request)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.INSERT_CATEGORY_SUCCESSFULLY),
                        responseData
                )
        );
    }


    @Operation(
            summary = "Cập nhật Category",
            description = "endpoint cho phép cập nhật tên, hình ảnh , parentId của category",
            tags = {"Categories"}
    )
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryCreateResponseDTO>> updateCategoryWithImage(
            @PathVariable Long id,
            @RequestPart("request") @Valid CategoryCreateRequestDTO request,
            BindingResult bindingResult,
            @RequestPart("imageFile") MultipartFile imageFile) {

        // Kiểm tra lỗi xác thực
        if (bindingResult.hasErrors()) {
            List<FieldErrorDetails> fieldErrors = bindingResult.getFieldErrors().stream()
                    .map(error -> new FieldErrorDetails(
                            error.getField(),
                            request,
                            localizationUtils.getLocalizedMessage(error.getDefaultMessage())
                    ))
                    .collect(Collectors.toList());

            logger.error("Category update failed with validation errors: {}", fieldErrors);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_FAILED),
                            fieldErrors
                    )
            );
        }


        if (imageFile != null && !imageFile.isEmpty() &&  !isValidFile(imageFile, ALLOWED_IMAGE_EXTENSIONS)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.VALIDATION_IMAGE),
                            "logoFile",
                            imageFile.getOriginalFilename(),
                            null
                    )
            );
        }

        // Cập nhật category và hình ảnh
        CategoryDTO updatedCategory = categoryService.updateCategoryWithImage(id, request, imageFile);
        request.setImageUrl(updatedCategory.getImageUrl());

        // Chuẩn hóa phản hồi thành công
        CategoryCreateResponseDTO responseData = CategoryCreateResponseDTO.builder()
                .id(updatedCategory.getId())
                .requestCategory(request)
                .build();

        logger.info("Category updated successfully with ID: {}", updatedCategory.getId());

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY),
                        responseData
                )
        );
    }


    @Operation(
            summary = "Cập nhật trạng thái isActive true/false",
            description = "endpoint cho phép cập nhật trạng thái is_active của categories thành true/false",
            tags = {"Categories"}
    )
    @PutMapping("status/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategoryStatus(
            @PathVariable Long id,
            @RequestParam("isActive") boolean isActive) {

        // Cập nhật trạng thái của danh mục
        CategoryDTO updatedCategory = categoryService.updateCategoryStatus(id, isActive);

        // Chuẩn hóa phản hồi thành công
        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY),
                        updatedCategory
                )
        );
    }

    @Operation(
            summary = "Xóa Category",
            description = "Xóa category theo Id",
            tags = "Categories"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.BANNER_DELETED_SUCCESSFULLY),
                null
        ));
    }

    @Operation(
            summary = "Lấy hình ảnh của category theo tên file",
            description = "Endpoint này cho phép lấy hình ảnh của category từ hệ thống file dựa trên tên file. " +
                    "Nếu file tồn tại, API sẽ trả về hình ảnh với loại media tương ứng (JPEG, PNG, v.v.). ",
            tags = {"Categories"}
    )
    @GetMapping("/image/{filename}")
        public ResponseEntity<Resource> getImage(@PathVariable String filename) {
            try {
                // Construct the file path
                Path filePath = Paths.get("uploads/images/categories/" + filename).normalize();
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
    private boolean isValidFile(MultipartFile file, List<String> allowedExtensions) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            return false;
        }

        // Lấy đuôi file và kiểm tra
        String fileExtension = getFileExtension(fileName);
        return allowedExtensions.contains(fileExtension.toLowerCase());
    }
    // Hỗ trợ nhận diện MediaType dựa trên đuôi file
    private String getFileExtension(String filename) {
        int lastIndexOfDot = filename.lastIndexOf(".");
        if (lastIndexOfDot == -1) {
            return ""; // Không có phần mở rộng
        }
        return filename.substring(lastIndexOfDot + 1);
    }

    private MediaType determineMediaType(String fileExtension) {
        switch (fileExtension) {
            case "png":
                return MediaType.IMAGE_PNG;
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "pdf":
                return MediaType.APPLICATION_PDF;
            case "svg":
                return MediaType.valueOf("image/svg+xml");
            default:
                return MediaType.APPLICATION_OCTET_STREAM; // Dùng loại này nếu không nhận diện được
        }
    }
}
