package com.example.DATN_Fashion_Shop_BE.dto.response.Ghn;

import lombok.*;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PreviewOrderResponse {
    private String expectedDeliveryTime;
    private int shippingFee;
    private int insuranceFee;
    private int totalFee;

    public static PreviewOrderResponse fromGHNResponse(GhnPreviewResponse response) {
        GhnPreviewData data = response.getData();
        return new PreviewOrderResponse(
                data.getExpected_delivery_time(),
                data.getFee().getMain_service(),
                data.getFee().getInsurance(),
                Integer.parseInt(data.getTotal_fee())
        );
    }
}
