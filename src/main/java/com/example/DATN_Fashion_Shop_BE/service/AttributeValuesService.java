package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.ColorDTO;
import com.example.DATN_Fashion_Shop_BE.dto.ProductMediaDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateColorRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateSizeRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values.*;
import com.example.DATN_Fashion_Shop_BE.model.Attribute;
import com.example.DATN_Fashion_Shop_BE.model.AttributeValue;
import com.example.DATN_Fashion_Shop_BE.model.ProductMedia;
import com.example.DATN_Fashion_Shop_BE.model.Role;
import com.example.DATN_Fashion_Shop_BE.repository.AttributePatternRepository;
import com.example.DATN_Fashion_Shop_BE.repository.AttributeRepository;
import com.example.DATN_Fashion_Shop_BE.repository.AttributeValueRepository;
import com.example.DATN_Fashion_Shop_BE.repository.RoleRepository;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeValuesService {
    private final AttributeRepository attributeRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final AttributePatternRepository attributePatternRepository;
    private final FileStorageService fileStorageService;
    private final LocalizationUtils localizationUtils;

    public CreateColorResponse createColor(CreateColorRequest request, MultipartFile file) {
        // Tìm Attribute có name là "Color"
        Attribute colorAttribute = attributeRepository.findByName("Color")
                .orElseThrow(() -> new ResourceNotFoundException("Attribute 'Color' not found"));

        // Lưu file và lấy URL
        String fileUrl = fileStorageService.uploadFileAndGetName(file,"images/products/colors");

        // Tạo mới AttributeValue
        AttributeValue attributeValue = AttributeValue.builder()
                .valueName(request.getValueName())
                .valueImg(fileUrl)
                .sortOrder(request.getSortOrder())
                .attribute(colorAttribute)
                .build();

        attributeValue = attributeValueRepository.save(attributeValue);

        return CreateColorResponse.fromAttributeValues(attributeValue);
    }

    public CreateSizeResponse createSize(CreateSizeRequest request) {
        // Tìm Attribute có name là "size"
        Attribute sizeAttribute = attributeRepository.findByName("Size")
                .orElseThrow(() -> new ResourceNotFoundException("Attribute 'Color' not found"));

        // Tạo mới AttributeValue
        AttributeValue attributeValue = AttributeValue.builder()
                .valueName(request.getValueName())
                .sortOrder(request.getSortOrder())
                .attribute(sizeAttribute)
                .build();

        attributeValue = attributeValueRepository.save(attributeValue);

        return CreateSizeResponse.fromAttributeValues(attributeValue);
    }

    public Page<ColorResponse> getAllColors(String nameKeyword, Pageable pageable) {
        Page<AttributeValue> page = attributeValueRepository.findAllColorsByName(nameKeyword, pageable);
        return page.map(ColorResponse::fromAttributeValues);
    }

    public Page<SizeResponse> getAllSizes(String nameKeyword, Pageable pageable) {
        Page<AttributeValue> page = attributeValueRepository.findAndSizesName(nameKeyword, pageable);
        return page.map(SizeResponse::fromAttributeValues);
    }

    public Page<AttributeValuePatternResponse> getAllPatterns(Pageable pageable) {
        return attributePatternRepository.findAll(pageable)
                .map(AttributeValuePatternResponse::fromAttributeValuePattern);
    }

    public Page<AttributeValueResponse> getAttributeValuesByPattern(Long patternId, String name, Pageable pageable) {
        return attributeValueRepository.findAllByPatternIdAndName(patternId, name, pageable)
                .map(AttributeValueResponse::fromAttributeValue);
    }

    public CreateColorResponse updateColor(Long id, CreateColorRequest updateRequest, MultipartFile colorImage) {
        // Tìm đối tượng màu theo id
        AttributeValue color = attributeValueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy màu với ID: " + id));

        // Cập nhật các trường từ updateRequest
        color.setValueName(updateRequest.getValueName());
        color.setSortOrder(updateRequest.getSortOrder());
        // Nếu có file mới, xóa file cũ và upload file mới
        if (colorImage != null) {
            fileStorageService.deleteFile(color.getValueImg(), "colors");
            String newFileName = fileStorageService.uploadFileAndGetName(colorImage, "images/products/colors");
            color.setValueImg(newFileName);
        }

        AttributeValue updatedColor = attributeValueRepository.save(color);
        return CreateColorResponse.fromAttributeValues(updatedColor);
    }

    public void deleteColor(Long id) {
        AttributeValue color = attributeValueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy màu với ID: " + id));
        // Nếu có file ảnh, xóa file ảnh trước
        if (color.getValueImg() != null) {
            fileStorageService.deleteFile(color.getValueImg(), "colors");
        }
        attributeValueRepository.delete(color);
    }

    public CreateSizeResponse updateSize(Long id, CreateSizeRequest updateRequest) {
        // Tìm đối tượng kích thước theo id
        AttributeValue size = attributeValueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kích thước với ID: " + id));

        // Cập nhật các trường
        size.setValueName(updateRequest.getValueName());
        size.setSortOrder(updateRequest.getSortOrder());
        AttributeValue updatedSize = attributeValueRepository.save(size);
        return CreateSizeResponse.fromAttributeValues(updatedSize);
    }

    public void deleteSize(Long id) {
        AttributeValue size = attributeValueRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kích thước với ID: " + id));
        attributeValueRepository.delete(size);
    }

}
