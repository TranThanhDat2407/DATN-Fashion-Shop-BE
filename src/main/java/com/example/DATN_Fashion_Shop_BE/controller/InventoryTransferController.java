package com.example.DATN_Fashion_Shop_BE.controller;

import com.example.DATN_Fashion_Shop_BE.component.LocalizationUtils;
import com.example.DATN_Fashion_Shop_BE.dto.request.inventory_transfer.InventoryTransferRequest;
import com.example.DATN_Fashion_Shop_BE.dto.response.ApiResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.PageResponse;
import com.example.DATN_Fashion_Shop_BE.dto.response.inventory_transfer.InventoryTransferResponse;
import com.example.DATN_Fashion_Shop_BE.model.InventoryTransfer;
import com.example.DATN_Fashion_Shop_BE.model.TransferStatus;
import com.example.DATN_Fashion_Shop_BE.service.InventoryTransferService;
import com.example.DATN_Fashion_Shop_BE.utils.ApiResponseUtils;
import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/inventory-transfers")
@AllArgsConstructor
public class InventoryTransferController {

    private final LocalizationUtils localizationUtils;
    private final InventoryTransferService inventoryTransferService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<InventoryTransferResponse>> createTransfer(
            @RequestBody InventoryTransferRequest request,
            @RequestParam(defaultValue = "vi") String langCode) {
        InventoryTransfer transfer = inventoryTransferService.createTransfer(request);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                InventoryTransferResponse.fromInventoryTransfer(transfer, langCode)
        ));
    }

    @PutMapping("/confirm/{transferId}")
    public ResponseEntity<ApiResponse<InventoryTransferResponse>> confirmTransfer(
            @PathVariable Long transferId,
            @RequestParam(defaultValue = "vi") String langCode) {
        InventoryTransfer transfer = inventoryTransferService.confirmTransfer(transferId);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                InventoryTransferResponse.fromInventoryTransfer(transfer, langCode)
        ));
    }

    @PutMapping("/cancel/{transferId}")
    public ResponseEntity<ApiResponse<InventoryTransferResponse>> cancelTransfer(
            @PathVariable Long transferId,
            @RequestParam(defaultValue = "vi") String langCode) {
        InventoryTransfer transfer = inventoryTransferService.cancelTransfer(transferId);
        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                InventoryTransferResponse.fromInventoryTransfer(transfer, langCode)
        ));
    }

    @GetMapping("/store")
    public ResponseEntity<ApiResponse<PageResponse<InventoryTransferResponse>>> getAllTransfersByStore(
            @RequestParam(required = false) Long storeId,
            @RequestParam(required = false) TransferStatus status,
            @RequestParam(required = false) Boolean isReturn,
            @RequestParam(defaultValue = "vi") String langCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Sort sort = Sort.by(
                Sort.Order.desc("updatedAt"),
                Sort.Order.asc("status")
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(ApiResponseUtils.successResponse(
                localizationUtils.getLocalizedMessage(MessageKeys.PRODUCTS_RETRIEVED_SUCCESSFULLY),
                PageResponse.fromPage(inventoryTransferService
                        .getAllTransfersByStore(storeId, status, isReturn, pageable, langCode)))
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryTransferResponse> getInventoryTransferById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "vi") String langCode) {
        InventoryTransferResponse response = inventoryTransferService.getInventoryTransferById(id, langCode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/store/{storeId}/history")
    public List<Object[]> getInventoryTransferHistoryByStore(@PathVariable Long storeId) {
        return inventoryTransferService.getInventoryTransferHistoryByStore(storeId);
    }
}
