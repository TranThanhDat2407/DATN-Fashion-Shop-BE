package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.audit.entity.CustomInventoryRevesionEntity;
import com.example.DATN_Fashion_Shop_BE.dto.response.audit.CategoryAudResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.inventory.InventoryAudResponse;
import com.example.DATN_Fashion_Shop_BE.model.Category;
import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import com.example.DATN_Fashion_Shop_BE.repository.InventoryRepository;
import com.example.DATN_Fashion_Shop_BE.repository.ProductVariantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryService categoryService; // ✅ Gọi Service thay vì Repo



    @PersistenceContext
    private EntityManager entityManager;

    private AuditReader getAuditReader() {
        return AuditReaderFactory.get(entityManager);
    }

    public Page<InventoryAudResponse> getInventoryHistoryByStore(
            Pageable pageable,
            Long id,
            Long updatedBy,
            Integer rev,
            String revType,
            LocalDateTime updatedAtFrom,
            LocalDateTime updatedAtTo,
            String productName,
            Long storeId,
            Long categoryId,
            String languageCode) {

        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(Inventory.class, false, true);

        query.add(AuditEntity.property("store_id").eq(storeId));

        if (updatedAtFrom != null) {
            query.add(AuditEntity.property("updatedAt").ge(updatedAtFrom));
        }
        if (updatedAtTo != null) {
            query.add(AuditEntity.property("updatedAt").le(updatedAtTo));
        }
        if (updatedBy != null) {
            query.add(AuditEntity.property("updatedBy").eq(updatedBy));
        }
        if (id != null) {
            query.add(AuditEntity.property("id").eq(id));
        }
        if (rev != null) {
            query.add(AuditEntity.revisionNumber().eq(rev));
        }
        if (revType != null && !revType.isEmpty()) {
            query.add(AuditEntity.revisionType().eq(RevisionType.valueOf(revType)));
        }

        List<Long> productVariantIds = new ArrayList<>();

        // 🔹 Lọc theo `categoryId`
        if (categoryId != null) {
            List<Long> categoryIds = categoryService.getAllChildCategoryIds(categoryId);
            productVariantIds.addAll(productVariantRepository.findProductVariantIdsByCategoryIds(categoryIds));
        }

        // 🔹 Lọc theo `productName`
        if (productName != null && !productName.trim().isEmpty()) {
            List<Long> variantIdsByName = productVariantRepository.findProductVariantIdsByProductName(productName, languageCode);
            productVariantIds.retainAll(variantIdsByName);
        }

        if (!productVariantIds.isEmpty()) {
            query.add(AuditEntity.relatedId("productVariantId").in(productVariantIds.toArray()));
        }

        query.addOrder(AuditEntity.revisionNumber().desc());

        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        List<?> results = query.getResultList();
        List<InventoryAudResponse> responseList = new ArrayList<>();

        for (Object result : results) {
            Object[] arr = (Object[]) result;
            Inventory inventory = (Inventory) arr[0];
            CustomInventoryRevesionEntity revEntity = (CustomInventoryRevesionEntity) arr[1]; // 🔹 Cập nhật kiểu dữ liệu
            RevisionType revisionType = (RevisionType) arr[2];

            // 🔹 Lấy giá trị delta_quantity trực tiếp từ database
            Integer deltaQuantity = revEntity.getDeltaQuantity();

            responseList.add(InventoryAudResponse.fromInventory(inventory, revEntity, revisionType, deltaQuantity, languageCode));
        }

        Number countResult = (Number) auditReader.createQuery()
                .forRevisionsOfEntity(Inventory.class, false, true)
                .addProjection(AuditEntity.revisionNumber().count())
                .getSingleResult();
        long total = countResult.longValue();

        return new PageImpl<>(responseList, pageable, total);
    }



}
