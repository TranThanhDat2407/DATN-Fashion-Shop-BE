package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateColorRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateSizeRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.inventory_transfer.InventoryTransferRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.inventory_transfer.InventoryTransferResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryTransferService {

    private final LocalizationUtils localizationUtils;
    private final InventoryTransferRepository inventoryTransferRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final StoreRepository storeRepository;
    private final InventoryTransferItemRepository inventoryTransferItemRepository;
    private final ProductVariantRepository productVariantRepository;


    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public InventoryTransfer createTransfer(InventoryTransferRequest request) {
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        // Kiểm tra và trừ inventory
        request.getTransferItems().forEach(item -> {
            ProductVariant productVariant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Product Variant not found"));

            // Lấy inventory của product variant trong kho (warehouse)
            Inventory warehouseInventory = inventoryRepository
                    .findByWarehouseIdAndProductVariantId(warehouse.getId(), productVariant.getId())
                    .orElseThrow(() -> new IllegalStateException("Inventory not found for product variant " + productVariant.getId() + " in warehouse"));

            if (warehouseInventory.getQuantityInStock() < item.getQuantity()) {
                throw new IllegalStateException("Not enough stock available in warehouse for product variant " + productVariant.getId());
            }

            // Trừ inventory từ warehouse
            warehouseInventory.setQuantityInStock(warehouseInventory.getQuantityInStock() - item.getQuantity());
            inventoryRepository.save(warehouseInventory);
        });

        InventoryTransfer transfer = InventoryTransfer.builder()
                .warehouse(warehouse)
                .store(store)
                .status(TransferStatus.PENDING)
                .isReturn(false)
                .transferItems(new ArrayList<>())
                .message(request.getMessage())
                .build();

        InventoryTransfer savedTransfer = inventoryTransferRepository.save(transfer);

        request.getTransferItems().forEach(item -> {
            ProductVariant productVariant = productVariantRepository.findById(item.getProductVariantId())
                    .orElseThrow(() -> new IllegalArgumentException("Product Variant not found"));
            InventoryTransferItem transferItem = new InventoryTransferItem();
            transferItem.setInventoryTransfer(savedTransfer);
            transferItem.setProductVariant(productVariant);
            transferItem.setQuantity(item.getQuantity());
            inventoryTransferItemRepository.save(transferItem);
        });

        return savedTransfer;
    }

    @Transactional
    public InventoryTransfer confirmTransfer(Long transferId) {
        InventoryTransfer transfer = inventoryTransferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer request not found"));

        if (transfer.getStatus() == TransferStatus.CONFIRMED) {
            throw new IllegalStateException("Transfer already confirmed");
        }

        for (InventoryTransferItem item : transfer.getTransferItems()) {
            ProductVariant productVariant = item.getProductVariant();

            // Bỏ phần kiểm tra và trừ inventory ở warehouse
            // Chỉ thực hiện cộng inventory vào store
            Inventory storeInventory = inventoryRepository
                    .findByStoreIdAndProductVariantId(
                            transfer.getStore().getId(),
                            productVariant.getId())
                    .orElse(new Inventory());

            storeInventory.setProductVariant(productVariant);
            storeInventory.setStore(transfer.getStore());
            int storeStock = storeInventory.getQuantityInStock() != null ?
                    storeInventory.getQuantityInStock() : 0;
            storeInventory.setQuantityInStock(storeStock + item.getQuantity());

            inventoryRepository.save(storeInventory);
        }

        transfer.setStatus(TransferStatus.CONFIRMED);
        return inventoryTransferRepository.save(transfer);
    }

    @Transactional
    public InventoryTransfer cancelTransfer(Long transferId) {
        InventoryTransfer transfer = inventoryTransferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer request not found"));

        if (transfer.getStatus() == TransferStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel a confirmed transfer");
        }

        // Chỉ hoàn lại inventory nếu trạng thái hiện tại là PENDING
        if (transfer.getStatus() == TransferStatus.PENDING ) {
            // Lấy danh sách các items trong phiếu chuyển kho
            List<InventoryTransferItem> transferItems = inventoryTransferItemRepository
                    .findByInventoryTransferId(transferId);

            for (InventoryTransferItem item : transferItems) {
                // Tìm inventory record tương ứng trong warehouse
                Inventory warehouseInventory = inventoryRepository
                        .findByWarehouseIdAndProductVariantId(
                                transfer.getWarehouse().getId(),
                                item.getProductVariant().getId())
                        .orElseThrow(() -> new IllegalStateException(
                                "Inventory not found for product variant " + item.getProductVariant().getId() +
                                        " in warehouse " + transfer.getWarehouse().getId()));

                // Cộng lại số lượng đã trừ
                warehouseInventory.setQuantityInStock(warehouseInventory.getQuantityInStock() + item.getQuantity());
                inventoryRepository.save(warehouseInventory);
            }
        }

        transfer.setStatus(TransferStatus.CANCELED);
        return inventoryTransferRepository.save(transfer);
    }

    public Page<InventoryTransferResponse> getAllTransfersByStore(
            Long storeId, TransferStatus status, Boolean isReturn,
            Pageable pageable, String langCode) {

        LocalDateTime warningThreshold = LocalDateTime.now().minusDays(10);

        Page<InventoryTransfer> transfersPage = inventoryTransferRepository.findWithWarningPriority(
                storeId, status, isReturn, warningThreshold, pageable);

        return transfersPage.map(transfer -> {
            InventoryTransferResponse response = InventoryTransferResponse.fromInventoryTransfer(transfer, langCode);

            return response;
        });
    }
    // Kiểm tra xem transfer có cần cảnh báo không
    private boolean isWarningTransfer(InventoryTransferResponse transfer) {
        if (!transfer.getStatus().equals(TransferStatus.PENDING)) return false;

        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
        return transfer.getCreatedAt().isBefore(tenDaysAgo);
    }

    public InventoryTransferResponse getInventoryTransferById(Long id, String langCode) {
        InventoryTransfer transfer = inventoryTransferRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory Transfer not found with id: " + id));

        return InventoryTransferResponse.fromInventoryTransfer(transfer, langCode);
    }

    public List<Object[]> getInventoryTransferHistoryByStore(Long storeId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        return auditReader.createQuery()
                .forRevisionsOfEntity(InventoryTransfer.class, false, true)
                .add(AuditEntity.property("store.id").eq(storeId))
                .getResultList();
    }
}
