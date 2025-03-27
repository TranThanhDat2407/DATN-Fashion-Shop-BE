package com.example.DATN_Fashion_Shop_BE.dto.response.inventory_transfer;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryTransferResponse extends BaseResponse {
    private Long id;
    private Long warehouseId;
    private Long storeId;
    private String storeName;
    private TransferStatus status;
    private String message;
    private Boolean isReturn;
    private Boolean warning;
    private List<InventoryTransferItemResponse> items;

    public static InventoryTransferResponse fromInventoryTransfer(InventoryTransfer transfer, String langCode) {
        InventoryTransferResponse response = InventoryTransferResponse.builder()
                .id(transfer.getId())
                .warehouseId(transfer.getWarehouse().getId())
                .storeId(transfer.getStore().getId())
                .storeName(transfer.getStore().getName())
                .status(transfer.getStatus())
                .message(transfer.getMessage())
                .isReturn(transfer.getIsReturn())
                .items(transfer.getTransferItems().stream()
                        .map(item -> InventoryTransferItemResponse.fromInventoryTransferItem(item, langCode))
                        .collect(Collectors.toList()))
                .build();
        response.setCreatedAt(transfer.getCreatedAt());
        response.setCreatedBy(transfer.getCreatedBy());
        response.setUpdatedAt(transfer.getUpdatedAt());
        response.setUpdatedBy(transfer.getUpdatedBy());
        response.setWarning(isWarningTransfer(response));
        return response;
    }

    private static boolean isWarningTransfer(InventoryTransferResponse transfer) {
        if (!transfer.getStatus().equals(TransferStatus.PENDING)) return false;
        LocalDateTime tenDaysAgo = LocalDateTime.now().minusDays(10);
        return transfer.getCreatedAt().isBefore(tenDaysAgo);
    }
}
