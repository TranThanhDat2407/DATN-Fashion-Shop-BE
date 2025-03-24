package com.example.DATN_Fashion_Shop_BE.service;


import com.example.DATN_Fashion_Shop_BE.dto.response.audit.CategoryAudResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.inventory.InventoryAudResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.inventory.WarehouseStockResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.model.Category;
import com.example.DATN_Fashion_Shop_BE.model.Inventory;
import com.example.DATN_Fashion_Shop_BE.model.ProductVariant;
import com.example.DATN_Fashion_Shop_BE.model.Store;
import com.example.DATN_Fashion_Shop_BE.repository.CategoryRepository;
import com.example.DATN_Fashion_Shop_BE.repository.InventoryRepository;
import com.example.DATN_Fashion_Shop_BE.repository.ProductVariantRepository;
import com.example.DATN_Fashion_Shop_BE.repository.StoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);
    private final StoreRepository storeRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private AuditReader getAuditReader() {
        return AuditReaderFactory.get(entityManager);
    }


    @Transactional
    public void reduceInventory(Long variantId, Integer quantity, Long storeId) throws DataNotFoundException {

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new DataNotFoundException("Product variant not found"));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new DataNotFoundException("store not found"));

        Inventory inventory = inventoryRepository.findByStoreIdAndProductVariantId( storeId, variantId)
                .orElseThrow(() -> new DataNotFoundException("Inventory record not found for this store"));

        if (inventory.getQuantityInStock() < quantity) {
            throw new IllegalStateException("Not enough stock available for this product variant!");
        }

        inventory.setQuantityInStock(inventory.getQuantityInStock() - quantity);
        inventoryRepository.save(inventory);

        logger.info("✅ Successfully deducted {} items of variant {} from store {}", quantity, variantId, storeId);
    }

    public Page<InventoryAudResponse> getInventoryHistoryByStore(
            Pageable pageable,
            Long id,
            Long updatedBy,
            Integer rev,
            String revType,
            LocalDateTime updatedAtFrom,
            LocalDateTime updatedAtTo,
            Long storeId,
            String languageCode) {

        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(Inventory.class, false, true);

        // 🔹 Kiểm tra storeId nếu có
        if (storeId != null) {
            query.add(AuditEntity.property("store_id").eq(storeId));
        }

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
            query.add(AuditEntity.id().eq(id));
        }
        if (rev != null) {
            query.add(AuditEntity.revisionNumber().eq(rev));
        }
        if (revType != null && !revType.isEmpty()) {
            query.add(AuditEntity.revisionType().eq(RevisionType.valueOf(revType)));
        }

        // 🔹 Sắp xếp theo `revisionNumber` mới nhất
        query.addOrder(AuditEntity.revisionNumber().desc());

        // 🔹 Phân trang
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        // 🔹 Lấy kết quả
        List<?> results = query.getResultList();
        List<InventoryAudResponse> responseList = new ArrayList<>();

        for (Object result : results) {
            Object[] arr = (Object[]) result;
            Inventory inventory = (Inventory) arr[0];
            DefaultRevisionEntity revEntity = (DefaultRevisionEntity) arr[1];
            RevisionType revisionType = (RevisionType) arr[2];

            Integer deltaQuantity = inventory.getDeltaQuantity();

            responseList.add(InventoryAudResponse.fromInventory(inventory, revEntity, revisionType, deltaQuantity, languageCode));
        }

        // 🔹 Tính tổng số bản ghi khớp với điều kiện
        Number countResult = (Number) auditReader.createQuery()
                .forRevisionsOfEntity(Inventory.class, false, true)
                .addProjection(AuditEntity.id().count())
                .getSingleResult();
        long total = countResult.longValue();

        return new PageImpl<>(responseList, pageable, total);
    }


    public Page<WarehouseStockResponse> getInventoryByWarehouseId(Long warehouseId, String languageCode,
                                                                  String productName, Long categoryId, int page, int size, String sortBy, String sortDir) {

        Sort sort = Sort.by(sortBy);
        sort = sortDir.equalsIgnoreCase("desc") ? sort.descending() : sort.ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Inventory> inventoryPage;
        List<Long> categoryIds = (categoryId != null) ? categoryRepository.findChildCategoryIds(categoryId) : new ArrayList<>();

        if (productName != null && categoryId != null) {
            inventoryPage = inventoryRepository.findByWarehouseIdAndProductVariant_Product_Translations_LanguageCodeAndProductVariant_Product_Translations_NameContainingIgnoreCaseAndProductVariant_Product_Categories_IdIn(
                    warehouseId, languageCode, productName, categoryIds, pageable);
        } else if (categoryId != null) {
            inventoryPage = inventoryRepository.findByWarehouseIdAndProductVariant_Product_Translations_LanguageCodeAndProductVariant_Product_Categories_IdIn(
                    warehouseId, languageCode, categoryIds, pageable);
        } else if (productName != null) {
            inventoryPage = inventoryRepository.findByWarehouseIdAndProductVariant_Product_Translations_LanguageCodeAndProductVariant_Product_Translations_NameContainingIgnoreCase(
                    warehouseId, languageCode, productName, pageable);
        } else {
            inventoryPage = inventoryRepository.findByWarehouseIdAndProductVariant_Product_Translations_LanguageCode(
                    warehouseId, languageCode, pageable);
        }

        List<WarehouseStockResponse> stockResponses = inventoryPage.getContent()
                .stream()
                .map(inventory -> WarehouseStockResponse.fromInventory(inventory, languageCode))
                .collect(Collectors.toList());

        return new PageImpl<>(stockResponses, pageable, inventoryPage.getTotalElements());
    }

}
