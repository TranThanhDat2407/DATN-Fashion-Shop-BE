package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateColorRequest;
import com.example.DATN_Fashion_Shop_BE.dto.request.attribute_values.CreateSizeRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.attribute_values.*;
import com.example.DATN_Fashion_Shop_BE.dto.response.inventory_transfer.InventoryTransferResponse;
import com.example.DATN_Fashion_Shop_BE.model.InventoryTransfer;
import com.example.DATN_Fashion_Shop_BE.service.AttributeValuesService;
import com.example.DATN_Fashion_Shop_BE.service.InventoryTransferService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/inventory-transfers")
@AllArgsConstructor
public class InventoryTransferController {

    private final LocalizationUtils localizationUtils;
    private final InventoryTransferService inventoryTransferService;
    private static final Logger logger = LoggerFactory.getLogger(InventoryTransferController.class);

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<InventoryTransferResponse>> createTransfer(
            @RequestParam Long warehouseId,
            @RequestParam Long storeId,
            @RequestParam Long productVariantId,
            @RequestParam Integer quantity) {
        InventoryTransfer transfer = inventoryTransferService.createTransfer(warehouseId, storeId, productVariantId, quantity);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                InventoryTransferResponse.fromInventoryTransfer(transfer)
        ));
    }

    @PutMapping("/confirm/{transferId}")
    public ResponseEntity<ApiResponse<InventoryTransferResponse>> confirmTransfer(@PathVariable Long transferId) {
        InventoryTransfer transfer = inventoryTransferService.confirmTransfer(transferId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                InventoryTransferResponse.fromInventoryTransfer(transfer)
        ));
    }

    @PutMapping("/cancel/{transferId}")
    public ResponseEntity<ApiResponse<InventoryTransferResponse>> cancelTransfer(@PathVariable Long transferId) {
        InventoryTransfer transfer = inventoryTransferService.cancelTransfer(transferId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                InventoryTransferResponse.fromInventoryTransfer(transfer)
        ));
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<InventoryTransferResponse>> returnToWarehouse(
            @RequestParam Long storeId,
            @RequestParam Long warehouseId,
            @RequestParam Long productVariantId,
            @RequestParam Integer quantity) {
        InventoryTransfer transfer = inventoryTransferService.returnToWarehouse(storeId, warehouseId, productVariantId, quantity);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                InventoryTransferResponse.fromInventoryTransfer(transfer)
        ));
    }

    @PutMapping("/confirm-return/{transferId}")
    public ResponseEntity<ApiResponse<InventoryTransferResponse>> confirmReturnTransfer(@PathVariable Long transferId) {
        InventoryTransfer transfer = inventoryTransferService.confirmReturnTransfer(transferId);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                InventoryTransferResponse.fromInventoryTransfer(transfer)
        ));
    }

}
