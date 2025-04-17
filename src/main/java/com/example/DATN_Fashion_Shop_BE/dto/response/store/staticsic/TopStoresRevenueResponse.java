package com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopStoresRevenueResponse {
    private Long storeId;
    private String storeName;
    private String city;
    private Double totalRevenue;
    private Integer year;
}
