package com.example.DATN_Fashion_Shop_BE.dto.request.order;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStoreOrderStatusRequest {
    private String statusName;
}

