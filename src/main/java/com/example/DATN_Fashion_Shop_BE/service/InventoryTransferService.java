package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateColorRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateSizeRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values.*;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryTransferService {

    private final LocalizationUtils localizationUtils;
    private final InventoryTransferRepository inventoryTransferRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final StoreRepository storeRepository;
    private final ProductVariantRepository productVariantRepository;


    @PersistenceContext
    private EntityManager entityManager;

    // Tạo yêu cầu chuyển kho
    @Transactional
    public InventoryTransfer createTransfer(Long warehouseId, Long storeId, Long productVariantId, Integer quantity) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new IllegalArgumentException("Product Variant not found"));

        // Kiểm tra hàng tồn kho tại warehouse
        int warehouseStock = inventoryRepository.findByProductVariantIdAndWarehouseNotNull(productVariantId)
                .stream().mapToInt(Inventory::getQuantityInStock).sum();

        if (warehouseStock < quantity) {
            throw new IllegalStateException("Not enough stock available in warehouse");
        }

        // Tạo yêu cầu chuyển kho
        InventoryTransfer transfer = InventoryTransfer.builder()
                .warehouse(warehouse)
                .store(store)
                .productVariant(productVariant)
                .quantity(quantity)
                .status(TransferStatus.PENDING)
                .build();

        return inventoryTransferRepository.save(transfer);
    }

    // Xác nhận hàng đã đến Store
    @Transactional
    public InventoryTransfer confirmTransfer(Long transferId) {
        InventoryTransfer transfer = inventoryTransferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer request not found"));

        if (transfer.getStatus() == TransferStatus.CONFIRMED) {
            throw new IllegalStateException("Transfer already confirmed");
        }

        // Cập nhật hàng tồn kho ở Warehouse
        Inventory warehouseInventory = inventoryRepository.findByProductVariantIdAndWarehouseNotNull(transfer.getProductVariant().getId())
                .stream().findFirst().orElseThrow(() -> new IllegalStateException("No inventory found in warehouse"));

        // Kiểm tra quantityInStock nếu nó là null, gán giá trị mặc định là 0
        Integer warehouseQuantityInStock = warehouseInventory.getQuantityInStock() != null ? warehouseInventory.getQuantityInStock() : 0;

        if (warehouseQuantityInStock < transfer.getQuantity()) {
            throw new IllegalStateException("Not enough stock in warehouse to confirm transfer");
        }
        warehouseInventory.setQuantityInStock(warehouseQuantityInStock - transfer.getQuantity());
        inventoryRepository.save(warehouseInventory);

        // Cập nhật hàng tồn kho ở Store
        Inventory storeInventory = inventoryRepository.findByProductVariantIdAndStoreNotNull(transfer.getProductVariant().getId())
                .stream().findFirst().orElse(new Inventory());

        // Kiểm tra quantityInStock nếu nó là null, gán giá trị mặc định là 0
        Integer storeQuantityInStock = storeInventory.getQuantityInStock() != null
                ? storeInventory.getQuantityInStock() : 0;

        storeInventory.setProductVariant(transfer.getProductVariant());
        storeInventory.setStore(transfer.getStore());
        storeInventory.setQuantityInStock(storeQuantityInStock + transfer.getQuantity());

        inventoryRepository.save(storeInventory);

        // Đánh dấu transfer là CONFIRMED
        transfer.setStatus(TransferStatus.CONFIRMED);
        return inventoryTransferRepository.save(transfer);
    }
    // Hủy yêu cầu chuyển kho
    @Transactional
    public InventoryTransfer cancelTransfer(Long transferId) {
        InventoryTransfer transfer = inventoryTransferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Transfer request not found"));

        if (transfer.getStatus() == TransferStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot cancel a confirmed transfer");
        }

        transfer.setStatus(TransferStatus.CANCELED);
        return inventoryTransferRepository.save(transfer);
    }

    @Transactional
    public InventoryTransfer returnToWarehouse(Long storeId, Long warehouseId, Long productVariantId, Integer quantity) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
        ProductVariant productVariant = productVariantRepository.findById(productVariantId)
                .orElseThrow(() -> new IllegalArgumentException("Product Variant not found"));

        // Kiểm tra số lượng hàng tại cửa hàng
        Inventory storeInventory = inventoryRepository.findByProductVariantIdAndStoreNotNull(productVariantId)
                .stream().findFirst().orElseThrow(() -> new IllegalStateException("No inventory found in store"));

        if (storeInventory.getQuantityInStock() < quantity) {
            throw new IllegalStateException("Not enough stock available in store");
        }

        // Tạo yêu cầu trả hàng về kho
        InventoryTransfer transfer = InventoryTransfer.builder()
                .warehouse(warehouse)
                .store(store)
                .productVariant(productVariant)
                .quantity(quantity)
                .status(TransferStatus.PENDING)
                .isReturn(true) // Đánh dấu đây là yêu cầu trả hàng
                .build();

        return inventoryTransferRepository.save(transfer);
    }

    @Transactional
    public InventoryTransfer confirmReturnTransfer(Long transferId) {
        InventoryTransfer transfer = inventoryTransferRepository.findById(transferId)
                .orElseThrow(() -> new IllegalArgumentException("Return transfer request not found"));

        if (transfer.getStatus() == TransferStatus.CONFIRMED) {
            throw new IllegalStateException("Return transfer already confirmed");
        }

        // Cập nhật kho cửa hàng (giảm số lượng)
        Inventory storeInventory = inventoryRepository.findByProductVariantIdAndStoreNotNull(transfer.getProductVariant().getId())
                .stream().findFirst().orElseThrow(() -> new IllegalStateException("No inventory found in store"));

        // Kiểm tra quantityInStock nếu nó là null, gán giá trị mặc định là 0
        int storeQuantityInStock = storeInventory.getQuantityInStock() != null ? storeInventory.getQuantityInStock() : 0;

        if (storeQuantityInStock < transfer.getQuantity()) {
            throw new IllegalStateException("Not enough stock in store to confirm return transfer");
        }

        storeInventory.setQuantityInStock(storeQuantityInStock - transfer.getQuantity());
        inventoryRepository.save(storeInventory);

        // Cập nhật kho tổng (tăng số lượng)
        Inventory warehouseInventory = inventoryRepository.findByProductVariantIdAndWarehouseNotNull(transfer.getProductVariant().getId())
                .stream().findFirst().orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setProductVariant(transfer.getProductVariant());
                    newInventory.setWarehouse(transfer.getWarehouse());
                    newInventory.setQuantityInStock(0); // Giá trị mặc định khi tạo mới
                    return newInventory;
                });

        int warehouseQuantityInStock = warehouseInventory.getQuantityInStock() != null ? warehouseInventory.getQuantityInStock() : 0;
        warehouseInventory.setQuantityInStock(warehouseQuantityInStock + transfer.getQuantity());

        inventoryRepository.save(warehouseInventory);

        // Đánh dấu transfer là CONFIRMED
        transfer.setStatus(TransferStatus.CONFIRMED);
        return inventoryTransferRepository.save(transfer);
    }


    public List<InventoryTransfer> getAllTransfersByStore(Long storeId) {
        return inventoryTransferRepository.findByStoreId(storeId);
    }

    public List<Object[]> getInventoryTransferHistoryByStore(Long storeId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        return auditReader.createQuery()
                .forRevisionsOfEntity(InventoryTransfer.class, false, true)
                .add(AuditEntity.property("store.id").eq(storeId))
                .getResultList();
    }
}
