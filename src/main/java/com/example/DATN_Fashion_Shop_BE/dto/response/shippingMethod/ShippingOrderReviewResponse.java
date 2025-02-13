package com.example.DATN_Fashion_Shop_BE.dto.response.shippingMethod;

import com.example.DATN_Fashion_Shop_BE.dto.response.Ghn.Fee;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ShippingOrderReviewResponse {
    @JsonProperty("total_fee")
    private Double totalFee;

    private LocalDateTime expected_delivery_time;

    private Fee fee;



}
