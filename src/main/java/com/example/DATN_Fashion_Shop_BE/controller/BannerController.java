package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.BannerDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.banner.BannerAdminResponseDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.banner.BannerCreateRequestDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.banner.BannerEditResponseDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.service.BannerService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/banners")
@AllArgsConstructor
public class BannerController {
    private final LocalizationUtils localizationUtils;
    private BannerService bannerService;
    private static final Logger logger = LoggerFactory.getLogger(BannerController.class);

    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList("png", "jpg", "jpeg", "avif", "gif");
    private static final List<String> ALLOWED_VIDEO_EXTENSIONS = Arrays.asList("mp4", "mov", "avi", "mkv");

    @Operation(
            summary = "Lấy danh sách banner với thông tin dịch ngôn ngữ",
            description = "API này trả về danh sách các banner  với thông tin dịch ngôn ngữ dựa trên `languageCode`. " +
                    "Nếu tham số `isActive` được truyền vào, API sẽ chỉ trả về các banner có trạng thái hoạt động (active) tương ứng. " +
                    "Tham số `isActive` là tùy chọn. Nếu không có, tất cả các banner sẽ được trả về mà không phân biệt trạng thái hoạt động. ",
            tags = "Banners"
    )
    @GetMapping("{languageCode}")
    public ResponseEntity<ApiResponse<List<BannerDTO>>> getBannerWithTranslation(
            @PathVariable String languageCode,
            @RequestParam(required = false) Boolean isActive) {

        List<BannerDTO> banners = bannerService.getBannersWithTranslations(languageCode, isActive);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.BANNER_RETRIEVED_SUCCESSFULLY),
                banners
        ));
    }

    @Operation(
            summary = "Lấy thông tin banner cho trang chỉnh sửa",
            description = "API này trả về thông tin chi tiết của một banner để chỉnh sửa, bao gồm logo, media, trạng thái và các thông tin khác.",
            tags = "Banners"
    )
    @GetMapping("edit/{id}")
    public ResponseEntity<ApiResponse<BannerEditResponseDTO>> getBannerForEdit(
            @PathVariable Long id) {

        BannerEditResponseDTO banner = bannerService.getBannerForEdit(id);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.BANNER_RETRIEVED_SUCCESSFULLY),
                banner
        ));
    }

    @Operation(
            summary = "Tạo một banner mới",
            description = "API này cho phép tạo một banner mới bao gồm thông tin yêu cầu và các file hình ảnh/video (nếu có).",
            tags = "Banners"
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BannerEditResponseDTO>> createBanner(
            @RequestPart("request") @Valid BannerCreateRequestDTO request,
            BindingResult bindingResult,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
            @RequestPart(value = "mediaFile", required = false) MultipartFile mediaFile) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            bindingResult,
                            localizationUtils.getLocalizedMessage(MessageKeys.INSERT_BANNER_SUCCESSFULLY),
                            localizationUtils
                    )
            );
        }

        // Kiểm tra logoFile, chỉ kiểm tra nếu logoFile không phải là null
        if (logoFile != null && !logoFile.isEmpty() &&  !isValidFile(logoFile, ALLOWED_IMAGE_EXTENSIONS)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.VALIDATION_IMAGE),
                            "logoFile",
                            logoFile.getOriginalFilename(),
                            null
                    )
            );
        }

        if (mediaFile != null) {
            if (!isValidFile(mediaFile, ALLOWED_IMAGE_EXTENSIONS) && !isValidFile(mediaFile, ALLOWED_VIDEO_EXTENSIONS)) {
                return ResponseEntity.badRequest().body(
                        ApiResponseUtils.errorResponse(
                                HttpStatus.BAD_REQUEST,
                                localizationUtils.getLocalizedMessage(MessageKeys.VALIDATION_IMAGE)
                                        +  " Or " +
                                localizationUtils.getLocalizedMessage(MessageKeys.VALIDATION_VIDEO)
                                ,
                                "mediaFile",
                                mediaFile.getOriginalFilename(),
                                null
                        )
                );
            }
        }


        // Xử lý logic tạo banner
        BannerEditResponseDTO createdBanner = bannerService.createBanner(request, logoFile, mediaFile);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.INSERT_BANNER_SUCCESSFULLY),
                createdBanner
        ));
    }

    @Operation(
            summary = "Cập nhật thông tin banner",
            description = "API này cho phép cập nhật thông tin banner, bao gồm logo, media và các thông tin khác.",
            tags = "Banners"
    )
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<BannerEditResponseDTO>> updateBanner(
            @PathVariable Long id,
            @RequestPart("request") @Valid BannerCreateRequestDTO request,
            BindingResult bindingResult,
            @RequestPart(value = "logoFile", required = false) MultipartFile logoFile,
            @RequestPart(value = "mediaFile", required = false) MultipartFile mediaFile) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ApiResponseUtils.generateValidationErrorResponse(
                            bindingResult,
                            localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY),
                            localizationUtils
                    )
            );
        }

        // Kiểm tra logoFile, chỉ kiểm tra nếu logoFile không phải là null
        if (logoFile != null && !logoFile.isEmpty() &&  !isValidFile(logoFile, ALLOWED_IMAGE_EXTENSIONS)) {
            return ResponseEntity.badRequest().body(
                    ApiResponseUtils.errorResponse(
                            HttpStatus.BAD_REQUEST,
                            localizationUtils.getLocalizedMessage(MessageKeys.VALIDATION_IMAGE),
                            "logoFile",
                            logoFile.getOriginalFilename(),
                            null
                    )
            );
        }

        if (mediaFile != null) {
            if (!isValidFile(mediaFile, ALLOWED_IMAGE_EXTENSIONS) && !isValidFile(mediaFile, ALLOWED_VIDEO_EXTENSIONS)) {
                return ResponseEntity.badRequest().body(
                        ApiResponseUtils.errorResponse(
                                HttpStatus.BAD_REQUEST,
                                localizationUtils.getLocalizedMessage(MessageKeys.VALIDATION_IMAGE)
                                        +  " Or " +
                                        localizationUtils.getLocalizedMessage(MessageKeys.VALIDATION_VIDEO)
                                ,
                                "mediaFile",
                                mediaFile.getOriginalFilename(),
                                null
                        )
                );
            }
        }


        BannerEditResponseDTO updatedBanner = bannerService.updateBanner(id, request, logoFile, mediaFile);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.UPDATE_CATEGORY_SUCCESSFULLY),
                updatedBanner
        ));
    }

    @Operation(
            summary = "Xóa một banner",
            description = "API này cho phép xóa một banner bằng cách sử dụng ID của banner.",
            tags = "Banners"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.BANNER_DELETED_SUCCESSFULLY),
                null
        ));
    }

    @Operation(
            summary = "Lấy danh sách banner phân trang cho admin",
            description = "API này trả về danh sách banner có phân trang dành cho admin. API hỗ trợ tìm kiếm và sắp xếp theo các trường khác nhau.",
            tags = "Banners"
    )
    @GetMapping("{languageCode}/banner/admin")
    public ResponseEntity<ApiResponse<PageResponse<BannerAdminResponseDTO>>> getBanners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @PathVariable String languageCode) {

        // Gọi dịch vụ để lấy danh sách banner phân trang
        Page<BannerAdminResponseDTO> pageResult = bannerService
                .getBannerPage(page, size, title, isActive, languageCode, sortBy, sortDir);

        // Tạo phản hồi cho API
        PageResponse<BannerAdminResponseDTO> response = PageResponse.<BannerAdminResponseDTO>builder()
                .content(pageResult.getContent())
                .pageNo(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalPages(pageResult.getTotalPages())
                .totalElements(pageResult.getTotalElements())
                .first(pageResult.isFirst())
                .last(pageResult.isLast())
                .build();

        // Trả về phản hồi thành công
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.BANNER_RETRIEVED_SUCCESSFULLY),
                response
        ));
    }

    @Operation(
            summary = "Đặt lịch kích hoạt và kết thúc cho banner",
            description = "API này cho phép cập nhật lịch kích hoạt và kết thúc cho banner, sử dụng ngày và giờ trong tương lai.",
            tags = "Banners"
    )
    @PutMapping("/{id}/schedule")
    public ResponseEntity<ApiResponse<BannerAdminResponseDTO>> scheduleBanner(
            @PathVariable Long id,
            @RequestParam("activationDate") @NotNull @Future LocalDateTime activationDate,
            @RequestParam("endDate") @NotNull @Future LocalDateTime endDate) {

        // Cập nhật ngày giờ kích hoạt và kết thúc cho banner
        BannerAdminResponseDTO updatedBanner = bannerService.scheduleBanner(id, activationDate, endDate);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                "Banner activation and end date scheduled successfully",
                updatedBanner
        ));
    }

    @Operation(
            summary = "Lấy file media cho banner",
            description = "API này trả về file media của banner (ví dụ: video hoặc hình ảnh) từ thư mục uploads.",
            tags = "Banners"
    )
    @GetMapping("/media/{filename}")
    public ResponseEntity<Resource> getMedia(@PathVariable String filename) {
        try {
            // Construct the file path
            Path filePath = Paths.get("uploads/images/banners/media/" + filename).normalize();
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

    @Operation(
            summary = "Lấy logo của banner",
            description = "API này trả về logo của banner từ thư mục uploads.",
            tags = "Banners"
    )
    @GetMapping("/logo/{filename}")
    public ResponseEntity<Resource> getLogo(@PathVariable String filename) {
        try {
            // Construct the file path
            Path filePath = Paths.get("uploads/images/banners/logo_images/" + filename).normalize();
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
