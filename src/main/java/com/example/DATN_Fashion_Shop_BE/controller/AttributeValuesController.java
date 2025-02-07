package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.BannerDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateColorRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateSizeRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.banner.BannerCreateRequestDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.banner.BannerAdminResponseDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.banner.BannerEditResponseDTO;
import com.example.DATN_Fashion_Shop_BE.service.AttributeValuesService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
@RequestMapping("${api.prefix}/attribute_values")
@AllArgsConstructor
public class AttributeValuesController {
    private final LocalizationUtils localizationUtils;
    private AttributeValuesService attributeValuesService;
    private static final Logger logger = LoggerFactory.getLogger(AttributeValuesController.class);

    @PostMapping("/color")
    public ResponseEntity<ApiResponse<CreateColorResponse>> createColor(
            @RequestPart("request") CreateColorRequest request,
            @RequestPart(value = "colorImage", required = false) MultipartFile valueImage
            ) {

        CreateColorResponse response = attributeValuesService.createColor(request, valueImage);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        response));
    }

    @PostMapping("/size")
    public ResponseEntity<ApiResponse<CreateSizeResponse>> createSize(
            @RequestPart("request") CreateSizeRequest request
    ) {

        CreateSizeResponse response = attributeValuesService.createSize(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseUtils.successResponse(
                        localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                        response));
    }


    @PutMapping(path = "/color/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreateColorResponse>> updateColor(
            @PathVariable Long id,
            @RequestPart("request") CreateColorRequest updateRequest,
            @RequestPart(value = "colorImage", required = false) MultipartFile colorImage) {

        CreateColorResponse response = attributeValuesService.updateColor(id, updateRequest, colorImage);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                response));
    }

    @PutMapping("/size/{id}")
    public ResponseEntity<ApiResponse<CreateSizeResponse>> updateSize(
            @PathVariable Long id,
            @RequestBody CreateSizeRequest updateRequest) {

        CreateSizeResponse response = attributeValuesService.updateSize(id, updateRequest);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                response));
    }

    @DeleteMapping("/color/{id}")
    public ResponseEntity<ApiResponse<String>> deleteColor(@PathVariable Long id) {
        attributeValuesService.deleteColor(id);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                null));
    }

    @DeleteMapping("/size/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSize(@PathVariable Long id) {
        attributeValuesService.deleteSize(id);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                null));
    }

    @GetMapping("/colors")
    public ResponseEntity<ApiResponse<PageResponse<ColorResponse>>> getAllColors(
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ColorResponse> colorsPage = attributeValuesService.getAllColors(name, pageable);

        PageResponse<ColorResponse> pageResponse = PageResponse.fromPage(colorsPage);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                pageResponse));
    }

    @GetMapping("/sizes")
    public ResponseEntity<ApiResponse<PageResponse<SizeResponse>>> getAllSizes(
            @RequestParam(value = "name", required = false, defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<SizeResponse> sizesPage = attributeValuesService.getAllSizes(name, pageable);

        PageResponse<SizeResponse> pageResponse = PageResponse.fromPage(sizesPage);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                pageResponse));
    }

    @GetMapping("/patterns")
    public ResponseEntity<ApiResponse<PageResponse<AttributeValuePatternResponse>>> getAllPatterns(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<AttributeValuePatternResponse> patternsPage = attributeValuesService.getAllPatterns(pageable);
        PageResponse<AttributeValuePatternResponse> pageResponse = PageResponse.fromPage(patternsPage);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                pageResponse));
    }

    @GetMapping("/{patternId}")
    public ResponseEntity<ApiResponse<PageResponse<AttributeValueResponse>>> getAttributeValuesByPattern(
            @PathVariable Long patternId,
            @RequestParam(value = "name", defaultValue = "") String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDir), sortBy));
        Page<AttributeValueResponse> valuesPage = attributeValuesService.getAttributeValuesByPattern(patternId, name, pageable);
        PageResponse<AttributeValueResponse> pageResponse = PageResponse.fromPage(valuesPage);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                pageResponse));
    }

    @GetMapping("/color/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {

            Path filePath = Paths.get("uploads/images/products/colors/" + filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

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
