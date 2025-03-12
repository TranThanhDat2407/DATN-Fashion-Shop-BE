package com.example.DATN_Fashion_Shop_BE.dto.response.inventory_transfer;

import com.example.DATN_Fashion_Shop_BE.dto.response.BaseResponse;
import com.example.DATN_Fashion_Shop_BE.model.*;
import lombok.*;

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
    private TransferStatus status;
    private String message;
    private Boolean isReturn;
    private List<InventoryTransferItemResponse> items;

    public static InventoryTransferResponse fromInventoryTransfer(InventoryTransfer transfer, String langCode) {
        InventoryTransferResponse response = InventoryTransferResponse.builder()
                .id(transfer.getId())
                .warehouseId(transfer.getWarehouse().getId())
                .storeId(transfer.getStore().getId())
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

        return response;
    }
}
