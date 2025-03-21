package com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreOrderComparisonResponse {
    private long customerOrder;
    private long guessOrder;
}
