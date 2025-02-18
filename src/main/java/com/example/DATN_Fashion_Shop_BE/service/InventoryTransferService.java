package com.example.DATN_Fashion_Shop_BE.service;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateColorRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateSizeRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values.*;
import com.example.DATN_Fashion_Shop_BE.model.*;
import com.example.DATN_Fashion_Shop_BE.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class InventoryTransferService {

    private final LocalizationUtils localizationUtils;
    private final InventoryTransferRepository inventoryTransferRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final StoreRepository storeRepository;
    private final ProductVariantRepository productVariantRepository;

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

        if (warehouseInventory.getQuantityInStock() < transfer.getQuantity()) {
            throw new IllegalStateException("Not enough stock in warehouse to confirm transfer");
        }
        warehouseInventory.setQuantityInStock(warehouseInventory.getQuantityInStock() - transfer.getQuantity());
        inventoryRepository.save(warehouseInventory);

        // Cập nhật hàng tồn kho ở Store
        Inventory storeInventory = inventoryRepository.findByProductVariantIdAndStoreNotNull(transfer.getProductVariant().getId())
                .stream().findFirst().orElse(new Inventory());

        storeInventory.setProductVariant(transfer.getProductVariant());
        storeInventory.setStore(transfer.getStore());
        storeInventory.setQuantityInStock(storeInventory.getQuantityInStock() + transfer.getQuantity());

        inventoryRepository.save(storeInventory);

        // Đánh dấu transfer là CONFIRMED
        transfer.setStatus(TransferStatus.CONFIRMED);
        return inventoryTransferRepository.save(transfer);
    }
}
