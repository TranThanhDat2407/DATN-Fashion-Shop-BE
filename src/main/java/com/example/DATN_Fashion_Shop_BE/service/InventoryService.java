package com.example.DATN_Fashion_Shop_BE.service;


import com.example.DATN_Fashion_Shop_BE.dto.request.inventory.WarehouseInventoryRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.inventory.InventoryAudResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.inventory.InventoryStatusResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.inventory.WarehouseInventoryResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.inventory.WarehouseStockResponse;
import com.example.DATN_Fashion_Shop_BE.exception.DataNotFoundException;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final OrderDetailRepository orderDetailRepository;
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

    public WarehouseInventoryResponse addWarehouseInventory(WarehouseInventoryRequest request) {
        // Validate warehouse exists
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Warehouse not found with id: " + request.getWarehouseId()));

        // Validate product variant exists
        ProductVariant productVariant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product variant not found with id: " + request.getProductVariantId()));

        // Check if inventory already exists
        Optional<Inventory> existingInventory = inventoryRepository
                .findByWarehouseIdAndProductVariantId(request.getWarehouseId(), request.getProductVariantId());

        Inventory inventory;
        if (existingInventory.isPresent()) {
            // Update existing inventory
            inventory = existingInventory.get();
            inventory.setQuantityInStock(inventory.getQuantityInStock() + request.getQuantityInStock());
        } else {
            // Create new inventory
            inventory = new Inventory();
            inventory.setWarehouse(warehouse);
            inventory.setProductVariant(productVariant);
            inventory.setQuantityInStock(request.getQuantityInStock());
            inventory.setStore(null); // Explicitly set store to null for warehouse inventory
        }

        Inventory savedInventory = inventoryRepository.save(inventory);
        return WarehouseInventoryResponse.fromInventory(savedInventory);
    }

    public void restoreInventoryFromCancelledOrder(Long orderId) throws DataNotFoundException {
        // Lấy tất cả các order items của đơn hàng
        List<OrderDetail> orderItems = orderDetailRepository.findByOrderId(orderId);

        for (OrderDetail item : orderItems) {
            // Tìm inventory tương ứng với product variant và store
            Inventory inventory = inventoryRepository.findByWarehouseIdAndProductVariantId(
                    1L,
                    item.getProductVariant().getId()
            ).orElseThrow(() -> new DataNotFoundException(
                    "Inventory not found for product variant: " + item.getProductVariant().getId() +
                            " and store: " + item.getOrder().getStore().getId()));

            // Hoàn trả số lượng
            inventory.setQuantityInStock(inventory.getQuantityInStock() + item.getQuantity());
            inventoryRepository.save(inventory);
        }
    }

    public WarehouseInventoryResponse updateWarehouseInventory(Long inventoryId,
                                                               Integer newQuantity) throws DataNotFoundException {
        // Validate inventory exists and belongs to warehouse (not store)
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found with id: " + inventoryId));

        if (inventory.getStore() != null) {
            throw new DataNotFoundException("Inventory belongs to store, not warehouse");
        }

        // Update quantity
        inventory.setQuantityInStock(newQuantity);
        Inventory updatedInventory = inventoryRepository.save(inventory);

        return WarehouseInventoryResponse.fromInventory(updatedInventory);
    }

    public Page<InventoryStatusResponse> getUnsoldProductsByStore(Long storeId, String langCode, Pageable pageable) {
        // Lấy toàn bộ danh sách
        List<InventoryStatusResponse> allItems = inventoryRepository.findUnsoldProductsByStore(storeId, langCode);

        // Tính toán phân trang thủ công
        int total = allItems.size();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), total);

        // Lấy sublist theo trang
        List<InventoryStatusResponse> pageItems = allItems.subList(start, end);

        // Tạo đối tượng Page
        return new PageImpl<>(pageItems, pageable, total);
    }

}
