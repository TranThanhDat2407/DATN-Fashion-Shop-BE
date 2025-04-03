package com.example.DATN_Fashion_Shop_BE.dto.response.revenue;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Top3Store {
    private Long store_id;
    private String store_name;
    private String store_address;
    private String store_phone;
    private Double totalRevenue;
}
