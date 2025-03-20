package com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StorePaymentComparisonResponse {
    private long cash;
    private long bankTransfer;
}
