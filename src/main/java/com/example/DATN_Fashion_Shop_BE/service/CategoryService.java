package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.CategoryDTO;
import com.example.DATN_Fashion_Shop_BE.dto.CategoryTranslationDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.category.CategoryAdminResponseDTO;
import com.example.DATN_Fashion_Shop_BE.dto.request.category.CategoryCreateRequestDTO;
import com.example.DATN_Fashion_Shop_BE.dto.response.category.CategoryEditResponseDTO;
import com.example.DATN_Fashion_Shop_BE.model.CategoriesTranslation;
import com.example.DATN_Fashion_Shop_BE.model.Category;
import com.example.DATN_Fashion_Shop_BE.model.Language;
import com.example.DATN_Fashion_Shop_BE.repository.CategoryRepository;
import com.example.DATN_Fashion_Shop_BE.repository.CategoryTranslationRepository;
import com.example.DATN_Fashion_Shop_BE.repository.LanguageRepository;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CategoryService {

    private CategoryRepository categoryRepository;

    private CategoryTranslationRepository categoryTranslationRepository;

    private LanguageRepository languageRepository;

    private FileStorageService fileStorageService;

    private final LocalizationUtils localizationUtils;

    public CategoryDTO getCategory(Long id, String languageCode) {
        // Tìm danh mục theo ID
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NOT_FOUND, id))
                );

        // Lấy bản dịch dựa vào ID danh mục và mã ngôn ngữ
        CategoriesTranslation translation = categoryTranslationRepository
                .findByCategoryIdAndLanguageCode(id, languageCode)
                .orElseGet(() -> {
                    // Nếu không tìm thấy bản dịch cho ngôn ngữ yêu cầu, kiểm tra bản dịch mặc định (tiếng Anh)
                    return categoryTranslationRepository.findByCategoryIdAndLanguageCode(id, "en")
                            .orElseThrow(() -> new RuntimeException(
                                    localizationUtils.getLocalizedMessage(
                                            MessageKeys.CATEGORY_NOT_FOUND_TRANSLATION,
                                            id, languageCode)
                            ));
                });

        // Ánh xạ dữ liệu sang DTO
        return CategoryDTO.builder()
                .id(category.getId())
                .imageUrl(category.getImageUrl())
                .name(translation.getName()) // Tên bản dịch
                .isActive(category.getIsActive())
                .build();
    }

    public List<CategoryDTO> getParentCategoriesWithTranslations(String languageCode, Boolean isActive) {
        // Lấy danh mục cha theo điều kiện isActive
        List<Category> parentCategories;
        if (isActive != null) {
            parentCategories = categoryRepository.findParentCategoriesByIsActive(isActive);
        } else {
            parentCategories = categoryRepository.findParentCategories();
        }

        // Lấy danh sách ID của danh mục cha
        List<Long> categoryIds = parentCategories.stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        // Lấy bản dịch theo danh sách ID và languageCode
        List<CategoriesTranslation> translations = categoryTranslationRepository
                .findByCategoryIdInAndLanguageCode(categoryIds, languageCode);

        // Tạo map từ ID danh mục -> bản dịch
        Map<Long, String> translationMap = translations.stream()
                .collect(Collectors.toMap(
                        translation -> translation.getCategory().getId(),
                        CategoriesTranslation::getName
                ));

        // Ánh xạ Entity sang DTO bằng Builder
        return parentCategories.stream()
                .map(category -> CategoryDTO.builder()
                        .id(category.getId())
                        .imageUrl(category.getImageUrl())
                        .name(translationMap.getOrDefault(category.getId(), ""))
                        .isActive(category.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }

    public List<CategoryDTO> getChildCategoriesWithTranslations(String languageCode, Long parentId, Boolean isActive) {
        // Lấy danh mục con theo điều kiện isActive
        List<Category> childCategories;
        if (isActive != null) {
            childCategories = categoryRepository.findChildCategoriesByParentIdAndIsActive(parentId, isActive);
        } else {
            childCategories = categoryRepository.findChildCategoriesByParentId(parentId);
        }

        // Lấy danh sách ID của các danh mục con
        List<Long> categoryIds = childCategories.stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        // Lấy các bản dịch theo danh sách ID và ngôn ngữ
        List<CategoriesTranslation> translations = categoryTranslationRepository
                .findByCategoryIdInAndLanguageCode(categoryIds, languageCode);

        // Tạo map từ ID danh mục -> bản dịch
        Map<Long, String> translationMap = translations.stream()
                .collect(Collectors.toMap(
                        translation -> translation.getCategory().getId(),
                        CategoriesTranslation::getName
                ));

        // Ánh xạ Entity sang DTO
        return childCategories.stream()
                .map(category -> CategoryDTO.builder()
                        .id(category.getId())
                        .imageUrl(category.getImageUrl())
                        .name(translationMap.getOrDefault(category.getId(), ""))
                        .isActive(category.getIsActive())
                        .build())
                .collect(Collectors.toList());
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


    public CategoryEditResponseDTO getCategoryForEdit(Long id) {
        // Lấy Category từ cơ sở dữ liệu
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NOT_FOUND, id))
                );

        // Lấy tất cả các bản dịch liên quan đến Category này
        List<CategoriesTranslation> translations = categoryTranslationRepository.findByCategoryId(id);

        // Map các bản dịch thành DTO
        List<CategoryTranslationDTO> translationDTOs = translations.stream()
                .map(translation -> new CategoryTranslationDTO(
                        translation.getLanguage().getCode(),
                        translation.getName()))
                .collect(Collectors.toList());

        // Tạo DTO response
        CategoryEditResponseDTO responseDTO = new CategoryEditResponseDTO();
        responseDTO.setId(category.getId());
        responseDTO.setImageUrl(category.getImageUrl());
        responseDTO.setParentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null);
        responseDTO.setIsActive(category.getIsActive());
        responseDTO.setTranslations(translationDTOs);

        return responseDTO;
    }

    @Transactional
    public CategoryDTO createCategoryWithImage(CategoryCreateRequestDTO request, MultipartFile imageFile) {
        // 1. Upload file và lấy URL
        String imageUrl = fileStorageService.uploadFileAndGetName(imageFile, "images/categories");

        // 2. Tạo Category
        Category category = new Category();
        category.setImageUrl(imageUrl);
        category.setParentCategory(request.getParentId() != null ?
                categoryRepository.findById(request.getParentId())
                        .orElseThrow(() -> new RuntimeException("Parent category not found"))
                : null);
        category = categoryRepository.save(category);

        // Tạo biến final cho category
        final Category savedCategory = category;

        // 3. Lưu các bản dịch
        List<CategoriesTranslation> translations = request.getTranslations().stream()
                .map(translationDTO -> {
                    Language language = languageRepository.findByCode(translationDTO.getLanguageCode())
                            .orElseThrow(() -> new RuntimeException("Language not found for code: " + translationDTO.getLanguageCode()));

                    // Tạo bản dịch cho danh mục
                    return new CategoriesTranslation(
                            null,
                            translationDTO.getName(),
                            savedCategory, // Sử dụng biến final
                            language
                    );
                })
                .collect(Collectors.toList());
        categoryTranslationRepository.saveAll(translations);

        // 4. Trả về DTO
        return CategoryDTO.builder()
                .id(category.getId())
                .imageUrl(category.getImageUrl())
                .name(request.getTranslations().get(0).getName()) // Mặc định tên theo ngôn ngữ đầu tiên
                .build();
    }

    @Transactional
    public CategoryDTO updateCategoryWithImage(Long id, CategoryCreateRequestDTO request, MultipartFile imageFile) {
        // Kiểm tra xem danh mục có tồn tại không
        final Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Nếu có file ảnh mới, tải lên và cập nhật URL ảnh
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageName = category.getImageUrl();
            if (!imageFile.isEmpty()) {
                fileStorageService.backupAndDeleteFile(imageName, "categories");
            }
            String imageUrl = fileStorageService.uploadFileAndGetName(imageFile, "/images/categories");
            category.setImageUrl(imageUrl);
        }

        // Cập nhật thông tin cha của danh mục (nếu có)
        if (request.getParentId() != null) {
            Category parentCategory = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            category.setParentCategory(parentCategory);
        } else {
            category.setParentCategory(null);
        }

        // Cập nhật hoặc tạo mới các bản dịch
        List<CategoriesTranslation> translations = request.getTranslations().stream()
                .map(translationDTO -> {
                    // Tìm kiếm Language theo mã ngôn ngữ
                    Language language = languageRepository.findByCode(translationDTO.getLanguageCode())
                            .orElseThrow(() -> new RuntimeException("Language not found for code: " + translationDTO.getLanguageCode()));

                    // Kiểm tra xem bản dịch có tồn tại không
                    CategoriesTranslation existingTranslation = categoryTranslationRepository.findByCategoryIdAndLanguageCode(id, language.getCode()).orElse(null);

                    if (existingTranslation != null) {
                        // Nếu bản dịch đã tồn tại, cập nhật thông tin
                        existingTranslation.setName(translationDTO.getName());
                        return existingTranslation;
                    }

                    // Nếu không tìm thấy bản dịch, tạo mới
                    return new CategoriesTranslation(
                            null,
                            translationDTO.getName(),
                            category,  // Gán danh mục cho bản dịch
                            language   // Gán ngôn ngữ cho bản dịch
                    );
                })
                .collect(Collectors.toList());

        // Lưu tất cả các bản dịch (cập nhật và tạo mới)
        categoryTranslationRepository.saveAll(translations);

        // Lưu lại thông tin danh mục đã được cập nhật
        categoryRepository.save(category);

        // Trả về DTO cho danh mục đã được cập nhật
        return CategoryDTO.builder()
                .id(category.getId())
                .imageUrl(category.getImageUrl())
                .name(request.getTranslations().get(0).getName()) // Lấy tên bản dịch đầu tiên
                .build();
    }

    @Transactional
    public CategoryDTO updateCategoryStatus(Long id, boolean isActive) {
        // Kiểm tra xem danh mục có tồn tại không
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Cập nhật trạng thái isActive
        category.setIsActive(isActive);

        // Lưu lại thông tin danh mục với trạng thái mới
        category = categoryRepository.save(category);

        // Trả về DTO của danh mục đã cập nhật
        return CategoryDTO.builder()
                .id(category.getId())
                .imageUrl(category.getImageUrl())
                .name(category.getTranslations().get(0).getName())  // Lấy tên bản dịch đầu tiên
                .isActive(category.getIsActive())  // Trả về trạng thái isActive
                .build();
    }

    public Page<CategoryAdminResponseDTO> getCategoriesPage(int page, int size, String name, Long parentId, Boolean isActive, String languageCode, String sortBy, String sortDir) {
        // Set default sorting direction to ascending if not provided
        Sort.Direction direction = (sortDir != null && sortDir.equalsIgnoreCase("desc")) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Build sorting based on the provided sort criteria
        Sort sort = Sort.by(direction, sortBy != null ? sortBy : "id"); // Default sorting by ID

        // Build pagination
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // Apply filters and fetch categories page
        Page<Category> categoriesPage = categoryRepository.findCategoriesByFilters(name, parentId, isActive, pageRequest);

        // Map categories to DTO
        Page<CategoryAdminResponseDTO> categoryDTOPage = categoriesPage.map(category -> {
            // Fetch translation based on the provided language code
            CategoriesTranslation translation = categoryTranslationRepository
                    .findByCategoryIdAndLanguageCode(category.getId(), languageCode)
                    .orElseThrow(() -> new RuntimeException("Translation not found for category ID: " + category.getId() + " and language: " + languageCode));

            // Variables for parent category
            String parentName = null;
            Long parentIdValue = null;

            if (category.getParentCategory() != null) {
                // Fetch parent translation for the category
                CategoriesTranslation parentTranslation = categoryTranslationRepository
                        .findByCategoryIdAndLanguageCode(category.getParentCategory().getId(), languageCode)
                        .orElseThrow(() -> new RuntimeException("Parent translation not found for category ID: " + category.getParentCategory().getId() + " and language: " + languageCode));
                parentName = parentTranslation.getName();  // Fetch translated name of parent category
                parentIdValue = category.getParentCategory().getId();  // Parent category ID
            }

            // Build the category DTO with the additional fields
            return CategoryAdminResponseDTO.builder()
                    .id(category.getId())
                    .imageUrl(category.getImageUrl())
                    .name(translation.getName())  // Using translated name
                    .isActive(category.getIsActive())
                    .parentId(parentIdValue)  // Parent ID as Long
                    .parentName(parentName)  // Parent category name
                    .createdAt(category.getCreatedAt())  // Add createdAt
                    .updatedAt(category.getUpdatedAt())  // Add updatedAt
                    .updatedBy(category.getUpdatedBy())
                    .createdBy(category.getCreatedBy())
                    .build();
        });

        return categoryDTOPage;
    }

    @PersistenceContext  // Inject EntityManager
    private EntityManager entityManager;

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NOT_FOUND)));

        String imageName = category.getImageUrl();
        if (imageName != null) {
            fileStorageService.backupAndDeleteFile(imageName, "categories");
        }
        categoryRepository.updateSubCategoriesParentToNull(id);

        categoryRepository.delete(category);
    }

    public List<Long> getAllChildCategoryIds(Long parentId) {
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(parentId);

        List<Long> childIds = categoryRepository.findChildCategoryIds(parentId);
        for (Long childId : childIds) {
            categoryIds.addAll(getAllChildCategoryIds(childId)); // Gọi đệ quy
        }

        return categoryIds;
    }

}
