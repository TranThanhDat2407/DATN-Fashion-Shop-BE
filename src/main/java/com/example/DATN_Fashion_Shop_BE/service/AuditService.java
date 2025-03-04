package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.dto.AuditRecord;
import com.example.DATN_Fashion_Shop_BE.dto.response.audit.CategoryAudResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.audit.CategoryTranslationAudResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.category.CategoryEditResponseDTO;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.CategoryRepository;
import com.example.DATN_Fashion_Shop_BE.repository.CategoryTranslationRepository;
import com.example.DATN_Fashion_Shop_BE.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {
    @PersistenceContext
    private EntityManager entityManager;

    private final CategoryTranslationRepository categoryTranslationRepository;

    private final CategoryRepository categoryRepository;

    private final UserRepository userRepository;

    private final FileStorageService fileStorageService;

    // Lấy đối tượng AuditReader từ EntityManager
    private AuditReader getAuditReader() {
        return AuditReaderFactory.get(entityManager);
    }

    public Page<CategoryAudResponse> searchCategoryAuditHistory(Pageable pageable,
                                                                Long id,
                                                                Long updatedBy,
                                                                Integer rev,
                                                                String revType,
                                                                LocalDateTime updatedAtFrom,
                                                                LocalDateTime updatedAtTo) {
        AuditReader auditReader = getAuditReader();
        AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(Category.class, false, true);

        if (updatedAtFrom != null) {
            query.add(AuditEntity.property("updatedAt").ge(updatedAtFrom));
        }
        // Filter theo updatedAt đến
        if (updatedAtTo != null) {
            query.add(AuditEntity.property("updatedAt").le(updatedAtTo));
        }

        if (updatedBy != null) {
            query.add(AuditEntity.property("updatedBy").eq(updatedBy));
        }

        // Áp dụng filter theo id (là thuộc tính của entity)
        if (id != null) {
            query.add(AuditEntity.property("id").eq(id));
        }
        // Filter theo revision id
        if (rev != null) {
            query.add(AuditEntity.revisionNumber().eq(rev));
        }
        // Filter theo revision type
        if (revType != null && !revType.isEmpty()) {
            query.add(AuditEntity.revisionType().eq(RevisionType.valueOf(revType)));
        }

        // Sắp xếp theo revision giảm dần (mới nhất về cũ nhất)
        query.addOrder(AuditEntity.revisionNumber().desc());

        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        query.setFirstResult(pageNumber * pageSize);
        query.setMaxResults(pageSize);

        List<?> results = query.getResultList();
        List<CategoryAudResponse> responseList = new ArrayList<>();

        // Mỗi kết quả trả về là một mảng chứa: [entity, revEntity, revType]
        for (Object result : results) {
            Object[] arr = (Object[]) result;
            Category category = (Category) arr[0];
            DefaultRevisionEntity revEntity = (DefaultRevisionEntity) arr[1];
            RevisionType revisionType = (RevisionType) arr[2]; // Đổi tên biến này

            responseList.add(CategoryAudResponse.from(category, revEntity, revisionType));
        }

        // Tính tổng số bản ghi sử dụng addProjection
        Number countResult = (Number) auditReader.createQuery()
                .forRevisionsOfEntity(Category.class, false, true)
                .addProjection(AuditEntity.revisionNumber().count())
                .getSingleResult();
        long total = countResult.longValue();

        return new PageImpl<>(responseList, pageable, total);
    }

    public Page<CategoryTranslationAudResponse> searchCategoryTranslationAuditHistory(
            Pageable pageable,
            Long id,
            Long updatedBy,
            Integer rev,
            String revType,
            LocalDateTime updatedAtFrom,
            LocalDateTime updatedAtTo) {

        AuditReader auditReader = getAuditReader();
        AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(CategoriesTranslation.class, false, true);

        // Thêm điều kiện lọc
        if (id != null) {
            query.add(AuditEntity.property("id").eq(id));
        }
        if (rev != null) {
            query.add(AuditEntity.revisionNumber().eq(rev));
        }
        if (revType != null) {
            query.add(AuditEntity.revisionType().eq(RevisionType.valueOf(revType)));
        }

        if (updatedBy != null) {
            query.add(AuditEntity.property("updatedBy").ge(updatedAtFrom));
        }

        if (updatedAtFrom != null) {
            query.add(AuditEntity.property("updatedAt").ge(updatedAtFrom));
        }

        if (updatedAtTo != null) {
            query.add(AuditEntity.property("updatedAt").le(updatedAtTo));
        }

        // Sắp xếp theo revision giảm dần
        query.addOrder(AuditEntity.revisionNumber().desc());

        // Phân trang
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        // Lấy kết quả
        List<?> results = query.getResultList();
        List<CategoryTranslationAudResponse> responseList = new ArrayList<>();

        for (Object result : results) {
            Object[] arr = (Object[]) result;
            CategoriesTranslation categoriesTranslation = (CategoriesTranslation) arr[0];
            DefaultRevisionEntity revEntity = (DefaultRevisionEntity) arr[1];
            RevisionType revisionType = (RevisionType) arr[2];

            responseList.add(CategoryTranslationAudResponse.from(categoriesTranslation, revEntity, revisionType));
        }

        // Đếm tổng số bản ghi
        Number countResult = (Number) auditReader.createQuery()
                .forRevisionsOfEntity(CategoriesTranslation.class, false, true)
                .addProjection(AuditEntity.revisionNumber().count())
                .getSingleResult();
        long total = countResult.longValue();

        return new PageImpl<>(responseList, pageable, total);
    }

    @Transactional
    public void undoLastCategoryTranslationRevision(Long categoryTranslationId) {
        AuditReader auditReader = getAuditReader();

        // Lấy 2 bản ghi gần nhất của CategoriesTranslation có id tương ứng
        List<?> auditResults = auditReader.createQuery()
                .forRevisionsOfEntity(CategoriesTranslation.class, false, true)
                .add(AuditEntity.property("id").eq(categoryTranslationId))
                .addOrder(AuditEntity.revisionNumber().desc()) // Sắp xếp theo revision mới nhất
                .setMaxResults(2) // Lấy bản ghi hiện tại và bản ghi trước đó
                .getResultList();

        if (auditResults.size() < 2) {
            throw new IllegalStateException("Không thể hoàn tác vì không có bản ghi trước đó.");
        }

        // Lấy bản ghi trước đó (bản ghi có index 1 trong danh sách kết quả)
        Object[] previousAuditData = (Object[]) auditResults.get(1);
        CategoriesTranslation previousTranslation = (CategoriesTranslation) previousAuditData[0];

        // Lấy bản ghi hiện tại từ database
        CategoriesTranslation currentTranslation = categoryTranslationRepository.findById(categoryTranslationId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy CategoriesTranslation với id: " + categoryTranslationId));

        // Cập nhật lại bản ghi hiện tại bằng dữ liệu từ bản ghi trước đó
        currentTranslation.setName(previousTranslation.getName());
        currentTranslation.setLanguage(previousTranslation.getLanguage());

        // Lưu lại thay đổi
        categoryTranslationRepository.save(currentTranslation);
    }

    @Transactional
    public void undoCategoryTranslationToRevision(Long categoryTranslationId, Integer revId) {
        AuditReader auditReader = getAuditReader();

        // Truy vấn bản ghi audit theo id và revId
        List<?> auditResults = auditReader.createQuery()
                .forRevisionsOfEntity(CategoriesTranslation.class, false, true)
                .add(AuditEntity.property("id").eq(categoryTranslationId))
                .add(AuditEntity.revisionNumber().eq(revId))
                .getResultList();

        if (auditResults.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy bản ghi audit với revId: " + revId);
        }

        // Lấy dữ liệu từ bản ghi audit
        Object[] auditData = (Object[]) auditResults.get(0);
        CategoriesTranslation previousTranslation = (CategoriesTranslation) auditData[0];
        RevisionType revType = (RevisionType) auditData[2];

        switch (revType) {
            case ADD:
                // Nếu bản ghi được tạo trước đó → Undo = XÓA
                categoryTranslationRepository.deleteById(categoryTranslationId);
                break;

            case MOD:
                // Nếu bản ghi bị sửa → Undo = KHÔI PHỤC DỮ LIỆU CŨ
                CategoriesTranslation currentTranslation = categoryTranslationRepository.findById(categoryTranslationId)
                        .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy CategoriesTranslation với id: " + categoryTranslationId));

                currentTranslation.setName(previousTranslation.getName());
                currentTranslation.setLanguage(previousTranslation.getLanguage());


                categoryTranslationRepository.save(currentTranslation);
                break;

            case DEL:
                // Nếu bản ghi bị xóa → Undo = TẠO LẠI
                CategoriesTranslation restoredTranslation = CategoriesTranslation.builder()
                        .id(previousTranslation.getId()) // Lấy lại ID cũ
                        .name(previousTranslation.getName())
                        .language(previousTranslation.getLanguage())
                        .build();

                categoryTranslationRepository.save(restoredTranslation);
                break;
        }
    }

    @Transactional
    public CategoryEditResponseDTO undoCategoryToRevision(Long categoryId, Integer revId) {
        AuditReader auditReader = getAuditReader();

        // Truy vấn bản ghi audit cho Category theo revId và categoryId
        List<?> auditResults = auditReader.createQuery()
                .forRevisionsOfEntity(Category.class, false, true)
                .add(AuditEntity.property("id").eq(categoryId))
                .add(AuditEntity.revisionNumber().eq(revId))
                .getResultList();

        if (auditResults.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy bản ghi audit với revId: " + revId);
        }

        // Lấy dữ liệu từ bản ghi audit
        Object[] auditData = (Object[]) auditResults.get(0);
        Category previousCategory = (Category) auditData[0];
        RevisionType revType = (RevisionType) auditData[2];

        Category restoredCategory = null;
        List<CategoriesTranslation> restoredTranslations = new ArrayList<>();

        switch (revType) {
            case ADD:
                categoryRepository.deleteById(categoryId);
                return null;

            case MOD:
                Category currentCategory = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Category với id: " + categoryId));

                // Detach the parent category to avoid session conflicts
                if (currentCategory.getParentCategory() != null) {
                    // Detach parent category to avoid proxy session issues
                    currentCategory.setParentCategory(null);
                }

                currentCategory.setImageUrl(previousCategory.getImageUrl());
                currentCategory.setIsActive(previousCategory.getIsActive());

                // Load parentCategory separately to avoid session issues
                if (previousCategory.getParentCategory() != null) {
                    // Load parentCategory from DB again to avoid session conflicts
                    currentCategory.setParentCategory(categoryRepository.findById(previousCategory.getParentCategory().getId())
                            .orElse(null));
                }

                categoryRepository.save(currentCategory);
                restoredCategory = currentCategory;

                break;


            case DEL:
                restoredCategory = Category.builder()
                        .imageUrl(previousCategory.getImageUrl())
                        .isActive(true)
                        .parentCategory(previousCategory.getParentCategory())
                        .build();

                fileStorageService.backupAndDeleteFile(previousCategory.getImageUrl(), "categories");

                restoredCategory = categoryRepository.save(restoredCategory); // Lưu trước để có ID

                // Tìm các bản dịch trong bảng audit
                List<?> categoryTranslationAuditResults = auditReader.createQuery()
                        .forRevisionsOfEntity(CategoriesTranslation.class, false, true)
                        .add(AuditEntity.property("category_id").eq(categoryId))
                        .add(AuditEntity.revisionNumber().eq(revId))
                        .getResultList();

                for (Object auditRecord : categoryTranslationAuditResults) {
                    Object[] translationData = (Object[]) auditRecord;
                    CategoriesTranslation previousTranslation = (CategoriesTranslation) translationData[0];

                    // Khôi phục lại CategoryTranslation
                    CategoriesTranslation restoredTranslation = new CategoriesTranslation();
                    restoredTranslation.setCategory(restoredCategory);
                    restoredTranslation.setName(previousTranslation.getName());
                    restoredTranslation.setLanguage(previousTranslation.getLanguage());

                    restoredTranslations.add(restoredTranslation);
                }

                categoryTranslationRepository.saveAll(restoredTranslations);

                // Gán lại translations cho Category trước khi trả về
                restoredCategory.setTranslations(restoredTranslations);
                break;
        }

        return CategoryEditResponseDTO.fromCategory(restoredCategory);
    }

    /**
     * Khôi phục category đã cập nhật, bao gồm ảnh nếu cần.
     */
    private void restoreUpdatedCategory(Category category) {
        Category existingCategory = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy category hiện tại."));

        // Khôi phục ảnh nếu khác với bản cập nhật
        if (category.getImageUrl() != null && !category.getImageUrl().equals(existingCategory.getImageUrl())) {
            fileStorageService.restoreFile(category.getImageUrl(), "categories");
        }

        // Cập nhật lại dữ liệu
        existingCategory.setImageUrl(category.getImageUrl());
        existingCategory.setIsActive(category.getIsActive());
        categoryRepository.save(existingCategory);
    }

    private void restoreDeletedCategory(Long categoryId) {
        AuditReader auditReader = getAuditReader();

        // 🔥 Tìm bản ghi `MOD` gần nhất trước khi bị xóa
        AuditQuery query = auditReader.createQuery()
                .forRevisionsOfEntity(Category.class, false, true)
                .add(AuditEntity.id().eq(categoryId))
                .add(AuditEntity.revisionType().ne(RevisionType.DEL)) // Loại bỏ bản ghi DELETE
                .addOrder(AuditEntity.revisionNumber().desc()) // Lấy bản ghi gần nhất
                .setMaxResults(1); // Chỉ lấy 1 bản ghi gần nhất

        List<Object[]> results = query.getResultList();
        if (results.isEmpty()) {
            throw new RuntimeException("Không tìm thấy dữ liệu trước khi xóa để khôi phục.");
        }

        Object[] auditData = results.get(0);
        Category categoryAtRev = (Category) auditData[0];

        // 🔥 Khôi phục ảnh từ backup nếu có
        if (categoryAtRev.getImageUrl() != null) {
            fileStorageService.restoreFile(categoryAtRev.getImageUrl(), "categories");
        }

        // 🔥 Tạo lại Category từ bản `MOD`
        Category restoredCategory = new Category();
        restoredCategory.setId(categoryAtRev.getId());
        restoredCategory.setImageUrl(categoryAtRev.getImageUrl());
        restoredCategory.setIsActive(true);

        categoryRepository.save(restoredCategory);
    }

}
