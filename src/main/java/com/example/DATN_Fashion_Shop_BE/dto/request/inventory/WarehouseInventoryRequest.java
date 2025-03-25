package com.example.DATN_Fashion_Shop_BE.dto.request.inventory;

import com.example.DATN_Fashion_Shop_BE.utils.MessageKeys;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseInventoryRequest {
    @NotNull
    private Long warehouseId;

    @NotNull
    private Long productVariantId;

    @NotNull
    @Min(0)
    private Integer quantityInStock;

}
