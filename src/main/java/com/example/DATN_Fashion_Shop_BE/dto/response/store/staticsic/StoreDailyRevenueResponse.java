package com.example.DATN_Fashion_Shop_BE.dto.response.store.staticsic;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreDailyRevenueResponse {
    private Integer day;
    private Integer month;
    private Integer year;
    private Double totalRevenue;
}
