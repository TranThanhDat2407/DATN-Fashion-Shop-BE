package com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CityRevenueResponse {
    private String city;
    private Double totalRevenue;
    private Long storeCount;
    private Integer year;
}
