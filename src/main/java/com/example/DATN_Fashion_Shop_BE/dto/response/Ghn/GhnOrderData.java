package com.example.DATN_Fashion_Shop_BE.dto.response.Ghn;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GhnOrderData {
    private String order_code;
    private String expected_delivery_time;
}
