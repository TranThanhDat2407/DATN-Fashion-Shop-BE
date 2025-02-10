package com.example.DATN_Fashion_Shop_BE.dto.response.Ghn;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GhnPreviewData {
    private String order_code;
    private String sort_code;
    private String trans_type;
    private String ward_encode;
    private String district_encode;
    private Fee fee;
    private String total_fee;
    private String expected_delivery_time;
}
